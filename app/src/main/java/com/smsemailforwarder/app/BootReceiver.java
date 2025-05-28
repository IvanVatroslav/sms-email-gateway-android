package com.smsemailforwarder.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smsemailforwarder.app.utils.PreferencesManager;

/**
 * Broadcast receiver for device boot events
 * Automatically starts the SMS forwarding service if enabled
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.d(TAG, "Boot completed or package replaced - checking auto-start");
            
            PreferencesManager preferencesManager = new PreferencesManager(context);
            
            if (preferencesManager.isAutoStart() && preferencesManager.isEmailConfigured()) {
                Log.d(TAG, "Starting ForwarderService automatically");
                
                Intent serviceIntent = new Intent(context, ForwarderService.class);
                context.startForegroundService(serviceIntent);
                
                // Update service state
                preferencesManager.setServiceEnabled(true);
            } else {
                Log.d(TAG, "Auto-start disabled or email not configured");
            }
        }
    }
} 