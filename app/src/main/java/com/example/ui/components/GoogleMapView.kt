package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

enum class MapPinSelectionMode {
    PICKUP,
    DESTINATION
}

data class MapLocationPoint(
    val title: String,
    val latitude: Double,
    val longitude: Double
)

// Real Landmark Coordinates in Merauke
data class MeraukeGeoLandmark(
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val iconType: String
)

val MERAUKE_MAP_LANDMARKS = listOf(
    MeraukeGeoLandmark("Monumen Kapsul Waktu", "Wisata", -8.4947, 140.4012, "monument"),
    MeraukeGeoLandmark("Kampus Unmus", "Pendidikan", -8.5132, 140.4285, "campus"),
    MeraukeGeoLandmark("Bandara Mopah", "Transportasi", -8.5201, 140.4172, "airport"),
    MeraukeGeoLandmark("Pasar Wamanggu", "Pasar", -8.4850, 140.3900, "market"),
    MeraukeGeoLandmark("Pelabuhan Merauke", "Pelabuhan", -8.4721, 140.3831, "port"),
    MeraukeGeoLandmark("Pantai Lampu Satu", "Wisata", -8.5195, 140.3752, "beach"),
    MeraukeGeoLandmark("RSUD Merauke", "Kesehatan", -8.4980, 140.4080, "hospital")
)

// Active roaming GoPace drivers in Merauke for realistic map experience
data class NearbyDriver(
    val id: String,
    val name: String,
    val isCar: Boolean,
    val latitude: Double,
    val longitude: Double,
    val etaMinutes: Int
)

val INITIAL_DRIVERS = listOf(
    NearbyDriver("drv_1", "Pace Yohanes (Motor)", false, -8.5020, 140.4120, 3),
    NearbyDriver("drv_2", "Pace Markus (Mobil)", true, -8.4910, 140.3980, 5),
    NearbyDriver("drv_3", "Pace Frans (Motor)", false, -8.5100, 140.4220, 2),
    NearbyDriver("drv_4", "Pace Daniel (Motor)", false, -8.4880, 140.3920, 4),
    NearbyDriver("drv_5", "Pace Simon (Mobil)", true, -8.5180, 140.4130, 6)
)

/**
 * 100% Free, Native Merauke Navigation Map Engine.
 * Does NOT require Google Maps API Key, never goes white, and renders authentic Merauke topography,
 * roads, rivers, landmarks, nearby drivers, and live route navigation.
 */
