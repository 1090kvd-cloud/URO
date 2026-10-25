# Bubble Level / Уровень
Native Android application inspired by the supplied photo and video; original vector/Canvas artwork rather than copied app assets. Android 8+, portrait phone UI, dark textured surface, fluorescent green horizontal/vertical/circular vials. Java and platform APIs, no runtime dependencies, network permissions, ads or analytics.

Gravity sensor with accelerometer fallback. Screen-relative axes, time-based filtering, two inclinations from gravity; calibration saved locally; lock freezes a complete sample. Never show a valid measurement before receiving sensor data. English/Russian automatic selection and explicit override. Sound gated by tolerance, motion stability and lock state. Settings expose tolerance and calibration reset.

Build a signed development APK locally using SDK build-tools; provide Gradle Android Studio project and GitHub Actions build. Local tests cover rotations, angles, zeroing, gravity inversion, invalid input, lock, rate independence and stability. Device calibration and physical accuracy still require a real phone. User supplied the empty, writable GitHub repository 1090kvd-cloud/URO; publish there through the connector.
