# LiveTranslate Android App

Real-time translation application with speech recognition and text-to-speech for Android.

## Features

- 🎤 Speech-to-Text recognition
- 🔄 Real-time translation (10+ languages)
- 🔊 Text-to-Speech output
- 📱 Native Android UI with Material Design
- 🚀 Automated CI/CD with GitHub Actions

## Supported Languages

- Russian 🇷🇺
- English 🇺🇸
- Spanish 🇪🇸
- French 🇫🇷
- German 🇩🇪
- Chinese 🇨🇳
- Japanese 🇯🇵
- Korean 🇰🇷
- Arabic 🇸🇦
- Italian 🇮🇹

## Download

Download the latest APK from [Releases](https://github.com/sdpiter/livetranslate-android/releases) or [Actions](https://github.com/sdpiter/livetranslate-android/actions).

## Build Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Local Build

```bash
# Clone repository
git clone https://github.com/sdpiter/livetranslate-android.git
cd livetranslate-android

# Build APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug
