package com.waycheck.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.RealityState
import com.waycheck.engine.RealityStatus
import com.waycheck.theme.AppThemeMode
import com.waycheck.theme.LocalDuxColors

@Composable
fun rememberBitmapFromUri(uri: Uri?): ImageBitmap? {
    val context = LocalContext.current
    return androidx.compose.runtime.remember(uri) {
        if (uri == null) return@remember null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun HudOverlay(
    state: RealityState,
    config: DestinationConfig,
    hasDestinationSet: Boolean,
    isCameraFeedVisible: Boolean,
    currentThemeMode: AppThemeMode,
    targetPhotoUri: Uri?,
    onToggleTheme: () -> Unit,
    onToggleCameraFeed: () -> Unit,
    onOpenWhereTo: () -> Unit,
    onClearDestination: () -> Unit,
    onMapPointSelected: (Double, Double) -> Unit,
    onToggleLowCamQualityMode: () -> Unit,
    onSpeakNow: () -> Unit,
    onCalibrateHeading: () -> Unit,
    onSimulateScenario: (RealityStatus?) -> Unit,
    activeSimulation: RealityStatus?,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuxColors.current

    val navBannerColor = when (state.status) {
        RealityStatus.CONFIRMED -> colors.accentGreen
        RealityStatus.UNCERTAIN -> colors.accentYellow
        RealityStatus.MISMATCH -> colors.accentRed
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─────────────────────────────────────────────
            // 1. TOP SECTION: Search Bar & Turn Banner
            // ─────────────────────────────────────────────
            Column {
                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Search Field Button (Asks "Where do you want to go?")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenWhereTo() }
                    ) {
                        Text(text = "🔍", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (hasDestinationSet) config.name else "Where do you want to go now?",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasDestinationSet) "Tap to change destination" else "Search place, insert photo, or tap map",
                                color = colors.accentYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Right Actions: Theme Toggle + Camera Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Theme Toggle (Sun / Moon)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (colors.isDark) Color(0xFF222222) else Color(0xFFE2E8F0))
                                .clickable { onToggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentThemeMode == AppThemeMode.AMOLED_BLACK) "☀️" else "🌙",
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Camera View Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCameraFeedVisible) colors.accentYellow else if (colors.isDark) Color(0xFF222222) else Color(0xFFE2E8F0))
                                .clickable { onToggleCameraFeed() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isCameraFeedVisible) "🗺️ Map" else "📷 Camera",
                                color = if (isCameraFeedVisible) Color.Black else colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Turn-by-Turn Navigation Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (hasDestinationSet) navBannerColor else colors.accentYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Direction Icon Circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (hasDestinationSet) navBannerColor.copy(alpha = 0.2f) else colors.accentYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (!hasDestinationSet) {
                                    "🌍"
                                } else {
                                    when (state.status) {
                                        RealityStatus.CONFIRMED -> "⬆️"
                                        RealityStatus.UNCERTAIN -> "🧭"
                                        RealityStatus.MISMATCH -> "↩️"
                                    }
                                },
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (!hasDestinationSet) {
                                Text(
                                    text = "Ready to Navigate",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap map or search place to set destination",
                                    color = colors.accentYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = state.naturalGuidance,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${state.distanceRemainingMeters.toInt()}m remaining • ${config.pedestrianGateName}",
                                    color = navBannerColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────
            // 2. CENTER SECTION: Offline Street Map OR Camera Feed
            // ─────────────────────────────────────────────
            val photoBitmap = rememberBitmapFromUri(targetPhotoUri)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (!isCameraFeedVisible) {
                    OfflineStreetMapView(
                        currentLat = state.currentLat,
                        currentLng = state.currentLng,
                        currentHeading = state.currentHeading,
                        targetLat = if (hasDestinationSet) config.targetLat else null,
                        targetLng = if (hasDestinationSet) config.targetLng else null,
                        targetName = if (hasDestinationSet) config.pedestrianGateName else "",
                        onMapPointSelected = onMapPointSelected,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ArVisionOverlay(
                        state = state,
                        targetPhotoUri = targetPhotoUri,
                        onSwitchToMap = onToggleCameraFeed,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // If user inserted a destination photo, show floating photo badge in corner
                if (targetPhotoUri != null && photoBitmap != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface.copy(alpha = 0.94f))
                            .border(1.5.dp, colors.accentYellow, RoundedCornerShape(12.dp))
                            .padding(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                bitmap = photoBitmap,
                                contentDescription = "Destination Picture",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "TARGET PHOTO", color = colors.accentYellow, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Text(text = "Visual Match Active", color = colors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────
            // 3. BOTTOM SECTION: DUX ETA & Guidance Card
            // ─────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!hasDestinationSet) {
                        // EXPLORATION / READY STATE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to Navigate",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🗺️ Offline Street Map • Live GPS Connected",
                                    color = colors.accentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap any road on the map to drop a pin, search any place, or insert a photo to navigate.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onOpenWhereTo,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentYellow,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "📍 Where do you want to go now?",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // ACTIVE NAVIGATION STATE
                        // If traveler inserted photo, show reference banner
                        if (targetPhotoUri != null && photoBitmap != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentYellow.copy(alpha = 0.1f))
                                    .border(1.dp, colors.accentYellow.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = photoBitmap,
                                    contentDescription = "Target Picture",
                                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("DESTINATION PHOTO ACTIVE", color = colors.accentYellow, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    Text("Match this entrance image when you arrive", color = colors.textPrimary, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Destination info & ETA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.pedestrianGateName,
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🚶 Dedicated Footpath • Real GPS Active",
                                    color = colors.accentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // ETA & Distance
                            Column(horizontalAlignment = Alignment.End) {
                                val mins = (state.distanceRemainingMeters / 75f).toInt().coerceAtLeast(1)
                                Text(
                                    text = "$mins min",
                                    color = colors.accentYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${state.distanceRemainingMeters.toInt()} meters (~${state.stepPacesRemaining} paces)",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Voice Button
                            Button(
                                onClick = onSpeakNow,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentYellow,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "🔊 Voice Guide",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }

                            // Camera View Button
                            Button(
                                onClick = onToggleCameraFeed,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (colors.isDark) Color(0xFF222222) else Color(0xFFE5E7EB),
                                    contentColor = colors.textPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = if (isCameraFeedVisible) "🗺️ Map" else "📷 Check Gate",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            // Clear / Change Destination Button
                            Button(
                                onClick = onClearDestination,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (colors.isDark) Color(0xFF222222) else Color(0xFFE5E7EB),
                                    contentColor = colors.textPrimary
                                ),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "✕ Change",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
