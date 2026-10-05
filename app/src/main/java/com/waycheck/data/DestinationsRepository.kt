package com.waycheck.data

import com.waycheck.R
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.LandmarkItem

object DestinationsRepository {
    val predefinedDestinations: List<DestinationConfig> = listOf(
        DestinationConfig(
            id = "manyata_gate_2",
            name = "Manyata Tech Park - Gate 2",
            subtitle = "Pedestrian Turnstiles • Shaded Footpath",
            category = "Tech Park",
            targetLat = 13.0475,
            targetLng = 77.6215,
            expectedHeading = 45f,
            pedestrianGateName = "Gate 2 Pedestrian Access",
            totalDistanceMeters = 55f,
            imageRes = R.drawable.gate2_pedestrian,
            photoCaption = "Official Gate 2 pedestrian entrance: stainless turnstiles, guard cabin on left, shaded canopy",
            verificationChecklist = listOf(
                "Signboard: 'GATE 2 PEDESTRIAN ACCESS ONLY'",
                "Brick security cabin with guard on immediate left",
                "Continuous yellow tactile tile walkway",
                "Vehicle gate is completely separated 35m to the right"
            ),
            landmarks = listOf(
                LandmarkItem("lm_security", "Security Post / Cabin", "Pass guard cabin on your left", 18f, -15f, "👮"),
                LandmarkItem("lm_walkway", "Paved Tree Footpath", "Walk straight along shaded tiles", 35f, 5f, "🌳"),
                LandmarkItem("lm_gate", "Gate 2 Pedestrian Turnstile", "Entry turnstile straight ahead", 55f, 0f, "🚪"),
                LandmarkItem("lm_hazard", "Vehicle Entry Ramp", "Car & bus lane • Avoid", 22f, 135f, "⛔", isHazard = true)
            )
        ),
        DestinationConfig(
            id = "bellandur_metro_a",
            name = "Bellandur Metro - Exit Gate A",
            subtitle = "Pedestrian Escalator & Footpath Plaza",
            category = "Metro",
            targetLat = 12.9260,
            targetLng = 77.6762,
            expectedHeading = 75f,
            pedestrianGateName = "Metro Exit A (Pedestrians)",
            totalDistanceMeters = 65f,
            imageRes = R.drawable.metro_exit_a,
            photoCaption = "Covered glass escalator and wide pedestrian staircase. Zero vehicular traffic on plaza.",
            verificationChecklist = listOf(
                "Purple/green overhead sign: 'METRO ENTRY / EXIT A - PEDESTRIANS'",
                "Glass-canopied escalator and wide concrete staircase",
                "Yellow tactile paving leading to platform entrance",
                "Direct footpath connection to Outer Ring Road bus stop"
            ),
            landmarks = listOf(
                LandmarkItem("lm_stairs", "Station Entrance Escalator", "Head towards covered escalator", 25f, 5f, "🚇"),
                LandmarkItem("lm_walkway", "Plaza Tactile Paving", "Follow yellow tactile tiles", 45f, 0f, "🚶"),
                LandmarkItem("lm_gate", "Exit A Pedestrian Turnstiles", "Ticketing gate straight ahead", 65f, 0f, "🚪"),
                LandmarkItem("lm_hazard", "ORR Fast Traffic Lane", "Heavy vehicular road • Stay on sidewalk", 20f, -90f, "⛔", isHazard = true)
            )
        ),
        DestinationConfig(
            id = "rmz_footpath_gate",
            name = "RMZ Infinity / Ecospace Footpath",
            subtitle = "Pedestrian Turnstile • Planters Boundary",
            category = "Tech Park",
            targetLat = 12.9345,
            targetLng = 77.6912,
            expectedHeading = 110f,
            pedestrianGateName = "RMZ Pedestrians Only Gate",
            totalDistanceMeters = 45f,
            imageRes = R.drawable.rmz_walkway,
            photoCaption = "Wide stone walkway with employee and visitor turnstiles, separated from car road by lush green planters.",
            verificationChecklist = listOf(
                "Overhead blue board: 'RMZ PEDESTRIANS ONLY - VISITOR / EMPLOYEE ENTRY'",
                "Security check cabin with glass windows",
                "Continuous stone pavement with tree shade",
                "Physical flower planters blocking two-wheelers and autos"
            ),
            landmarks = listOf(
                LandmarkItem("lm_planters", "Green Planters & Sidewalk", "Keep planters on your left", 15f, -10f, "🌿"),
                LandmarkItem("lm_guard", "Security Checkpoint Cabin", "Check in at security cabin", 30f, 0f, "👮"),
                LandmarkItem("lm_gate", "Main Turnstile Barrier", "Tap badge or visitor pass", 45f, 0f, "🚪"),
                LandmarkItem("lm_hazard", "Basement Parking Ramp", "Vehicles descending • Do not enter", 18f, -120f, "⛔", isHazard = true)
            )
        )
    )

    fun search(query: String): List<DestinationConfig> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return predefinedDestinations
        return predefinedDestinations.filter {
            it.name.lowercase().contains(trimmed) ||
            it.subtitle.lowercase().contains(trimmed) ||
            it.category.lowercase().contains(trimmed) ||
            it.pedestrianGateName.lowercase().contains(trimmed)
        }
    }
}
