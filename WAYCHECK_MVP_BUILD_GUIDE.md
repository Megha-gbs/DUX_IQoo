# WAYCHECK — MVP Build Guide (Do Not Get Stuck)

**Lock this.** Do not change the idea. Do not add features.

> Maps know where you are. WAYCHECK knows whether you are actually there.

**Submission that shortlists (Hyderabad winner advice):** working APK + GitHub + PPT + 90s video. PPT alone loses.

---

## 0. What you ship (only this)

One Android app. Airplane mode ON the whole demo.

| Demo | What judges see |
|------|-----------------|
| **Wrong entrance** | Walk toward vehicle/parking gate. Phone: **MISMATCH**. Voice: “Pedestrian Gate 2 is behind you.” |
| Status HUD | **CONFIRMED** / **UNCERTAIN** / **MISMATCH** |
| Sensors | Camera + GPS + IMU + mic (TTS) + on-device vision |

**Do not build:** bus OCR, parking return, live maps, cloud LLM, BLE, EV, chat.

---

## 1. Scoring — build to this, not vibes

| Weight | Dimension | How WAYCHECK hits it |
|--------|-----------|----------------------|
| 30% | End product quality | One flow that never fails in airplane mode |
| 20% | Novelty / impact | Last 100–500 m Indian navigation (real need) |
| 15% | Phone use (HackTracker) | Camera + voice + on-device AI every session |
| 15% | Technical depth | Sensor fusion + confidence, not a wrapper |
| 10% | Office Kit (HackTracker) | Phone = demo. Laptop = debug via Office Kit |
| 10% | Demo 3–5 min | 90s mismatch + 2 min architecture |

**HackTracker is automatic.** Use camera, voice, on-device model, and Office Kit for real. Do not fake it.

---

## 2. Stack (do not swap mid-build)

| Layer | Choice | Why | Fallback if stuck |
|-------|--------|-----|-------------------|
| App | **Kotlin + Jetpack Compose** | Native sensors, iQOO Android | Flutter only if you already know it |
| Camera | CameraX | Standard | — |
| Location | Fused Location / GPS | Works offline | — |
| IMU | `TYPE_ROTATION_VECTOR` | Heading | `TYPE_ORIENTATION` |
| Vision | **MediaPipe Tasks** (Image Classifier / Object Detector) TFLite | On-device, free, no cloud | If model fails: keyword labels from a tiny custom list + user confirm |
| OCR (optional later) | ML Kit on-device text | Only after demo 1 works | Skip |
| Maps graph | **None for MVP** | Hardcoded 1 destination + expected heading + 2 “gates” | OsmDroid only if extra time |
| Voice out | Android TTS | Offline | — |
| Voice in | Skip for MVP | Don’t waste time | — |
| LLM | **None** | Hallucinations + size | — |
| Backend | **None** | 0 network in manifest | — |
| Storage | DataStore / JSON | Baselines | — |

**Open source (copy these names into Gradle):**

- `androidx.camera:camera-camera2` + `camera-lifecycle` + `camera-view`
- `androidx.compose.*`
- `com.google.mediapipe:tasks-vision` (or `com.google.ai.edge.litert`)
- Android TTS (`android.speech.tts.TextToSpeech`)
- Location: Play Services Location **or** `LocationManager` (prefer LocationManager so it works without Play)

**Models:** MediaPipe EfficientNet-lite / Object detector `.task` file in `app/src/main/assets/`. Apache/BSD. No API key.

---

## 3. Which AI to use for what (free trials)

Use **one AI per job**. If it loops, switch. Never ask two AIs the same broken prompt.

| Job | Tool | Prompt style | Stop if |
|-----|------|--------------|---------|
| **Repo + Android project skeleton** | **Antigravity** or **Cursor** (trial) | “Empty Compose app: CameraX preview, LocationManager, RotationVector, TTS, 3 status colors. No backend. INTERNET not in manifest.” | Gradle sync fails twice → paste error to Claude |
| **Sensor fusion logic** | **Claude** (claude.ai trial) | Paste `SensorFusion.kt` + “heading vs expected; MISMATCH if Δheading > 90° OR vision label in {parking, car, vehicle, garage}” | Code doesn’t compile → Cursor inline fix |
| **MediaPipe glue** | **Gemini** (Antigravity) or official MediaPipe Android sample | “Image classifier on CameraX ImageProxy, labels + score, 200ms” | Model too slow → drop to 1 fps stills |
| **UI (HUD)** | **v0 / Claude artifacts** then paste into Compose | Big status, 3 colors, camera behind | Looks busy → delete extras |
| **Gradle / SDK errors** | **ChatGPT** or **Grok** | Paste **full** stacktrace only | 10 min → use Fallback table |
| **Stuck architecture** | **Grok** | “MVP only 1 demo, no extra features” | You start adding bus/parking → stop |
| **README + GitHub** | Any | Short: problem, install APK, airplane mode steps | — |
| **PPT** | Already done | Do not regenerate unless asked | — |
| **Video script** | Grok / ChatGPT | 90 seconds, airplane icon visible | — |

