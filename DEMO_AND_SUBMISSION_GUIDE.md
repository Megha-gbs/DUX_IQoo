# WAYCHECK — Demo & Submission Guide (iQOO Hackathon)

This guide provides the exact 90-second video demo script, device installation steps, and judging pitch points to maximize your score across all evaluation criteria.

---

## 🏆 Scoring Alignment Matrix

| Evaluation Dimension | Weight | How WAYCHECK Hits 100% |
| :--- | :---: | :--- |
| **End Product Quality** | **30%** | Full native Jetpack Compose app with real-time CameraX preview, aviation-grade HUD, and zero-stutter offline inference. |
| **Novelty / Real-World Impact** | **20%** | Tackles the severe Indian "Last 100–500m Physical Mismatch" problem (complex campus gate selection, pedestrian vs vehicle ramps). |
| **Phone Hardware Use (HackTracker)** | **15%** | Camera + GPS LocationManager + Rotation Vector IMU + Android TTS + On-device MediaPipe Edge AI every active frame. |
| **Technical Depth** | **15%** | Mathematical sensor fusion (`RealityEngine`), confidence scoring equation, not an API wrapper. |
| **Office Kit Integration** | **10%** | Live screen mirroring, ADB logcat, and push-testing via iQOO Office Kit on Windows laptop. |
| **Live Pitch & Video** | **10%** | 90s crisp, punchy demo with Airplane Mode visible in status bar from second 1 to 90. |

---

## 📲 How to Install & Run on Your Phone

The compiled APK is ready at:
`c:\Users\gundr\OneDrive\Desktop\WayCheck\app\build\outputs\apk\debug\app-debug.apk`

### Option 1: Via USB / iQOO Office Kit (Recommended for HackTracker points)
1. Connect your iQOO/Android phone via USB to your laptop.
2. Enable **USB Debugging** in Developer Options.
3. Open **iQOO Office Kit** on your laptop and mirror your phone screen.
4. Run:
   ```powershell
   & "C:\Users\gundr\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
   ```

### Option 2: Direct Transfer
Send `app-debug.apk` to your phone via WhatsApp/Drive/Nearby Share, tap to install, and open **WAYCHECK**.

---

## 🎬 90-Second Video Demo Script (Airplane Mode)

> **Golden Rule:** Ensure the ✈️ **Airplane Mode** icon is clearly visible in your phone's top status bar throughout the entire recording!

### Scene 1: The Setup (0:00 – 0:15)
* **Visual:** Close-up of phone screen pulling down quick settings showing **Airplane Mode: ON** (Wi-Fi OFF, Mobile Data OFF).
* **Voiceover:** *"Maps know where you are. WAYCHECK knows whether you are actually there. In Indian complexes, GPS leads you to a single pin—often pointing straight into a dangerous vehicle ramp instead of the pedestrian turnstile."*

### Scene 2: Pedestrian Path Confirmed (0:15 – 0:35)
* **Visual:** Open WAYCHECK. Camera fills screen. Phone pointed toward the pedestrian walkway.
* **HUD:** Glowing Green **CONFIRMED** badge. Telemetry shows Heading aligned (`ΔHeading: 8°`), MediaPipe detects walkway/stairs.
* **Voiceover:** *"Here on the verified pedestrian path, our on-device RealityEngine fuses hardware IMU rotation vectors, offline GPS, and MediaPipe vision in real time. Confidence is 94%."*

### Scene 3: The Wrong Turn — Vehicle Gate Mismatch (0:35 – 0:60)
* **Visual:** Walk towards a parking entrance / car barrier, or turn 90° away from the gate.
* **HUD:** Instantly flashes Red **MISMATCH**.
* **Audio (Phone TTS):** 🔊 *"Pedestrian Gate 2 is behind you."*
* **HUD Telemetry:** Red warning `⚠️ > 90° OFF` + Vision identifies `parking lot / vehicles (92%)`.
* **Voiceover:** *"The moment we turn toward the vehicle gate, WAYCHECK detects the physical anomaly. No network delay, zero cloud reliance. The offline voice immediately redirects us."*

### Scene 4: Return & Recovery (0:60 – 0:75)
* **Visual:** Turn back around toward the pedestrian path.
* **HUD:** Flips back to Green **CONFIRMED**. Phone announces: 🔊 *"Pedestrian Gate 2 straight ahead."*
* **Voiceover:** *"Turn around, and reality lock is re-established instantly."*

### Scene 5: Architectural Wrap-up (0:75 – 0:90)
* **Visual:** Flash the architecture diagram from `README.md` or the `AndroidManifest.xml` showing zero network permissions.
* **Voiceover:** *"Zero network permissions in our manifest. 100% on-device edge intelligence. Built for iQOO. WAYCHECK: Physical verification for the last 500 meters."*

---

## 💡 Quick Tips for Demo & Live Q&A

1. **Indoor / Stage Presentation:**
   If presenting indoors without cars or gates nearby, use the on-screen **"Demo: Car Gate"** and **"Demo: Ped Gate"** interactive toggle buttons. It demonstrates the full state machine and audio response immediately to judges without risk.
2. **Calibration Button:**
   Use the **"🧭 Zero Target"** button to calibrate the destination corridor heading to whichever direction you are currently facing in the room.
3. **Mute / Unmute:**
   Use the **"🔈 Mute"** button if presenting in a loud auditorium, or hit **"🔊 Speak HUD"** to manually force speech output anytime.
