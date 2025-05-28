package com.smsemailforwarder.app;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smsemailforwarder.app.utils.ConfigurationManager;
import com.smsemailforwarder.app.utils.NotificationHelper;
import com.smsemailforwarder.app.utils.PreferencesManager;
import com.smsemailforwarder.app.utils.SmsFilter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Advanced Settings Activity for enhanced configuration options
 * Handles SMS filtering, notification settings, backup/restore, and advanced preferences
 */
public class AdvancedSettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "AdvancedSettingsActivity";
    
    // SMS Filtering UI Components
    private SwitchMaterial filterEnabledSwitch;
    private AutoCompleteTextView filterModeSpinner;
    private SwitchMaterial spamFilterSwitch;
    private TextInputEditText minLengthEdit;
    private TextInputEditText maxLengthEdit;
    private ChipGroup blockedNumbersChipGroup;
    private ChipGroup allowedNumbersChipGroup;
    private ChipGroup keywordsChipGroup;
    private EditText addNumberEdit;
    private EditText addKeywordEdit;
    
    // Notification Settings UI Components
    private SwitchMaterial notificationEnabledSwitch;
    private SwitchMaterial notificationSoundSwitch;
    private SwitchMaterial notificationVibrateSwitch;
    private SwitchMaterial notificationLedSwitch;
    private SwitchMaterial showEmailStatusSwitch;
    private SwitchMaterial showSmsPreviewSwitch;
    private SeekBar notificationPrioritySeekBar;
    private TextView priorityLabel;
    
    // Message Formatting UI Components
    private SwitchMaterial includeCarrierSwitch;
    private SwitchMaterial includeMessageCountSwitch;
    private TextInputEditText dateFormatEdit;
    private TextInputEditText timeFormatEdit;
    private TextInputEditText subjectFormatEdit;
    
    // Advanced Settings UI Components
    private TextInputEditText retryCountEdit;
    private TextInputEditText retryDelayEdit;
    private TextInputEditText connectionTimeoutEdit;
    private SwitchMaterial debugModeSwitch;
    
    // Action Buttons
    private Button testFilterButton;
    private Button backupButton;
    private Button restoreButton;
    private Button shareConfigButton;
    private Button resetButton;
    private Button diagnosticsButton;
    
    // Utilities
    private PreferencesManager preferencesManager;
    private ConfigurationManager configurationManager;
    private NotificationHelper notificationHelper;
    private Handler uiHandler;
    
    // Filter modes
    private final String[] filterModes = {"None", "Blacklist", "Whitelist"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);
        
        Log.d(TAG, "AdvancedSettingsActivity created");
        
        preferencesManager = new PreferencesManager(this);
        configurationManager = new ConfigurationManager(this);
        notificationHelper = new NotificationHelper(this);
        uiHandler = new Handler(Looper.getMainLooper());
        
        setupActionBar();
        initializeViews();
        setupListeners();
        loadSettings();
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Advanced Settings");
        }
    }
    
    private void initializeViews() {
        // SMS Filtering
        filterEnabledSwitch = findViewById(R.id.filter_enabled_switch);
        filterModeSpinner = findViewById(R.id.filter_mode_spinner);
        spamFilterSwitch = findViewById(R.id.spam_filter_switch);
        minLengthEdit = findViewById(R.id.min_length_edit);
        maxLengthEdit = findViewById(R.id.max_length_edit);
        blockedNumbersChipGroup = findViewById(R.id.blocked_numbers_chip_group);
        allowedNumbersChipGroup = findViewById(R.id.allowed_numbers_chip_group);
        keywordsChipGroup = findViewById(R.id.keywords_chip_group);
        addNumberEdit = findViewById(R.id.add_number_edit);
        addKeywordEdit = findViewById(R.id.add_keyword_edit);
        
        // Notification Settings
        notificationEnabledSwitch = findViewById(R.id.notification_enabled_switch);
        notificationSoundSwitch = findViewById(R.id.notification_sound_switch);
        notificationVibrateSwitch = findViewById(R.id.notification_vibrate_switch);
        notificationLedSwitch = findViewById(R.id.notification_led_switch);
        showEmailStatusSwitch = findViewById(R.id.show_email_status_switch);
        showSmsPreviewSwitch = findViewById(R.id.show_sms_preview_switch);
        notificationPrioritySeekBar = findViewById(R.id.notification_priority_seekbar);
        priorityLabel = findViewById(R.id.priority_label);
        
        // Message Formatting
        includeCarrierSwitch = findViewById(R.id.include_carrier_switch);
        includeMessageCountSwitch = findViewById(R.id.include_message_count_switch);
        dateFormatEdit = findViewById(R.id.date_format_edit);
        timeFormatEdit = findViewById(R.id.time_format_edit);
        subjectFormatEdit = findViewById(R.id.subject_format_edit);
        
        // Advanced Settings
        retryCountEdit = findViewById(R.id.retry_count_edit);
        retryDelayEdit = findViewById(R.id.retry_delay_edit);
        connectionTimeoutEdit = findViewById(R.id.connection_timeout_edit);
        debugModeSwitch = findViewById(R.id.debug_mode_switch);
        
        // Action Buttons
        testFilterButton = findViewById(R.id.test_filter_button);
        backupButton = findViewById(R.id.backup_button);
        restoreButton = findViewById(R.id.restore_button);
        shareConfigButton = findViewById(R.id.share_config_button);
        resetButton = findViewById(R.id.reset_button);
        diagnosticsButton = findViewById(R.id.diagnostics_button);
        
        // Setup filter mode spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, filterModes);
        filterModeSpinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // Filter mode selection
        filterModeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMode = filterModes[position];
            PreferencesManager.FilterMode mode = PreferencesManager.FilterMode.valueOf(selectedMode.toUpperCase());
            preferencesManager.setFilterMode(mode);
            updateFilterModeVisibility(mode);
        });
        
        // Add number button
        findViewById(R.id.add_number_button).setOnClickListener(v -> addNumber());
        
        // Add keyword button
        findViewById(R.id.add_keyword_button).setOnClickListener(v -> addKeyword());
        
        // Notification priority seekbar
        notificationPrioritySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePriorityLabel(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferencesManager.setNotificationPriority(seekBar.getProgress() - 2); // -2 to 2 range
                notificationHelper.updateNotificationChannels();
            }
        });
        
        // Action buttons
        testFilterButton.setOnClickListener(v -> showFilterTestDialog());
        backupButton.setOnClickListener(v -> createBackup());
        restoreButton.setOnClickListener(v -> showRestoreDialog());
        shareConfigButton.setOnClickListener(v -> shareConfiguration());
        resetButton.setOnClickListener(v -> showResetDialog());
        diagnosticsButton.setOnClickListener(v -> showDiagnostics());
        
        // Auto-save switches
        setupAutoSaveSwitches();
    }
    
    private void setupAutoSaveSwitches() {
        filterEnabledSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setFilterEnabled(checked));
        spamFilterSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setFilterSpam(checked));
        notificationEnabledSwitch.setOnCheckedChangeListener((v, checked) -> {
            preferencesManager.setNotificationEnabled(checked);
            notificationHelper.updateNotificationChannels();
        });
        notificationSoundSwitch.setOnCheckedChangeListener((v, checked) -> {
            preferencesManager.setNotificationSound(checked);
            notificationHelper.updateNotificationChannels();
        });
        notificationVibrateSwitch.setOnCheckedChangeListener((v, checked) -> {
            preferencesManager.setNotificationVibrate(checked);
            notificationHelper.updateNotificationChannels();
        });
        notificationLedSwitch.setOnCheckedChangeListener((v, checked) -> {
            preferencesManager.setNotificationLed(checked);
            notificationHelper.updateNotificationChannels();
        });
        showEmailStatusSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setShowEmailStatus(checked));
        showSmsPreviewSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setShowSmsPreview(checked));
        includeCarrierSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setIncludeCarrierInfo(checked));
        includeMessageCountSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setIncludeMessageCount(checked));
        debugModeSwitch.setOnCheckedChangeListener((v, checked) -> preferencesManager.setDebugMode(checked));
    }
    
    private void loadSettings() {
        // SMS Filtering
        filterEnabledSwitch.setChecked(preferencesManager.isFilterEnabled());
        filterModeSpinner.setText(preferencesManager.getFilterMode().name().toLowerCase(), false);
        spamFilterSwitch.setChecked(preferencesManager.isFilterSpam());
        minLengthEdit.setText(String.valueOf(preferencesManager.getMinMessageLength()));
        maxLengthEdit.setText(String.valueOf(preferencesManager.getMaxMessageLength()));
        
        // Load chip groups
        loadNumberChips();
        loadKeywordChips();
        
        // Notification Settings
        notificationEnabledSwitch.setChecked(preferencesManager.isNotificationEnabled());
        notificationSoundSwitch.setChecked(preferencesManager.isNotificationSound());
        notificationVibrateSwitch.setChecked(preferencesManager.isNotificationVibrate());
        notificationLedSwitch.setChecked(preferencesManager.isNotificationLed());
        showEmailStatusSwitch.setChecked(preferencesManager.isShowEmailStatus());
        showSmsPreviewSwitch.setChecked(preferencesManager.isShowSmsPreview());
        
        int priority = preferencesManager.getNotificationPriority() + 2; // Convert -2 to 2 range to 0 to 4
        notificationPrioritySeekBar.setProgress(priority);
        updatePriorityLabel(priority);
        
        // Message Formatting
        includeCarrierSwitch.setChecked(preferencesManager.isIncludeCarrierInfo());
        includeMessageCountSwitch.setChecked(preferencesManager.isIncludeMessageCount());
        dateFormatEdit.setText(preferencesManager.getDateFormat());
        timeFormatEdit.setText(preferencesManager.getTimeFormat());
        subjectFormatEdit.setText(preferencesManager.getEmailSubjectFormat());
        
        // Advanced Settings
        retryCountEdit.setText(String.valueOf(preferencesManager.getEmailRetryCount()));
        retryDelayEdit.setText(String.valueOf(preferencesManager.getEmailRetryDelay()));
        connectionTimeoutEdit.setText(String.valueOf(preferencesManager.getConnectionTimeout()));
        debugModeSwitch.setChecked(preferencesManager.isDebugMode());
        
        updateFilterModeVisibility(preferencesManager.getFilterMode());
    }
    
    private void loadNumberChips() {
        blockedNumbersChipGroup.removeAllViews();
        allowedNumbersChipGroup.removeAllViews();
        
        for (String number : preferencesManager.getBlockedNumbers()) {
            addChipToGroup(blockedNumbersChipGroup, number, true);
        }
        
        for (String number : preferencesManager.getAllowedNumbers()) {
            addChipToGroup(allowedNumbersChipGroup, number, true);
        }
    }
    
    private void loadKeywordChips() {
        keywordsChipGroup.removeAllViews();
        
        for (String keyword : preferencesManager.getFilterKeywords()) {
            addChipToGroup(keywordsChipGroup, keyword, false);
        }
    }
    
    private void addChipToGroup(ChipGroup chipGroup, String text, boolean isNumber) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            updateChipGroupPreferences(chipGroup, isNumber);
        });
        chipGroup.addView(chip);
    }
    
    private void updateChipGroupPreferences(ChipGroup chipGroup, boolean isNumber) {
        Set<String> items = new HashSet<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            items.add(chip.getText().toString());
        }
        
        if (isNumber) {
            if (chipGroup == blockedNumbersChipGroup) {
                preferencesManager.setBlockedNumbers(items);
            } else if (chipGroup == allowedNumbersChipGroup) {
                preferencesManager.setAllowedNumbers(items);
            }
        } else {
            preferencesManager.setFilterKeywords(items);
        }
    }
    
    private void addNumber() {
        String number = addNumberEdit.getText().toString().trim();
        if (!number.isEmpty()) {
            PreferencesManager.FilterMode mode = preferencesManager.getFilterMode();
            if (mode == PreferencesManager.FilterMode.BLACKLIST) {
                addChipToGroup(blockedNumbersChipGroup, number, true);
                updateChipGroupPreferences(blockedNumbersChipGroup, true);
            } else if (mode == PreferencesManager.FilterMode.WHITELIST) {
                addChipToGroup(allowedNumbersChipGroup, number, true);
                updateChipGroupPreferences(allowedNumbersChipGroup, true);
            }
            addNumberEdit.setText("");
        }
    }
    
    private void addKeyword() {
        String keyword = addKeywordEdit.getText().toString().trim();
        if (!keyword.isEmpty()) {
            addChipToGroup(keywordsChipGroup, keyword, false);
            updateChipGroupPreferences(keywordsChipGroup, false);
            addKeywordEdit.setText("");
        }
    }
    
    private void updateFilterModeVisibility(PreferencesManager.FilterMode mode) {
        findViewById(R.id.blocked_numbers_section).setVisibility(
            mode == PreferencesManager.FilterMode.BLACKLIST ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.allowed_numbers_section).setVisibility(
            mode == PreferencesManager.FilterMode.WHITELIST ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    
    private void updatePriorityLabel(int progress) {
        String[] priorities = {"Minimum", "Low", "Default", "High", "Maximum"};
        priorityLabel.setText("Priority: " + priorities[progress]);
    }
    
    private void showFilterTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Test SMS Filter");
        
        // Create custom dialog layout
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_test, null);
        EditText senderEdit = dialogView.findViewById(R.id.test_sender_edit);
        EditText messageEdit = dialogView.findViewById(R.id.test_message_edit);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Test", (dialog, which) -> {
            String sender = senderEdit.getText().toString().trim();
            String message = messageEdit.getText().toString().trim();
            
            if (!sender.isEmpty() && !message.isEmpty()) {
                testFilter(sender, message);
            } else {
                Toast.makeText(this, "Please enter both sender and message", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void testFilter(String sender, String message) {
        SmsFilter smsFilter = new SmsFilter(this);
        SmsFilter.FilterResult result = smsFilter.testMessage(sender, message);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Test Result");
        builder.setMessage(result.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void createBackup() {
        new Thread(() -> {
            File backupFile = configurationManager.createBackup();
            
            uiHandler.post(() -> {
                if (backupFile != null) {
                    Toast.makeText(this, "Backup created: " + backupFile.getName(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to create backup", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void showRestoreDialog() {
        File[] backups = configurationManager.getAvailableBackups();
        
        if (backups.length == 0) {
            Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] backupNames = new String[backups.length];
        for (int i = 0; i < backups.length; i++) {
            backupNames[i] = backups[i].getName();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Backup to Restore");
        builder.setItems(backupNames, (dialog, which) -> {
            restoreFromBackup(backups[which]);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void restoreFromBackup(File backupFile) {
        new Thread(() -> {
            boolean success = configurationManager.restoreFromBackup(backupFile);
            
            uiHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "Configuration restored successfully", Toast.LENGTH_SHORT).show();
                    loadSettings(); // Reload UI
                } else {
                    Toast.makeText(this, "Failed to restore configuration", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void shareConfiguration() {
        Intent shareIntent = configurationManager.shareConfiguration();
        if (shareIntent != null) {
            startActivity(shareIntent);
        } else {
            Toast.makeText(this, "Failed to create configuration file", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Configuration");
        builder.setMessage("This will reset all settings to defaults. A backup will be created automatically. Continue?");
        builder.setPositiveButton("Reset", (dialog, which) -> {
            boolean success = configurationManager.resetToDefaults();
            if (success) {
                Toast.makeText(this, "Configuration reset to defaults", Toast.LENGTH_SHORT).show();
                loadSettings(); // Reload UI
            } else {
                Toast.makeText(this, "Failed to reset configuration", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showDiagnostics() {
        String diagnostics = configurationManager.getConfigurationSummary() + "\n\n" +
                           new SmsFilter(this).getFilteringStats() + "\n\n" +
                           notificationHelper.getNotificationSettingsSummary();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("System Diagnostics");
        builder.setMessage(diagnostics);
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Share", (dialog, which) -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, diagnostics);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SMS Email Forwarder Diagnostics");
            startActivity(Intent.createChooser(shareIntent, "Share Diagnostics"));
        });
        builder.show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveTextSettings();
    }
    
    private void saveTextSettings() {
        // Save text field values
        try {
            preferencesManager.setMinMessageLength(Integer.parseInt(minLengthEdit.getText().toString()));
            preferencesManager.setMaxMessageLength(Integer.parseInt(maxLengthEdit.getText().toString()));
            preferencesManager.setEmailRetryCount(Integer.parseInt(retryCountEdit.getText().toString()));
            preferencesManager.setEmailRetryDelay(Integer.parseInt(retryDelayEdit.getText().toString()));
            preferencesManager.setConnectionTimeout(Integer.parseInt(connectionTimeoutEdit.getText().toString()));
            
            preferencesManager.setDateFormat(dateFormatEdit.getText().toString());
            preferencesManager.setTimeFormat(timeFormatEdit.getText().toString());
            preferencesManager.setEmailSubjectFormat(subjectFormatEdit.getText().toString());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid number format in settings", e);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 