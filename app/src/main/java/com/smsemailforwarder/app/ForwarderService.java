package com.smsemailforwarder.app;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.smsemailforwarder.app.utils.NotificationHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;

/**
 * Foreground service for SMS-to-Email forwarding
 * Ensures continuous operation and SMS monitoring
 * Handles service lifecycle and battery optimization
 */
public class ForwarderService extends Service {
    
    private static final String TAG = "ForwarderService";
    
    // Service actions
    public static final String ACTION_START_SERVICE = "com.smsemailforwarder.START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "com.smsemailforwarder.STOP_SERVICE";
    public static final String ACTION_RESTART_SERVICE = "com.smsemailforwarder.RESTART_SERVICE";
    
    // Service state
    private static boolean isServiceRunning = false;
    
    // Components
    private NotificationHelper notificationHelper;
    private PreferencesManager preferencesManager;
    private PowerManager.WakeLock wakeLock;
    private SmsReceiver smsReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ForwarderService created");
        
        // Initialize components
        notificationHelper = new NotificationHelper(this);
        preferencesManager = new PreferencesManager(this);
        
        // Initialize wake lock for reliable operation
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMSForwarder:ServiceWakeLock"
        );
        
        // Initialize SMS receiver for dynamic registration
        smsReceiver = new SmsReceiver();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ForwarderService onStartCommand");
        
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "Service action: " + action);
            
            switch (action != null ? action : ACTION_START_SERVICE) {
                case ACTION_START_SERVICE:
                    startForwarderService();
                    break;
                case ACTION_STOP_SERVICE:
                    stopForwarderService();
                    return START_NOT_STICKY;
                case ACTION_RESTART_SERVICE:
                    restartForwarderService();
                    break;
                default:
                    startForwarderService();
                    break;
            }
        } else {
            startForwarderService();
        }
        
        // Return START_STICKY to ensure service restarts if killed
        return START_STICKY;
    }
    
    /**
     * Starts the foreground service and SMS monitoring
     */
    private void startForwarderService() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running");
            return;
        }
        
        Log.i(TAG, "Starting SMS-to-Email forwarder service");
        
        // Check if service should be enabled
        if (!preferencesManager.isServiceEnabled()) {
            Log.w(TAG, "Service is disabled in preferences");
            stopSelf();
            return;
        }
        
        // Check email configuration
        if (!preferencesManager.isEmailConfigured()) {
            Log.e(TAG, "Email not configured, cannot start service");
            notificationHelper.showErrorNotification(
                "Configuration Error",
                "Please configure email settings before starting the service"
            );
            stopSelf();
            return;
        }
        
        try {
            // Start foreground service with notification
            startForeground(
                NotificationHelper.NOTIFICATION_ID_SERVICE,
                notificationHelper.createServiceNotification().build()
            );
            
            // Acquire wake lock for reliable operation
            if (!wakeLock.isHeld()) {
                wakeLock.acquire(10 * 60 * 1000L); // 10 minutes timeout
                Log.d(TAG, "Wake lock acquired");
            }
            
            // Register SMS receiver dynamically (backup to manifest registration)
            registerSmsReceiver();
            
            // Update service state
            isServiceRunning = true;
            
            Log.i(TAG, "SMS-to-Email forwarder service started successfully");
            
            // Show success notification
            notificationHelper.showSmsReceivedNotification(
                "Service Started",
                "SMS forwarding is now active"
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting forwarder service", e);
            notificationHelper.showErrorNotification(
                "Service Start Error",
                "Failed to start SMS forwarding: " + e.getMessage()
            );
            stopSelf();
        }
    }
    
    /**
     * Stops the foreground service and SMS monitoring
     */
    private void stopForwarderService() {
        Log.i(TAG, "Stopping SMS-to-Email forwarder service");
        
        try {
            // Unregister SMS receiver
            unregisterSmsReceiver();
            
            // Release wake lock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "Wake lock released");
            }
            
            // Update service state
            isServiceRunning = false;
            
            // Stop foreground service
            stopForeground(true);
            
            Log.i(TAG, "SMS-to-Email forwarder service stopped");
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping forwarder service", e);
        }
        
        // Stop the service
        stopSelf();
    }
    
    /**
     * Restarts the forwarder service
     */
    private void restartForwarderService() {
        Log.i(TAG, "Restarting SMS-to-Email forwarder service");
        
        stopForwarderService();
        
        // Small delay before restart
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        startForwarderService();
    }
    
    /**
     * Registers SMS receiver dynamically
     */
    private void registerSmsReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            filter.setPriority(1000); // High priority
            
            registerReceiver(smsReceiver, filter);
            Log.d(TAG, "SMS receiver registered dynamically");
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to register SMS receiver dynamically", e);
            // Not critical - manifest registration should still work
        }
    }
    
    /**
     * Unregisters SMS receiver
     */
    private void unregisterSmsReceiver() {
        try {
            if (smsReceiver != null) {
                unregisterReceiver(smsReceiver);
                Log.d(TAG, "SMS receiver unregistered");
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to unregister SMS receiver", e);
        }
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "ForwarderService onDestroy");
        
        try {
            // Clean up resources
            unregisterSmsReceiver();
            
            // Release wake lock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "Wake lock released in onDestroy");
            }
            
            // Update service state
            isServiceRunning = false;
            
            // Cancel service notifications
            if (notificationHelper != null) {
                notificationHelper.cancelNotification(NotificationHelper.NOTIFICATION_ID_SERVICE);
            }
            
            Log.i(TAG, "ForwarderService destroyed and cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // This service doesn't support binding
        return null;
    }
    
    /**
     * Static method to check if service is running
     */
    public static boolean isRunning() {
        return isServiceRunning;
    }
    
    /**
     * Static method to start the service
     */
    public static void startService(android.content.Context context) {
        Intent intent = new Intent(context, ForwarderService.class);
        intent.setAction(ACTION_START_SERVICE);
        context.startForegroundService(intent);
    }
    
    /**
     * Static method to stop the service
     */
    public static void stopService(android.content.Context context) {
        Intent intent = new Intent(context, ForwarderService.class);
        intent.setAction(ACTION_STOP_SERVICE);
        context.startService(intent);
    }
    
    /**
     * Static method to restart the service
     */
    public static void restartService(android.content.Context context) {
        Intent intent = new Intent(context, ForwarderService.class);
        intent.setAction(ACTION_RESTART_SERVICE);
        context.startForegroundService(intent);
    }
} 