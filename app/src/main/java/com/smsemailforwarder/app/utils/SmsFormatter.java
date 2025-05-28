package com.smsemailforwarder.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for formatting SMS messages for email content
 * Handles Croatian character encoding and proper email formatting
 */
public class SmsFormatter {
    
    private static final String TAG = "SmsFormatter";
    
    // Date format patterns
    private static final String DATE_FORMAT_FULL = "EEEE, dd MMMM yyyy 'at' HH:mm:ss";
    private static final String DATE_FORMAT_SHORT = "dd/MM/yyyy HH:mm";
    private static final String DATE_FORMAT_EMAIL_SUBJECT = "dd.MM.yyyy HH:mm";
    
    /**
     * Formats email subject based on user preferences
     */
    public static String formatEmailSubject(String sender, long timestamp, PreferencesManager prefs) {
        String subjectFormat = prefs.getEmailSubjectFormat();
        
        // Default format if none specified
        if (subjectFormat == null || subjectFormat.isEmpty()) {
            subjectFormat = "SMS from %s - %s";
        }
        
        String formattedTimestamp = formatTimestamp(timestamp, DATE_FORMAT_EMAIL_SUBJECT);
        String cleanSender = formatSender(sender);
        
        try {
            return String.format(subjectFormat, cleanSender, formattedTimestamp);
        } catch (Exception e) {
            // Fallback to simple format if string formatting fails
            return "SMS from " + cleanSender + " - " + formattedTimestamp;
        }
    }
    
    /**
     * Formats email body with SMS content and metadata
     */
    public static String formatEmailBody(String sender, String message, long timestamp, PreferencesManager prefs) {
        StringBuilder body = new StringBuilder();
        
        // Email header
        body.append("📱 SMS Message Received\n");
        body.append("════════════════════════════════════════\n\n");
        
        // Sender information
        if (prefs.isIncludeSender()) {
            body.append("From: ").append(formatSender(sender)).append("\n");
            
            // Add carrier info if detectable
            String carrier = detectCarrier(sender);
            if (!carrier.equals("Unknown/International")) {
                body.append("Carrier: ").append(carrier).append("\n");
            }
        }
        
        // Timestamp information
        if (prefs.isIncludeTimestamp()) {
            body.append("Received: ").append(formatTimestamp(timestamp, DATE_FORMAT_FULL)).append("\n");
        }
        
        body.append("\n");
        
        // Message content
        body.append("Message:\n");
        body.append("────────────────────────────────────────\n");
        body.append(formatMessageContent(message));
        body.append("\n────────────────────────────────────────\n\n");
        
        // Footer
        body.append("Message length: ").append(message.length()).append(" characters\n");
        body.append("Forwarded by SMS-to-Email Forwarder\n");
        
        return body.toString();
    }
    
    /**
     * Formats sender for display (cleans up phone number)
     */
    public static String formatSender(String sender) {
        if (sender == null || sender.isEmpty()) {
            return "Unknown Sender";
        }
        
        // If it's already formatted with +385, keep it clean
        if (sender.startsWith("+385")) {
            return sender;
        }
        
        // Clean up any weird formatting
        return sender.replaceAll("[^+\\d\\s\\-()]", "");
    }
    
    /**
     * Formats message content for email (handles Croatian characters)
     */
    public static String formatMessageContent(String message) {
        if (message == null) {
            return "[Empty message]";
        }
        
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return "[Empty message]";
        }
        
        // Ensure proper line breaks and formatting
        return trimmed.replaceAll("\\r\\n", "\n")
                     .replaceAll("\\r", "\n")
                     .replaceAll("\\n{3,}", "\n\n"); // Limit excessive line breaks
    }
    
    /**
     * Formats timestamp using specified pattern
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            // Fallback to simple format
            SimpleDateFormat fallback = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return fallback.format(new Date(timestamp));
        }
    }
    
    /**
     * Detects Croatian carrier from phone number
     */
    public static String detectCarrier(String phoneNumber) {
        if (phoneNumber == null) return "Unknown/International";
        
        String cleaned = phoneNumber.replaceAll("[^\\d]", "");
        
        // Croatian mobile prefixes (based on numbering plan)
        if (cleaned.startsWith("38591") || cleaned.startsWith("91")) {
            return "A1 Croatia";
        } else if (cleaned.startsWith("38598") || cleaned.startsWith("38599") || 
                   cleaned.startsWith("98") || cleaned.startsWith("99")) {
            return "Hrvatski Telekom (HT)";
        } else if (cleaned.startsWith("38595") || cleaned.startsWith("95")) {
            return "Tele2 Croatia";
        } else if (cleaned.startsWith("385")) {
            return "Croatia (Other carrier)";
        }
        
        return "Unknown/International";
    }
    
    /**
     * Validates message content for basic sanity checks
     */
    public static boolean isValidMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = message.trim();
        
        // Check reasonable length limits
        if (trimmed.length() > 2000) {
            return false; // Too long for typical SMS
        }
        
        // Check for some actual content (not just special chars)
        return trimmed.matches(".*[a-zA-Z0-9čćžšđČĆŽŠĐ].*");
    }
    
    /**
     * Creates a simple plain text version for basic email clients
     */
    public static String formatSimpleEmailBody(String sender, String message, long timestamp) {
        StringBuilder body = new StringBuilder();
        
        body.append("SMS from: ").append(formatSender(sender)).append("\n");
        body.append("Time: ").append(formatTimestamp(timestamp, DATE_FORMAT_SHORT)).append("\n");
        body.append("Message: ").append(formatMessageContent(message)).append("\n");
        
        return body.toString();
    }
    
    /**
     * Escapes special characters for email safety
     */
    public static String escapeForEmail(String text) {
        if (text == null) return "";
        
        return text.replaceAll("[\u0000-\u001F\u007F]", ""); // Remove control characters
    }
} 