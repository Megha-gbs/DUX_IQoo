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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.RealityState
import com.waycheck.theme.LocalDuxColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * DUX Street Map:
 * Shows asphalt road, pedestrian footpath, campus buildings,
 * green lawn, navigation polyline, user location puck with directional chevron,
 * and landmark pins.
 */
@Composable
fun NormalStreetMap(
    state: RealityState,
    config: DestinationConfig,
    onDoubleTapUserLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuxColors.current
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val gpsPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "gpsPulse"
    )

    // Palette for Normal Map (Dark AMOLED vs Clean Light)
    val mapBg = if (colors.isDark) Color(0xFF0D0D10) else Color(0xFFF3F4F6)
    val roadColor = if (colors.isDark) Color(0xFF1E1E24) else Color(0xFFFFFFFF)
    val roadBorderColor = if (colors.isDark) Color(0xFF2C2C34) else Color(0xFFD1D5DB)
    val roadDividerColor = if (colors.isDark) Color(0xFF383842) else Color(0xFFE5E7EB)
    val footpathColor = if (colors.isDark) Color(0xFF242832) else Color(0xFFE2E8F0)
    val buildingColor = if (colors.isDark) Color(0xFF15171C) else Color(0xFFE9ECF0)
    val buildingBorderColor = if (colors.isDark) Color(0xFF282B34) else Color(0xFFCBD5E1)
    val parkColor = if (colors.isDark) Color(0xFF122018) else Color(0xFFE6F4EA)
    val parkBorderColor = if (colors.isDark) Color(0xFF1D3426) else Color(0xFFC6E7D0)
    val streetLabelColor = if (colors.isDark) Color(0xFF8E95A5) else Color(0xFF6B7280)
    val buildingLabelColor = if (colors.isDark) Color(0xFFA0A6B5) else Color(0xFF4B5563)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(mapBg)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTapUserLocation()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ─────────────────────────────────────────────
            // 1. BASE MAP LAYOUT: Roads, Buildings & Footpaths
            // ─────────────────────────────────────────────

            // Green Campus Park Area (Top Right)
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(w * 0.48f, 20f),
                size = Size(w * 0.48f, h * 0.32f),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
            drawRoundRect(
                color = parkBorderColor,
                topLeft = Offset(w * 0.48f, 20f),
                size = Size(w * 0.48f, h * 0.32f),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Campus Building 1: "Main Office Tower A" (Top Left)
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(20f, 20f),
                size = Size(w * 0.40f, h * 0.28f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = buildingBorderColor,
                topLeft = Offset(20f, 20f),
                size = Size(w * 0.40f, h * 0.28f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Campus Building 2: "Security Checkpost & Reception"
            val securityBoxTop = Offset(20f, h * 0.35f)
            val securityBoxSize = Size(w * 0.35f, h * 0.16f)
            drawRoundRect(
                color = buildingColor,
                topLeft = securityBoxTop,
                size = securityBoxSize,
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = buildingBorderColor,
                topLeft = securityBoxTop,
                size = securityBoxSize,
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Campus Building 3: "Convention Centre / Tower B" (Bottom Left)
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(20f, h * 0.68f),
                size = Size(w * 0.38f, h * 0.26f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = buildingBorderColor,
                topLeft = Offset(20f, h * 0.68f),
                size = Size(w * 0.38f, h * 0.26f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Main Vehicular Road (Running on the right side)
            val roadLeft = w * 0.62f
            val roadWidth = w * 0.34f
            drawRect(
                color = roadColor,
                topLeft = Offset(roadLeft, 0f),
                size = Size(roadWidth, h)
            )
            // Road outer curbs
            drawLine(
                color = roadBorderColor,
                start = Offset(roadLeft, 0f),
                end = Offset(roadLeft, h),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = roadBorderColor,
                start = Offset(roadLeft + roadWidth, 0f),
                end = Offset(roadLeft + roadWidth, h),
                strokeWidth = 2.dp.toPx()
            )
            // Dashed Road Center Line
            drawLine(
                color = roadDividerColor,
                start = Offset(roadLeft + (roadWidth / 2f), 0f),
                end = Offset(roadLeft + (roadWidth / 2f), h),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            )

            // Restricted Vehicle Entry Ramp (Turning off to basement)
            val rampPath = Path().apply {
                moveTo(roadLeft, h * 0.70f)
                cubicTo(
                    roadLeft - 20f, h * 0.75f,
                    roadLeft - 60f, h * 0.82f,
                    roadLeft - 70f, h
                )
                lineTo(roadLeft, h)
                close()
            }
            drawPath(
                path = rampPath,
                color = if (colors.isDark) Color(0xFF281418) else Color(0xFFFFECEF)
            )

            // Dedicated Pedestrian Footpath / Paved Walkway (Runs between buildings and road)
            val walkLeft = w * 0.44f
            val walkWidth = w * 0.16f
            drawRect(
                color = footpathColor,
                topLeft = Offset(walkLeft, 0f),
                size = Size(walkWidth, h)
            )
            // Footpath Tactile Border
            drawLine(
                color = colors.accentYellow.copy(alpha = 0.4f),
                start = Offset(walkLeft + (walkWidth / 2f), 0f),
                end = Offset(walkLeft + (walkWidth / 2f), h),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )

            // ─────────────────────────────────────────────
            // 2. STREET LABELS & TEXT
            // ─────────────────────────────────────────────
            drawText(
                textMeasurer = textMeasurer,
                text = "TOWER A",
                topLeft = Offset(32f, 32f),
                style = TextStyle(
                    color = buildingLabelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "SECURITY POST",
                topLeft = Offset(28f, h * 0.35f + 12f),
                style = TextStyle(
                    color = buildingLabelColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "TOWER B / AUDITORIUM",
                topLeft = Offset(32f, h * 0.68f + 16f),
                style = TextStyle(
                    color = buildingLabelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "CAMPUS LAWN",
                topLeft = Offset(w * 0.52f, 32f),
                style = TextStyle(
                    color = if (colors.isDark) Color(0xFF34D399) else Color(0xFF059669),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // Vertical Road Label
            drawText(
                textMeasurer = textMeasurer,
                text = "SERVICE ROAD",
                topLeft = Offset(roadLeft + 16f, h * 0.45f),
                style = TextStyle(
                    color = streetLabelColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            // Footpath Label
            drawText(
                textMeasurer = textMeasurer,
                text = "PEDESTRIAN FOOTPATH",
                topLeft = Offset(walkLeft + 4f, h * 0.20f),
                style = TextStyle(
                    color = colors.accentYellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // ─────────────────────────────────────────────
            // 3. NAVIGATION ROUTE POLYLINE (Walking Route)
            // ─────────────────────────────────────────────
            // Starting point: User Location (Center Bottom along footpath)
            val userPos = Offset(walkLeft + (walkWidth / 2f), h * 0.72f)

            // Target Gate: Pedestrian Gate 2 (Ahead along footpath)
            val gatePos = Offset(walkLeft + (walkWidth / 2f), h * 0.22f)

            // Intermediate milestone waypoint (near security post)
            val securityPoint = Offset(walkLeft + (walkWidth / 2f), h * 0.45f)

            // Draw glowing walking route path
            val routePath = Path().apply {
                moveTo(userPos.x, userPos.y)
                lineTo(securityPoint.x, securityPoint.y)
                lineTo(gatePos.x, gatePos.y)
            }

            // Route Glow Shadow
            drawPath(
                path = routePath,
                color = colors.accentYellow.copy(alpha = 0.25f),
                style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Main Route Line
            drawPath(
                path = routePath,
                color = colors.accentYellow,
                style = Stroke(width = 4.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Small direction arrows along route
            drawCircle(
                color = Color.Black,
                radius = 2.5.dp.toPx(),
                center = Offset(walkLeft + (walkWidth / 2f), h * 0.58f)
            )
            drawCircle(
                color = Color.Black,
                radius = 2.5.dp.toPx(),
                center = Offset(walkLeft + (walkWidth / 2f), h * 0.33f)
            )

            // ─────────────────────────────────────────────
            // 4. LANDMARK PINS ALONG THE STREET
            // ─────────────────────────────────────────────

            // A) Security Guard Post Pin
            drawCircle(
                color = colors.surface,
                radius = 12.dp.toPx(),
                center = Offset(walkLeft - 18.dp.toPx(), h * 0.45f)
            )
            drawCircle(
                color = colors.border,
                radius = 12.dp.toPx(),
                center = Offset(walkLeft - 18.dp.toPx(), h * 0.45f),
                style = Stroke(1.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "👮",
                topLeft = Offset(walkLeft - 26.dp.toPx(), h * 0.45f - 10.dp.toPx()),
                style = TextStyle(fontSize = 12.sp)
            )

            // B) Vehicle Hazard Warning Pin (On the Car Ramp)
            val hazardPos = Offset(roadLeft - 20.dp.toPx(), h * 0.82f)
            drawCircle(
                color = colors.accentRed,
                radius = 11.dp.toPx(),
                center = hazardPos
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = hazardPos
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "Car Ramp (No Entry)",
                topLeft = Offset(hazardPos.x - 40.dp.toPx(), hazardPos.y + 14.dp.toPx()),
                style = TextStyle(
                    color = colors.accentRed,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // ─────────────────────────────────────────────
            // 5. DESTINATION PIN
            // ─────────────────────────────────────────────
            // Pin shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 8.dp.toPx(),
                center = Offset(gatePos.x, gatePos.y + 10.dp.toPx())
            )
            // Pin Outer Glow
            drawCircle(
                color = colors.accentGreen.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = gatePos
            )
            // Pin Main Circle
            drawCircle(
                color = colors.accentGreen,
                radius = 11.dp.toPx(),
                center = gatePos
            )
            drawCircle(
                color = Color.Black,
                radius = 4.dp.toPx(),
                center = gatePos
            )
            // Destination Label
            drawText(
                textMeasurer = textMeasurer,
                text = config.pedestrianGateName,
                topLeft = Offset(gatePos.x - 30.dp.toPx(), gatePos.y - 28.dp.toPx()),
                style = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    background = colors.surface.copy(alpha = 0.9f)
                )
            )

            // ─────────────────────────────────────────────
            // 6. USER LOCATION PUCK
            // ─────────────────────────────────────────────
            // Accuracy halo (pulsing)
            drawCircle(
                color = Color(0xFF0084FF).copy(alpha = (1f - gpsPulse) * 0.35f),
                radius = 16.dp.toPx() + gpsPulse * 24.dp.toPx(),
                center = userPos
            )

            // White border ring
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = userPos
            )

            // Vibrant Blue Center Dot
            drawCircle(
                color = Color(0xFF007AFF),
                radius = 9.dp.toPx(),
                center = userPos
            )

            // Heading directional chevron / beam
            val headingRad = Math.toRadians((state.currentHeading - 90f).toDouble())
            val arrowLength = 16.dp.toPx()
            val tipX = userPos.x + (arrowLength * cos(headingRad)).toFloat()
            val tipY = userPos.y + (arrowLength * sin(headingRad)).toFloat()

            // Direction Arrow
            val arrowPath = Path().apply {
                moveTo(tipX, tipY)
                val leftWingX = userPos.x + (8.dp.toPx() * cos(headingRad - 2.4)).toFloat()
                val leftWingY = userPos.y + (8.dp.toPx() * sin(headingRad - 2.4)).toFloat()
                val rightWingX = userPos.x + (8.dp.toPx() * cos(headingRad + 2.4)).toFloat()
                val rightWingY = userPos.y + (8.dp.toPx() * sin(headingRad + 2.4)).toFloat()
                lineTo(leftWingX, leftWingY)
                lineTo(userPos.x, userPos.y)
                lineTo(rightWingX, rightWingY)
                close()
            }
            drawPath(
                path = arrowPath,
                color = Color.White
            )

            // "You" label below user puck
            drawText(
                textMeasurer = textMeasurer,
                text = "You",
                topLeft = Offset(userPos.x - 8.dp.toPx(), userPos.y + 14.dp.toPx()),
                style = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Top street navigation hint overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface.copy(alpha = 0.92f))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accentYellow)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MAP",
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "  •  Double-tap your location for Camera",
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Map Legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface.copy(alpha = 0.88f))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚶 Footpath  •  🏢 Campus  •  🚗 Road  •  🚪 Gate",
                color = colors.textSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
