package com.smsemailforwarder.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.smsemailforwarder.app.utils.PreferencesManager;

/**
 * Main Activity for SMS-to-Email Forwarder
 * Displays service status and provides controls for starting/stopping the service
 */
public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 124;

    private TextView serviceStatusText;
    private Button startStopButton;
    private Button testButton;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(this);
        
        initializeViews();
        setupClickListeners();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void initializeViews() {
        serviceStatusText = findViewById(R.id.service_status_text);
        startStopButton = findViewById(R.id.start_stop_button);
        testButton = findViewById(R.id.test_button);
    }

    private void setupClickListeners() {
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleService();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTestEmail();
            }
        });
    }

    private void checkPermissions() {
        String[] requiredPermissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        // Check for Android 13+ notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            String[] permissionsWithNotification = {
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
            };
            requiredPermissions = permissionsWithNotification;
        }

        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "SMS permissions are required for the app to function", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateServiceStatus() {
        // TODO: Check if ForwarderService is running
        // For now, show basic status
        if (preferencesManager.isServiceEnabled()) {
            serviceStatusText.setText("Service: Running");
            serviceStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            startStopButton.setText("Stop Service");
        } else {
            serviceStatusText.setText("Service: Stopped");
            serviceStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            startStopButton.setText("Start Service");
        }
    }

    private void toggleService() {
        if (preferencesManager.isServiceEnabled()) {
            stopForwarderService();
        } else {
            startForwarderService();
        }
    }

    private void startForwarderService() {
        if (!preferencesManager.isEmailConfigured()) {
            Toast.makeText(this, "Please configure email settings first", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        Intent serviceIntent = new Intent(this, ForwarderService.class);
        startForegroundService(serviceIntent);
        preferencesManager.setServiceEnabled(true);
        updateServiceStatus();
        Toast.makeText(this, "SMS Forwarder Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopForwarderService() {
        Intent serviceIntent = new Intent(this, ForwarderService.class);
        stopService(serviceIntent);
        preferencesManager.setServiceEnabled(false);
        updateServiceStatus();
        Toast.makeText(this, "SMS Forwarder Service Stopped", Toast.LENGTH_SHORT).show();
    }

    private void sendTestEmail() {
        if (!preferencesManager.isEmailConfigured()) {
            Toast.makeText(this, "Please configure email settings first", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        Intent emailIntent = new Intent(this, EmailService.class);
        emailIntent.putExtra("test_mode", true);
        emailIntent.putExtra("sender", "Test");
        emailIntent.putExtra("message", "This is a test message from SMS-to-Email Forwarder");
        emailIntent.putExtra("timestamp", System.currentTimeMillis());
        startService(emailIntent);
        
        Toast.makeText(this, "Sending test email...", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 