package com.waycheck.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.waycheck.theme.LocalDuxColors

/**
 * Real Globe Map:
 * Interactive OpenStreetMap (OSM) showing the real world with real streets,
 * buildings, roads, and cities anywhere in India and the world.
 * Zero third-party watermarks.
 * Tracks user's real GPS latitude and longitude in real time.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealGlobeMapView(
    currentLat: Double?,
    currentLng: Double?,
    currentHeading: Float,
    targetLat: Double?,
    targetLng: Double?,
    targetName: String,
    onMapPointSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuxColors.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // HTML template hosting Leaflet + OpenStreetMap
    val htmlContent = remember(colors.isDark) {
        val tileFilter = if (colors.isDark) {
            "filter: brightness(0.65) invert(1) contrast(3) hue-rotate(200deg) saturate(0.35);"
        } else {
            ""
        }
        val bgHex = if (colors.isDark) "#0a0a0c" else "#f3f4f6"

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: $bgHex; }
                .leaflet-tile { $tileFilter }
                .leaflet-control-attribution { display: none !important; }
                .user-puck {
                    width: 22px; height: 22px; background: #007AFF; border: 3px solid #FFFFFF;
                    border-radius: 50%; box-shadow: 0 0 12px rgba(0,122,255,0.7);
                    position: relative;
                }
                .user-heading-cone {
                    width: 0; height: 0;
                    border-left: 7px solid transparent; border-right: 7px solid transparent;
                    border-bottom: 14px solid #FFD200;
                    position: absolute; top: -14px; left: 4px;
                }
                .dest-pin {
                    width: 26px; height: 26px; background: #00E676; border: 3px solid #000000;
                    border-radius: 50%; box-shadow: 0 0 10px rgba(0,230,118,0.8);
                    display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([13.6231, 79.2898], 17);
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: ''
                }).addTo(map);

                var userMarker = null;
                var destMarker = null;
                var routePolyline = null;
                var hasInitialCentered = false;

                map.on('click', function(e) {
                    AndroidBridge.onMapClicked(e.latlng.lat, e.latlng.lng);
                });

                function updateUserLocation(lat, lng, heading) {
                    var latlng = [lat, lng];
                    if (!userMarker) {
                        var puckIcon = L.divIcon({
                            className: 'user-puck-container',
                            html: '<div class="user-puck"><div class="user-heading-cone" id="heading-cone"></div></div>',
                            iconSize: [24, 24],
                            iconAnchor: [12, 12]
                        });
                        userMarker = L.marker(latlng, { icon: puckIcon }).addTo(map);
                    } else {
                        userMarker.setLatLng(latlng);
                    }

                    var cone = document.getElementById('heading-cone');
                    if (cone) {
                        cone.style.transform = 'rotate(' + heading + 'deg)';
                    }

                    if (!hasInitialCentered) {
                        hasInitialCentered = true;
                        map.setView(latlng, 17);
                    }
                    updateRoute();
                }

                function updateDestination(lat, lng, name) {
                    var destLatLng = [lat, lng];
                    if (!destMarker) {
                        var destIcon = L.divIcon({
                            className: 'dest-pin-container',
                            html: '<div class="dest-pin">📍</div>',
                            iconSize: [28, 28],
                            iconAnchor: [14, 14]
                        });
                        destMarker = L.marker(destLatLng, { icon: destIcon }).addTo(map);
                    } else {
                        destMarker.setLatLng(destLatLng);
                    }
                    updateRoute();
                }

                function updateRoute() {
                    if (userMarker && destMarker) {
                        var p1 = userMarker.getLatLng();
                        var p2 = destMarker.getLatLng();
                        if (routePolyline) {
                            routePolyline.setLatLngs([p1, p2]);
                        } else {
                            routePolyline = L.polyline([p1, p2], {
                                color: '#FFD200',
                                weight: 5,
                                opacity: 0.9,
                                dashArray: '10, 8'
                            }).addTo(map);
                        }
                    }
                }

                function clearDestination() {
                    if (destMarker) {
                        map.removeLayer(destMarker);
                        destMarker = null;
                    }
                    if (routePolyline) {
                        map.removeLayer(routePolyline);
                        routePolyline = null;
                    }
                }

                function centerOnUser() {
                    if (userMarker) {
                        map.setView(userMarker.getLatLng(), 18);
                    }
                }

                AndroidBridge.onMapReady();
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    // Push live GPS updates to Leaflet
    LaunchedEffect(currentLat, currentLng, currentHeading, isMapLoaded) {
        if (isMapLoaded && currentLat != null && currentLng != null && webViewRef != null) {
            val script = "updateUserLocation($currentLat, $currentLng, $currentHeading);"
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // Push destination updates to Leaflet (or clear if none set)
    LaunchedEffect(targetLat, targetLng, targetName, isMapLoaded) {
        if (isMapLoaded && webViewRef != null) {
            if (targetLat != null && targetLng != null) {
                val script = "updateDestination($targetLat, $targetLng, '${targetName.replace("'", "")}');"
                webViewRef?.evaluateJavascript(script, null)
            } else {
                webViewRef?.evaluateJavascript("clearDestination();", null)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (colors.isDark) Color(0xFF0D0D10) else Color(0xFFF3F4F6))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.loadsImagesAutomatically = true

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onMapClicked(lat: Double, lng: Double) {
                            onMapPointSelected(lat, lng)
                        }

                        @JavascriptInterface
                        fun onMapReady() {
                            isMapLoaded = true
                        }
                    }, "AndroidBridge")

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                        }
                    }

                    loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating "Re-center on My Location" FAB
        FloatingActionButton(
            onClick = {
                webViewRef?.evaluateJavascript("centerOnUser();", null)
            },
            shape = CircleShape,
            containerColor = colors.surface,
            contentColor = colors.accentYellow,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(44.dp)
        ) {
            Text(text = "🎯", fontSize = 18.sp)
        }

        // Tap hint at top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface.copy(alpha = 0.90f))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🌍 Real Street Map • Tap anywhere to navigate",
                color = colors.textPrimary,
                fontSize = 10.sp
            )
        }
    }
}
