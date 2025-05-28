package com.smsemailforwarder.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smsemailforwarder.app.utils.EmailConfiguration;
import com.smsemailforwarder.app.utils.EmailTestHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;
import com.smsemailforwarder.app.utils.ServiceManager;

/**
 * Settings Activity for configuring email and service options
 * Modern Material Design UI with comprehensive email configuration
 */
public class SettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "SettingsActivity";
    
    // UI Components
    private AutoCompleteTextView providerSpinner;
    private TextInputEditText senderEmailEdit;
    private TextInputEditText senderPasswordEdit;
    private TextInputEditText recipientEmailEdit;
    private TextInputEditText smtpServerEdit;
    private TextInputEditText smtpPortEdit;
    private SwitchMaterial sslSwitch;
    private SwitchMaterial autoStartSwitch;
    private SwitchMaterial serviceEnabledSwitch;
    
    private TextInputLayout senderEmailLayout;
    private TextInputLayout senderPasswordLayout;
    private TextInputLayout recipientEmailLayout;
    private TextInputLayout smtpServerLayout;
    private TextInputLayout smtpPortLayout;
    
    private Button saveButton;
    private Button testEmailButton;
    private Button testSmsButton;
    private Button resetButton;
    
    // Utilities
    private PreferencesManager preferencesManager;
    private Handler uiHandler;
    
    // Email providers
    private final String[] emailProviders = {
        "Gmail", "Outlook/Hotmail", "Yahoo Mail", "Custom SMTP"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        Log.d(TAG, "SettingsActivity created");
        
        preferencesManager = new PreferencesManager(this);
        uiHandler = new Handler(Looper.getMainLooper());
        
        setupActionBar();
        initializeViews();
        setupListeners();
        loadSettings();
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }
    
    private void initializeViews() {
        // Provider selection
        providerSpinner = findViewById(R.id.provider_spinner);
        
        // Email configuration
        senderEmailEdit = findViewById(R.id.sender_email_edit);
        senderPasswordEdit = findViewById(R.id.sender_password_edit);
        recipientEmailEdit = findViewById(R.id.recipient_email_edit);
        smtpServerEdit = findViewById(R.id.smtp_server_edit);
        smtpPortEdit = findViewById(R.id.smtp_port_edit);
        
        // Input layouts for validation
        senderEmailLayout = findViewById(R.id.sender_email_layout);
        senderPasswordLayout = findViewById(R.id.sender_password_layout);
        recipientEmailLayout = findViewById(R.id.recipient_email_layout);
        smtpServerLayout = findViewById(R.id.smtp_server_layout);
        smtpPortLayout = findViewById(R.id.smtp_port_layout);
        
        // Switches
        sslSwitch = findViewById(R.id.ssl_switch);
        autoStartSwitch = findViewById(R.id.auto_start_switch);
        serviceEnabledSwitch = findViewById(R.id.service_enabled_switch);
        
        // Buttons
        saveButton = findViewById(R.id.save_button);
        testEmailButton = findViewById(R.id.test_email_button);
        testSmsButton = findViewById(R.id.test_sms_button);
        resetButton = findViewById(R.id.reset_button);
        
        // Setup provider spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, emailProviders);
        providerSpinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // Provider selection listener
        providerSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProvider = emailProviders[position];
            applyProviderSettings(selectedProvider);
        });
        
        // Email validation listeners
        senderEmailEdit.addTextChangedListener(new EmailValidationWatcher(senderEmailLayout));
        recipientEmailEdit.addTextChangedListener(new EmailValidationWatcher(recipientEmailLayout));
        
        // SMTP server validation
        smtpServerEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validateSmtpServer();
            }
        });
        
        // Port validation
        smtpPortEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validatePort();
            }
        });
        
        // Button listeners
        saveButton.setOnClickListener(v -> saveSettings());
        testEmailButton.setOnClickListener(v -> testEmail());
        testSmsButton.setOnClickListener(v -> testSmsEmail());
        resetButton.setOnClickListener(v -> showResetDialog());
        
        // Service switch listener
        serviceEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !validateConfiguration()) {
                serviceEnabledSwitch.setChecked(false);
                Toast.makeText(this, "Please complete email configuration first", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void applyProviderSettings(String provider) {
        EmailConfiguration.EmailProvider config = null;
        
        switch (provider) {
            case "Gmail":
                config = EmailConfiguration.getGmailConfig();
                break;
            case "Outlook/Hotmail":
                config = EmailConfiguration.getOutlookConfig();
                break;
            case "Yahoo Mail":
                config = EmailConfiguration.getYahooConfig();
                break;
            case "Custom SMTP":
                // Clear fields for custom configuration
                smtpServerEdit.setText("");
                smtpPortEdit.setText("");
                sslSwitch.setChecked(true);
                return;
        }
        
        if (config != null) {
            smtpServerEdit.setText(config.smtpServer);
            smtpPortEdit.setText(String.valueOf(config.port));
            sslSwitch.setChecked(config.useSSL);
        }
    }
    
    private void loadSettings() {
        // Load email settings
        senderEmailEdit.setText(preferencesManager.getEmailUsername());
        senderPasswordEdit.setText(preferencesManager.getEmailPassword());
        recipientEmailEdit.setText(preferencesManager.getEmailRecipient());
        smtpServerEdit.setText(preferencesManager.getEmailSmtpServer());
        smtpPortEdit.setText(String.valueOf(preferencesManager.getEmailSmtpPort()));
        sslSwitch.setChecked(preferencesManager.getEmailUseSSL());
        
        // Load service settings
        autoStartSwitch.setChecked(preferencesManager.isAutoStart());
        serviceEnabledSwitch.setChecked(preferencesManager.isServiceEnabled());
        
        // Detect and set provider
        String username = preferencesManager.getEmailUsername();
        if (username != null && !username.isEmpty()) {
            String detectedProvider = EmailConfiguration.detectProvider(username);
            if (detectedProvider != null) {
                providerSpinner.setText(detectedProvider, false);
            }
        }
    }
    
    private boolean validateConfiguration() {
        boolean isValid = true;
        
        // Validate sender email
        String senderEmail = senderEmailEdit.getText().toString().trim();
        if (senderEmail.isEmpty() || !EmailConfiguration.isValidEmail(senderEmail)) {
            senderEmailLayout.setError("Please enter a valid sender email address");
            isValid = false;
        } else {
            senderEmailLayout.setError(null);
        }
        
        // Validate password
        String password = senderPasswordEdit.getText().toString().trim();
        if (password.isEmpty()) {
            senderPasswordLayout.setError("Please enter email password");
            isValid = false;
        } else {
            senderPasswordLayout.setError(null);
        }
        
        // Validate recipient email
        String recipientEmail = recipientEmailEdit.getText().toString().trim();
        if (recipientEmail.isEmpty() || !EmailConfiguration.isValidEmail(recipientEmail)) {
            recipientEmailLayout.setError("Please enter a valid recipient email address");
            isValid = false;
        } else {
            recipientEmailLayout.setError(null);
        }
        
        // Validate SMTP server
        String smtpServer = smtpServerEdit.getText().toString().trim();
        if (smtpServer.isEmpty()) {
            smtpServerLayout.setError("Please enter SMTP server");
            isValid = false;
        } else {
            smtpServerLayout.setError(null);
        }
        
        // Validate port
        String portStr = smtpPortEdit.getText().toString().trim();
        if (portStr.isEmpty()) {
            smtpPortLayout.setError("Please enter SMTP port");
            isValid = false;
        } else {
            try {
                int port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    smtpPortLayout.setError("Port must be between 1 and 65535");
                    isValid = false;
                } else {
                    smtpPortLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                smtpPortLayout.setError("Please enter a valid port number");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void validateSmtpServer() {
        String server = smtpServerEdit.getText().toString().trim();
        if (server.isEmpty()) {
            smtpServerLayout.setError("SMTP server is required");
        } else if (!server.contains(".")) {
            smtpServerLayout.setError("Please enter a valid server address");
        } else {
            smtpServerLayout.setError(null);
        }
    }
    
    private void validatePort() {
        String portStr = smtpPortEdit.getText().toString().trim();
        if (!portStr.isEmpty()) {
            try {
                int port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    smtpPortLayout.setError("Port must be between 1 and 65535");
                } else {
                    smtpPortLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                smtpPortLayout.setError("Please enter a valid number");
            }
        }
    }
    
    private void saveSettings() {
        if (!validateConfiguration()) {
            Toast.makeText(this, "Please fix validation errors", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            // Save email settings
            preferencesManager.setEmailUsername(senderEmailEdit.getText().toString().trim());
            preferencesManager.setEmailPassword(senderPasswordEdit.getText().toString().trim());
            preferencesManager.setEmailRecipient(recipientEmailEdit.getText().toString().trim());
            preferencesManager.setEmailSmtpServer(smtpServerEdit.getText().toString().trim());
            preferencesManager.setEmailSmtpPort(Integer.parseInt(smtpPortEdit.getText().toString().trim()));
            preferencesManager.setEmailUseSSL(sslSwitch.isChecked());
            
            // Save service settings
            preferencesManager.setAutoStart(autoStartSwitch.isChecked());
            preferencesManager.setServiceEnabled(serviceEnabledSwitch.isChecked());
            
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            
            // Update service if enabled
            if (serviceEnabledSwitch.isChecked()) {
                ServiceManager.startSmsForwarding(this);
            } else {
                ServiceManager.stopSmsForwarding(this);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving settings", e);
            Toast.makeText(this, "Error saving settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void testEmail() {
        if (!validateConfiguration()) {
            Toast.makeText(this, "Please complete configuration first", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Save current settings temporarily for testing
        saveSettings();
        
        EmailTestHelper.sendTestEmail(this);
        Toast.makeText(this, "Sending test email...", Toast.LENGTH_SHORT).show();
    }
    
    private void testSmsEmail() {
        if (!validateConfiguration()) {
            Toast.makeText(this, "Please complete configuration first", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Save current settings temporarily for testing
        saveSettings();
        
        EmailTestHelper.sendSampleSmsEmail(this);
        Toast.makeText(this, "Sending sample SMS email...", Toast.LENGTH_SHORT).show();
    }
    
    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Settings")
                .setMessage("Are you sure you want to reset all settings to default values? This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> resetSettings())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void resetSettings() {
        // Clear all fields
        senderEmailEdit.setText("");
        senderPasswordEdit.setText("");
        recipientEmailEdit.setText("");
        smtpServerEdit.setText("");
        smtpPortEdit.setText("587");
        sslSwitch.setChecked(true);
        autoStartSwitch.setChecked(false);
        serviceEnabledSwitch.setChecked(false);
        providerSpinner.setText("", false);
        
        // Clear all errors
        senderEmailLayout.setError(null);
        senderPasswordLayout.setError(null);
        recipientEmailLayout.setError(null);
        smtpServerLayout.setError(null);
        smtpPortLayout.setError(null);
        
        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * TextWatcher for email validation
     */
    private class EmailValidationWatcher implements TextWatcher {
        private final TextInputLayout layout;
        
        public EmailValidationWatcher(TextInputLayout layout) {
            this.layout = layout;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            String email = s.toString().trim();
            if (!email.isEmpty() && !EmailConfiguration.isValidEmail(email)) {
                layout.setError("Please enter a valid email address");
            } else {
                layout.setError(null);
            }
        }
    }
} 