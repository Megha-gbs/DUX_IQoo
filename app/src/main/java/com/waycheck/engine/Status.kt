package com.waycheck.engine

import androidx.annotation.DrawableRes

enum class RealityStatus {
    CONFIRMED,
    UNCERTAIN,
    MISMATCH
}

data class LandmarkItem(
    val id: String,
    val name: String,
    val instruction: String,
    val distanceMeters: Float,
    val bearingOffsetDegrees: Float,
    val iconEmoji: String,
    val isHazard: Boolean = false,
    val isHighlighted: Boolean = false
)

data class DestinationConfig(
    val id: String = "gate_2",
    val name: String = "Pedestrian Gate 2 (Auditorium)",
    val subtitle: String = "Main Tech Park • Pedestrian Turnstiles",
    val category: String = "Tech Park",
    val targetLat: Double = 13.6231,
    val targetLng: Double = 79.2898,
    val expectedHeading: Float = 45f,
    val pedestrianGateName: String = "Pedestrian Gate 2",
    val totalDistanceMeters: Float = 55f,
    @DrawableRes val imageRes: Int? = null,
    val photoCaption: String = "Photo of exact pedestrian entrance gate and turnstiles",
    val verificationChecklist: List<String> = listOf(
        "Look for: 'GATE 2 PEDESTRIAN ACCESS ONLY' sign",
        "Security guard cabin located on the left",
        "Dedicated yellow tactile tiles on footpath",
        "Vehicle ramp separated 30m away to the right"
    ),
    val landmarks: List<LandmarkItem> = listOf(
        LandmarkItem("lm_security", "Security Post / Cabin", "Pass guard cabin on your left", 18f, -15f, "👮"),
        LandmarkItem("lm_walkway", "Paved Tree Footpath", "Walk straight along shaded tiles", 35f, 5f, "🌳"),
        LandmarkItem("lm_gate", "Gate 2 Pedestrian Turnstile", "Entry turnstile straight ahead", 55f, 0f, "🚪"),
        LandmarkItem("lm_hazard", "Vehicle Entry Ramp & Barrier", "Restricted • Car & Bus zone", 22f, 135f, "⛔", isHazard = true)
    )
)

data class VisionDetection(
    val label: String,
    val confidence: Float,
    val isWrongEntity: Boolean
)

data class RealityState(
    val status: RealityStatus = RealityStatus.UNCERTAIN,
    val confidenceScore: Float = 0.5f,
    val headingDiff: Float = 0f,
    val currentHeading: Float = 0f,
    val expectedHeading: Float = 45f,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val gpsAccuracy: Float? = null,
    val distanceRemainingMeters: Float = 55f,
    val stepPacesRemaining: Int = 73,
    val isLowCameraQuality: Boolean = false,
    val isPedometerActive: Boolean = true,
    val stepsWalked: Int = 0,
    val currentLandmark: LandmarkItem? = null,
    val naturalGuidance: String = "Follow the walkway straight ahead",
    val topVisionLabels: List<VisionDetection> = emptyList(),
    val landmarks: List<LandmarkItem> = emptyList(),
    val primaryReason: String = "Acquiring reality lock...",
    val voiceInstruction: String? = null
)
