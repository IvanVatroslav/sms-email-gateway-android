package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Enhanced utility class for managing app preferences and settings
 * Handles email configuration, service state, SMS filtering, and advanced options
 */
public class PreferencesManager {
    
    private static final String TAG = "PreferencesManager";
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
    private static final String KEY_INCLUDE_CARRIER_INFO = "include_carrier_info";
    private static final String KEY_INCLUDE_MESSAGE_COUNT = "include_message_count";
    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_TIME_FORMAT = "time_format";
    
    // SMS Filtering Keys
    private static final String KEY_FILTER_ENABLED = "filter_enabled";
    private static final String KEY_BLOCKED_NUMBERS = "blocked_numbers";
    private static final String KEY_ALLOWED_NUMBERS = "allowed_numbers";
    private static final String KEY_FILTER_MODE = "filter_mode"; // BLACKLIST, WHITELIST, NONE
    private static final String KEY_FILTER_KEYWORDS = "filter_keywords";
    private static final String KEY_FILTER_SPAM = "filter_spam";
    private static final String KEY_MIN_MESSAGE_LENGTH = "min_message_length";
    private static final String KEY_MAX_MESSAGE_LENGTH = "max_message_length";
    
    // Notification Settings Keys
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_NOTIFICATION_SOUND = "notification_sound";
    private static final String KEY_NOTIFICATION_VIBRATE = "notification_vibrate";
    private static final String KEY_NOTIFICATION_LED = "notification_led";
    private static final String KEY_NOTIFICATION_PRIORITY = "notification_priority";
    private static final String KEY_SHOW_EMAIL_STATUS = "show_email_status";
    private static final String KEY_SHOW_SMS_PREVIEW = "show_sms_preview";
    
    // Advanced Settings Keys
    private static final String KEY_EMAIL_RETRY_COUNT = "email_retry_count";
    private static final String KEY_EMAIL_RETRY_DELAY = "email_retry_delay";
    private static final String KEY_CONNECTION_TIMEOUT = "connection_timeout";
    private static final String KEY_DEBUG_MODE = "debug_mode";
    private static final String KEY_BATTERY_OPTIMIZATION_WARNED = "battery_optimization_warned";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_APP_VERSION = "app_version";
    
    // Default Values
    private static final String DEFAULT_SUBJECT_FORMAT = "SMS from %s - %s";
    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final int DEFAULT_SMTP_PORT_TLS = 587;
    private static final int DEFAULT_SMTP_PORT_SSL = 465;
    private static final int DEFAULT_EMAIL_RETRY_COUNT = 3;
    private static final int DEFAULT_EMAIL_RETRY_DELAY = 5000; // 5 seconds
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int DEFAULT_MIN_MESSAGE_LENGTH = 1;
    private static final int DEFAULT_MAX_MESSAGE_LENGTH = 1000;
    
    // Filter modes
    public enum FilterMode {
        NONE, BLACKLIST, WHITELIST
    }
    
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
    
    // Enhanced Message Formatting Methods
    public void setIncludeCarrierInfo(boolean include) {
        preferences.edit().putBoolean(KEY_INCLUDE_CARRIER_INFO, include).apply();
    }
    
    public boolean isIncludeCarrierInfo() {
        return preferences.getBoolean(KEY_INCLUDE_CARRIER_INFO, true);
    }
    
    public void setIncludeMessageCount(boolean include) {
        preferences.edit().putBoolean(KEY_INCLUDE_MESSAGE_COUNT, include).apply();
    }
    
    public boolean isIncludeMessageCount() {
        return preferences.getBoolean(KEY_INCLUDE_MESSAGE_COUNT, false);
    }
    
    public void setDateFormat(String format) {
        preferences.edit().putString(KEY_DATE_FORMAT, format).apply();
    }
    
