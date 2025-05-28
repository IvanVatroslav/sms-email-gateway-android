package com.smsemailforwarder.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.smsemailforwarder.app.utils.NotificationHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;
import com.smsemailforwarder.app.utils.SmsFormatter;

/**
 * Broadcast receiver for incoming SMS messages
 * Handles SMS reception, parsing, and forwarding to EmailService
 * Supports Croatian carriers: A1, HT (Hrvatski Telekom), Tele2
 */
public class SmsReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String PDU_TYPE = "pdus";
    private static final String FORMAT_TYPE = "format";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "Received non-SMS intent, ignoring");
            return;
        }
        
        Log.d(TAG, "SMS received, processing...");
        
        // Check if service is enabled
        PreferencesManager preferencesManager = new PreferencesManager(context);
        if (!preferencesManager.isServiceEnabled()) {
            Log.d(TAG, "SMS forwarding service is disabled, ignoring SMS");
            return;
        }
        
        // Check if email is configured
        if (!preferencesManager.isEmailConfigured()) {
            Log.w(TAG, "Email not configured, cannot forward SMS");
            
            // Show error notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showErrorNotification(
                "SMS Forwarder Error", 
                "Email not configured. Please set up email settings."
            );
            return;
        }
        
        try {
            // Extract SMS data from intent
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e(TAG, "SMS bundle is null");
                return;
            }
            
            Object[] pdus = (Object[]) bundle.get(PDU_TYPE);
            if (pdus == null || pdus.length == 0) {
                Log.e(TAG, "No SMS PDUs found");
                return;
            }
            
            String format = bundle.getString(FORMAT_TYPE);
            Log.d(TAG, "SMS format: " + format + ", PDUs count: " + pdus.length);
            
            // Process each SMS PDU (for multi-part messages)
            StringBuilder fullMessageBody = new StringBuilder();
            String senderNumber = null;
            long timestamp = System.currentTimeMillis();
            
            for (Object pdu : pdus) {
                SmsMessage smsMessage = createSmsMessage((byte[]) pdu, format);
                if (smsMessage != null) {
                    // Get sender info (use first PDU's sender)
                    if (senderNumber == null) {
                        senderNumber = smsMessage.getDisplayOriginatingAddress();
                        timestamp = smsMessage.getTimestampMillis();
                    }
                    
                    // Append message body (for concatenated SMS)
                    String messageBody = smsMessage.getMessageBody();
                    if (messageBody != null) {
                        fullMessageBody.append(messageBody);
                    }
                }
            }
            
            // Validate extracted data
            if (senderNumber == null || fullMessageBody.length() == 0) {
                Log.e(TAG, "Invalid SMS data - sender: " + senderNumber + 
                           ", message length: " + fullMessageBody.length());
                return;
            }
            
            // Clean and format the data
            String cleanSender = cleanPhoneNumber(senderNumber);
            String messageContent = fullMessageBody.toString().trim();
            
            // Validate message content
            if (!SmsFormatter.isValidMessage(messageContent)) {
                Log.w(TAG, "Message content validation failed");
                return;
            }
            
            // Log the SMS details (for debugging)
            String carrier = SmsFormatter.detectCarrier(cleanSender);
            Log.i(TAG, "SMS parsed successfully:");
            Log.i(TAG, "  Sender: " + cleanSender + " (" + carrier + ")");
            Log.i(TAG, "  Timestamp: " + SmsFormatter.formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss"));
            Log.i(TAG, "  Message length: " + messageContent.length() + " characters");
            Log.d(TAG, "  Message preview: " + 
                  (messageContent.length() > 50 ? 
                   messageContent.substring(0, 50) + "..." : messageContent));
            
            // Show notification that SMS was received
            NotificationHelper notificationHelper = new NotificationHelper(context);
            String messagePreview = messageContent.length() > 30 ? 
                                  messageContent.substring(0, 30) + "..." : messageContent;
            notificationHelper.showSmsReceivedNotification(cleanSender, messagePreview);
            
            // Forward to EmailService
            forwardSmsToEmail(context, cleanSender, messageContent, timestamp);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS", e);
            
            // Show error notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showErrorNotification(
                "SMS Processing Error", 
                "Failed to process incoming SMS: " + e.getMessage()
            );
        }
    }
    
    /**
     * Creates SmsMessage from PDU with proper format handling
     */
    private SmsMessage createSmsMessage(byte[] pdu, String format) {
        try {
            if (format != null) {
                return SmsMessage.createFromPdu(pdu, format);
            } else {
                // Fallback for older Android versions
                return SmsMessage.createFromPdu(pdu);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating SMS message from PDU", e);
            return null;
        }
    }
    
    /**
     * Cleans and normalizes phone numbers for Croatian carriers
     * Handles various formats from A1, HT, and Tele2
     */
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "Unknown";
        }
        
        // Remove any whitespace and special characters except + and digits
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
        
        // Handle Croatian number formats
        if (cleaned.startsWith("+385")) {
            // International format: +385xxxxxxxxx
            return cleaned;
        } else if (cleaned.startsWith("385")) {
            // International without +: 385xxxxxxxxx
            return "+" + cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() >= 8) {
            // National format: 0xxxxxxxxx -> +385xxxxxxxxx
            return "+385" + cleaned.substring(1);
        } else if (cleaned.length() >= 8 && cleaned.length() <= 9 && !cleaned.startsWith("0")) {
            // Local format without leading 0: xxxxxxxxx -> +385xxxxxxxxx
            return "+385" + cleaned;
        }
        
        // Return as-is if no pattern matches (could be short code or special number)
        return phoneNumber;
    }
    
    /**
     * Forwards SMS data to EmailService for sending
     */
    private void forwardSmsToEmail(Context context, String sender, String message, long timestamp) {
        Log.d(TAG, "Forwarding SMS to EmailService");
        
        try {
            Intent emailIntent = new Intent(context, EmailService.class);
            emailIntent.putExtra("sender", sender);
            emailIntent.putExtra("message", message);
            emailIntent.putExtra("timestamp", timestamp);
            emailIntent.putExtra("test_mode", false);
            
            // Start the email service
            context.startService(emailIntent);
            
            Log.i(TAG, "SMS forwarding initiated successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting EmailService", e);
            
            // Show error notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showErrorNotification(
                "Email Service Error", 
                "Failed to start email forwarding service"
            );
        }
    }
} 