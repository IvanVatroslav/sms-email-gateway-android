package com.smsemailforwarder.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Foreground service for SMS-to-Email forwarding
 * This will be implemented in Phase 4
 */
public class ForwarderService extends Service {
    
    private static final String TAG = "ForwarderService";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ForwarderService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ForwarderService started - Full implementation coming in Phase 4");
        
        // TODO: Phase 4 Implementation
        // 1. Create foreground notification
        // 2. Start foreground service
        // 3. Set up SMS monitoring
        // 4. Handle service restart on crash
        // 5. Manage wake locks
        
        return START_STICKY; // Service should be restarted if killed
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ForwarderService destroyed");
        
        // TODO: Cleanup resources, stop notifications
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // This service doesn't support binding
        return null;
    }
} 