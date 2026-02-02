# Repository Guidelines

## Project Structure & Module Organization
- `app/` contains the Android application module.
  - `app/src/main/java/com/example/huntopia/` Kotlin sources (e.g., `MainActivity.kt`).
  - `app/src/main/res/` Android resources (layouts, drawables, values, mipmaps).
  - `app/src/main/AndroidManifest.xml` app manifest.
  - `app/src/test/` local unit tests.
  - `app/src/androidTest/` instrumented device tests.
- Root Gradle config: `build.gradle.kts`, `settings.gradle.kts`, `gradle/`.

## Build, Test, and Development Commands
Run from repository root with the Gradle wrapper:
- `./gradlew assembleDebug` — build a debug APK.
- `./gradlew installDebug` — install debug APK on a connected device/emulator.
- `./gradlew test` — run local unit tests.
- `./gradlew connectedAndroidTest` — run instrumented tests on a device/emulator.
- `./gradlew lint` — run Android Lint checks.

## Coding Style & Naming Conventions
- Kotlin source uses 4-space indentation; keep Kotlin idiomatic and prefer `camelCase` for variables/functions and `PascalCase` for classes.
- Android resources follow standard naming: `snake_case` for XML files/IDs (e.g., `activity_main.xml`).
- Keep package name `com.example.huntopia` consistent across new classes.

## Testing Guidelines
- Local unit tests live in `app/src/test/` and use JUnit 4.
- Instrumented tests live in `app/src/androidTest/` and use AndroidX test runner/Espresso.
- Name test classes with `*Test` suffix (e.g., `MainActivityTest`).

## Commit & Pull Request Guidelines
- No Git history is available in this repository, so follow a clear convention (e.g., `type: short summary` like `feat: add map screen`).
- PRs should include a concise description, linked issue (if any), and screenshots for UI changes.

## Configuration Notes
- `local.properties` is machine-specific (SDK path); do not commit secrets or local paths.
