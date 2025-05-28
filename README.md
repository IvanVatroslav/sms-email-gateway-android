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

**Next Phases:**
- Phase 2: SMS Reception (4-5 hours)
- Phase 3: Email Functionality (5-6 hours)
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

### Planned
- 🔄 Automatic SMS reception and forwarding
- 🔄 Email configuration and sending (Gmail, Outlook, Custom SMTP)
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
│   │   ├── SmsReceiver.java               🔄 SMS reception (Phase 2)
│   │   ├── EmailService.java              🔄 Email sending (Phase 3)
│   │   ├── ForwarderService.java          🔄 Background service (Phase 4)
│   │   ├── SettingsActivity.java          🔄 Configuration UI (Phase 5)
│   │   ├── BootReceiver.java              ✅ Auto-start on boot
│   │   └── utils/
│   │       └── PreferencesManager.java    ✅ Settings management
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
- **A1 Croatia** 
- **Hrvatski Telekom (HT)**
- **Tele2 Croatia**

Supports Croatian character encoding (UTF-8) for proper display of Croatian letters (č, ć, ž, š, đ).

## Security Features

- Email credentials stored securely using Android Keystore
- No SMS content logging by default
- TLS/SSL encryption for email transmission
- Optional local message history with encryption

## Development Progress

**Phase 1 Completion Summary:**
- Total files created: 15
- Core architecture: ✅ Complete
- UI foundation: ✅ Complete  
- Permission system: ✅ Complete
- Settings management: ✅ Complete
- Build configuration: ✅ Complete

**Ready for Phase 2:** SMS Reception implementation

## License

This project is developed for personal/family use. See project plan for full details.

## Support

For Croatian users: App will include Croatian language support in Phase 5.
For developers: Follow the phase-by-phase implementation plan in the project documentation.