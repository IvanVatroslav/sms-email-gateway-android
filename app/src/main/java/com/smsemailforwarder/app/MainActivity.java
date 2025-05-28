package com.smsemailforwarder.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smsemailforwarder.app.utils.EmailTestHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;
import com.smsemailforwarder.app.utils.ServiceManager;

/**
 * Main Activity for SMS-to-Email Forwarder
 * Modern Material Design UI with comprehensive service control
 * Displays service status and provides controls for configuration and testing
 */
public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 124;

    // UI Components
    private TextView serviceStatusText;
    private TextView serviceDetailText;
    private TextView emailStatusText;
    private Button startStopButton;
    private Button testEmailButton;
    private Button testSmsButton;
    private FloatingActionButton settingsFab;
    private MaterialCardView serviceCard;
    private MaterialCardView emailCard;
    private SwipeRefreshLayout swipeRefresh;

    // Utilities
    private PreferencesManager preferencesManager;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(this);
        uiHandler = new Handler(Looper.getMainLooper());
        
        initializeViews();
        setupClickListeners();
        checkPermissions();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void initializeViews() {
        // Status displays
        serviceStatusText = findViewById(R.id.service_status_text);
        serviceDetailText = findViewById(R.id.service_detail_text);
        emailStatusText = findViewById(R.id.email_status_text);
        
        // Buttons
        startStopButton = findViewById(R.id.start_stop_button);
        testEmailButton = findViewById(R.id.test_email_button);
        testSmsButton = findViewById(R.id.test_sms_button);
        settingsFab = findViewById(R.id.settings_fab);
        
        // Cards and containers
        serviceCard = findViewById(R.id.service_card);
        emailCard = findViewById(R.id.email_card);
        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    private void setupClickListeners() {
        startStopButton.setOnClickListener(v -> toggleService());
        testEmailButton.setOnClickListener(v -> sendTestEmail());
        testSmsButton.setOnClickListener(v -> sendTestSms());
        settingsFab.setOnClickListener(v -> openSettings());
        
        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            updateUI();
            uiHandler.postDelayed(() -> swipeRefresh.setRefreshing(false), 1000);
        });
        
        // Long press for detailed status
        serviceCard.setOnLongClickListener(v -> {
            showDetailedStatus();
            return true;
        });
        
        emailCard.setOnLongClickListener(v -> {
            showEmailConfiguration();
            return true;
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
                showPermissionDialog();
            } else {
                updateUI();
            }
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("SMS permissions are required for the app to function properly. Please grant permissions in Settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUI() {
        updateServiceStatus();
        updateEmailStatus();
        updateButtons();
    }

    private void updateServiceStatus() {
        String statusText = ServiceManager.getServiceStatusText(this);
        boolean isRunning = ServiceManager.isSmsForwardingRunning();
        
        serviceStatusText.setText("Service: " + statusText);
        
        // Set status color
        int colorRes;
        switch (statusText) {
            case "Running":
                colorRes = R.color.status_running;
                serviceDetailText.setText("SMS forwarding is active and monitoring messages");
                break;
            case "Stopped":
                colorRes = R.color.status_stopped;
                serviceDetailText.setText("Service is configured but not running");
                break;
            case "Disabled":
                colorRes = R.color.status_disabled;
                serviceDetailText.setText("Service is disabled in settings");
                break;
            case "Not Configured":
                colorRes = R.color.status_error;
                serviceDetailText.setText("Email configuration required");
                break;
            default:
                colorRes = R.color.status_unknown;
                serviceDetailText.setText("Unknown status");
                break;
        }
        
        serviceStatusText.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private void updateEmailStatus() {
        if (preferencesManager.isEmailConfigured()) {
            String recipient = preferencesManager.getEmailRecipient();
            String server = preferencesManager.getEmailSmtpServer();
            
            emailStatusText.setText("Email: Configured");
            emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_running));
            
            // Show masked recipient
            String maskedRecipient = maskEmail(recipient);
            emailCard.setContentDescription("Email configured for " + maskedRecipient + " via " + server);
        } else {
            emailStatusText.setText("Email: Not Configured");
            emailStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_error));
            emailCard.setContentDescription("Email configuration required");
        }
    }

    private void updateButtons() {
        boolean isRunning = ServiceManager.isSmsForwardingRunning();
        boolean isConfigured = preferencesManager.isEmailConfigured();
        
        // Start/Stop button
        if (isRunning) {
            startStopButton.setText("Stop Service");
            startStopButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_stop));
        } else {
            startStopButton.setText("Start Service");
            startStopButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_start));
        }
        
        startStopButton.setEnabled(isConfigured);
        
        // Test buttons
        testEmailButton.setEnabled(isConfigured);
        testSmsButton.setEnabled(isConfigured);
    }

    private void toggleService() {
        boolean isRunning = ServiceManager.isSmsForwardingRunning();
        
        if (isRunning) {
            if (ServiceManager.stopSmsForwarding(this)) {
                Toast.makeText(this, "SMS Forwarder Service Stopped", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (ServiceManager.startSmsForwarding(this)) {
                Toast.makeText(this, "SMS Forwarder Service Started", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Update UI after a short delay to allow service state to change
        uiHandler.postDelayed(this::updateUI, 500);
    }

    private void sendTestEmail() {
        if (!EmailTestHelper.validateEmailConfiguration(this)) {
            Toast.makeText(this, "Please check email configuration", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        EmailTestHelper.sendTestEmail(this);
        Toast.makeText(this, "Sending test email...", Toast.LENGTH_SHORT).show();
    }

    private void sendTestSms() {
        if (!EmailTestHelper.validateEmailConfiguration(this)) {
            Toast.makeText(this, "Please check email configuration", Toast.LENGTH_LONG).show();
            openSettings();
            return;
        }

        EmailTestHelper.sendSampleSmsEmail(this);
        Toast.makeText(this, "Sending sample SMS email...", Toast.LENGTH_SHORT).show();
    }

    private void showDetailedStatus() {
        String detailedStatus = ServiceManager.getDetailedServiceStatus(this);
        
        new AlertDialog.Builder(this)
                .setTitle("Service Status Details")
                .setMessage(detailedStatus)
                .setPositiveButton("OK", null)
                .setNeutralButton("Refresh", (dialog, which) -> updateUI())
                .show();
    }

    private void showEmailConfiguration() {
        String configSummary = EmailTestHelper.getConfigurationSummary(this);
        
        new AlertDialog.Builder(this)
                .setTitle("Email Configuration")
                .setMessage(configSummary)
                .setPositiveButton("OK", null)
                .setNeutralButton("Settings", (dialog, which) -> openSettings())
                .show();
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[Not set]";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            
            if (username.length() <= 2) {
                return "*".repeat(username.length()) + domain;
            } else {
                return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) + domain;
            }
        }
        
        return email.length() > 4 ? email.substring(0, 2) + "*".repeat(email.length() - 4) + email.substring(email.length() - 2) : "****";
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
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_restart_service) {
            restartService();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About SMS-to-Email Forwarder")
                .setMessage("Version 1.0\n\nForwards SMS messages to email addresses.\n\nDesigned for Croatian users with support for A1, HT, and Tele2 carriers.\n\nSupports Croatian characters and UTF-8 encoding.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void restartService() {
        if (ServiceManager.restartSmsForwarding(this)) {
            Toast.makeText(this, "Service restarted", Toast.LENGTH_SHORT).show();
            uiHandler.postDelayed(this::updateUI, 1000);
        }
    }
} 