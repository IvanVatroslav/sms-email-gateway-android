package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smsemailforwarder.app.EmailService;

/**
 * Utility class for testing email functionality
 * Provides methods to test email configuration and send test messages
 */
public class EmailTestHelper {
    
    private static final String TAG = "EmailTestHelper";
    
    /**
     * Sends a test email to verify configuration
     */
    public static void sendTestEmail(Context context) {
        Log.d(TAG, "Initiating test email send");
        
        Intent testIntent = new Intent(context, EmailService.class);
        testIntent.putExtra("test_mode", true);
        
        context.startService(testIntent);
    }
    
    /**
     * Sends a sample SMS email for testing
     */
    public static void sendSampleSmsEmail(Context context) {
        Log.d(TAG, "Sending sample SMS email");
        
        Intent smsIntent = new Intent(context, EmailService.class);
        smsIntent.putExtra("sender", "+385 91 123 4567");
        smsIntent.putExtra("message", "Ovo je test poruka za SMS-to-Email forwarder. Podržava hrvatske znakove: čćžšđ!");
        smsIntent.putExtra("timestamp", System.currentTimeMillis());
        smsIntent.putExtra("test_mode", false);
        
        context.startService(smsIntent);
    }
    
    /**
     * Validates email configuration without sending
     */
    public static boolean validateEmailConfiguration(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        
        if (!prefs.isEmailConfigured()) {
            Log.w(TAG, "Email not configured");
            return false;
        }
        
        // Check required fields
        String server = prefs.getEmailSmtpServer();
        String username = prefs.getEmailUsername();
        String password = prefs.getEmailPassword();
        String recipient = prefs.getEmailRecipient();
        
        if (server == null || server.isEmpty()) {
            Log.w(TAG, "SMTP server not configured");
            return false;
        }
        
        if (username == null || username.isEmpty()) {
            Log.w(TAG, "Email username not configured");
            return false;
        }
        
        if (password == null || password.isEmpty()) {
            Log.w(TAG, "Email password not configured");
            return false;
        }
        
        if (recipient == null || recipient.isEmpty()) {
            Log.w(TAG, "Email recipient not configured");
            return false;
        }
        
        if (!EmailConfiguration.isValidEmail(username)) {
            Log.w(TAG, "Invalid sender email format: " + username);
            return false;
        }
        
        if (!EmailConfiguration.isValidEmail(recipient)) {
            Log.w(TAG, "Invalid recipient email format: " + recipient);
            return false;
        }
        
        Log.i(TAG, "Email configuration validation passed");
        return true;
    }
    
    /**
     * Gets a summary of the current email configuration
     */
    public static String getConfigurationSummary(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        StringBuilder summary = new StringBuilder();
        
        summary.append("Email Configuration Summary:\n");
        summary.append("═══════════════════════════════════\n\n");
        
        if (!prefs.isEmailConfigured()) {
            summary.append("❌ Email not configured\n\n");
            summary.append("Please configure email settings:\n");
            summary.append("• Sender email address\n");
            summary.append("• Email password/app password\n");
            summary.append("• Recipient email address\n");
            summary.append("• SMTP server settings");
            return summary.toString();
        }
        
        // Basic configuration
        String username = prefs.getEmailUsername();
        String recipient = prefs.getEmailRecipient();
        String server = prefs.getEmailSmtpServer();
        int port = prefs.getEmailSmtpPort();
        boolean useSSL = prefs.getEmailUseSSL();
        
        summary.append("✅ Email Configured\n\n");
        
        // Sender information
        summary.append("Sender: ").append(maskEmail(username)).append("\n");
        summary.append("Recipient: ").append(maskEmail(recipient)).append("\n\n");
        
        // Server information
        summary.append("SMTP Server: ").append(server).append("\n");
        summary.append("Port: ").append(port).append("\n");
        summary.append("Encryption: ").append(useSSL ? "SSL/TLS" : "None").append("\n\n");
        
        // Provider detection
        String provider = EmailConfiguration.detectProvider(username);
        if (provider != null) {
            summary.append("Provider: ").append(provider).append("\n");
        }
        
        // Validation status
        summary.append("\nValidation:\n");
        if (EmailConfiguration.isValidEmail(username)) {
            summary.append("✅ Sender email format valid\n");
        } else {
            summary.append("❌ Sender email format invalid\n");
        }
        
        if (EmailConfiguration.isValidEmail(recipient)) {
            summary.append("✅ Recipient email format valid\n");
        } else {
            summary.append("❌ Recipient email format invalid\n");
        }
        
        if (server != null && !server.isEmpty()) {
            summary.append("✅ SMTP server configured\n");
        } else {
            summary.append("❌ SMTP server not configured\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Masks an email address for privacy
     */
    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[Not set]";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            
            if (username.length() <= 2) {
                return "*".repeat(username.length()) + domain;
            } else {
                return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) + domain;
            }
        }
        
        return email.length() > 4 ? email.substring(0, 2) + "*".repeat(email.length() - 4) + email.substring(email.length() - 2) : "****";
    }
    
    /**
     * Quick setup for Gmail with validation
     */
    public static boolean setupGmailQuick(Context context, String gmailAddress, String appPassword, String recipientEmail) {
        if (!EmailConfiguration.isValidEmail(gmailAddress)) {
            Log.e(TAG, "Invalid Gmail address format");
            return false;
        }
        
        if (!EmailConfiguration.isValidEmail(recipientEmail)) {
            Log.e(TAG, "Invalid recipient email format");
            return false;
        }
        
        if (appPassword == null || appPassword.length() < 8) {
            Log.e(TAG, "App password too short or missing");
            return false;
        }
        
        PreferencesManager prefs = new PreferencesManager(context);
        prefs.setupGmail(gmailAddress, appPassword, recipientEmail);
        
        Log.i(TAG, "Gmail configuration saved successfully");
        return true;
    }
    
    /**
     * Quick setup for Outlook with validation
     */
    public static boolean setupOutlookQuick(Context context, String outlookAddress, String password, String recipientEmail) {
        if (!EmailConfiguration.isValidEmail(outlookAddress)) {
            Log.e(TAG, "Invalid Outlook address format");
            return false;
        }
        
        if (!EmailConfiguration.isValidEmail(recipientEmail)) {
            Log.e(TAG, "Invalid recipient email format");
            return false;
        }
        
        if (password == null || password.length() < 6) {
            Log.e(TAG, "Password too short or missing");
            return false;
        }
        
        PreferencesManager prefs = new PreferencesManager(context);
        prefs.setupOutlook(outlookAddress, password, recipientEmail);
        
        Log.i(TAG, "Outlook configuration saved successfully");
        return true;
    }
} 