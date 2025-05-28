package com.smsemailforwarder.app;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.smsemailforwarder.app.utils.NotificationHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;
import com.smsemailforwarder.app.utils.SmsFormatter;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * IntentService for handling email sending operations
 * Supports Gmail, Outlook, and custom SMTP servers
 * Handles SMS-to-email forwarding with Croatian character support
 */
public class EmailService extends IntentService {
    
    private static final String TAG = "EmailService";
    
    // Retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 2000;
    
    // Email timeout configuration
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    
    public EmailService() {
        super("EmailService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Received null intent");
            return;
        }
        
        Log.d(TAG, "EmailService started");
        
        PreferencesManager prefs = new PreferencesManager(this);
        NotificationHelper notificationHelper = new NotificationHelper(this);
        
        // Check if email is configured
        if (!prefs.isEmailConfigured()) {
            Log.e(TAG, "Email not configured");
            notificationHelper.showErrorNotification(
                "Email Configuration Error",
                "Please configure email settings before using SMS forwarding"
            );
            return;
        }
        
        try {
            boolean isTestMode = intent.getBooleanExtra("test_mode", false);
            
            if (isTestMode) {
                handleTestEmail(prefs, notificationHelper);
            } else {
                handleSmsEmail(intent, prefs, notificationHelper);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in EmailService", e);
            notificationHelper.showErrorNotification(
                "Email Service Error",
                "Failed to process email: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles sending test email to verify configuration
     */
    private void handleTestEmail(PreferencesManager prefs, NotificationHelper notificationHelper) {
        Log.d(TAG, "Sending test email");
        
        String subject = "SMS Forwarder Test - " + SmsFormatter.formatTimestamp(System.currentTimeMillis(), "dd.MM.yyyy HH:mm");
        String body = buildTestEmailBody();
        
        boolean success = sendEmailWithRetry(prefs, subject, body, notificationHelper);
        
        if (success) {
            Log.i(TAG, "Test email sent successfully");
            notificationHelper.showEmailSentNotification(prefs.getEmailRecipient());
        }
    }
    
    /**
     * Handles sending SMS content via email
     */
    private void handleSmsEmail(Intent intent, PreferencesManager prefs, NotificationHelper notificationHelper) {
        // Extract SMS data from intent
        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("message");
        long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());
        
        Log.d(TAG, "Processing SMS email - Sender: " + sender + ", Message length: " + 
              (message != null ? message.length() : 0));
        
        if (sender == null || message == null) {
            Log.e(TAG, "Invalid SMS data received");
            notificationHelper.showErrorNotification(
                "SMS Data Error",
                "Invalid SMS data received for email forwarding"
            );
            return;
        }
        
        // Format email content
        String subject = SmsFormatter.formatEmailSubject(sender, timestamp, prefs);
        String body = SmsFormatter.formatEmailBody(sender, message, timestamp, prefs);
        
        Log.d(TAG, "Email formatted - Subject: " + subject);
        
        // Send email with retry logic
        boolean success = sendEmailWithRetry(prefs, subject, body, notificationHelper);
        
        if (success) {
            Log.i(TAG, "SMS email sent successfully to " + prefs.getEmailRecipient());
            notificationHelper.showEmailSentNotification(prefs.getEmailRecipient());
        }
    }
    
    /**
     * Sends email with retry logic
     */
    private boolean sendEmailWithRetry(PreferencesManager prefs, String subject, String body, NotificationHelper notificationHelper) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            Log.d(TAG, "Email send attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS);
            
            try {
                sendEmail(prefs, subject, body);
                Log.i(TAG, "Email sent successfully on attempt " + attempt);
                return true;
                
            } catch (Exception e) {
                Log.w(TAG, "Email send attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    Log.e(TAG, "All email send attempts failed", e);
                    notificationHelper.showErrorNotification(
                        "Email Send Failed",
                        "Failed to send email after " + MAX_RETRY_ATTEMPTS + " attempts: " + e.getMessage()
                    );
                    return false;
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Core email sending method using JavaMail API
     */
    private void sendEmail(PreferencesManager prefs, String subject, String body) throws MessagingException {
        // Get email configuration
        String smtpServer = prefs.getEmailSmtpServer();
        int smtpPort = prefs.getEmailSmtpPort();
        String username = prefs.getEmailUsername();
        String password = prefs.getEmailPassword();
        String recipient = prefs.getEmailRecipient();
        boolean useStartTLS = prefs.isEmailUseStartTLS();
        boolean useSSL = prefs.isEmailUseSSL();
        
        Log.d(TAG, "Configuring email - Server: " + smtpServer + ":" + smtpPort + 
              ", TLS: " + useStartTLS + ", SSL: " + useSSL);
        
        // Configure SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.connectiontimeout", String.valueOf(CONNECTION_TIMEOUT));
        props.put("mail.smtp.timeout", String.valueOf(READ_TIMEOUT));
        
        // Configure encryption
        if (useSSL) {
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        
        if (useStartTLS) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        
        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        // Enable debug mode for troubleshooting
        session.setDebug(Log.isLoggable(TAG, Log.DEBUG));
        
        // Create and send message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);
        
        // Set UTF-8 encoding for Croatian characters
        message.setHeader("Content-Type", "text/plain; charset=UTF-8");
        
        Log.d(TAG, "Sending email via " + smtpServer);
        Transport.send(message);
        Log.i(TAG, "Email sent successfully");
    }
    
    /**
     * Builds test email body content
     */
    private String buildTestEmailBody() {
        StringBuilder body = new StringBuilder();
        
        body.append("📧 SMS-to-Email Forwarder Test\n");
        body.append("═══════════════════════════════════════\n\n");
        
        body.append("This is a test email to verify your SMS-to-Email forwarder configuration.\n\n");
        
        body.append("Configuration Details:\n");
        body.append("• Test sent: ").append(SmsFormatter.formatTimestamp(System.currentTimeMillis(), "EEEE, dd MMMM yyyy 'at' HH:mm:ss")).append("\n");
        body.append("• Character encoding: UTF-8 (supports Croatian: čćžšđ)\n");
        body.append("• Service status: Active\n\n");
        
        body.append("If you received this email, your SMS forwarding is configured correctly!\n\n");
        
        body.append("Next steps:\n");
        body.append("1. Enable the SMS forwarding service in the app\n");
        body.append("2. Send a test SMS to your device\n");
        body.append("3. Check that SMS messages are forwarded to this email\n\n");
        
        body.append("═══════════════════════════════════════\n");
        body.append("SMS-to-Email Forwarder for Android\n");
        body.append("Croatian carrier support: A1, HT, Tele2\n");
        
        return body.toString();
    }
}