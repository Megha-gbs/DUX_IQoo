package com.waycheck.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waycheck.engine.LandmarkItem
import com.waycheck.engine.RealityState
import com.waycheck.engine.RealityStatus
import com.waycheck.theme.LocalDuxColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DuxRadarMap(
    state: RealityState,
    onDoubleTapUserLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuxColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val mapBg = if (colors.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val mapBorder = colors.border
    val gridLineColor = if (colors.isDark) Color(0xFF1C1C1C) else Color(0xFFE5E7EB)
    val ringColor = if (colors.isDark) Color(0xFF262626) else Color(0xFFD1D5DB)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(mapBg)
            .border(1.dp, mapBorder, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTapUserLocation()
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f - 20f
            val scaleFactor = maxRadius / 70f // 70 meters range

            // 1. Radar distance range rings
            val ringDistances = listOf(20f, 40f, 60f)
            ringDistances.forEach { dist ->
                val r = dist * scaleFactor
                drawCircle(
                    color = ringColor,
                    radius = r,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                )
            }

            // Cross-grid axis lines
            drawLine(
                color = gridLineColor,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridLineColor,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // 2. User Sight Cone (Heading field of view ~60 degrees)
            val headingRad = Math.toRadians((state.currentHeading - 90f).toDouble())
            val coneAngle = Math.toRadians(32.0)

            val leftRay = Offset(
                x = center.x + (maxRadius * 0.95f * cos(headingRad - coneAngle)).toFloat(),
                y = center.y + (maxRadius * 0.95f * sin(headingRad - coneAngle)).toFloat()
            )
            val rightRay = Offset(
                x = center.x + (maxRadius * 0.95f * cos(headingRad + coneAngle)).toFloat(),
                y = center.y + (maxRadius * 0.95f * sin(headingRad + coneAngle)).toFloat()
            )

            val conePath = Path().apply {
                moveTo(center.x, center.y)
                lineTo(leftRay.x, leftRay.y)
                lineTo(rightRay.x, rightRay.y)
                close()
            }

            drawPath(
                path = conePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentYellow.copy(alpha = 0.35f),
                        colors.accentYellow.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                )
            )

            // 3. User pulsing accuracy wave
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = (1f - pulseRadius) * 0.45f),
                radius = 14f + pulseRadius * 30f,
                center = center
            )

            // User location dot (vibrant cyan center)
            drawCircle(
                color = Color(0xFF0091EA),
                radius = 9.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = center
            )

            // Heading pointer tip
            val pointerTip = Offset(
                x = center.x + (20.dp.toPx() * cos(headingRad)).toFloat(),
                y = center.y + (20.dp.toPx() * sin(headingRad)).toFloat()
            )
            drawLine(
                color = colors.accentYellow,
                start = center,
                end = pointerTip,
                strokeWidth = 3.5.dp.toPx()
            )

            // 4. Draw Route Trail and Landmarks
            val destinationLandmark = state.landmarks.firstOrNull { it.id == "lm_gate" }
            if (destinationLandmark != null) {
                val destAngleRad = Math.toRadians((state.currentHeading + destinationLandmark.bearingOffsetDegrees - 90f).toDouble())
                val destRadiusPx = destinationLandmark.distanceMeters.coerceIn(10f, 65f) * scaleFactor
                val destOffset = Offset(
                    x = center.x + (destRadiusPx * cos(destAngleRad)).toFloat(),
                    y = center.y + (destRadiusPx * sin(destAngleRad)).toFloat()
                )

                // Dotted route line to destination
                drawLine(
                    color = colors.accentGreen,
                    start = center,
                    end = destOffset,
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )

                // Destination Pin Ring
                drawCircle(
                    color = colors.accentGreen.copy(alpha = 0.25f),
                    radius = 16.dp.toPx(),
                    center = destOffset
                )
                drawCircle(
                    color = colors.accentYellow,
                    radius = 9.dp.toPx(),
                    center = destOffset
                )
                drawCircle(
                    color = Color.Black,
                    radius = 4.dp.toPx(),
                    center = destOffset
                )
            }

            // Draw other landmark markers
            state.landmarks.filter { it.id != "lm_gate" }.forEach { lm ->
                val lmAngleRad = Math.toRadians((state.currentHeading + lm.bearingOffsetDegrees - 90f).toDouble())
                val lmDistPx = lm.distanceMeters.coerceIn(8f, 65f) * scaleFactor
                val lmOffset = Offset(
                    x = center.x + (lmDistPx * cos(lmAngleRad)).toFloat(),
                    y = center.y + (lmDistPx * sin(lmAngleRad)).toFloat()
                )

                if (lm.isHazard) {
                    drawCircle(
                        color = colors.accentRed,
                        radius = 6.dp.toPx(),
                        center = lmOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = lmOffset
                    )
                } else {
                    drawCircle(
                        color = if (lm.isHighlighted) colors.accentGreen else Color(0xFF888888),
                        radius = 7.dp.toPx(),
                        center = lmOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = lmOffset
                    )
                }
            }
        }

        // Top Banner / double tap hint
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (colors.isDark) Color(0xFF141414).copy(alpha = 0.90f) else Color(0xFFE5E7EB))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accentYellow)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "RADAR",
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "  •  Double-tap center blue dot for Camera",
                color = colors.textSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Map Legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "● You  •  ● Gate  •  ● Footpath  •  ▲ Car Ramp",
                color = colors.textSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
