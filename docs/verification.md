# Verification — 2026-10-25

- Model and bubble mapping suite: 59 checks pass via `sh scripts/test.sh`.
- SDK build: successful with Android SDK 35, build-tools 35.0.0, Java 17.0.20.
- APK verifier: v2 and v3 signatures valid, one signer. Alignment check passed.
- Badging: com.kvd.bubblelevel, version 1.0.0 (1), min API 26, target API 35; English and Russian labels present; no uses-permission declarations.
- Separate code review identified three material issues, all fixed: bubble high-side direction, stale live readiness on invalid data, sample-frequency-dependent stability. Regression checks now cover each, including 5°/s motion at 10/50/100 Hz and held invalid samples.
- Rendering uses vector Canvas artwork; no source app graphics or video frames are included.
- Runtime/device/emulator testing was not available. Android Studio/Gradle build and hosted Actions still require their own run; the SDK build route is verified locally.
- Development signing only. CI regenerates its debug key; cross-key updates require uninstall. For production updates, configure a persistent private release key.

## Version 1.0.1

Application name is «Уровень» in both locales. New original adaptive launcher icon depicts an amber spirit level with a centered green vial; artwork is contained within the adaptive safe area. Version code 2; existing local development signing key retained for in-place updates. Build, badging, signature continuity and ZIP alignment checked for the new APK.

## Version 1.1.0

Four explicit palettes (classic green, amber, ice blue, light), persistent theme ID, live palette update without recreating the activity, localized theme menu and themed dialogs. Version code 3. SDK build and signature checks passed; the 59 existing measurement checks pass unchanged. Runtime appearance, theme picker interaction and persistence have not been tested on a physical Android device.

## Version 1.1.1

Audio root cause found in source: tone generator level 35/100 and 100 ms beep. Use MAX_VOLUME (100), a 400 ms dual-frequency DTMF 5 tone, immediate preview on sound enable, and hardware volume buttons targeting STREAM_MUSIC. Disabling sound stops an active tone; pause still releases resources. System volume is not raised automatically. Acoustic loudness and output routing remain unverified on the user device.

## Version 1.2.0

Sound control now has persistent auto/both/X/Y modes. Automatic posture selection uses uncalibrated inclination so saved zero offsets do not select the wrong vial. Circular display remains based on both axes; sound and status use the selected target. Stable/valid sensor, sound enabled and hold-off gates still apply; target changes rearm the one-shot signal. Settings and long-press Sound expose the selector in both languages. 59 measurement and 30 axis sound checks pass, including actual gravity vectors for flat, standing and either side, selected-axis independence, off-level rejection, tolerance and nonfinite input. Physical playback and UI interactions remain unverified on a device.
