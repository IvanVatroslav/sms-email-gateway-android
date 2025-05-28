package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for managing configuration backup, restore, import, and export
 * Handles file operations and configuration validation
 */
public class ConfigurationManager {
    
    private static final String TAG = "ConfigurationManager";
    private static final String BACKUP_DIR = "SMSEmailForwarder";
    private static final String BACKUP_FILE_PREFIX = "sms_email_config_";
    private static final String BACKUP_FILE_EXTENSION = ".json";
    
    private final Context context;
    private final PreferencesManager preferencesManager;
    
    public ConfigurationManager(Context context) {
        this.context = context;
        this.preferencesManager = new PreferencesManager(context);
    }
    
    /**
     * Create a backup of current configuration
     * @return File object of the backup file, or null if failed
     */
    public File createBackup() {
        try {
            // Create backup directory if it doesn't exist
            File backupDir = getBackupDirectory();
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                Log.e(TAG, "Failed to create backup directory");
                return null;
            }
            
            // Generate backup filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = BACKUP_FILE_PREFIX + timestamp + BACKUP_FILE_EXTENSION;
            File backupFile = new File(backupDir, filename);
            
            // Export settings to JSON
            String configJson = preferencesManager.exportSettingsToJson();
            if (TextUtils.isEmpty(configJson)) {
                Log.e(TAG, "Failed to export settings to JSON");
                return null;
            }
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream(backupFile)) {
                fos.write(configJson.getBytes("UTF-8"));
                fos.flush();
            }
            
