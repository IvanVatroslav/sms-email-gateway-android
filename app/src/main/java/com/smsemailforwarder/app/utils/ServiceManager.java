package com.smsemailforwarder.app.utils;

import android.content.Context;
import android.util.Log;

import com.smsemailforwarder.app.ForwarderService;

/**
 * Utility class for managing the ForwarderService lifecycle
 * Provides easy methods for starting, stopping, and checking service status
 */
public class ServiceManager {
    
    private static final String TAG = "ServiceManager";
    
    /**
     * Starts the SMS forwarding service with validation
     */
    public static boolean startSmsForwarding(Context context) {
        Log.d(TAG, "Attempting to start SMS forwarding service");
        
        PreferencesManager prefs = new PreferencesManager(context);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        
        // Validate configuration before starting
        if (!prefs.isEmailConfigured()) {
            Log.e(TAG, "Cannot start service - email not configured");
            notificationHelper.showErrorNotification(
                "Configuration Required",
                "Please configure email settings before starting SMS forwarding"
            );
            return false;
        }
        
        try {
            // Enable service in preferences
            prefs.setServiceEnabled(true);
            
            // Start the service
            ForwarderService.startService(context);
            
            Log.i(TAG, "SMS forwarding service start initiated");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting SMS forwarding service", e);
            notificationHelper.showErrorNotification(
                "Service Start Error",
                "Failed to start SMS forwarding: " + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Stops the SMS forwarding service
     */
    public static boolean stopSmsForwarding(Context context) {
        Log.d(TAG, "Attempting to stop SMS forwarding service");
        
        PreferencesManager prefs = new PreferencesManager(context);
        
        try {
            // Disable service in preferences
            prefs.setServiceEnabled(false);
            
            // Stop the service
            ForwarderService.stopService(context);
            
            Log.i(TAG, "SMS forwarding service stop initiated");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping SMS forwarding service", e);
            
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showErrorNotification(
                "Service Stop Error",
                "Failed to stop SMS forwarding: " + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Restarts the SMS forwarding service
     */
    public static boolean restartSmsForwarding(Context context) {
        Log.d(TAG, "Attempting to restart SMS forwarding service");
        
        try {
            ForwarderService.restartService(context);
            Log.i(TAG, "SMS forwarding service restart initiated");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error restarting SMS forwarding service", e);
            
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showErrorNotification(
                "Service Restart Error",
                "Failed to restart SMS forwarding: " + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Checks if the SMS forwarding service is running
     */
    public static boolean isSmsForwardingRunning() {
        return ForwarderService.isRunning();
    }
    
    /**
     * Gets the current service status as a user-friendly string
     */
    public static String getServiceStatusText(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        
        if (!prefs.isEmailConfigured()) {
            return "Not Configured";
        }
        
        if (!prefs.isServiceEnabled()) {
            return "Disabled";
        }
        
        if (ForwarderService.isRunning()) {
            return "Running";
        } else {
            return "Stopped";
        }
    }
    
    /**
     * Gets detailed service status information
     */
    public static String getDetailedServiceStatus(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        StringBuilder status = new StringBuilder();
        
        status.append("SMS Forwarding Service Status:\n");
        status.append("═══════════════════════════════════\n");
        
        // Configuration status
        status.append("Email Configured: ").append(prefs.isEmailConfigured() ? "Yes" : "No").append("\n");
        status.append("Service Enabled: ").append(prefs.isServiceEnabled() ? "Yes" : "No").append("\n");
        status.append("Auto-Start: ").append(prefs.isAutoStart() ? "Yes" : "No").append("\n");
        
        // Runtime status
        status.append("Currently Running: ").append(ForwarderService.isRunning() ? "Yes" : "No").append("\n");
        
        // Overall status
        String overallStatus = getServiceStatusText(context);
        status.append("Overall Status: ").append(overallStatus).append("\n");
        
        return status.toString();
    }
    
    /**
     * Validates service configuration and returns issues
     */
    public static String validateServiceConfiguration(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        StringBuilder issues = new StringBuilder();
        
        if (!prefs.isEmailConfigured()) {
            issues.append("• Email configuration missing\n");
        }
        
        // Check email validation
        if (prefs.isEmailConfigured()) {
            String username = prefs.getEmailUsername();
            String recipient = prefs.getEmailRecipient();
            String server = prefs.getEmailSmtpServer();
            
            if (!EmailConfiguration.isValidEmail(username)) {
                issues.append("• Invalid sender email format\n");
            }
            
            if (!EmailConfiguration.isValidEmail(recipient)) {
                issues.append("• Invalid recipient email format\n");
            }
            
            if (server == null || server.isEmpty()) {
                issues.append("• SMTP server not configured\n");
            }
        }
        
        if (issues.length() == 0) {
            return "Configuration is valid ✓";
        } else {
            return "Configuration Issues:\n" + issues.toString();
        }
    }
    
    /**
     * Enables or disables auto-start functionality
     */
    public static void setAutoStart(Context context, boolean enabled) {
        PreferencesManager prefs = new PreferencesManager(context);
        prefs.setAutoStart(enabled);
        
        Log.i(TAG, "Auto-start " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Checks if auto-start is enabled
     */
    public static boolean isAutoStartEnabled(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        return prefs.isAutoStart();
    }
} 