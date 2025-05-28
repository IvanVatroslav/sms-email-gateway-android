# SMS-to-Email Android Forwarder

An Android application that automatically forwards incoming SMS messages to email addresses. Designed specifically for Croatian users to help elderly people communicate via SMS while receiving messages as emails.

## Project Status

**Phase 1: Project Setup** ✅ **COMPLETED**
- ✅ Android Studio project structure created
- ✅ Dependencies configured (JavaMail, WorkManager, Preferences)
- ✅ AndroidManifest.xml with all required permissions
- ✅ Basic Java class structure implemented
- ✅ Material Design 3 UI foundation
- ✅ PreferencesManager utility class

**Phase 2: SMS Reception** ✅ **COMPLETED**
- ✅ Complete SMS broadcast receiver implementation
- ✅ Multi-part SMS message handling
- ✅ Croatian carrier support (A1, HT, Tele2)
- ✅ Phone number normalization and formatting
- ✅ SMS content validation and filtering
- ✅ Message formatting utilities
- ✅ Notification system for SMS processing
- ✅ Error handling and logging

**Phase 3: Email Functionality** ✅ **COMPLETED**
- ✅ Complete EmailService implementation with JavaMail API
- ✅ SMTP configuration for Gmail, Outlook, Yahoo, and custom servers
- ✅ Email retry logic with exponential backoff
- ✅ UTF-8 encoding support for Croatian characters
- ✅ Email provider configuration presets
- ✅ Email validation and testing utilities
- ✅ Test email functionality
- ✅ Comprehensive error handling and notifications

**Next Phases:**
- Phase 4: Background Service (4-5 hours)
- Phase 5: User Interface (6-8 hours)
- Phase 6: Configuration & Preferences (3-4 hours)
- Phase 7: Testing & Debugging (8-10 hours)
- Phase 8: Optimization & Polish (4-6 hours)

## Features

### Completed
- ✅ Android project structure
- ✅ Permission management system
- ✅ Settings storage system
- ✅ Material Design 3 UI
- ✅ Service architecture foundation
- ✅ **SMS reception and parsing**
- ✅ **Croatian carrier compatibility**
- ✅ **Multi-part SMS handling**
- ✅ **Phone number normalization**
- ✅ **Message validation and filtering**
- ✅ **Notification system**
- ✅ **Complete email sending functionality**
- ✅ **SMTP server support (Gmail, Outlook, Yahoo, Custom)**
- ✅ **Email configuration presets**
- ✅ **Email testing and validation**
- ✅ **Croatian character encoding (UTF-8)**

### Planned
- 🔄 Background service for continuous operation
- 🔄 Simple configuration UI
- 🔄 Croatian language support
- 🔄 Auto-start on device boot
- 🔄 Battery optimization handling

## Technical Requirements

- **Minimum SDK:** Android 6.0 (API 23)
- **Target SDK:** Android 13+ (API 33+)
- **Language:** Java
- **IDE:** Android Studio

## Current Project Structure

```
app/
├── src/main/
│   ├── java/com/smsemailforwarder/app/
│   │   ├── MainActivity.java              ✅ Status display & controls
│   │   ├── SmsReceiver.java               ✅ SMS reception & parsing
│   │   ├── EmailService.java              ✅ Complete email sending
│   │   ├── ForwarderService.java          🔄 Background service (Phase 4)
│   │   ├── SettingsActivity.java          🔄 Configuration UI (Phase 5)
│   │   ├── BootReceiver.java              ✅ Auto-start on boot
│   │   └── utils/
│   │       ├── PreferencesManager.java    ✅ Settings management
│   │       ├── SmsFormatter.java          ✅ SMS formatting utilities
│   │       ├── NotificationHelper.java    ✅ Notification management
│   │       ├── EmailConfiguration.java    ✅ Email provider presets
│   │       └── EmailTestHelper.java       ✅ Email testing utilities
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml          ✅ Main UI layout
│   │   │   └── activity_settings.xml      🔄 Settings UI (Phase 5)
│   │   ├── values/
│   │   │   ├── strings.xml                ✅ String resources
│   │   │   ├── colors.xml                 ✅ Material Design colors
│   │   │   └── themes.xml                 ✅ Material Design theme
│   │   ├── menu/
│   │   │   └── main_menu.xml              ✅ App menu
│   │   ├── drawable/
│   │   │   └── ic_notification.xml        ✅ Notification icons
│   │   └── xml/
│   │       ├── backup_rules.xml           ✅ Data backup rules
│   │       └── data_extraction_rules.xml  ✅ Data safety rules
│   └── AndroidManifest.xml                ✅ Permissions & components
└── build.gradle                           ✅ Dependencies configured
```

