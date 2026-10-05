package com.waycheck.engine

import android.location.Location
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RealityEngine(
    private var config: DestinationConfig = DestinationConfig()
) {
    // Labels indicating wrong physical zone for pedestrian navigation (vehicles, parking, highway ramps)
    private val wrongEntityKeywords = setOf(
        "car", "automobile", "vehicle", "parking", "garage", "lot",
        "truck", "bus", "suv", "cab", "taxicab", "motor scooter", "motorcycle",
        "traffic light", "freeway", "highway", "toll", "barrier", "boom barrier",
        "minivan", "pickup", "convertible", "limousine", "wheel", "street sign"
    )

    // Labels indicating verified pedestrian infrastructure
    private val expectedEntityKeywords = setOf(
        "pedestrian", "walkway", "stairs", "door", "gate", "sliding door",
        "sidewalk", "path", "entrance", "turnstile", "ramp", "corridor", "pavement",
        "footpath", "archway"
    )

    private var stepsWalkedAccumulator = 0
    private var isLowCameraQualityForced = false

    fun setLowCameraQualityMode(enabled: Boolean) {
        this.isLowCameraQualityForced = enabled
    }

    fun isLowCameraQualityMode(): Boolean = isLowCameraQualityForced

    fun recordStep() {
        stepsWalkedAccumulator++
    }

    fun updateConfig(newConfig: DestinationConfig) {
        this.config = newConfig
    }

    fun getConfig(): DestinationConfig = config

    fun anchorTargetAhead(currentLoc: Location?, currentHeading: Float, gateName: String = "Pedestrian Gate 2"): DestinationConfig {
        val headingRad = Math.toRadians(currentHeading.toDouble())
        val distance = 55.0 // meters ahead
        val lat = currentLoc?.latitude ?: 13.6225
        val lng = currentLoc?.longitude ?: 79.2892

        val dLat = (distance * cos(headingRad)) / 111111.0
        val dLng = (distance * sin(headingRad)) / (111111.0 * cos(Math.toRadians(lat)))

        val newTargetLat = lat + dLat
        val newTargetLng = lng + dLng

        this.config = DestinationConfig(
            id = "custom_anchor",
            name = gateName,
            subtitle = "Calibrated Corridor • 55m Ahead",
            category = "Custom Anchor",
            targetLat = newTargetLat,
            targetLng = newTargetLng,
            expectedHeading = currentHeading,
            pedestrianGateName = gateName,
            totalDistanceMeters = distance.toFloat(),
            imageRes = null,
            photoCaption = "Corridor calibrated straight ahead from current position",
            landmarks = listOf(
                LandmarkItem("lm_security", "Security Post / Cabin", "Pass guard cabin on your left", 16f, -10f, "👮"),
                LandmarkItem("lm_walkway", "Paved Tree Footpath", "Follow shaded walkway straight", 32f, 0f, "🌳"),
                LandmarkItem("lm_gate", gateName, "Entry turnstile straight ahead", 55f, 0f, "🚪"),
                LandmarkItem("lm_hazard", "Vehicle Entry Ramp", "Car & bus lane • Avoid", 20f, 135f, "⛔", isHazard = true)
            )
        )
        return this.config
    }

    fun calculateHeadingDiff(currentHeading: Float, targetHeading: Float): Float {
        var diff = (currentHeading - targetHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return abs(diff)
    }

    fun evaluate(
        currentHeading: Float,
        location: Location?,
        visionLabels: List<Pair<String, Float>>,
        forcedOverride: RealityStatus? = null
    ): RealityState {
        var effectiveTargetHeading = config.expectedHeading
        var distanceRemaining = config.totalDistanceMeters

        if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
            val targetLocation = Location("").apply {
                latitude = config.targetLat
                longitude = config.targetLng
            }
            val measuredDist = location.distanceTo(targetLocation)
            if (measuredDist >= 1f) {
                distanceRemaining = measuredDist
                effectiveTargetHeading = (location.bearingTo(targetLocation) + 360f) % 360f
            }
        }

        // Pedometer stride estimation (average 0.75m per pace)
        val pacesRemaining = (distanceRemaining / 0.75f).toInt().coerceAtLeast(1)

        if (forcedOverride != null) {
            val isMismatch = forcedOverride == RealityStatus.MISMATCH
            val isUncertain = forcedOverride == RealityStatus.UNCERTAIN

            val updatedLandmarks = config.landmarks.map { lm ->
                lm.copy(
                    isHighlighted = if (isMismatch) lm.isHazard else !lm.isHazard
                )
            }

            val gateName = config.pedestrianGateName.ifEmpty { "destination" }

            return RealityState(
                status = forcedOverride,
                confidenceScore = if (isUncertain) 0.35f else 0.95f,
                headingDiff = if (isMismatch) 140f else if (isUncertain) 45f else 8f,
                currentHeading = currentHeading,
                expectedHeading = effectiveTargetHeading,
                currentLat = location?.latitude ?: config.targetLat,
                currentLng = location?.longitude ?: config.targetLng,
                gpsAccuracy = location?.accuracy ?: 8.0f,
                distanceRemainingMeters = distanceRemaining,
                stepPacesRemaining = pacesRemaining,
                isLowCameraQuality = isLowCameraQualityForced,
                stepsWalked = stepsWalkedAccumulator,
                currentLandmark = updatedLandmarks.firstOrNull { it.isHighlighted },
                naturalGuidance = if (isMismatch) {
                    "Wrong way – vehicle entrance. Turn around."
                } else if (isUncertain) {
                    "Follow path towards $gateName."
                } else {
                    "Walk straight ${distanceRemaining.toInt()} metres."
                },
                topVisionLabels = when (forcedOverride) {
                    RealityStatus.MISMATCH -> listOf(
                        VisionDetection("boom barrier / vehicle ramp", 0.94f, true),
                        VisionDetection("car parking", 0.85f, true)
                    )
                    RealityStatus.CONFIRMED -> listOf(
                        VisionDetection("pedestrian walkway", 0.95f, false),
                        VisionDetection("turnstile entrance", 0.88f, false)
                    )
                    RealityStatus.UNCERTAIN -> listOf(
                        VisionDetection("unrecognized corridor", 0.35f, false)
                    )
                },
                landmarks = updatedLandmarks,
                primaryReason = if (isMismatch) "Vehicle gate approach identified" else "Verified pedestrian access",
                voiceInstruction = if (isMismatch) {
                    "Wrong way for $gateName – vehicle entrance. Turn around."
                } else if (isUncertain) {
                    "Follow path towards $gateName."
                } else {
                    "Walk straight ${distanceRemaining.toInt()} metres."
                }
            )
        }

        val headingDiff = calculateHeadingDiff(currentHeading, effectiveTargetHeading)
        val isHeadingMismatch = headingDiff > 75f

        // Only consider vision labels with solid confidence (>= 0.20)
        val processedVision = visionLabels
            .filter { it.second >= 0.20f }
            .map { (label, score) ->
                val lower = label.lowercase()
                val isWrong = wrongEntityKeywords.any { lower.contains(it) }
                VisionDetection(label, score, isWrong)
            }

        val detectedWrongEntity = processedVision.firstOrNull { it.isWrongEntity && it.confidence >= 0.30f }
        val detectedExpectedEntity = processedVision.firstOrNull { !it.isWrongEntity && it.confidence >= 0.30f }

        // Camera quality check: if camera has 0 recognizable labels or forced low quality
        val isCameraDegraded = isLowCameraQualityForced || (processedVision.isEmpty() && visionLabels.isNotEmpty() && visionLabels.all { it.second < 0.25f })

        val gpsAccuracy = location?.accuracy
        val gpsAccuracyOk = gpsAccuracy != null && gpsAccuracy <= 35f

        val status: RealityStatus
        val reason: String
        val voice: String?
        val guidance: String
        val gateName = config.pedestrianGateName.ifEmpty { "destination" }

        if (detectedWrongEntity != null) {
            status = RealityStatus.MISMATCH
            reason = "Vehicle zone identified ahead (${(detectedWrongEntity.confidence * 100).toInt()}%)"
            voice = "Wrong way for $gateName – vehicle entrance. Turn around."
            guidance = "Wrong way – vehicle entrance. Turn around."
        } else if (isHeadingMismatch) {
            status = RealityStatus.MISMATCH
            reason = "Off pedestrian path by ${headingDiff.toInt()}°"
            voice = "Wrong way for $gateName. Turn around."
            guidance = "Wrong way – off route. Turn around."
        } else if (isCameraDegraded) {
            status = if (headingDiff <= 35f) RealityStatus.CONFIRMED else RealityStatus.UNCERTAIN
            reason = "Camera low light / blur • Step Guide Active (~$pacesRemaining paces)"
            voice = "Walk straight $pacesRemaining paces."
            guidance = "Walk straight $pacesRemaining paces."
        } else if (!gpsAccuracyOk && processedVision.isEmpty()) {
            status = RealityStatus.UNCERTAIN
            reason = "Aligning compass & GPS"
            voice = null
            guidance = "Point phone forward along walkway."
        } else {
            status = RealityStatus.CONFIRMED
            reason = if (detectedExpectedEntity != null) {
                "Verified physical path: ${detectedExpectedEntity.label}"
            } else {
                "Corridor aligned (${headingDiff.toInt()}° deviation)"
            }
            if (distanceRemaining <= 25f) {
                voice = "$gateName is 20 metres ahead."
                guidance = "$gateName is 20 metres ahead."
            } else {
                voice = "Walk straight ${distanceRemaining.toInt()} metres."
                guidance = "Walk straight ${distanceRemaining.toInt()} metres."
            }
        }

        // Update landmarks with active proximity
        val updatedLandmarks = config.landmarks.map { lm ->
            val isRelevant = if (status == RealityStatus.MISMATCH) lm.isHazard else (!lm.isHazard && lm.distanceMeters <= distanceRemaining + 20f)
            lm.copy(isHighlighted = isRelevant)
        }

        val headingScore = (1f - (headingDiff / 180f)).coerceIn(0f, 1f)
        val visionScore = if (isCameraDegraded) 0.65f else (detectedExpectedEntity?.confidence ?: if (detectedWrongEntity != null) 0.15f else 0.7f)
        val gpsScore = if (gpsAccuracyOk) 0.9f else 0.4f
        val confidence = (headingScore * 0.45f + visionScore * 0.35f + gpsScore * 0.2f).coerceIn(0.1f, 0.99f)

        return RealityState(
            status = status,
            confidenceScore = confidence,
            headingDiff = headingDiff,
            currentHeading = currentHeading,
            expectedHeading = effectiveTargetHeading,
            currentLat = location?.latitude,
            currentLng = location?.longitude,
            gpsAccuracy = gpsAccuracy,
            distanceRemainingMeters = distanceRemaining,
            stepPacesRemaining = pacesRemaining,
            isLowCameraQuality = isCameraDegraded,
            stepsWalked = stepsWalkedAccumulator,
            currentLandmark = updatedLandmarks.firstOrNull { it.isHighlighted },
            naturalGuidance = guidance,
            topVisionLabels = processedVision,
            landmarks = updatedLandmarks,
            primaryReason = reason,
            voiceInstruction = voice
        )
    }
}
