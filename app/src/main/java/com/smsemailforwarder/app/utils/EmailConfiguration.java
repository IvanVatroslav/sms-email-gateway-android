package com.smsemailforwarder.app.utils;

/**
 * Utility class for email provider configurations
 * Provides SMTP settings for popular email providers
 */
public class EmailConfiguration {
    
    /**
     * Email provider configuration data class
     */
    public static class EmailProvider {
        public final String name;
        public final String smtpServer;
        public final int smtpPort;
        public final boolean useStartTLS;
        public final boolean useSSL;
        public final String description;
        
        public EmailProvider(String name, String smtpServer, int smtpPort, 
                           boolean useStartTLS, boolean useSSL, String description) {
            this.name = name;
            this.smtpServer = smtpServer;
            this.smtpPort = smtpPort;
            this.useStartTLS = useStartTLS;
            this.useSSL = useSSL;
            this.description = description;
        }
    }
    
    // Predefined email providers
    public static final EmailProvider GMAIL = new EmailProvider(
        "Gmail",
        "smtp.gmail.com",
        587,
        true,
        false,
        "Use app password, not regular password"
    );
    
    public static final EmailProvider OUTLOOK = new EmailProvider(
        "Outlook/Hotmail",
        "smtp-mail.outlook.com",
        587,
        true,
        false,
        "Use regular Microsoft account password"
    );
    
    public static final EmailProvider YAHOO = new EmailProvider(
        "Yahoo Mail",
        "smtp.mail.yahoo.com",
        587,
        true,
        false,
        "Use app password for enhanced security"
    );
    
    public static final EmailProvider GMAIL_SSL = new EmailProvider(
        "Gmail (SSL)",
        "smtp.gmail.com",
        465,
        false,
        true,
        "SSL connection with app password"
    );
    
    public static final EmailProvider CUSTOM = new EmailProvider(
        "Custom SMTP",
        "",
        587,
        true,
        false,
        "Configure your own SMTP server"
    );
    
    /**
     * Get all available email providers
     */
    public static EmailProvider[] getAllProviders() {
        return new EmailProvider[] {
            GMAIL,
            OUTLOOK,
            YAHOO,
            GMAIL_SSL,
            CUSTOM
        };
    }
    
    /**
     * Get provider by name
     */
    public static EmailProvider getProviderByName(String name) {
        for (EmailProvider provider : getAllProviders()) {
            if (provider.name.equals(name)) {
                return provider;
            }
        }
        return CUSTOM;
    }
    
    /**
     * Auto-detect provider from email address
     */
    public static EmailProvider detectProvider(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) {
            return CUSTOM;
        }
        
        String domain = extractDomain(emailAddress);
        
        switch (domain.toLowerCase()) {
            case "gmail.com":
                return GMAIL;
            case "outlook.com":
            case "hotmail.com":
            case "live.com":
                return OUTLOOK;
            case "yahoo.com":
            case "yahoo.hr":
                return YAHOO;
            default:
                return CUSTOM;
        }
    }
    
    /**
     * Extract domain from email address
     */
    private static String extractDomain(String emailAddress) {
        int atIndex = emailAddress.lastIndexOf('@');
        if (atIndex > 0 && atIndex < emailAddress.length() - 1) {
            return emailAddress.substring(atIndex + 1);
        }
        return "";
    }
    
    /**
     * Validate email address format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Get setup instructions for a provider
     */
    public static String getSetupInstructions(EmailProvider provider) {
        switch (provider.name) {
            case "Gmail":
                return "Gmail Setup:\n" +
                       "1. Enable 2-factor authentication\n" +
                       "2. Generate app password:\n" +
                       "   - Go to Google Account settings\n" +
                       "   - Security → 2-Step Verification\n" +
                       "   - App passwords → Generate\n" +
                       "3. Use app password (not regular password)\n" +
                       "4. Server: smtp.gmail.com, Port: 587, TLS: Yes";
                       
            case "Outlook/Hotmail":
                return "Outlook Setup:\n" +
                       "1. Use your regular Microsoft account password\n" +
                       "2. May need to enable 'Less secure app access'\n" +
                       "3. Server: smtp-mail.outlook.com\n" +
                       "4. Port: 587, TLS: Yes";
                       
            case "Yahoo Mail":
                return "Yahoo Mail Setup:\n" +
                       "1. Generate app password:\n" +
                       "   - Account Info → Account Security\n" +
                       "   - Generate app password\n" +
                       "2. Use app password (not regular password)\n" +
                       "3. Server: smtp.mail.yahoo.com, Port: 587, TLS: Yes";
                       
            default:
                return "Custom SMTP Setup:\n" +
                       "1. Contact your email provider for SMTP settings\n" +
                       "2. Common ports: 587 (TLS), 465 (SSL), 25 (insecure)\n" +
                       "3. Most providers require authentication\n" +
                       "4. Use app passwords when available";
        }
    }
} 