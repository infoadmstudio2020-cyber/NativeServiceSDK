# Setup Guide for NativeServiceSDK

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK with API 34

## Local Development

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run the `app` module on a device or emulator

## Publishing to JitPack

1. Push your code to GitHub
2. Create a release tag: `git tag v1.0.1 && git push --tags`
3. Visit [jitpack.io](https://jitpack.io) and look up your repo
4. JitPack builds automatically on first use

## Gradle Wrapper

Included Gradle wrapper uses Gradle 8.7.
Update via: `./gradlew wrapper --gradle-version=<new_version>`