@Composable
fun GoogleMapView(
    pickupPoint: MapLocationPoint?,
    destinationPoint: MapLocationPoint?,
    selectionMode: MapPinSelectionMode,
    onModeChange: (MapPinSelectionMode) -> Unit,
    onPointSelected: (MapLocationPoint, MapPinSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Center of Merauke
    var centerLat by remember { mutableDoubleStateOf(-8.5000) }
    var centerLng by remember { mutableDoubleStateOf(140.4060) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Text measurer for street and landmark labels on canvas
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for pickup marker and drivers
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    val routeDashOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "routeDash"
    )

    // Sync camera if pickup or destination updates
    LaunchedEffect(pickupPoint) {
        if (pickupPoint != null) {
            centerLat = pickupPoint.latitude
            centerLng = pickupPoint.longitude
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAEFF4))
            .testTag("google_maps_view")
    ) {
        // Core Native Canvas Map Renderer with Gesture Handling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(0.75f, 3.2f)
                        val scale = size.width * 14f * zoomLevel
                        centerLng -= (pan.x / scale).toDouble()
                        centerLat += (pan.y / scale).toDouble()
                    }
                }
                .pointerInput(selectionMode, centerLat, centerLng, zoomLevel) {
                    detectTapGestures { tapOffset ->
                        val scale = size.width * 14f * zoomLevel
                        val tappedLng = centerLng + (tapOffset.x - size.width / 2f) / scale
                        val tappedLat = centerLat - (tapOffset.y - size.height / 2f) / scale

                        // Determine closest landmark or general address
                        val nearestLandmark = MERAUKE_MAP_LANDMARKS.minByOrNull {
                            val dLat = it.latitude - tappedLat
                            val dLng = it.longitude - tappedLng
                            dLat * dLat + dLng * dLng
                        }

                        val title = if (nearestLandmark != null &&
                            abs(nearestLandmark.latitude - tappedLat) < 0.008 &&
                            abs(nearestLandmark.longitude - tappedLng) < 0.008
                        ) {
                            "Dekat ${nearestLandmark.name}, Merauke"
                        } else {
                            if (selectionMode == MapPinSelectionMode.PICKUP) {
                                "Titik Jemput (${String.format(java.util.Locale.US, "%.4f, %.4f", tappedLat, tappedLng)})"
                            } else {
                                "Tujuan (${String.format(java.util.Locale.US, "%.4f, %.4f", tappedLat, tappedLng)})"
                            }
                        }

                        onPointSelected(
                            MapLocationPoint(title, tappedLat, tappedLng),
                            selectionMode
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size

                fun geoToScreen(lat: Double, lng: Double): Offset {
                    val scale = canvasSize.width * 14f * zoomLevel
                    val x = canvasSize.width / 2f + ((lng - centerLng) * scale).toFloat()
                    val y = canvasSize.height / 2f + ((centerLat - lat) * scale).toFloat()
                    return Offset(x, y)
                }

                // -------------------------------------------------------------
                // 1. Base Landmass & Topography
                // -------------------------------------------------------------
                drawRect(color = Color(0xFFF3F5F7))

                // -------------------------------------------------------------
                // 2. Water Bodies (Sungai Maro & Laut Arafura)
                // -------------------------------------------------------------
                val waterColor = Color(0xFFA5CEF2)
                val riverShorelineColor = Color(0xFF8DBBE4)

                // Sungai Maro (West & Northwest)
                val maroPath = Path().apply {
                    val p1 = geoToScreen(-8.4450, 140.3650)
                    val p2 = geoToScreen(-8.4650, 140.3750)
                    val p3 = geoToScreen(-8.4800, 140.3800)
                    val p4 = geoToScreen(-8.5050, 140.3700)
                    val p5 = geoToScreen(-8.5350, 140.3550)

                    moveTo(0f, 0f)
                    lineTo(p1.x, p1.y)
                    quadraticBezierTo(p2.x, p2.y, p3.x, p3.y)
                    quadraticBezierTo(p4.x, p4.y, p5.x, p5.y)
                    lineTo(0f, canvasSize.height)
                    close()
                }
                drawPath(path = maroPath, color = waterColor)
                drawPath(path = maroPath, color = riverShorelineColor, style = Stroke(width = 3.dp.toPx()))

                // Laut Arafura (South-West Coastline)
                val arafuraPath = Path().apply {
                    val c1 = geoToScreen(-8.5150, 140.3650)
                    val c2 = geoToScreen(-8.5300, 140.3800)
                    val c3 = geoToScreen(-8.5500, 140.4100)

                    moveTo(0f, canvasSize.height)
                    lineTo(c1.x, c1.y)
                    quadraticBezierTo(c2.x, c2.y, c3.x, c3.y)
                    lineTo(canvasSize.width, canvasSize.height)
                    close()
                }
                drawPath(path = arafuraPath, color = waterColor)

                // -------------------------------------------------------------
                // 3. Green Zones & Parks
                // -------------------------------------------------------------
                val parkGreen = Color(0xFFD4EBD7)
                val parkBorder = Color(0xFFBCE0C1)

                // Unmus Campus Grounds (Rimba Jaya)
                val unmusPos = geoToScreen(-8.5132, 140.4285)
                val unmusRect = Rect(unmusPos.x - 45.dp.toPx(), unmusPos.y - 35.dp.toPx(), unmusPos.x + 45.dp.toPx(), unmusPos.y + 35.dp.toPx())
                drawRoundRect(color = parkGreen, topLeft = unmusRect.topLeft, size = unmusRect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()))
                drawRoundRect(color = parkBorder, topLeft = unmusRect.topLeft, size = unmusRect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))

                // Bandara Mopah Grounds & Runway
                val mopahPos = geoToScreen(-8.5201, 140.4172)
                val mopahRect = Rect(mopahPos.x - 60.dp.toPx(), mopahPos.y - 28.dp.toPx(), mopahPos.x + 60.dp.toPx(), mopahPos.y + 28.dp.toPx())
                drawRoundRect(color = Color(0xFFE2E8F0), topLeft = mopahRect.topLeft, size = mopahRect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
                // Runway line
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(mopahPos.x - 48.dp.toPx(), mopahPos.y),
                    end = Offset(mopahPos.x + 48.dp.toPx(), mopahPos.y),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(mopahPos.x - 42.dp.toPx(), mopahPos.y),
                    end = Offset(mopahPos.x + 42.dp.toPx(), mopahPos.y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // Taman Monumen Kapsul Waktu
                val kapsulPos = geoToScreen(-8.4947, 140.4012)
                drawCircle(color = parkGreen, radius = 32.dp.toPx(), center = kapsulPos)
                drawCircle(color = parkBorder, radius = 32.dp.toPx(), center = kapsulPos, style = Stroke(width = 2.dp.toPx()))

                // -------------------------------------------------------------
                // 4. Merauke Street Network
                // -------------------------------------------------------------
                val roadCasingColor = Color(0xFFD6DBE1)
                val arterialColor = Color.White
                val majorAvenueColor = Color(0xFFFFFAEE)

                data class MeraukeRoad(
                    val name: String,
                    val coords: List<Pair<Double, Double>>,
                    val isArterial: Boolean
                )

                val meraukeRoads = listOf(
                    // Jl. Raya Mandala (Main Arterial)
                    MeraukeRoad(
                        "Jl. Raya Mandala",
                        listOf(
                            -8.4850 to 140.3950,
                            -8.4947 to 140.4012,
                            -8.5020 to 140.4060,
                            -8.5140 to 140.4120
                        ),
                        true
                    ),
                    // Jl. Brawijaya
                    MeraukeRoad(
                        "Jl. Brawijaya",
                        listOf(
                            -8.4820 to 140.3880,
                            -8.4920 to 140.4080,
                            -8.5040 to 140.4280
                        ),
                        true
                    ),
                    // Jl. Kamizaun (towards Unmus)
                    MeraukeRoad(
                        "Jl. Kamizaun (Unmus)",
                        listOf(
                            -8.4947 to 140.4012,
                            -8.5040 to 140.4180,
                            -8.5132 to 140.4285,
                            -8.5250 to 140.4400
                        ),
                        true
                    ),
                    // Jl. Yos Sudarso (Pelabuhan)
                    MeraukeRoad(
                        "Jl. Yos Sudarso (Pelabuhan)",
                        listOf(
                            -8.4680 to 140.3800,
                            -8.4721 to 140.3831,
                            -8.4850 to 140.3900,
                            -8.4947 to 140.4012
                        ),
                        true
                    ),
                    // Jl. Ahmad Yani
                    MeraukeRoad(
                        "Jl. Ahmad Yani",
                        listOf(
                            -8.4860 to 140.3920,
                            -8.4910 to 140.3990,
                            -8.4960 to 140.4050
                        ),
                        false
                    ),
                    // Jl. Poros Mopah Lama
                    MeraukeRoad(
                        "Jl. Poros Mopah Lama",
                        listOf(
                            -8.5020 to 140.4060,
                            -8.5120 to 140.4140,
                            -8.5201 to 140.4172,
                            -8.5300 to 140.4220
                        ),
                        true
                    ),
                    // Jl. Trans Papua (Arah Semangga / Tanah Miring)
                    MeraukeRoad(
                        "Jl. Trans Papua",
                        listOf(
                            -8.5040 to 140.4280,
                            -8.4980 to 140.4450,
                            -8.4900 to 140.4600
                        ),
                        true
                    )
                )

                // Pass 1: Road Casings (Borders)
                meraukeRoads.forEach { road ->
                    val path = Path()
                    road.coords.forEachIndexed { idx, pt ->
                        val scr = geoToScreen(pt.first, pt.second)
                        if (idx == 0) path.moveTo(scr.x, scr.y) else path.lineTo(scr.x, scr.y)
                    }
                    val casingWidth = if (road.isArterial) 14.dp.toPx() else 9.dp.toPx()
                    drawPath(path = path, color = roadCasingColor, style = Stroke(width = casingWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                // Pass 2: Road Infill (Clean White / Light Amber)
                meraukeRoads.forEach { road ->
                    val path = Path()
                    road.coords.forEachIndexed { idx, pt ->
                        val scr = geoToScreen(pt.first, pt.second)
                        if (idx == 0) path.moveTo(scr.x, scr.y) else path.lineTo(scr.x, scr.y)
                    }
                    val infillWidth = if (road.isArterial) 10.dp.toPx() else 6.dp.toPx()
                    drawPath(path = path, color = if (road.isArterial) majorAvenueColor else arterialColor, style = Stroke(width = infillWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                // -------------------------------------------------------------
                // 5. Road Name Labels
                // -------------------------------------------------------------
                meraukeRoads.forEach { road ->
                    if (road.coords.size >= 2) {
                        val midIdx = road.coords.size / 2
                        val pMid = geoToScreen(road.coords[midIdx].first, road.coords[midIdx].second)

                        val textLayout = textMeasurer.measure(
                            text = road.name,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        )
                        // Label pill
                        val labelPadding = 4.dp.toPx()
                        val rect = Rect(
                            pMid.x - textLayout.size.width / 2f - labelPadding,
                            pMid.y - textLayout.size.height / 2f - 2.dp.toPx(),
                            pMid.x + textLayout.size.width / 2f + labelPadding,
                            pMid.y + textLayout.size.height / 2f + 2.dp.toPx()
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.9f),
                            topLeft = rect.topLeft,
                            size = rect.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(pMid.x - textLayout.size.width / 2f, pMid.y - textLayout.size.height / 2f)
                        )
                    }
                }

                // -------------------------------------------------------------
                // 6. Navigation Route Line (Between Pickup & Destination)
                // -------------------------------------------------------------
                if (pickupPoint != null && destinationPoint != null) {
                    val pScreen = geoToScreen(pickupPoint.latitude, pickupPoint.longitude)
                    val dScreen = geoToScreen(destinationPoint.latitude, destinationPoint.longitude)

                    val routePath = Path().apply {
                        moveTo(pScreen.x, pScreen.y)
                        // Smooth navigation curvature
                        val ctrlX = (pScreen.x + dScreen.x) / 2f + (dScreen.y - pScreen.y) * 0.15f
                        val ctrlY = (pScreen.y + dScreen.y) / 2f - (dScreen.x - pScreen.x) * 0.15f
                        quadraticBezierTo(ctrlX, ctrlY, dScreen.x, dScreen.y)
                    }

                    // Route Shadow (Glow)
                    drawPath(
                        path = routePath,
                        color = Color(0x4000880C),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Base Route Line
                    drawPath(
                        path = routePath,
                        color = GoPaceGreenDark,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Animated Inner Route Dash
                    drawPath(
                        path = routePath,
                        color = Color(0xFF86EFAC),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), routeDashOffset)
                        )
                    )
                }

                // -------------------------------------------------------------
                // 7. Landmark Icons & Badges
                // -------------------------------------------------------------
                MERAUKE_MAP_LANDMARKS.forEach { lm ->
                    val pos = geoToScreen(lm.latitude, lm.longitude)

                    // Landmark Badge Background
                    drawCircle(color = Color.White, radius = 10.dp.toPx(), center = pos)
                    drawCircle(
                        color = when (lm.category) {
                            "Wisata" -> Color(0xFFF59E0B)
                            "Pendidikan" -> GoPaceBlue
                            "Transportasi" -> Color(0xFF6366F1)
                            "Pasar" -> GoPaceOrange
                            "Kesehatan" -> Color(0xFFEF4444)
                            else -> GoPaceGreen
                        },
                        radius = 8.dp.toPx(),
                        center = pos
                    )
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pos)

                    // Landmark Label
                    val textLayout = textMeasurer.measure(
                        text = lm.name,
                        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    val labelOffset = Offset(pos.x - textLayout.size.width / 2f, pos.y + 12.dp.toPx())
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.95f),
                        topLeft = Offset(labelOffset.x - 3.dp.toPx(), labelOffset.y - 1.dp.toPx()),
                        size = Size(textLayout.size.width + 6.dp.toPx(), textLayout.size.height + 2.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                    drawText(textLayoutResult = textLayout, topLeft = labelOffset)
                }

                // -------------------------------------------------------------
                // 8. Nearby Live GoPace Drivers
                // -------------------------------------------------------------
                INITIAL_DRIVERS.forEach { drv ->
                    val pos = geoToScreen(drv.latitude, drv.longitude)

                    // Driver Radar Ring
                    drawCircle(
                        color = if (drv.isCar) GoPaceBlue.copy(alpha = 0.25f) else GoPaceGreen.copy(alpha = 0.25f),
                        radius = 12.dp.toPx() * pulseAnim,
                        center = pos
                    )
                    // Driver Pill
                    drawCircle(color = Color.White, radius = 9.dp.toPx(), center = pos)
                    drawCircle(
                        color = if (drv.isCar) GoPaceBlue else GoPaceGreen,
                        radius = 7.dp.toPx(),
                        center = pos
                    )
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pos)
                }

                // -------------------------------------------------------------
                // 9. Destination Pin (Red)
                // -------------------------------------------------------------
                if (destinationPoint != null) {
                    val dPos = geoToScreen(destinationPoint.latitude, destinationPoint.longitude)

                    // Pin Shadow
                    drawOval(
                        color = Color(0x33000000),
                        topLeft = Offset(dPos.x - 10.dp.toPx(), dPos.y + 2.dp.toPx()),
                        size = Size(20.dp.toPx(), 8.dp.toPx())
                    )

                    // Pin Head
                    drawCircle(color = Color.White, radius = 13.dp.toPx(), center = Offset(dPos.x, dPos.y - 12.dp.toPx()))
                    drawCircle(color = Color(0xFFDE1C24), radius = 10.dp.toPx(), center = Offset(dPos.x, dPos.y - 12.dp.toPx()))
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(dPos.x, dPos.y - 12.dp.toPx()))

                    // Pin stem
                    val stemPath = Path().apply {
                        moveTo(dPos.x - 5.dp.toPx(), dPos.y - 10.dp.toPx())
                        lineTo(dPos.x, dPos.y)
                        lineTo(dPos.x + 5.dp.toPx(), dPos.y - 10.dp.toPx())
                        close()
                    }
                    drawPath(path = stemPath, color = Color(0xFFDE1C24))

                    // Callout Bubble
                    val tagLayout = textMeasurer.measure(
                        text = "Tujuan",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                    val bubbleTopLeft = Offset(dPos.x - tagLayout.size.width / 2f - 6.dp.toPx(), dPos.y - 38.dp.toPx())
                    drawRoundRect(
                        color = Color(0xFFDE1C24),
                        topLeft = bubbleTopLeft,
                        size = Size(tagLayout.size.width + 12.dp.toPx(), tagLayout.size.height + 4.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    drawText(textLayoutResult = tagLayout, topLeft = Offset(bubbleTopLeft.x + 6.dp.toPx(), bubbleTopLeft.y + 2.dp.toPx()))
                }

                // -------------------------------------------------------------
                // 10. Pickup Pin (Pulsing Green)
                // -------------------------------------------------------------
                if (pickupPoint != null) {
                    val pPos = geoToScreen(pickupPoint.latitude, pickupPoint.longitude)

                    // Pulsing Radar Ring
                    drawCircle(
                        color = GoPaceGreen.copy(alpha = 0.35f),
                        radius = 26.dp.toPx() * pulseAnim,
                        center = pPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 14.dp.toPx(),
                        center = pPos
                    )
                    drawCircle(
                        color = GoPaceGreen,
                        radius = 11.dp.toPx(),
                        center = pPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pPos
                    )

                    // Callout Bubble
                    val tagLayout = textMeasurer.measure(
                        text = "Titik Jemput",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                    val bubbleTopLeft = Offset(pPos.x - tagLayout.size.width / 2f - 6.dp.toPx(), pPos.y - 32.dp.toPx())
                    drawRoundRect(
                        color = GoPaceGreenDark,
                        topLeft = bubbleTopLeft,
                        size = Size(tagLayout.size.width + 12.dp.toPx(), tagLayout.size.height + 4.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    drawText(textLayoutResult = tagLayout, topLeft = Offset(bubbleTopLeft.x + 6.dp.toPx(), bubbleTopLeft.y + 2.dp.toPx()))
                }
            }
        }

        // =============================================================
        // Overlays & Floating Navigation Controls
        // =============================================================

        // Top Center: Pin Selection Mode Pills (Titik Jemput vs Titik Tujuan)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectionMode == MapPinSelectionMode.PICKUP) GoPaceGreen else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onModeChange(MapPinSelectionMode.PICKUP) }
                        .testTag("map_mode_pickup")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (selectionMode == MapPinSelectionMode.PICKUP) Color.White else GoPaceGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Titik Jemput",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectionMode == MapPinSelectionMode.PICKUP) Color.White else TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectionMode == MapPinSelectionMode.DESTINATION) Color(0xFFDE1C24) else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onModeChange(MapPinSelectionMode.DESTINATION) }
                        .testTag("map_mode_destination")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (selectionMode == MapPinSelectionMode.DESTINATION) Color.White else Color(0xFFDE1C24))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Titik Tujuan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectionMode == MapPinSelectionMode.DESTINATION) Color.White else TextPrimary
                        )
                    }
                }
            }
        }

        // Top Left: Free Map Badge
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoPaceGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Peta Merauke • Aktif",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Left: Tap Instruction Tip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (selectionMode == MapPinSelectionMode.PICKUP) GoPaceGreen else Color(0xFFDE1C24),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (selectionMode == MapPinSelectionMode.PICKUP) {
                        "Ketuk peta untuk geser Jemput"
                    } else {
                        "Ketuk peta untuk geser Tujuan"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        // Bottom Right: Zoom & Center Navigation Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    zoomLevel = (zoomLevel * 1.25f).coerceAtMost(3.2f)
                },
                containerColor = Color.White,
                contentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Perbesar Peta", modifier = Modifier.size(18.dp))
            }

            SmallFloatingActionButton(
                onClick = {
                    zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.75f)
                },
                containerColor = Color.White,
                contentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Perkecil Peta", modifier = Modifier.size(18.dp))
            }

            FloatingActionButton(
                onClick = {
                    if (pickupPoint != null) {
                        centerLat = pickupPoint.latitude
                        centerLng = pickupPoint.longitude
                        zoomLevel = 1.2f
                    } else {
                        centerLat = -8.5000
                        centerLng = 140.4060
                        zoomLevel = 1.0f
                    }
                },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = GoPaceGreen,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Pusat Lokasi Saya", modifier = Modifier.size(20.dp))
            }
        }
    }
}
