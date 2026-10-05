package com.waycheck

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.waycheck.data.DestinationsRepository
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.LandmarkItem
import com.waycheck.engine.RealityEngine
import com.waycheck.engine.RealityStatus
import com.waycheck.sensors.CameraAnalyzer
import com.waycheck.sensors.GpsStore
import com.waycheck.sensors.ImuStore
import com.waycheck.theme.AppThemeMode
import com.waycheck.theme.LocalDuxColors
import com.waycheck.theme.WayCheckTheme
import com.waycheck.tts.Speaker
import com.waycheck.ui.DestinationConfirmDialog
import com.waycheck.ui.HudOverlay
import com.waycheck.ui.SearchDestinationSheet
import com.waycheck.ui.WhereToGoSheet
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var imuStore: ImuStore
    private lateinit var gpsStore: GpsStore
    private lateinit var speaker: Speaker
    private lateinit var cameraAnalyzer: CameraAnalyzer
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imuStore = ImuStore(this)
        gpsStore = GpsStore(this)
        speaker = Speaker(this)
        cameraAnalyzer = CameraAnalyzer(this)

        enableEdgeToEdge()
        setContent {
            var currentThemeMode by remember { mutableStateOf(AppThemeMode.AMOLED_BLACK) }

            WayCheckTheme(themeMode = currentThemeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalDuxColors.current.background
                ) {
                    WayCheckMainScreen(
                        imuStore = imuStore,
                        gpsStore = gpsStore,
                        speaker = speaker,
                        cameraAnalyzer = cameraAnalyzer,
                        cameraExecutor = cameraExecutor,
                        currentThemeMode = currentThemeMode,
                        onToggleTheme = {
                            currentThemeMode = if (currentThemeMode == AppThemeMode.AMOLED_BLACK) {
                                AppThemeMode.CLEAN_LIGHT
                            } else {
                                AppThemeMode.AMOLED_BLACK
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        imuStore.start()
        gpsStore.start()
    }

    override fun onPause() {
        super.onPause()
        imuStore.stop()
        gpsStore.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        speaker.shutdown()
        cameraAnalyzer.close()
        cameraExecutor.shutdown()
    }
}

@Composable
fun WayCheckMainScreen(
    imuStore: ImuStore,
    gpsStore: GpsStore,
    speaker: Speaker,
    cameraAnalyzer: CameraAnalyzer,
    cameraExecutor: java.util.concurrent.ExecutorService,
    currentThemeMode: AppThemeMode,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results[Manifest.permission.CAMERA] == true
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            gpsStore.start()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (!hasPermissions) {
        PermissionRequestView(
            onRequest = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    } else {
        WayCheckLiveHud(
            imuStore = imuStore,
            gpsStore = gpsStore,
            speaker = speaker,
            cameraAnalyzer = cameraAnalyzer,
            cameraExecutor = cameraExecutor,
            currentThemeMode = currentThemeMode,
            onToggleTheme = onToggleTheme
        )
    }
}

@Composable
fun WayCheckLiveHud(
    imuStore: ImuStore,
    gpsStore: GpsStore,
    speaker: Speaker,
    cameraAnalyzer: CameraAnalyzer,
    cameraExecutor: java.util.concurrent.ExecutorService,
    currentThemeMode: AppThemeMode,
    onToggleTheme: () -> Unit
) {
    val heading by imuStore.heading.collectAsState()
    val location by gpsStore.location.collectAsState()
    val detections by cameraAnalyzer.detections.collectAsState()

    var hasDestinationSet by remember { mutableStateOf(false) }
    var config by remember {
        mutableStateOf(
            DestinationConfig(
                id = "explore_mode",
                name = "Where do you want to go now?",
                subtitle = "Tap map, search place, or insert photo",
                category = "Live Map",
                targetLat = 13.6231,
                targetLng = 79.2898,
                expectedHeading = 0f,
                pedestrianGateName = "Select Destination",
                totalDistanceMeters = 0f,
                photoCaption = "Tap map or insert photo of destination",
                landmarks = emptyList()
            )
        )
    }
    val realityEngine = remember { RealityEngine(config) }

    var isLowCamQualityForced by remember { mutableStateOf(false) }
    var activeSimulation by remember { mutableStateOf<RealityStatus?>(null) }
    var previousStatus by remember { mutableStateOf<RealityStatus?>(null) }

    val realityState = remember(heading, location, detections, activeSimulation, config, isLowCamQualityForced) {
        realityEngine.updateConfig(config)
        realityEngine.setLowCameraQualityMode(isLowCamQualityForced)
        realityEngine.evaluate(
            currentHeading = heading,
            location = location,
            visionLabels = detections,
            forcedOverride = activeSimulation
        )
    }

    var isCameraFeedVisible by remember { mutableStateOf(false) }
    // Ask "Where do you want to go now?" on open!
    var isWhereToSheetOpen by remember { mutableStateOf(true) }
    var isSearchSheetOpen by remember { mutableStateOf(false) }
    var destinationPendingConfirmation by remember { mutableStateOf<DestinationConfig?>(null) }
    var targetPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val colors = LocalDuxColors.current

    // Greet user on open / resume when no destination is set
    LaunchedEffect(Unit) {
        if (!hasDestinationSet) {
            speaker.speak("Hi, where do you want to go?", force = true)
        }
    }

    // Trigger voice alert when reality status flips (CONFIRMED <-> MISMATCH)
    LaunchedEffect(realityState.status, realityState.voiceInstruction) {
        if (hasDestinationSet && realityState.status != previousStatus) {
            previousStatus = realityState.status
            if (realityState.voiceInstruction != null) {
                speaker.speak(realityState.voiceInstruction)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // LAYER 1: Camera View (Active for real-time vision)
        if (isCameraFeedVisible) {
            CameraPreview(
                analyzer = cameraAnalyzer,
                executor = cameraExecutor,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55000000))
            )
        } else {
            // Keep Camera analysis running silently in background
            Box(modifier = Modifier.size(1.dp)) {
                CameraPreview(
                    analyzer = cameraAnalyzer,
                    executor = cameraExecutor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // LAYER 2: Map & Navigation HUD
        HudOverlay(
            state = realityState,
            config = config,
            hasDestinationSet = hasDestinationSet,
            isCameraFeedVisible = isCameraFeedVisible,
            currentThemeMode = currentThemeMode,
            targetPhotoUri = targetPhotoUri,
            onToggleTheme = onToggleTheme,
            onToggleCameraFeed = {
                isCameraFeedVisible = !isCameraFeedVisible
            },
            onOpenWhereTo = {
                isWhereToSheetOpen = true
            },
            onClearDestination = {
                hasDestinationSet = false
                targetPhotoUri = null
                speaker.speak("Hi, where do you want to go?", force = true)
            },
            onMapPointSelected = { tappedLat, tappedLng ->
                // User tapped on the real world map!
                hasDestinationSet = true
                val currentLoc = location
                val calculatedDist = if (currentLoc != null && currentLoc.latitude != 0.0) {
                    val destLoc = android.location.Location("").apply {
                        latitude = tappedLat
                        longitude = tappedLng
                    }
                    currentLoc.distanceTo(destLoc)
                } else {
                    80f
                }
                val newConfig = DestinationConfig(
                    id = "map_pin_${System.currentTimeMillis()}",
                    name = "Pinned Location",
                    subtitle = "Tapped on Street Map",
                    targetLat = tappedLat,
                    targetLng = tappedLng,
                    expectedHeading = heading,
                    pedestrianGateName = "Pinned Location",
                    totalDistanceMeters = calculatedDist,
                    landmarks = listOf(
                        LandmarkItem("lm_map", "Pinned Location", "Follow path towards pinned location", calculatedDist, 0f, "📍")
                    )
                )
                config = newConfig
                realityEngine.updateConfig(newConfig)
                speaker.speak("Okay, navigating to ${newConfig.name}. Follow the route.", force = true)
            },
            onToggleLowCamQualityMode = {
                isLowCamQualityForced = !isLowCamQualityForced
                realityEngine.setLowCameraQualityMode(isLowCamQualityForced)
                if (isLowCamQualityForced) {
                    speaker.speak("Low light mode active. Pedometer and compass guidance engaged.", force = true)
                } else {
                    speaker.speak("Camera vision guidance restored.", force = true)
                }
            },
            onSpeakNow = {
                val text = realityState.voiceInstruction ?: realityState.naturalGuidance
                speaker.speak(text, force = true)
            },
            onCalibrateHeading = {
                hasDestinationSet = true
                config = realityEngine.anchorTargetAhead(location, heading, config.pedestrianGateName)
                speaker.speak("Target anchored directly ahead along current path.", force = true)
            },
            onSimulateScenario = { simulation ->
                activeSimulation = simulation
            },
            activeSimulation = activeSimulation
        )

        // LAYER 3: "Where do you want to go now?" First-Launch Modal
        if (isWhereToSheetOpen) {
            WhereToGoSheet(
                onSearchClicked = {
                    isWhereToSheetOpen = false
                    isSearchSheetOpen = true
                },
                onPhotoSelected = { photoUri ->
                    isWhereToSheetOpen = false
                    targetPhotoUri = photoUri
                    hasDestinationSet = true

                    // Configure target as visual photo target straight ahead along current path
                    val photoConfig = realityEngine.anchorTargetAhead(location, heading, "Photo Matched Entrance").copy(
                        id = "photo_target",
                        name = "Photo Target Location",
                        subtitle = "Navigating to Matched Picture",
                        category = "Visual Target",
                        photoCaption = "Reference photograph provided by traveler",
                        landmarks = listOf(
                            LandmarkItem("lm_photo", "Visual Landmark", "Look for entrance matching your photo", 50f, 0f, "🖼️")
                        )
                    )
                    config = photoConfig
                    realityEngine.updateConfig(photoConfig)
                    speaker.speak("Okay, navigating to ${photoConfig.name}. Follow the route.", force = true)
                },
                onTapMapClicked = {
                    isWhereToSheetOpen = false
                    speaker.speak("Tap anywhere on the street map to set your destination.", force = true)
                },
                onAnchorAheadClicked = {
                    isWhereToSheetOpen = false
                    hasDestinationSet = true
                    config = realityEngine.anchorTargetAhead(location, heading, "Entrance Straight Ahead")
                    speaker.speak("Okay, navigating to Entrance Straight Ahead. Follow the route.", force = true)
                },
                onDismiss = {
                    isWhereToSheetOpen = false
                }
            )
        }

        // LAYER 4: Search Destination Sheet
        if (isSearchSheetOpen) {
            SearchDestinationSheet(
                onSelectForConfirmation = { selectedDest ->
                    isSearchSheetOpen = false
                    destinationPendingConfirmation = selectedDest
                },
                onAnchorCurrentHeading = {
                    hasDestinationSet = true
                    config = realityEngine.anchorTargetAhead(location, heading, "Entrance Ahead")
                    speaker.speak("Okay, navigating to Entrance Ahead. Follow the route.", force = true)
                },
                onDismiss = {
                    isSearchSheetOpen = false
                }
            )
        }

        // LAYER 5: Exact Area Picture Confirmation Dialog
        if (destinationPendingConfirmation != null) {
            DestinationConfirmDialog(
                destination = destinationPendingConfirmation!!,
                onConfirm = { confirmedDest ->
                    hasDestinationSet = true
                    config = confirmedDest
                    realityEngine.updateConfig(confirmedDest)
                    destinationPendingConfirmation = null
                    val destName = confirmedDest.pedestrianGateName.ifEmpty { confirmedDest.name }
                    speaker.speak("Okay, navigating to $destName. Follow the route.", force = true)
                },
                onDismiss = {
                    destinationPendingConfirmation = null
                }
            )
        }
    }
}

@Composable
fun CameraPreview(
    analyzer: ImageAnalysis.Analyzer,
    executor: java.util.concurrent.ExecutorService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, analyzer)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera bind failed: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@Composable
fun PermissionRequestView(onRequest: () -> Unit) {
    val colors = LocalDuxColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.accentYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    color = Color.Black,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DUX",
                color = colors.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "Global Pedestrian & Landmark Navigation",
                color = colors.accentYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Works for travelers & local pedestrians everywhere.\nCamera validates entrance signs & turnstiles.\nGPS & Sensors guide you directly to the gate.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentYellow,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "START NAVIGATION",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}
