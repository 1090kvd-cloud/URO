# Bubble Level Implementation Plan

**Goal:** Installable Android spirit level, English/Russian, with source ready for its own GitHub repository.
**Architecture:** Plain Java activity, custom Canvas view, pure Java measurement model. Android sensors feed model; preferences retain calibration and language. No network dependency at runtime.
**Tech Stack:** Java 17 compiler targeting Java 8, Android SDK 35, AGP 8.9.2.
**Spec:** docs/design.md

## Global Constraints
- Android 8+; portrait phone UI; English and Russian.
- Do not fake measurements when unavailable; no Internet permission.
- GitHub repository must be separate from existing projects.

## Review Focus
- No sensor or delayed first event: show unavailable/waiting, disable calibration/hold.
- Screen rotation mapping and upside-down gravity must preserve finite signed inclinations.
- Frozen readings must not be changed by new samples or calibration.
- Pause/resume must restart filtering and release audio/sensor resources.
- Language changes, process restart and compact screen layout must remain usable.

## Tasks
- [x] Model: write executable Java behavior tests, prove missing model fails, implement inclinations/filter/calibration/hold; run tests.
- [x] UI: implement Canvas vials, native accessible controls, localized settings and sensor lifecycle.
- [x] Build: SDK build script, Android Studio Gradle project, Actions CI; compile, sign, verify package and localization.
- [x] Review and deliver APK/source; publish to user-provided repository 1090kvd-cloud/URO.
