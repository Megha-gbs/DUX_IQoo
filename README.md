# WAYCHECK

> **Maps know where you are. WAYCHECK knows whether you are actually there.**

Built for the **iQOO Hackathon**.

---

## ⚡ The Problem: The Last 100–500m Physical Mismatch

In Indian urban navigation—hospitals, tech parks, metro stations, university campuses, and event arenas—traditional GPS maps navigate you to a single pin. But physical reality has multiple disjoint access points:
* High-speed vehicle entry ramps vs. pedestrian turnstiles.
* Locked staff/service gates vs. public visitor gates.
* Multi-gate complexes where choosing the wrong entrance costs a 1 km detour or walking across busy highway traffic.

GPS cannot tell the difference between facing a pedestrian turnstile and walking straight into a parking boom barrier. **WAYCHECK verifies the physical path in real-time.**

---

## 🛡️ 0 Network Permissions: 100% On-Device Edge AI

WAYCHECK operates in **complete Airplane Mode**.
Inspect our `AndroidManifest.xml`—you will find **ZERO** network permissions:
```xml
<!-- 0 Network Permissions: Complete Privacy & Zero Cloud Latency -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
* **No Cloud API keys.**
* **No LLM hallucinations.**
* **No telemetry tracking.**
* Works underground, inside airport terminal approaches, or in dead zones with 0 cell reception.

---

## 🔬 Sensor Fusion & Architecture (`RealityEngine`)

WAYCHECK fuses 4 concurrent hardware sensors on-device:

```
                  ┌──────────────────────┐
                  │ CameraX Video Stream │
                  └──────────┬───────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │ MediaPipe Tasks Vision      │
              │ (EfficientNet-Lite0 TFLite) │
              └──────────────┬──────────────┘
                             │
 [Hardware IMU / Gyro] ──────┼────── [LocationManager (GPS)]
 (TYPE_ROTATION_VECTOR)      │       (Latitude/Longitude/Accuracy)
                             ▼
               ┌───────────────────────────┐
               │    com.waycheck.engine    │
               │       RealityEngine       │
               └─────────────┬─────────────┘
                             │
                             ▼
             ┌───────────────────────────────┐
             │       Status HUD / TTS        │
             │ CONFIRMED | UNCERTAIN | MISMATCH│
             └───────────────────────────────┘
```

### The Sensor Fusion Heuristic:
```kotlin
val headingDiff = calculateHeadingDiff(currentHeading, targetHeading)
val isHeadingMismatch = headingDiff > 90°
val detectedWrongEntity = visionLabels.any { it in WRONG_SET } // {car, vehicle, parking, barrier, garage}

status = when {
    detectedWrongEntity || isHeadingMismatch -> RealityStatus.MISMATCH
    !gpsAccuracyOk && visionLabels.isEmpty()  -> RealityStatus.UNCERTAIN
    else                                     -> RealityStatus.CONFIRMED
}
```

* **CONFIRMED (Green)**: Heading aligned to designated pedestrian corridor & safe pedestrian path verified.
* **MISMATCH (Red)**: Vehicle gate, parking lot, or opposing heading (>90° off-axis) detected. Immediate voice alert: *"Pedestrian Gate 2 is behind you."*
* **UNCERTAIN (Amber)**: Low confidence sensory lock or obstruction.

---

## 📱 90-Second Demo Walkthrough

1. **Enable Airplane Mode** on your iQOO/Android device (airplane icon visible in status bar).
2. Launch **WAYCHECK**. Grant Camera & GPS permissions.
3. Aim phone down the designated pedestrian pathway: Status reads **CONFIRMED** (Green).
4. Walk towards the vehicle gate or parking entrance (or tap **"Demo: Car Gate"** for indoor booth presentations):
   * Status instantly flips to **MISMATCH** (Red pulsing HUD).
   * Offline TTS immediately speaks: *"Pedestrian Gate 2 is behind you."*
   * Vision telemetry highlights identified vehicle/barrier classes with confidence scores.
5. Turn around toward the pedestrian path: Status reverts to **CONFIRMED**.

---

## 📂 Project Structure

```
WayCheck/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── classifier.tflite     # MediaPipe Vision Model
│   │   │   └── classifier.task
│   │   ├── java/com/waycheck/
│   │   │   ├── MainActivity.kt       # CameraX PreviewView + HUD Coordinator
│   │   │   ├── engine/
│   │   │   │   ├── RealityEngine.kt  # Sensor Fusion & Confidence Engine
│   │   │   │   └── Status.kt         # State & Config definitions
│   │   │   ├── sensors/
│   │   │   │   ├── CameraAnalyzer.kt # MediaPipe ImageClassifier
│   │   │   │   ├── GpsStore.kt       # Hardware LocationManager
│   │   │   │   └── ImuStore.kt       # Rotation Vector & Orientation
│   │   │   ├── tts/
│   │   │   │   └── Speaker.kt        # Offline Android TextToSpeech
│   │   │   └── ui/
│   │   │       └── Hud.kt            # Jetpack Compose Aviation-Grade HUD
│   │   └── AndroidManifest.xml       # 0 Network Permissions
│   └── build.gradle.kts
└── WAYCHECK_MVP_BUILD_GUIDE.md
```

---

## 🛠️ Build & Install

### Requirements
* Android 8.0 (API 26) or higher. Target SDK: Android 14+ (API 34/36).
* Java 17+ & Android SDK installed.

### Build APK
```bash
./gradlew assembleDebug
```
Output APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install to Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
