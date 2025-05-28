package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Utility class for managing app preferences and settings
 * Handles email configuration, service state, and other app settings
 */
public class PreferencesManager {
    
    private static final String PREF_NAME = "SMSEmailForwarderPrefs";
    
    // Email Configuration Keys
    private static final String KEY_EMAIL_CONFIGURED = "email_configured";
    private static final String KEY_SMTP_SERVER = "smtp_server";
    private static final String KEY_SMTP_PORT = "smtp_port";
    private static final String KEY_EMAIL_USERNAME = "email_username";
    private static final String KEY_EMAIL_PASSWORD = "email_password";
    private static final String KEY_FROM_EMAIL = "from_email";
    private static final String KEY_TO_EMAIL = "to_email";
    private static final String KEY_USE_TLS = "use_tls";
    private static final String KEY_USE_SSL = "use_ssl";
    
    // Service Configuration Keys
    private static final String KEY_SERVICE_ENABLED = "service_enabled";
    private static final String KEY_AUTO_START = "auto_start";
    
    // Message Formatting Keys
    private static final String KEY_INCLUDE_TIMESTAMP = "include_timestamp";
    private static final String KEY_INCLUDE_SENDER = "include_sender";
    private static final String KEY_EMAIL_SUBJECT_FORMAT = "email_subject_format";
    
    // Default Values
    private static final String DEFAULT_SUBJECT_FORMAT = "SMS from %s - %s";
    private static final int DEFAULT_SMTP_PORT_TLS = 587;
    private static final int DEFAULT_SMTP_PORT_SSL = 465;
    
    private final SharedPreferences preferences;
    
    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    // Email Configuration Methods
    public void saveEmailConfiguration(String smtpServer, int smtpPort, String username, 
                                     String password, String fromEmail, String toEmail, 
                                     boolean useTls, boolean useSsl) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SMTP_SERVER, smtpServer);
        editor.putInt(KEY_SMTP_PORT, smtpPort);
        editor.putString(KEY_EMAIL_USERNAME, username);
        editor.putString(KEY_EMAIL_PASSWORD, password);
        editor.putString(KEY_FROM_EMAIL, fromEmail);
        editor.putString(KEY_TO_EMAIL, toEmail);
        editor.putBoolean(KEY_USE_TLS, useTls);
        editor.putBoolean(KEY_USE_SSL, useSsl);
        editor.putBoolean(KEY_EMAIL_CONFIGURED, true);
        editor.apply();
    }
    
    public boolean isEmailConfigured() {
        return preferences.getBoolean(KEY_EMAIL_CONFIGURED, false) &&
               !TextUtils.isEmpty(getSmtpServer()) &&
               !TextUtils.isEmpty(getEmailUsername()) &&
               !TextUtils.isEmpty(getToEmail());
    }
    
    public String getSmtpServer() {
        return preferences.getString(KEY_SMTP_SERVER, "");
    }
    
    public int getSmtpPort() {
        return preferences.getInt(KEY_SMTP_PORT, DEFAULT_SMTP_PORT_TLS);
    }
    
    public String getEmailUsername() {
        return preferences.getString(KEY_EMAIL_USERNAME, "");
    }
    
    public String getEmailPassword() {
        return preferences.getString(KEY_EMAIL_PASSWORD, "");
    }
    
    public String getFromEmail() {
        return preferences.getString(KEY_FROM_EMAIL, getEmailUsername());
    }
    
    public String getToEmail() {
        return preferences.getString(KEY_TO_EMAIL, "");
    }
    
    public boolean isUseTls() {
        return preferences.getBoolean(KEY_USE_TLS, true);
    }
    
    public boolean isUseSsl() {
        return preferences.getBoolean(KEY_USE_SSL, false);
    }
    
    // Alias methods for EmailService compatibility
    public String getEmailSmtpServer() {
        return getSmtpServer();
    }
    
    public int getEmailSmtpPort() {
        return getSmtpPort();
    }
    
    public String getEmailRecipient() {
        return getToEmail();
    }
    
    public boolean isEmailUseStartTLS() {
        return isUseTls();
    }
    
    public boolean isEmailUseSSL() {
        return isUseSsl();
    }
    
    // Service Configuration Methods
    public void setServiceEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply();
    }
    
    public boolean isServiceEnabled() {
        return preferences.getBoolean(KEY_SERVICE_ENABLED, false);
    }
    
    public void setAutoStart(boolean autoStart) {
        preferences.edit().putBoolean(KEY_AUTO_START, autoStart).apply();
    }
    
    public boolean isAutoStart() {
        return preferences.getBoolean(KEY_AUTO_START, true);
    }
    
    // Message Formatting Methods
    public void setIncludeTimestamp(boolean include) {
        preferences.edit().putBoolean(KEY_INCLUDE_TIMESTAMP, include).apply();
    }
    
    public boolean isIncludeTimestamp() {
        return preferences.getBoolean(KEY_INCLUDE_TIMESTAMP, true);
    }
    
    public void setIncludeSender(boolean include) {
        preferences.edit().putBoolean(KEY_INCLUDE_SENDER, include).apply();
    }
    
    public boolean isIncludeSender() {
        return preferences.getBoolean(KEY_INCLUDE_SENDER, true);
    }
    
    public void setEmailSubjectFormat(String format) {
        preferences.edit().putString(KEY_EMAIL_SUBJECT_FORMAT, format).apply();
    }
    
    public String getEmailSubjectFormat() {
        return preferences.getString(KEY_EMAIL_SUBJECT_FORMAT, DEFAULT_SUBJECT_FORMAT);
    }
    
    // Quick setup methods for common email providers
    public void setupGmail(String username, String password, String toEmail) {
        saveEmailConfiguration(
            "smtp.gmail.com", 
            587, 
            username, 
            password, 
            username, 
            toEmail, 
            true, 
            false
        );
    }
    
    public void setupOutlook(String username, String password, String toEmail) {
        saveEmailConfiguration(
            "smtp-mail.outlook.com", 
            587, 
            username, 
            password, 
            username, 
            toEmail, 
            true, 
            false
        );
    }
    
    // Clear all settings
    public void clearAllSettings() {
        preferences.edit().clear().apply();
    }
    
    // Export settings for backup (returns a simple string representation)
    public String exportSettings() {
        StringBuilder sb = new StringBuilder();
        sb.append("SMTP Server: ").append(getSmtpServer()).append("\n");
        sb.append("SMTP Port: ").append(getSmtpPort()).append("\n");
        sb.append("Username: ").append(getEmailUsername()).append("\n");
        sb.append("To Email: ").append(getToEmail()).append("\n");
        sb.append("Use TLS: ").append(isUseTls()).append("\n");
        sb.append("Auto Start: ").append(isAutoStart()).append("\n");
        sb.append("Include Timestamp: ").append(isIncludeTimestamp()).append("\n");
        sb.append("Include Sender: ").append(isIncludeSender()).append("\n");
        return sb.toString();
    }
} 