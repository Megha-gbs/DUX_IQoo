package com.waycheck.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waycheck.engine.RealityState
import com.waycheck.engine.RealityStatus
import com.waycheck.theme.LocalDuxColors

@Composable
fun ArVisionOverlay(
    state: RealityState,
    targetPhotoUri: Uri? = null,
    onSwitchToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuxColors.current

    val reticleColor = when (state.status) {
        RealityStatus.CONFIRMED -> colors.accentGreen
        RealityStatus.UNCERTAIN -> colors.accentYellow
        RealityStatus.MISMATCH -> colors.accentRed
    }

    val infiniteTransition = rememberInfiniteTransition(label = "reticleScan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, reticleColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        // AR Reticle Brackets Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val bracketLen = 32.dp.toPx()
            val stroke = 2.5.dp.toPx()

            // 4 Corner Brackets
            // Top Left
            drawLine(reticleColor, Offset(24f, 24f), Offset(24f + bracketLen, 24f), stroke)
            drawLine(reticleColor, Offset(24f, 24f), Offset(24f, 24f + bracketLen), stroke)

            // Top Right
            drawLine(reticleColor, Offset(w - 24f, 24f), Offset(w - 24f - bracketLen, 24f), stroke)
            drawLine(reticleColor, Offset(w - 24f, 24f), Offset(w - 24f, 24f + bracketLen), stroke)

            // Bottom Left
            drawLine(reticleColor, Offset(24f, h - 24f), Offset(24f + bracketLen, h - 24f), stroke)
            drawLine(reticleColor, Offset(24f, h - 24f), Offset(24f, h - 24f - bracketLen), stroke)

            // Bottom Right
            drawLine(reticleColor, Offset(w - 24f, h - 24f), Offset(w - 24f - bracketLen, h - 24f), stroke)
            drawLine(reticleColor, Offset(w - 24f, h - 24f), Offset(w - 24f, h - 24f - bracketLen), stroke)

            // Center targeting crosshair
            val crosshairSize = 14.dp.toPx()
            drawLine(reticleColor.copy(alpha = 0.6f), Offset(cx - crosshairSize, cy), Offset(cx + crosshairSize, cy), 1.5.dp.toPx())
            drawLine(reticleColor.copy(alpha = 0.6f), Offset(cx, cy - crosshairSize), Offset(cx, cy + crosshairSize), 1.5.dp.toPx())

            // Center circle
            drawCircle(
                color = reticleColor.copy(alpha = 0.4f),
                radius = 28.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // Horizontal scanning radar laser line
            val scanY = h * scanOffset
            drawLine(
                color = reticleColor.copy(alpha = 0.25f),
                start = Offset(40f, scanY),
                end = Offset(w - 40f, scanY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Top Banner inside AR View
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.dp, reticleColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .clickable { onSwitchToMap() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👁️ LIVE SYSTEM VISION",
                color = reticleColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• Tap for 🗺️ Map",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Picture-in-Picture Target Photo Preview (For Travelers / Strangers)
        val photoBitmap = rememberBitmapFromUri(targetPhotoUri)
        if (targetPhotoUri != null && photoBitmap != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.88f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.accentYellow),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(width = 96.dp, height = 110.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "REFERENCE",
                        color = colors.accentYellow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Image(
                        bitmap = photoBitmap,
                        contentDescription = "Target Picture",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Bottom Detection Chip inside AR View
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val topLabel = state.topVisionLabels.firstOrNull()?.label ?: "Scanning physical pathway..."
            Text(
                text = "DETECTED: $topLabel",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sensor Alignment: Heading ${state.currentHeading.toInt()}° // Target ${state.expectedHeading.toInt()}°",
                color = colors.textSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