    public String getDateFormat() {
        return preferences.getString(KEY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
    }
    
    public void setTimeFormat(String format) {
        preferences.edit().putString(KEY_TIME_FORMAT, format).apply();
    }
    
    public String getTimeFormat() {
        return preferences.getString(KEY_TIME_FORMAT, DEFAULT_TIME_FORMAT);
    }
    
    // SMS Filtering Methods
    public void setFilterEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_FILTER_ENABLED, enabled).apply();
    }
    
    public boolean isFilterEnabled() {
        return preferences.getBoolean(KEY_FILTER_ENABLED, false);
    }
    
    public void setFilterMode(FilterMode mode) {
        preferences.edit().putString(KEY_FILTER_MODE, mode.name()).apply();
    }
    
    public FilterMode getFilterMode() {
        String mode = preferences.getString(KEY_FILTER_MODE, FilterMode.NONE.name());
        try {
            return FilterMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return FilterMode.NONE;
        }
    }
    
    public void setBlockedNumbers(Set<String> numbers) {
        preferences.edit().putStringSet(KEY_BLOCKED_NUMBERS, numbers).apply();
    }
    
    public Set<String> getBlockedNumbers() {
        return preferences.getStringSet(KEY_BLOCKED_NUMBERS, new HashSet<>());
    }
    
    public void setAllowedNumbers(Set<String> numbers) {
        preferences.edit().putStringSet(KEY_ALLOWED_NUMBERS, numbers).apply();
    }
    
    public Set<String> getAllowedNumbers() {
        return preferences.getStringSet(KEY_ALLOWED_NUMBERS, new HashSet<>());
    }
    
    public void setFilterKeywords(Set<String> keywords) {
        preferences.edit().putStringSet(KEY_FILTER_KEYWORDS, keywords).apply();
    }
    
    public Set<String> getFilterKeywords() {
        return preferences.getStringSet(KEY_FILTER_KEYWORDS, new HashSet<>());
    }
    
    public void setFilterSpam(boolean filter) {
        preferences.edit().putBoolean(KEY_FILTER_SPAM, filter).apply();
    }
    
    public boolean isFilterSpam() {
        return preferences.getBoolean(KEY_FILTER_SPAM, true);
    }
    
    public void setMinMessageLength(int length) {
        preferences.edit().putInt(KEY_MIN_MESSAGE_LENGTH, length).apply();
    }
    
    public int getMinMessageLength() {
        return preferences.getInt(KEY_MIN_MESSAGE_LENGTH, DEFAULT_MIN_MESSAGE_LENGTH);
    }
    
    public void setMaxMessageLength(int length) {
        preferences.edit().putInt(KEY_MAX_MESSAGE_LENGTH, length).apply();
    }
    
    public int getMaxMessageLength() {
        return preferences.getInt(KEY_MAX_MESSAGE_LENGTH, DEFAULT_MAX_MESSAGE_LENGTH);
    }
    
    // Notification Settings Methods
    public void setNotificationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }
    
    public boolean isNotificationEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }
    
    public void setNotificationSound(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply();
    }
    
    public boolean isNotificationSound() {
        return preferences.getBoolean(KEY_NOTIFICATION_SOUND, true);
    }
    
    public void setNotificationVibrate(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_VIBRATE, enabled).apply();
    }
    
    public boolean isNotificationVibrate() {
        return preferences.getBoolean(KEY_NOTIFICATION_VIBRATE, true);
    }
    
    public void setNotificationLed(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_LED, enabled).apply();
    }
    
    public boolean isNotificationLed() {
        return preferences.getBoolean(KEY_NOTIFICATION_LED, true);
    }
    
    public void setNotificationPriority(int priority) {
        preferences.edit().putInt(KEY_NOTIFICATION_PRIORITY, priority).apply();
    }
    
    public int getNotificationPriority() {
        return preferences.getInt(KEY_NOTIFICATION_PRIORITY, 0); // PRIORITY_DEFAULT
    }
    
    public void setShowEmailStatus(boolean show) {
        preferences.edit().putBoolean(KEY_SHOW_EMAIL_STATUS, show).apply();
    }
    
    public boolean isShowEmailStatus() {
        return preferences.getBoolean(KEY_SHOW_EMAIL_STATUS, true);
    }
    
    public void setShowSmsPreview(boolean show) {
        preferences.edit().putBoolean(KEY_SHOW_SMS_PREVIEW, show).apply();
    }
    
    public boolean isShowSmsPreview() {
        return preferences.getBoolean(KEY_SHOW_SMS_PREVIEW, true);
    }
    
    // Advanced Settings Methods
    public void setEmailRetryCount(int count) {
        preferences.edit().putInt(KEY_EMAIL_RETRY_COUNT, count).apply();
    }
    
    public int getEmailRetryCount() {
        return preferences.getInt(KEY_EMAIL_RETRY_COUNT, DEFAULT_EMAIL_RETRY_COUNT);
    }
    
    public void setEmailRetryDelay(int delay) {
        preferences.edit().putInt(KEY_EMAIL_RETRY_DELAY, delay).apply();
    }
    
    public int getEmailRetryDelay() {
        return preferences.getInt(KEY_EMAIL_RETRY_DELAY, DEFAULT_EMAIL_RETRY_DELAY);
    }
    
    public void setConnectionTimeout(int timeout) {
        preferences.edit().putInt(KEY_CONNECTION_TIMEOUT, timeout).apply();
    }
    
    public int getConnectionTimeout() {
        return preferences.getInt(KEY_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }
    
    public void setDebugMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply();
    }
    
    public boolean isDebugMode() {
        return preferences.getBoolean(KEY_DEBUG_MODE, false);
    }
    
    public void setBatteryOptimizationWarned(boolean warned) {
        preferences.edit().putBoolean(KEY_BATTERY_OPTIMIZATION_WARNED, warned).apply();
    }
    
    public boolean isBatteryOptimizationWarned() {
        return preferences.getBoolean(KEY_BATTERY_OPTIMIZATION_WARNED, false);
    }
    
    public void setFirstRun(boolean firstRun) {
        preferences.edit().putBoolean(KEY_FIRST_RUN, firstRun).apply();
    }
    
    public boolean isFirstRun() {
        return preferences.getBoolean(KEY_FIRST_RUN, true);
    }
    
    public void setAppVersion(String version) {
        preferences.edit().putString(KEY_APP_VERSION, version).apply();
    }
    
    public String getAppVersion() {
        return preferences.getString(KEY_APP_VERSION, "1.0.0");
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
    
    // Enhanced backup and restore methods
    public String exportSettingsToJson() {
        try {
            JSONObject json = new JSONObject();
            
            // Email settings
            json.put("smtp_server", getSmtpServer());
            json.put("smtp_port", getSmtpPort());
            json.put("email_username", getEmailUsername());
            json.put("to_email", getToEmail());
            json.put("use_tls", isUseTls());
            json.put("use_ssl", isUseSsl());
            
            // Service settings
            json.put("service_enabled", isServiceEnabled());
            json.put("auto_start", isAutoStart());
            
            // Message formatting
            json.put("include_timestamp", isIncludeTimestamp());
            json.put("include_sender", isIncludeSender());
            json.put("include_carrier_info", isIncludeCarrierInfo());
            json.put("include_message_count", isIncludeMessageCount());
            json.put("email_subject_format", getEmailSubjectFormat());
            json.put("date_format", getDateFormat());
            json.put("time_format", getTimeFormat());
            
            // SMS filtering
            json.put("filter_enabled", isFilterEnabled());
            json.put("filter_mode", getFilterMode().name());
            json.put("filter_spam", isFilterSpam());
            json.put("min_message_length", getMinMessageLength());
            json.put("max_message_length", getMaxMessageLength());
            
            // Notification settings
            json.put("notification_enabled", isNotificationEnabled());
            json.put("notification_sound", isNotificationSound());
            json.put("notification_vibrate", isNotificationVibrate());
            json.put("notification_led", isNotificationLed());
            json.put("show_email_status", isShowEmailStatus());
            json.put("show_sms_preview", isShowSmsPreview());
            
            // Advanced settings
            json.put("email_retry_count", getEmailRetryCount());
            json.put("email_retry_delay", getEmailRetryDelay());
            json.put("connection_timeout", getConnectionTimeout());
            json.put("debug_mode", isDebugMode());
            
            return json.toString(2); // Pretty print with 2-space indentation
            
        } catch (JSONException e) {
            Log.e(TAG, "Error exporting settings to JSON", e);
            return null;
        }
    }
    
    public boolean importSettingsFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            SharedPreferences.Editor editor = preferences.edit();
            
            // Email settings (excluding password for security)
            if (json.has("smtp_server")) editor.putString(KEY_SMTP_SERVER, json.getString("smtp_server"));
            if (json.has("smtp_port")) editor.putInt(KEY_SMTP_PORT, json.getInt("smtp_port"));
            if (json.has("email_username")) editor.putString(KEY_EMAIL_USERNAME, json.getString("email_username"));
            if (json.has("to_email")) editor.putString(KEY_TO_EMAIL, json.getString("to_email"));
            if (json.has("use_tls")) editor.putBoolean(KEY_USE_TLS, json.getBoolean("use_tls"));
            if (json.has("use_ssl")) editor.putBoolean(KEY_USE_SSL, json.getBoolean("use_ssl"));
            
            // Service settings
            if (json.has("auto_start")) editor.putBoolean(KEY_AUTO_START, json.getBoolean("auto_start"));
            
            // Message formatting
            if (json.has("include_timestamp")) editor.putBoolean(KEY_INCLUDE_TIMESTAMP, json.getBoolean("include_timestamp"));
            if (json.has("include_sender")) editor.putBoolean(KEY_INCLUDE_SENDER, json.getBoolean("include_sender"));
            if (json.has("include_carrier_info")) editor.putBoolean(KEY_INCLUDE_CARRIER_INFO, json.getBoolean("include_carrier_info"));
            if (json.has("include_message_count")) editor.putBoolean(KEY_INCLUDE_MESSAGE_COUNT, json.getBoolean("include_message_count"));
            if (json.has("email_subject_format")) editor.putString(KEY_EMAIL_SUBJECT_FORMAT, json.getString("email_subject_format"));
            if (json.has("date_format")) editor.putString(KEY_DATE_FORMAT, json.getString("date_format"));
            if (json.has("time_format")) editor.putString(KEY_TIME_FORMAT, json.getString("time_format"));
            
            // SMS filtering
            if (json.has("filter_enabled")) editor.putBoolean(KEY_FILTER_ENABLED, json.getBoolean("filter_enabled"));
            if (json.has("filter_mode")) editor.putString(KEY_FILTER_MODE, json.getString("filter_mode"));
            if (json.has("filter_spam")) editor.putBoolean(KEY_FILTER_SPAM, json.getBoolean("filter_spam"));
            if (json.has("min_message_length")) editor.putInt(KEY_MIN_MESSAGE_LENGTH, json.getInt("min_message_length"));
            if (json.has("max_message_length")) editor.putInt(KEY_MAX_MESSAGE_LENGTH, json.getInt("max_message_length"));
            
            // Notification settings
            if (json.has("notification_enabled")) editor.putBoolean(KEY_NOTIFICATION_ENABLED, json.getBoolean("notification_enabled"));
            if (json.has("notification_sound")) editor.putBoolean(KEY_NOTIFICATION_SOUND, json.getBoolean("notification_sound"));
            if (json.has("notification_vibrate")) editor.putBoolean(KEY_NOTIFICATION_VIBRATE, json.getBoolean("notification_vibrate"));
            if (json.has("notification_led")) editor.putBoolean(KEY_NOTIFICATION_LED, json.getBoolean("notification_led"));
            if (json.has("show_email_status")) editor.putBoolean(KEY_SHOW_EMAIL_STATUS, json.getBoolean("show_email_status"));
            if (json.has("show_sms_preview")) editor.putBoolean(KEY_SHOW_SMS_PREVIEW, json.getBoolean("show_sms_preview"));
            
            // Advanced settings
            if (json.has("email_retry_count")) editor.putInt(KEY_EMAIL_RETRY_COUNT, json.getInt("email_retry_count"));
            if (json.has("email_retry_delay")) editor.putInt(KEY_EMAIL_RETRY_DELAY, json.getInt("email_retry_delay"));
            if (json.has("connection_timeout")) editor.putInt(KEY_CONNECTION_TIMEOUT, json.getInt("connection_timeout"));
            if (json.has("debug_mode")) editor.putBoolean(KEY_DEBUG_MODE, json.getBoolean("debug_mode"));
            
            editor.apply();
            Log.i(TAG, "Settings imported successfully from JSON");
            return true;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error importing settings from JSON", e);
            return false;
        }
    }
    
    // Configuration validation
    public boolean validateConfiguration() {
        return isEmailConfigured() && 
               !TextUtils.isEmpty(getSmtpServer()) &&
               getSmtpPort() > 0 && getSmtpPort() <= 65535 &&
               !TextUtils.isEmpty(getEmailUsername()) &&
               !TextUtils.isEmpty(getToEmail());
    }
    
    // Get configuration summary for diagnostics
    public String getConfigurationSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Email Configuration ===\n");
        sb.append("SMTP Server: ").append(getSmtpServer()).append("\n");
        sb.append("SMTP Port: ").append(getSmtpPort()).append("\n");
        sb.append("Username: ").append(maskEmail(getEmailUsername())).append("\n");
        sb.append("To Email: ").append(maskEmail(getToEmail())).append("\n");
        sb.append("Use TLS: ").append(isUseTls()).append("\n");
        sb.append("Use SSL: ").append(isUseSsl()).append("\n");
        sb.append("Configured: ").append(isEmailConfigured()).append("\n\n");
        
        sb.append("=== Service Configuration ===\n");
        sb.append("Service Enabled: ").append(isServiceEnabled()).append("\n");
        sb.append("Auto Start: ").append(isAutoStart()).append("\n\n");
        
        sb.append("=== Message Formatting ===\n");
        sb.append("Include Timestamp: ").append(isIncludeTimestamp()).append("\n");
        sb.append("Include Sender: ").append(isIncludeSender()).append("\n");
        sb.append("Include Carrier: ").append(isIncludeCarrierInfo()).append("\n");
        sb.append("Subject Format: ").append(getEmailSubjectFormat()).append("\n\n");
        
        sb.append("=== SMS Filtering ===\n");
        sb.append("Filter Enabled: ").append(isFilterEnabled()).append("\n");
        sb.append("Filter Mode: ").append(getFilterMode()).append("\n");
        sb.append("Filter Spam: ").append(isFilterSpam()).append("\n\n");
        
        sb.append("=== Advanced Settings ===\n");
        sb.append("Retry Count: ").append(getEmailRetryCount()).append("\n");
        sb.append("Retry Delay: ").append(getEmailRetryDelay()).append("ms\n");
        sb.append("Connection Timeout: ").append(getConnectionTimeout()).append("ms\n");
        sb.append("Debug Mode: ").append(isDebugMode()).append("\n");
        
        return sb.toString();
    }
    
    private String maskEmail(String email) {
        if (TextUtils.isEmpty(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        String masked = username.substring(0, 2) + "***" + "@" + domain;
        return masked;
    }
}