**Antigravity:** generate files, run Gradle, fix red underlines. Do not let it “improve architecture.”

**Cursor / Windsurf trial:** same. Agent mode ON for first scaffold, then OFF — you drive.

**Copilot:** autocomplete only. Not for design.

**Rule:** If an agent rewrites the whole app, reject the diff. Keep the last compiling commit.

---

## 4. App shape (files — create these and stop)

```
app/src/main/java/com/waycheck/
  MainActivity.kt          // one screen
  sensors/GpsStore.kt
  sensors/ImuStore.kt
  sensors/CameraAnalyzer.kt
  engine/RealityEngine.kt  // THE product
  engine/Status.kt         // CONFIRMED | UNCERTAIN | MISMATCH
  ui/Hud.kt
  tts/Speaker.kt
assets/
  classifier.task          // MediaPipe model
AndroidManifest.xml        // CAMERA, ACCESS_FINE_LOCATION, NO INTERNET
```

**`RealityEngine` (only logic that matters):**

```
confidence = min(gpsAccuracyOk, imuStable, visionScore)

if !gpsAccuracyOk && !visionOk → UNCERTAIN
if Δheading > 90° OR vision in WRONG_SET → MISMATCH
else → CONFIRMED
```

WRONG_SET for demo 1: `parking, car, vehicle, garage, lot`  
Expected: pedestrian gate / walkway / stairs / door

Hardcode destination: one lat/lng + expected bearing you measure on your street today.

---

## 5. Clock (do not skip commits)

### Day 0 — today (4–6 h)

| Time | You | AI |
|------|-----|-----|
| 0:00 | Android Studio, SDK 34, empty Compose app, GitHub `waycheck` private→public later | Antigravity: generate empty project |
| 0:45 | Camera preview fills screen | Cursor: CameraX Compose sample |
| 1:30 | GPS lat/lng + accuracy on HUD | Claude: LocationManager snippet |
| 2:15 | Heading degrees on HUD | Claude: RotationVector |
| 3:00 | **Hardcoded** MISMATCH if heading off >90° | You type this. No AI. |
| 3:30 | TTS speaks status on change | ChatGPT: TTS 10-line |
| 4:00 | Commit `mvp-sensors`. Run on your phone | — |
| 4:30 | Airplane mode. Walk. Confirm heading changes | — |

**Stuck gate:** if CameraX not showing in 90 min → copy official CameraX “Getting started” sample, delete yours, paste HUD on top.

### Day 1 (6–8 h)

| Time | You | AI |
|------|-----|-----|
| 0:00 | Add MediaPipe classifier on frames | Gemini + MediaPipe Android hello-world |
| 2:00 | Print top-3 labels on HUD | — |
| 3:00 | Wire labels into RealityEngine | Claude: only that file |
| 4:00 | Record 10 walks: right path vs parking | Spreadsheet, not AI |
| 5:00 | Tune threshold so 8/10 correct | You |
| 6:00 | Debug APK. Airplane mode video 90s | — |
| 7:00 | README + screenshots | Grok: 20 lines max |

**Stuck gate:** MediaPipe Gradle hell > 60 min → skip model. Use a **manual “I see parking” button** PLUS heading mismatch. Camera still ON (HackTracker). Add model later if time.

### Day 2 — submit pack

- [ ] `assembleDebug` APK in GitHub Releases  
- [ ] README: problem, install, airplane-mode steps, 0 INTERNET  
- [ ] PPT (existing 8 slides)  
- [ ] 90s video (airplane icon in status bar)  
- [ ] Form one-liner: *Maps know where you are. WAYCHECK knows if you’re actually there.*

---

## 6. Manifest (copy exactly the spirit)

**Permissions:** `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE` only if needed.

**Do not add:** `INTERNET`, `ACCESS_NETWORK_STATE` if you can avoid it. Winner Taar: **0 network permissions**. Copy that flex.

---

## 7. Office Kit (10% — do not ignore)

During any laptop time:

1. Phone screen mirrored via **iQOO Office Kit**
2. Push APK / pull logcat through it
3. Keep phone connected while coding

HackTracker counts this. A laptop-only build loses 10% + 15% phone use.

---

## 8. Demo script (90s)

1. Show airplane mode.  
2. Start WAYCHECK. Camera live. Status CONFIRMED on the correct path.  
3. Walk to parking/vehicle entrance.  
4. **MISMATCH** + voice.  
5. Turn around. CONFIRMED.  
6. “We verify the physical path. Offline. On-device.” Stop.

Backup: same clip pre-recorded, labeled BACKUP.

---

## 9. Anti-stuck rules

1. **Compile every 30 minutes.** If not, revert last change.  
2. **One feature until it works on a real phone.**  
3. If AI rewrites >3 files, discard.  
4. No new library after Day 0 except MediaPipe.  
5. If vision fails, ship heading-mismatch + live camera. Still a product.  
6. Sleep. A crashing APK at 4am loses 30% quality.

---

## 10. What “done” means

You can hand a stranger the APK, they enable airplane mode, walk 50 m the wrong way, and the phone shouts MISMATCH without you touching it.

That is the shortlist. Everything else is optional.
