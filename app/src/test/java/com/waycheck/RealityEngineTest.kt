package com.waycheck

import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.RealityEngine
import com.waycheck.engine.RealityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealityEngineTest {

    private val engine = RealityEngine(
        DestinationConfig(
            id = "test_gate",
            name = "Test Gate 2",
            expectedHeading = 45f,
            pedestrianGateName = "Pedestrian Gate 2"
        )
    )

    @Test
    fun testHeadingDifferenceCalculation() {
        val diff1 = engine.calculateHeadingDiff(45f, 45f)
        assertEquals(0f, diff1, 0.01f)

        val diff2 = engine.calculateHeadingDiff(55f, 45f)
        assertEquals(10f, diff2, 0.01f)

        val diff3 = engine.calculateHeadingDiff(355f, 5f)
        assertEquals(10f, diff3, 0.01f)

        val diffOpposite = engine.calculateHeadingDiff(225f, 45f)
        assertEquals(180f, diffOpposite, 0.01f)
    }

    @Test
    fun testHeadingMismatchTriggersMismatchStatus() {
        // Facing 225 degrees (180 degrees away from expected 45 degrees)
        val state = engine.evaluate(
            currentHeading = 225f,
            location = null,
            visionLabels = emptyList()
        )

        assertEquals(RealityStatus.MISMATCH, state.status)
        assertTrue(state.headingDiff > 75f)
        assertTrue(state.voiceInstruction?.contains("Gate 2") == true)
    }

    @Test
    fun testVehicleDetectionTriggersMismatchStatus() {
        // Aligned heading, but camera sees parking lot / vehicles
        val state = engine.evaluate(
            currentHeading = 45f,
            location = null,
            visionLabels = listOf(
                Pair("parking lot", 0.88f),
                Pair("car", 0.72f)
            )
        )

        assertEquals(RealityStatus.MISMATCH, state.status)
        assertTrue(state.topVisionLabels.any { it.isWrongEntity })
        assertTrue(state.voiceInstruction?.contains("vehicle") == true || state.voiceInstruction?.contains("Gate 2") == true)
    }

    @Test
    fun testPedestrianPathConfirmed() {
        // Aligned heading + camera sees pedestrian walkway
        val state = engine.evaluate(
            currentHeading = 48f,
            location = null,
            visionLabels = listOf(
                Pair("pedestrian walkway", 0.91f),
                Pair("stairs", 0.80f)
            )
        )

        assertEquals(RealityStatus.CONFIRMED, state.status)
        assertTrue(state.confidenceScore > 0.6f)
        assertTrue(state.voiceInstruction?.contains("Gate 2") == true || state.voiceInstruction?.contains("straight") == true)
    }
}
