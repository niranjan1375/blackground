# Blackground - Minimal Always-On Display

A minimal Android app in Kotlin that mimics a simple always-on display in landscape mode.

## Features

- **Fullscreen Activity**: Pure black background for power efficiency
- **Landscape Orientation**: Locked to landscape mode
- **Time & Date Display**: Shows current time (HH:MM) and date (EEE, MMM d) in white text
- **Battery Efficient**: Updates only once per minute (not every second)
- **Screen Wake Lock**: Keeps screen awake while app is running
- **Reduced Brightness**: Automatically reduces screen brightness for power savings
- **Clean Exit**: Back button exits cleanly with no background services
- **Minimal APK**: Uses only core Android APIs, no external dependencies

## Build Requirements

- Android SDK 21+ (Android 5.0)
- Target SDK 33
- Kotlin support

## Installation

1. Open the project in Android Studio
2. Build and run on your Android device
3. The app will launch directly into the always-on display mode

## Usage

- The app displays the current time and date centered on a black background
- Press the back button to exit the app cleanly
- The screen brightness is automatically reduced while the app is running
- Time updates every minute to conserve battery

## File Structure

```
app/
├── src/main/
│   ├── java/com/example/blackground/
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml
│   │   └── values/
│   │       ├── strings.xml
│   │       └── styles.xml
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```