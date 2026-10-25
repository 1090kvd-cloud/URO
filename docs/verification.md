# Verification — 2026-10-25

- Model and bubble mapping suite: 59 checks pass via `sh scripts/test.sh`.
- SDK build: successful with Android SDK 35, build-tools 35.0.0, Java 17.0.20.
- APK verifier: v2 and v3 signatures valid, one signer. Alignment check passed.
- Badging: com.kvd.bubblelevel, version 1.0.0 (1), min API 26, target API 35; English and Russian labels present; no uses-permission declarations.
- Separate code review identified three material issues, all fixed: bubble high-side direction, stale live readiness on invalid data, sample-frequency-dependent stability. Regression checks now cover each, including 5°/s motion at 10/50/100 Hz and held invalid samples.
- Rendering uses vector Canvas artwork; no source app graphics or video frames are included.
- Runtime/device/emulator testing was not available. Android Studio/Gradle build and hosted Actions still require their own run; the SDK build route is verified locally.
- Development signing only. CI regenerates its debug key; cross-key updates require uninstall. For production updates, configure a persistent private release key.