            Log.i(TAG, "Configuration backup created: " + backupFile.getAbsolutePath());
            return backupFile;
            
        } catch (IOException e) {
            Log.e(TAG, "Error creating configuration backup", e);
            return null;
        }
    }
    
    /**
     * Restore configuration from a backup file
     * @param backupFile The backup file to restore from
     * @return true if restore was successful, false otherwise
     */
    public boolean restoreFromBackup(File backupFile) {
        if (backupFile == null || !backupFile.exists() || !backupFile.canRead()) {
            Log.e(TAG, "Backup file is invalid or not readable");
            return false;
        }
        
        try {
            // Read file content
            StringBuilder content = new StringBuilder();
            try (FileInputStream fis = new FileInputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    content.append(new String(buffer, 0, bytesRead, "UTF-8"));
                }
            }
            
            // Import settings from JSON
            boolean success = preferencesManager.importSettingsFromJson(content.toString());
            
            if (success) {
                Log.i(TAG, "Configuration restored from: " + backupFile.getAbsolutePath());
            } else {
                Log.e(TAG, "Failed to import settings from backup file");
            }
            
            return success;
            
        } catch (IOException e) {
            Log.e(TAG, "Error restoring configuration from backup", e);
            return false;
        }
    }
    
    /**
     * Share configuration as a file
     * @return Intent for sharing the configuration file, or null if failed
     */
    public Intent shareConfiguration() {
        File backupFile = createBackup();
        if (backupFile == null) {
            return null;
        }
        
        try {
            // Use FileProvider to share the file securely
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                backupFile
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SMS Email Forwarder Configuration");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "SMS Email Forwarder configuration backup");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            return Intent.createChooser(shareIntent, "Share Configuration");
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing configuration", e);
            return null;
        }
    }
    
    /**
     * Import configuration from a JSON string
     * @param jsonConfig The JSON configuration string
     * @return ImportResult with success status and details
     */
    public ImportResult importConfiguration(String jsonConfig) {
        ImportResult result = new ImportResult();
        
        if (TextUtils.isEmpty(jsonConfig)) {
            result.success = false;
            result.errorMessage = "Configuration data is empty";
            return result;
        }
        
        // Validate JSON format
        if (!isValidJsonConfiguration(jsonConfig)) {
            result.success = false;
            result.errorMessage = "Invalid configuration format";
            return result;
        }
        
        // Create backup before importing
        File currentBackup = createBackup();
        if (currentBackup != null) {
            result.backupFile = currentBackup;
        }
        
        // Import the new configuration
        boolean importSuccess = preferencesManager.importSettingsFromJson(jsonConfig);
        
        if (importSuccess) {
            result.success = true;
            result.message = "Configuration imported successfully";
            
            // Validate the imported configuration
            if (!preferencesManager.validateConfiguration()) {
                result.warnings.append("Warning: Imported configuration may be incomplete. ");
                result.warnings.append("Please verify email settings.\n");
            }
        } else {
            result.success = false;
            result.errorMessage = "Failed to import configuration";
            
            // Restore from backup if available
            if (currentBackup != null) {
                restoreFromBackup(currentBackup);
                result.errorMessage += " (Previous configuration restored)";
            }
        }
        
        return result;
    }
    
    /**
     * Get list of available backup files
     * @return Array of backup files, or empty array if none found
     */
    public File[] getAvailableBackups() {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            return new File[0];
        }
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> 
            name.startsWith(BACKUP_FILE_PREFIX) && name.endsWith(BACKUP_FILE_EXTENSION));
        
        return backupFiles != null ? backupFiles : new File[0];
    }
    
    /**
     * Delete old backup files, keeping only the most recent ones
     * @param keepCount Number of recent backups to keep
     * @return Number of files deleted
     */
    public int cleanupOldBackups(int keepCount) {
        File[] backups = getAvailableBackups();
        if (backups.length <= keepCount) {
            return 0; // Nothing to delete
        }
        
        // Sort by last modified date (newest first)
        java.util.Arrays.sort(backups, (f1, f2) -> 
            Long.compare(f2.lastModified(), f1.lastModified()));
        
        int deletedCount = 0;
        for (int i = keepCount; i < backups.length; i++) {
            if (backups[i].delete()) {
                deletedCount++;
                Log.d(TAG, "Deleted old backup: " + backups[i].getName());
            }
        }
        
        Log.i(TAG, "Cleaned up " + deletedCount + " old backup files");
        return deletedCount;
    }
    
    /**
     * Get backup directory
     */
    private File getBackupDirectory() {
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(documentsDir, BACKUP_DIR);
    }
    
    /**
     * Validate if the JSON string is a valid configuration
     */
    private boolean isValidJsonConfiguration(String jsonConfig) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(jsonConfig);
            
            // Check for required fields
            return json.has("smtp_server") || 
                   json.has("email_username") || 
                   json.has("to_email") ||
                   json.has("service_enabled") ||
                   json.has("auto_start");
                   
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Invalid JSON configuration", e);
            return false;
        }
    }
    
    /**
     * Get configuration summary for display
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== Configuration Summary ===\n\n");
        summary.append(preferencesManager.getConfigurationSummary());
        
        // Add backup information
        File[] backups = getAvailableBackups();
        summary.append("\n=== Backup Information ===\n");
        summary.append("Available Backups: ").append(backups.length).append("\n");
        
        if (backups.length > 0) {
            // Show most recent backup
            java.util.Arrays.sort(backups, (f1, f2) -> 
                Long.compare(f2.lastModified(), f1.lastModified()));
            
            File mostRecent = backups[0];
            Date lastBackup = new Date(mostRecent.lastModified());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            summary.append("Last Backup: ").append(sdf.format(lastBackup)).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Reset configuration to defaults
     */
    public boolean resetToDefaults() {
        try {
            // Create backup before reset
            File backup = createBackup();
            
            // Clear all settings
            preferencesManager.clearAllSettings();
            
            Log.i(TAG, "Configuration reset to defaults" + 
                  (backup != null ? " (backup created)" : ""));
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error resetting configuration", e);
            return false;
        }
    }
    
    /**
     * Result class for import operations
     */
    public static class ImportResult {
        public boolean success;
        public String message;
        public String errorMessage;
        public StringBuilder warnings = new StringBuilder();
        public File backupFile;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ImportResult{");
            sb.append("success=").append(success);
            if (!TextUtils.isEmpty(message)) {
                sb.append(", message='").append(message).append("'");
            }
            if (!TextUtils.isEmpty(errorMessage)) {
                sb.append(", error='").append(errorMessage).append("'");
            }
            if (warnings.length() > 0) {
                sb.append(", warnings='").append(warnings.toString().trim()).append("'");
            }
            if (backupFile != null) {
                sb.append(", backup='").append(backupFile.getName()).append("'");
            }
            sb.append("}");
            return sb.toString();
        }
    }
} 