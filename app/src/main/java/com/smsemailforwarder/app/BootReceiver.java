package com.smsemailforwarder.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smsemailforwarder.app.utils.NotificationHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;

/**
 * Broadcast receiver for device boot events
 * Automatically starts the SMS forwarding service if enabled
 * Handles app updates and package replacements
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.i(TAG, "Device boot completed or package updated - checking auto-start");
            
            PreferencesManager preferencesManager = new PreferencesManager(context);
            
            // Check if auto-start is enabled
            if (!preferencesManager.isAutoStart()) {
                Log.d(TAG, "Auto-start is disabled in preferences");
                return;
            }
            
            // Check if email is configured
            if (!preferencesManager.isEmailConfigured()) {
                Log.w(TAG, "Email not configured, cannot auto-start service");
                
                // Show notification about configuration needed
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showErrorNotification(
                    "SMS Forwarder Auto-Start Failed",
                    "Email configuration required. Please open the app to configure."
                );
                return;
            }
            
            try {
                Log.i(TAG, "Starting ForwarderService automatically");
                
                // Enable service in preferences
                preferencesManager.setServiceEnabled(true);
                
                // Start the ForwarderService using the static method
                ForwarderService.startService(context);
                
                Log.i(TAG, "ForwarderService auto-start initiated successfully");
                
                // Show success notification
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showSmsReceivedNotification(
                    "SMS Forwarder Started",
                    "Auto-started after device boot"
                );
                
            } catch (Exception e) {
                Log.e(TAG, "Error auto-starting ForwarderService", e);
                
                // Show error notification
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showErrorNotification(
                    "Auto-Start Failed",
                    "Failed to start SMS forwarding service: " + e.getMessage()
                );
            }
            
        } else {
            Log.d(TAG, "Received unhandled action: " + action);
        }
    }
} 