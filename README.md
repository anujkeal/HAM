# HAM — Hold A Minute

HAM is an Android app-virtualization host built with Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow and Room. It aims to create independent virtual instances of supported installed apps.

> Early development: this milestone adds the application shell, Room-backed clone records, installed-app picker and adapter for the selected BlackBox engine. It is not yet a verified general-purpose cloning product.

## Architecture
- app/: Kotlin, Compose UI and MVVM.
- data/: Room entities, DAO, database and repository.
- domain/: installed-app model.
- engine/: replaceable virtualization interface and BlackBox adapter.
- engine/blackbox/: pinned upstream engine Git submodule.

## Engine
Blacks-BlackBox (https://github.com/Black00Z/Blacks-BlackBox), Apache-2.0, pinned to commit 40282a7bf4500948cfd598fc67e6e63114b26dd9. The engine is a Java/C++ framework with Android framework hooks and proxy components. Its upstream build declares NDK 29.0.13846066 and Java 21 source compatibility.

## SDK choices
- minSdk 26 as requested.
- compileSdk 36.
- targetSdk 28 initially to match the engine's hidden-API compatibility assumptions. This is a compatibility compromise, not a security bypass or Play Store recommendation.
- ARM64 and ARMv7 native ABIs are inherited from the engine.

## Build
Install JDK 21, Android SDK Platform 36 and NDK 29.0.13846066. Clone with submodules using: git clone --recurse-submodules https://github.com/anujkeal/HAM.git. Then run cd HAM and ./gradlew assembleDebug. If already cloned without submodules, run git submodule update --init --recursive.

APK output: app/build/outputs/apk/debug/app-debug.apk.

## Implemented in this milestone
- [x] Kotlin + Compose Material 3 shell
- [x] Light and dark color schemes; violet and lime accents
- [x] Room clone records and StateFlow view model
- [x] Launchable installed-app picker, search and system-app toggle
- [x] Replaceable engine interface and BlackBox adapter using upstream APIs
- [x] Build an .apks bundle from the installed base APK and split APK files before virtual installation
- [x] Enable the engine daemon service for virtual app lifecycle support
- [x] Engine initialization in Application.attachBaseContext
- [x] Clone, launch, rename, clear data, force stop and delete wiring
- [x] Initial risk-app list

## Not yet implemented / not yet validated
- [ ] Per-clone icon retrieval and richer clone cards
- [ ] Confirmation dialogs for destructive actions
- [ ] Runtime permission forwarding and notification relay
- [ ] Foreground-service lifecycle
- [ ] Engine log export and dedicated crash-recovery screen
- [ ] Biometric/PIN lock
- [ ] Split APK installation validation on physical devices
- [ ] Isolated WebView profile fallback
- [ ] Real-device compatibility tests

## Compatibility matrix
No physical-device test results are claimed for HAM yet.

| Android | Device | App | HAM result |
|---|---|---|---|
| 16 / API 36 | ARM64 | Simple app | Not tested |
| 16 / API 36 | ARM64 | Instagram | Not tested |
| 16 / API 36 | ARM64 | WhatsApp | Not tested |
| 14 / API 34 | ARM64 | Simple app | Not tested |

The engine upstream documents Android 16 hardening and .apks import support, but integrated HAM behavior must be verified independently. Hardware-backed Play Integrity, banking apps, anti-cheat games, FCM relay, some direct camera HAL paths and OEM background restrictions may fail.

## Permissions
HAM retains package visibility, network, wake-lock, foreground-service, notification, camera, microphone, audio and legacy storage permissions. The manifest merger removes 237 unrelated upstream permission declarations, including vendor-specific and privileged permissions. Runtime permission forwarding is not complete; individual cloned apps may therefore still lack access to camera, microphone, media or notifications.

## Licensing
HAM application code is MIT (see LICENSE). The separately licensed BlackBox engine is Apache-2.0. Retain upstream notices and comply with its third-party dependency licenses.
