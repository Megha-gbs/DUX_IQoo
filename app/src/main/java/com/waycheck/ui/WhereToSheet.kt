package com.waycheck.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waycheck.theme.LocalDuxColors

@Composable
fun WhereToGoSheet(
    onSearchClicked: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onTapMapClicked: () -> Unit,
    onAnchorAheadClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDuxColors.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable { onDismiss() }
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}, // prevent dismiss on card tap
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WHERE DO YOU WANT TO GO?",
                            color = colors.accentYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Select how you'd like to set your destination",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (colors.isDark) Color(0xFF222222) else Color(0xFFE2E8F0))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Search Name or Address
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF141416) else Color(0xFFF8FAFC))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable {
                            onDismiss()
                            onSearchClicked()
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔍", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Search Place or Gate Name",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Search nearby entrances, tech parks, metro gates...",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "➔", color = colors.accentYellow, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Insert / Pick Photo (FOR STRANGERS & TRAVELERS)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accentYellow.copy(alpha = 0.12f))
                        .border(1.5.dp, colors.accentYellow.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🖼️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Insert Photo of Destination",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.accentYellow)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "TRAVELER",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = "Have a picture of the gate or building? Pick it and DUX will guide you there.",
                                color = colors.accentYellow,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "➔", color = colors.accentYellow, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: Tap Anywhere on Real Globe Map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF141416) else Color(0xFFF8FAFC))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable {
                            onDismiss()
                            onTapMapClicked()
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌍", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Drop Pin on Real Street Map",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Explore the world map and tap any building or street point",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "➔", color = colors.accentYellow, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 4: Calibrate Ahead
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF141416) else Color(0xFFF8FAFC))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable {
                            onDismiss()
                            onAnchorAheadClicked()
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧭", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calibrate Target 50m Ahead",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sets destination straight in the direction you are pointing",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "➔", color = colors.accentYellow, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