## Dependencies

### Core Dependencies
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.11.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`

### Email Functionality
- `com.sun.mail:android-mail:1.6.7`
- `com.sun.mail:android-activation:1.6.7`

### Background Services
- `androidx.work:work-runtime:2.9.0`

### Preferences
- `androidx.preference:preference:1.2.1`

## Setup Instructions

### Prerequisites
1. **Android Studio** (latest version)
2. **Android SDK** with API 23+ and 34
3. **Croatian SIM card** for testing
4. **Email account** with app password (Gmail/Outlook)

### Build & Install
```bash
# Clone the repository
git clone <repository-url>
cd sms-email-gateway-android

# Open in Android Studio
# File -> Open -> Select the project directory

# Build the project
./gradlew build

# Install on device
./gradlew installDebug
```

### Configuration (Phase 5)
1. Install the app on an Android device
2. Grant SMS and notification permissions
3. Configure email settings in the Settings screen
4. Test email connectivity
5. Start the SMS forwarding service

## Croatian Network Compatibility

The app is designed to work with Croatian mobile carriers:
- **A1 Croatia** (prefix: +385 91)
- **Hrvatski Telekom (HT)** (prefixes: +385 98, +385 99)
- **Tele2 Croatia** (prefix: +385 95)

Features Croatian-specific functionality:
- ✅ Phone number normalization for all Croatian formats
- ✅ Carrier detection and identification
- ✅ UTF-8 encoding for Croatian characters (č, ć, ž, š, đ)
- ✅ Multi-part SMS handling for longer messages

## Security Features

- Email credentials stored securely using Android Keystore
- No SMS content logging by default
- TLS/SSL encryption for email transmission
- Optional local message history with encryption
- Input validation and sanitization
- Error handling with user notifications

## Development Progress

**Phase 3 Completion Summary:**
- Total files created: 21 (+3 new email utility classes)
- Email Functionality: ✅ Complete
- SMTP Support: ✅ Gmail, Outlook, Yahoo, Custom servers
- Email Testing: ✅ Complete with validation utilities
- Croatian encoding: ✅ UTF-8 support implemented
- Error handling: ✅ Comprehensive retry logic

**Current Capabilities:**
- ✅ Receive and parse SMS messages
- ✅ Handle multi-part SMS
- ✅ Normalize Croatian phone numbers
- ✅ Validate message content
- ✅ Show processing notifications
- ✅ **Send emails via SMTP with retry logic**
- ✅ **Support major email providers**
- ✅ **Test email configuration**
- ✅ **Format emails with Croatian characters**

**Ready for Phase 4:** Background service implementation

## Email Provider Support

The app supports the following email providers with pre-configured settings:

### Gmail
- **Server:** smtp.gmail.com
- **Port:** 587 (TLS) or 465 (SSL)
- **Requirements:** App password (not regular password)
- **Setup:** Enable 2FA → Generate app password

### Outlook/Hotmail
- **Server:** smtp-mail.outlook.com  
- **Port:** 587 (TLS)
- **Requirements:** Regular Microsoft account password
- **Setup:** May need to enable "Less secure app access"

### Yahoo Mail
- **Server:** smtp.mail.yahoo.com
- **Port:** 587 (TLS)
- **Requirements:** App password
- **Setup:** Account Security → Generate app password

### Custom SMTP
- **Configurable:** Any SMTP server
- **Ports:** 587 (TLS), 465 (SSL), 25 (insecure)
- **Authentication:** Username/password based

## Email Testing

The app includes comprehensive email testing utilities:

```java
// Test email configuration
EmailTestHelper.validateEmailConfiguration(context);

// Send test email
EmailTestHelper.sendTestEmail(context);

// Send sample SMS email
EmailTestHelper.sendSampleSmsEmail(context);

// Quick Gmail setup
EmailTestHelper.setupGmailQuick(context, "user@gmail.com", "apppassword", "recipient@email.com");
```

## License

This project is developed for personal/family use. See project plan for full details.

## Support

For Croatian users: App will include Croatian language support in Phase 5.
For developers: Follow the phase-by-phase implementation plan in the project documentation.