package com.smsemailforwarder.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.smsemailforwarder.app.MainActivity;
import com.smsemailforwarder.app.R;

/**
 * Utility class for managing notifications
 * Handles service status, SMS processing, and error notifications
 */
public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    
    // Notification channels
    public static final String CHANNEL_SERVICE = "sms_forwarder_service";
    public static final String CHANNEL_SMS_PROCESSING = "sms_processing";
    public static final String CHANNEL_ERRORS = "sms_errors";
    
    // Notification IDs
    public static final int NOTIFICATION_ID_SERVICE = 1001;
    public static final int NOTIFICATION_ID_SMS_RECEIVED = 1002;
    public static final int NOTIFICATION_ID_EMAIL_SENT = 1003;
    public static final int NOTIFICATION_ID_ERROR = 1004;
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
            notificationManager.createNotificationChannel(serviceChannel);
            
            // SMS processing channel
            NotificationChannel smsChannel = new NotificationChannel(
                CHANNEL_SMS_PROCESSING,
                "SMS Processing",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            smsChannel.setDescription("Notifications about SMS message processing");
            notificationManager.createNotificationChannel(smsChannel);
            
            // Error channel
            NotificationChannel errorChannel = new NotificationChannel(
                CHANNEL_ERRORS,
                "Errors & Warnings",
                NotificationManager.IMPORTANCE_HIGH
            );
            errorChannel.setDescription("Important error notifications");
            notificationManager.createNotificationChannel(errorChannel);
        }
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        notificationManager.notify(NOTIFICATION_ID_SMS_RECEIVED, builder.build());
    }
    
    /**
     * Shows notification when email is successfully sent
     */
    public void showEmailSentNotification(String recipient) {
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
        
        notificationManager.notify(NOTIFICATION_ID_ERROR, builder.build());
    }
    
    /**
     * Cancels all non-service notifications
     */
    public void cancelProcessingNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_SMS_RECEIVED);
        notificationManager.cancel(NOTIFICATION_ID_EMAIL_SENT);
    }
    
    /**
     * Cancels specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
} 