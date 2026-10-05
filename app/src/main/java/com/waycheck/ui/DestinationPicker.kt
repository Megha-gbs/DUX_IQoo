package com.waycheck.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.waycheck.engine.DestinationConfig
import com.waycheck.engine.LandmarkItem

val PredefinedDestinations = listOf(
    DestinationConfig(
        id = "gate_2",
        name = "Pedestrian Gate 2 (Auditorium)",
        subtitle = "Main Convention Centre Entrance",
        targetLat = 13.6231,
        targetLng = 79.2898,
        expectedHeading = 45f,
        pedestrianGateName = "Pedestrian Gate 2",
        totalDistanceMeters = 55f,
        landmarks = listOf(
            LandmarkItem("lm_security", "Security Post / Cabin", "Pass guard cabin on your left", 18f, -15f, "👮"),
            LandmarkItem("lm_walkway", "Paved Tree Footpath", "Walk straight along shaded tiles", 35f, 5f, "🌳"),
            LandmarkItem("lm_gate", "Gate 2 Pedestrian Turnstile", "Entry turnstile straight ahead", 55f, 0f, "🚪"),
            LandmarkItem("lm_hazard", "Vehicle Entry Ramp", "Car & bus lane • Avoid", 22f, 135f, "⛔", isHazard = true)
        )
    ),
    DestinationConfig(
        id = "metro_gate_a",
        name = "Metro Pillar 42 Subway Entry",
        subtitle = "Blue Line Pedestrian Subway",
        targetLat = 13.6235,
        targetLng = 79.2890,
        expectedHeading = 80f,
        pedestrianGateName = "Subway Gate A",
        totalDistanceMeters = 65f,
        landmarks = listOf(
            LandmarkItem("lm_tea", "Chai Kiosk / Corner", "Keep tea stall on right", 20f, 10f, "☕"),
            LandmarkItem("lm_pillar", "Metro Pillar 42", "Walk past pillar to stairs", 45f, 0f, "🚇"),
            LandmarkItem("lm_gate", "Subway Gate A Stairs", "Take downstairs pedestrian entry", 65f, 0f, "🚪"),
            LandmarkItem("lm_hazard", "Bus Rapid Lane", "Heavy traffic lane • Do not cross", 25f, 90f, "⛔", isHazard = true)
        )
    ),
    DestinationConfig(
        id = "hospital_block",
        name = "Hospital Block A Walkway",
        subtitle = "Patient & Visitor Entrance",
        targetLat = 13.6228,
        targetLng = 79.2905,
        expectedHeading = 120f,
        pedestrianGateName = "Block A Visitor Gate",
        totalDistanceMeters = 40f,
        landmarks = listOf(
            LandmarkItem("lm_reception", "Outpatient Reception Post", "Walk past reception counter", 15f, -5f, "🏥"),
            LandmarkItem("lm_ramp", "Wheelchair Pedestrian Ramp", "Follow gently sloped ramp", 30f, 0f, "♿"),
            LandmarkItem("lm_gate", "Block A Glass Sliding Doors", "Automatic sensor door entrance", 40f, 0f, "🚪"),
            LandmarkItem("lm_hazard", "Ambulance Emergency Bay", "Emergency vehicles only", 18f, -90f, "⛔", isHazard = true)
        )
    )
)

@Composable
fun DestinationPickerCard(
    currentDestination: DestinationConfig,
    onSelectDestination: (DestinationConfig) -> Unit,
    onAnchorCurrent: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT DESTINATION",
                    color = Color(0xFFFFD200),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "✕ Close",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Anchor Ahead Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFD200))
                    .clickable {
                        onAnchorCurrent()
                        onDismiss()
                    }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧭", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Calibrate Target Directly Ahead (55m)",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sets destination along whichever direction you are currently facing",
                            color = Color(0xFF333333),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "POPULAR LOCAL ENTRANCES",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            PredefinedDestinations.forEach { dest ->
                val isSelected = dest.id == currentDestination.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF222634) else Color(0xFF181B24))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFFFD200) else Color(0xFF2A2E3D),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onSelectDestination(dest)
                            onDismiss()
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dest.name,
                                color = if (isSelected) Color(0xFFFFD200) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dest.subtitle,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${dest.totalDistanceMeters.toInt()}m",
                            color = Color(0xFFFFD200),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
