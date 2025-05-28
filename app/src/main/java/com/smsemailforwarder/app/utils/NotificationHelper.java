package com.smsemailforwarder.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.smsemailforwarder.app.MainActivity;
import com.smsemailforwarder.app.R;

/**
 * Enhanced utility class for managing notifications
 * Handles service status, SMS processing, and error notifications
 * Supports customizable notification preferences
 */
public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    
    // Notification channels
    public static final String CHANNEL_SERVICE = "sms_forwarder_service";
    public static final String CHANNEL_SMS_PROCESSING = "sms_processing";
    public static final String CHANNEL_ERRORS = "sms_errors";
    public static final String CHANNEL_INFO = "sms_info";
    
    // Notification IDs
    public static final int NOTIFICATION_ID_SERVICE = 1001;
    public static final int NOTIFICATION_ID_SMS_RECEIVED = 1002;
    public static final int NOTIFICATION_ID_EMAIL_SENT = 1003;
    public static final int NOTIFICATION_ID_ERROR = 1004;
    public static final int NOTIFICATION_ID_INFO = 1005;
    public static final int NOTIFICATION_ID_FILTERED = 1006;
    
    private final Context context;
    private final NotificationManager notificationManager;
    private final PreferencesManager preferencesManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.preferencesManager = new PreferencesManager(context);
        createNotificationChannels();
    }
    
    /**
     * Creates notification channels for Android 8.0+
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Service channel
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                context.getString(R.string.service_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription(context.getString(R.string.service_notification_channel_description));
            serviceChannel.setShowBadge(false);
            serviceChannel.enableLights(false);
            serviceChannel.enableVibration(false);
            notificationManager.createNotificationChannel(serviceChannel);
            
            // SMS processing channel
            NotificationChannel smsChannel = new NotificationChannel(
                CHANNEL_SMS_PROCESSING,
                "SMS Processing",
                getNotificationImportance()
            );
            smsChannel.setDescription("Notifications about SMS message processing");
            configureChannelSettings(smsChannel);
            notificationManager.createNotificationChannel(smsChannel);
            
            // Error channel
            NotificationChannel errorChannel = new NotificationChannel(
                CHANNEL_ERRORS,
                "Errors & Warnings",
                NotificationManager.IMPORTANCE_HIGH
            );
            errorChannel.setDescription("Important error notifications");
            errorChannel.enableLights(true);
            errorChannel.enableVibration(true);
            errorChannel.setLightColor(0xFFFF0000); // Red
            notificationManager.createNotificationChannel(errorChannel);
            
            // Info channel
            NotificationChannel infoChannel = new NotificationChannel(
                CHANNEL_INFO,
                "Information",
                NotificationManager.IMPORTANCE_LOW
            );
            infoChannel.setDescription("General information notifications");
            configureChannelSettings(infoChannel);
            notificationManager.createNotificationChannel(infoChannel);
        }
    }
    
    /**
     * Configure channel settings based on user preferences
     */
    private void configureChannelSettings(NotificationChannel channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.enableLights(preferencesManager.isNotificationLed());
            channel.enableVibration(preferencesManager.isNotificationVibrate());
            
            if (preferencesManager.isNotificationSound()) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(defaultSoundUri, null);
            } else {
                channel.setSound(null, null);
            }
            
            if (preferencesManager.isNotificationLed()) {
                channel.setLightColor(0xFF00FF00); // Green
            }
        }
    }
    
    /**
     * Get notification importance based on user preferences
     */
    private int getNotificationImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int priority = preferencesManager.getNotificationPriority();
            switch (priority) {
                case NotificationCompat.PRIORITY_HIGH:
                    return NotificationManager.IMPORTANCE_HIGH;
                case NotificationCompat.PRIORITY_LOW:
                    return NotificationManager.IMPORTANCE_LOW;
                case NotificationCompat.PRIORITY_MIN:
                    return NotificationManager.IMPORTANCE_MIN;
                default:
                    return NotificationManager.IMPORTANCE_DEFAULT;
            }
        }
        return NotificationManager.IMPORTANCE_DEFAULT;
    }
    
    /**
     * Creates foreground service notification
     */
    public NotificationCompat.Builder createServiceNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(context, CHANNEL_SERVICE)
                .setContentTitle(context.getString(R.string.service_notification_title))
                .setContentText(context.getString(R.string.service_notification_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }
    
    /**
     * Shows notification when SMS is received and processed
     */
    public void showSmsReceivedNotification(String sender, String messagePreview) {
        if (!preferencesManager.isNotificationEnabled()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = "SMS Forwarded";
        String text = "From: " + SmsFormatter.formatSender(sender);
        if (messagePreview != null && !messagePreview.isEmpty()) {
            text += " - " + (messagePreview.length() > 30 ? 
                           messagePreview.substring(0, 30) + "..." : messagePreview);
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SMS_PROCESSING)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(preferencesManager.getNotificationPriority());
        
        // Apply notification preferences
        applyNotificationPreferences(builder);
        
        notificationManager.notify(NOTIFICATION_ID_SMS_RECEIVED, builder.build());
    }
    
    /**
     * Shows notification when email is successfully sent
     */
    public void showEmailSentNotification(String recipient) {
        if (!preferencesManager.isNotificationEnabled() || !preferencesManager.isShowEmailStatus()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SMS_PROCESSING)
                .setContentTitle("Email Sent")
                .setContentText("SMS forwarded to " + recipient)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Apply minimal notification preferences for status updates
        if (preferencesManager.isNotificationSound()) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }
        
        notificationManager.notify(NOTIFICATION_ID_EMAIL_SENT, builder.build());
    }
    
    /**
     * Shows error notification
     */
    public void showErrorNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ERRORS)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        
        // Always show error notifications with full alerts
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        
        notificationManager.notify(NOTIFICATION_ID_ERROR, builder.build());
    }
    
    /**
     * Shows general information notification
     */
    public void showInfoNotification(String title, String message) {
        if (!preferencesManager.isNotificationEnabled()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INFO)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Apply minimal notification preferences for info messages
        if (preferencesManager.isNotificationSound()) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        }
        
        notificationManager.notify(NOTIFICATION_ID_INFO, builder.build());
    }
    
    /**
     * Shows notification when SMS is filtered
     */
    public void showFilteredNotification(String sender, String reason) {
        if (!preferencesManager.isNotificationEnabled() || !preferencesManager.isDebugMode()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INFO)
                .setContentTitle("SMS Filtered")
                .setContentText("From: " + SmsFormatter.formatSender(sender) + " - " + reason)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MIN);
        
        notificationManager.notify(NOTIFICATION_ID_FILTERED, builder.build());
    }
    
    /**
     * Apply user notification preferences to builder
     */
    private void applyNotificationPreferences(NotificationCompat.Builder builder) {
        int defaults = 0;
        
        if (preferencesManager.isNotificationSound()) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        
        if (preferencesManager.isNotificationVibrate()) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        
        if (preferencesManager.isNotificationLed()) {
            defaults |= NotificationCompat.DEFAULT_LIGHTS;
        }
        
        if (defaults > 0) {
            builder.setDefaults(defaults);
        }
    }
    
    /**
     * Update notification channels when preferences change
     */
    public void updateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Recreate channels with updated settings
            createNotificationChannels();
        }
    }
    
    /**
     * Cancels all non-service notifications
     */
    public void cancelProcessingNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_SMS_RECEIVED);
        notificationManager.cancel(NOTIFICATION_ID_EMAIL_SENT);
        notificationManager.cancel(NOTIFICATION_ID_INFO);
        notificationManager.cancel(NOTIFICATION_ID_FILTERED);
    }
    
    /**
     * Cancels specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Check if notifications are enabled system-wide
     */
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
    
    /**
     * Get notification settings summary for diagnostics
     */
    public String getNotificationSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Notification Settings ===\n");
        sb.append("System Enabled: ").append(areNotificationsEnabled()).append("\n");
        sb.append("App Enabled: ").append(preferencesManager.isNotificationEnabled()).append("\n");
        sb.append("Sound: ").append(preferencesManager.isNotificationSound()).append("\n");
        sb.append("Vibrate: ").append(preferencesManager.isNotificationVibrate()).append("\n");
        sb.append("LED: ").append(preferencesManager.isNotificationLed()).append("\n");
        sb.append("Priority: ").append(preferencesManager.getNotificationPriority()).append("\n");
        sb.append("Show Email Status: ").append(preferencesManager.isShowEmailStatus()).append("\n");
        sb.append("Show SMS Preview: ").append(preferencesManager.isShowSmsPreview()).append("\n");
        return sb.toString();
    }
} 