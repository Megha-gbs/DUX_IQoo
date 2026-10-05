package com.waycheck.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waycheck.data.DestinationsRepository
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.LandmarkItem
import com.waycheck.theme.LocalDuxColors

@Composable
fun SearchDestinationSheet(
    onSelectForConfirmation: (DestinationConfig) -> Unit,
    onAnchorCurrentHeading: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalDuxColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Tech Park", "Metro")

    val matchingDestinations = remember(searchQuery, selectedCategory) {
        DestinationsRepository.search(searchQuery).filter {
            if (selectedCategory == "All") true else it.category == selectedCategory
        }
    }

    // Full Screen Overlay Modal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable { onDismiss() }
            .padding(horizontal = 14.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}, // prevent dismiss when clicking inside card
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEARCH NEARBY ENTRANCES",
                        color = colors.accentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
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

                Spacer(modifier = Modifier.height(12.dp))

                // SEARCH TEXT INPUT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (colors.isDark) Color(0xFF1E1E1E) else Color(0xFFF1F5F9))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔍", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search gate, metro exit, tech park...",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colors.accentYellow),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CATEGORY CHIPS
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) colors.accentYellow else if (colors.isDark) Color(0xFF222222) else Color(0xFFE2E8F0))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ANCHOR FORWARD BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF1E1E1E) else Color(0xFFE5E7EB))
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable {
                            onAnchorCurrentHeading()
                            onDismiss()
                        }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧭", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Anchor Destination Ahead (55m)",
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Calibrate target right along your current heading",
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VERIFIED NEARBY ENTRANCES (${matchingDestinations.size})",
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // RESULTS LIST
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (searchQuery.isNotBlank() && matchingDestinations.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.accentYellow.copy(alpha = 0.15f))
                                    .border(1.dp, colors.accentYellow, RoundedCornerShape(14.dp))
                                    .clickable {
                                        val custom = DestinationConfig(
                                            id = "custom_${System.currentTimeMillis()}",
                                            name = searchQuery.trim(),
                                            subtitle = "Custom Search Destination",
                                            category = "Search",
                                            targetLat = 13.6231,
                                            targetLng = 79.2898,
                                            expectedHeading = 0f,
                                            pedestrianGateName = searchQuery.trim(),
                                            totalDistanceMeters = 70f,
                                            photoCaption = "Search destination: ${searchQuery.trim()}",
                                            landmarks = listOf(
                                                LandmarkItem("lm_custom", searchQuery.trim(), "Follow route towards ${searchQuery.trim()}", 70f, 0f, "📍")
                                            )
                                        )
                                        onSelectForConfirmation(custom)
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📍", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Navigate to \"${searchQuery.trim()}\"",
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap to set as destination route",
                                            color = colors.accentYellow,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(text = "➔", color = colors.accentYellow, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    items(matchingDestinations) { dest ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (colors.isDark) Color(0xFF141414) else Color(0xFFF8FAFC))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .clickable {
                                    // Open photo confirmation for this destination!
                                    onSelectForConfirmation(dest)
                                }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail of exact photo
                                if (dest.imageRes != null) {
                                    Image(
                                        painter = painterResource(id = dest.imageRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accentYellow),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🚪", fontSize = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dest.name,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${dest.category} • ${dest.totalDistanceMeters.toInt()}m",
                                        color = colors.accentYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tap to verify area picture & start",
                                        color = colors.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = "➔",
                                    color = colors.textSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
