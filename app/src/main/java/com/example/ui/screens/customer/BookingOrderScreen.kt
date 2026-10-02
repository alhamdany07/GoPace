package com.example.ui.screens.customer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.location.MeraukeLocationManager
import com.example.location.UserLocationInfo
import com.example.ui.components.GoogleMapView
import com.example.ui.components.MapLocationPoint
import com.example.ui.components.MapPinSelectionMode
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// Predefined Merauke Destination Suggestions with verified coordinates
data class DestinationLandmarkPreset(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val defaultBenchmark: String
)

val MERAUKE_DESTINATIONS = listOf(
    DestinationLandmarkPreset(
        "Kampus Unmus Merauke",
        "Jl. Kamizaun, Rimba Jaya",
        -8.5132,
        140.4285,
        "Depan Gedung Rektorat Musamus"
    ),
    DestinationLandmarkPreset(
        "Monumen Kapsul Waktu",
        "Kawasan Pemda, Mandala",
        -8.4947,
        140.4012,
        "Pintu masuk utama monumen depan kantor bupati"
    ),
    DestinationLandmarkPreset(
        "Bandara Mopah Merauke",
        "Jl. Martadinata, Mopah Lama",
        -8.5201,
        140.4172,
        "Area drop-off keberangkatan terminal bandara"
    ),
    DestinationLandmarkPreset(
        "Pasar Wamanggu",
        "Jl. Pasar Baru, Maro",
        -8.4850,
        140.3900,
        "Depan pintu gerbang barat Pasar Wamanggu"
    ),
    DestinationLandmarkPreset(
        "Pelabuhan Yos Sudarso",
        "Jl. Yos Sudarso, Merauke Kota",
        -8.4721,
        140.3831,
        "Pos pintu masuk dermaga Pelni"
    ),
    DestinationLandmarkPreset(
        "Pantai Lampu Satu",
        "Kelurahan Buti, Merauke",
        -8.5195,
        140.3752,
        "Dekat menara suar mercusuar Lampu Satu"
    ),
    DestinationLandmarkPreset(
        "RSUD Merauke",
        "Jl. Soekarjo Wiryopranoto",
        -8.4980,
        140.4080,
        "Depan lobi IGD rumah sakit umum"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingOrderScreen(
    serviceType: ServiceType,
    landmarks: List<MeraukeLandmark>,
    suburbanZones: List<SuburbanZone>,
    walletBalance: Int,
    initialDestination: String = "",
    initialSuburbanZone: SuburbanZone? = null,
    onBack: () -> Unit,
    onOrderCreated: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val userLocation by MeraukeLocationManager.currentLocation.collectAsStateWithLifecycle()

    var activeService by remember { mutableStateOf(serviceType) }
    var pickup by remember(userLocation.areaName) { mutableStateOf(userLocation.fullLabel) }
    var destination by remember { mutableStateOf(initialDestination.ifEmpty { "Monumen Kapsul Waktu Merauke" }) }
    var benchmarkNote by remember { mutableStateOf("Samping kios mama Papua, pagar biru depan pohon pinang") }
    var selectedZone by remember { mutableStateOf<SuburbanZone?>(initialSuburbanZone) }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedPromoCode by remember { mutableStateOf("UNMUSAHEE") }
    var applyPromo by remember { mutableStateOf(true) }

    var mapSelectionMode by remember { mutableStateOf(MapPinSelectionMode.PICKUP) }
    var pickupPoint by remember(userLocation.areaName) {
        mutableStateOf(MapLocationPoint(userLocation.areaName, userLocation.latitude, userLocation.longitude))
    }
    var destinationPoint by remember {
        mutableStateOf(MapLocationPoint("Monumen Kapsul Waktu Merauke", -8.4947, 140.4012))
    }

    fun calculateDistanceKm(p1: MapLocationPoint, p2: MapLocationPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(p1.latitude)) * kotlin.math.cos(Math.toRadians(p2.latitude)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        val dist = r * c
        return (kotlin.math.round(dist * 10) / 10.0).coerceIn(1.2, 50.0)
    }

    val calculatedDistance = remember(pickupPoint, destinationPoint, selectedZone) {
        if (selectedZone != null) selectedZone!!.distanceApproxKm
        else calculateDistanceKm(pickupPoint, destinationPoint)
    }

    fun calculateFareFor(svc: ServiceType): Int {
        val bFare = svc.basePrice
        val dFare = (calculatedDistance * svc.perKmPrice).toInt()
        val sSurcharge = selectedZone?.surcharge ?: 0
        val disc = if (applyPromo) 4000 else 0
        return (bFare + dFare + sSurcharge - disc).coerceAtLeast(5000)
    }

    val totalFare = calculateFareFor(activeService)
    val estimatedMinutes = (calculatedDistance * 2.5 + 3).toInt().coerceAtLeast(4)

    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    val availableServices = listOf(
        ServiceType.PACE_RIDE,
        ServiceType.PACE_CAR,
        ServiceType.PACE_SEND,
        ServiceType.PACE_MART
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Column(modifier = Modifier.widthIn(max = 960.dp)) {
                            Text(
                                text = "Pesan ${activeService.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Layanan Transportasi & Rute Merauke",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 840.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Pembayaran",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Rp ${indonesianFormat.format(totalFare)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = GoPaceGreenDark
                                )
                            }

                            // Payment selector badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedPayment == PaymentMethod.CASH) SoftBackground else GoPaceBlueContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedPayment = if (selectedPayment == PaymentMethod.CASH) {
                                            PaymentMethod.QRIS
                                        } else {
                                            PaymentMethod.CASH
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (selectedPayment == PaymentMethod.CASH) Icons.Default.Payments else Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = if (selectedPayment == PaymentMethod.CASH) GoPaceGreen else GoPaceBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = selectedPayment.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Ganti Cara Bayar",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val newOrder = Order(
                                    serviceType = activeService,
                                    customerName = "Mace Maria Kaize",
                                    customerPhone = "081248001122",
                                    pickupLocation = pickup,
                                    destinationLocation = destination,
                                    benchmarkNote = benchmarkNote,
                                    suburbanZone = selectedZone,
                                    distanceKm = calculatedDistance,
                                    baseFare = activeService.basePrice,
                                    distanceFare = (calculatedDistance * activeService.perKmPrice).toInt(),
                                    suburbanSurcharge = selectedZone?.surcharge ?: 0,
                                    discount = if (applyPromo) 4000 else 0,
                                    totalFare = totalFare,
                                    paymentMethod = selectedPayment,
                                    status = OrderStatus.SEARCHING
                                )
                                onOrderCreated(newOrder)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("order_service_button")
                        ) {
                            Text(
                                text = "Pesan ${activeService.title} Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = SoftBackground
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrWide = maxWidth >= 760.dp

            if (isTabletOrWide) {
                // Two-Pane Supporting Layout for Tablets & Large Foldables
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1040.dp)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Pane: Map and Route Information
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BookingMapCard(
                                pickupPoint = pickupPoint,
                                destinationPoint = destinationPoint,
                                mapSelectionMode = mapSelectionMode,
                                calculatedDistance = calculatedDistance,
                                estimatedMinutes = estimatedMinutes,
                                mapHeight = 340.dp,
                                onModeChange = { mapSelectionMode = it },
                                onPointSelected = { point, mode ->
                                    if (mode == MapPinSelectionMode.PICKUP) {
                                        pickupPoint = point
                                        pickup = point.title
                                    } else {
                                        destinationPoint = point
                                        destination = point.title
                                    }
                                }
                            )

                            BookingSuburbanZoneCard(
                                selectedZone = selectedZone,
                                suburbanZones = suburbanZones,
                                indonesianFormat = indonesianFormat,
                                onSelectZone = { zone ->
                                    selectedZone = zone
                                    if (zone != null) destination = zone.name
                                }
                            )
                        }

                        // Right Pane: Services, Location Inputs, Landmark Note
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                BookingServicesCard(
                                    activeService = activeService,
                                    availableServices = availableServices,
                                    estimatedMinutes = estimatedMinutes,
                                    indonesianFormat = indonesianFormat,
                                    calculateFareFor = { calculateFareFor(it) },
                                    onSelectService = { activeService = it }
                                )
                            }

                            item {
                                BookingLocationInputsCard(
                                    pickup = pickup,
                                    destination = destination,
                                    userLocation = userLocation,
                                    onPickupChange = {
                                        pickup = it
                                        pickupPoint = pickupPoint.copy(title = it)
                                    },
                                    onDestinationChange = {
                                        destination = it
                                        destinationPoint = destinationPoint.copy(title = it)
                                    },
                                    onUseCurrentGps = {
                                        pickup = userLocation.fullLabel
                                        pickupPoint = MapLocationPoint(userLocation.areaName, userLocation.latitude, userLocation.longitude)
                                    },
                                    onSelectLandmark = { preset ->
                                        destination = preset.name
                                        destinationPoint = MapLocationPoint(preset.name, preset.latitude, preset.longitude)
                                        benchmarkNote = preset.defaultBenchmark
                                    }
                                )
                            }

                            item {
                                BookingBenchmarkCard(
                                    benchmarkNote = benchmarkNote,
                                    onBenchmarkChange = { benchmarkNote = it }
                                )
                            }
                        }
                    }
                }
            } else {
                // Single Column Mobile Phone Layout (Fits all screen sizes smoothly)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 620.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            BookingServicesCard(
                                activeService = activeService,
                                availableServices = availableServices,
                                estimatedMinutes = estimatedMinutes,
                                indonesianFormat = indonesianFormat,
                                calculateFareFor = { calculateFareFor(it) },
                                onSelectService = { activeService = it }
                            )
                        }

                        item {
                            BookingMapCard(
                                pickupPoint = pickupPoint,
                                destinationPoint = destinationPoint,
                                mapSelectionMode = mapSelectionMode,
                                calculatedDistance = calculatedDistance,
                                estimatedMinutes = estimatedMinutes,
                                mapHeight = 220.dp,
                                onModeChange = { mapSelectionMode = it },
                                onPointSelected = { point, mode ->
                                    if (mode == MapPinSelectionMode.PICKUP) {
                                        pickupPoint = point
                                        pickup = point.title
                                    } else {
                                        destinationPoint = point
                                        destination = point.title
                                    }
                                }
                            )
                        }

                        item {
                            BookingLocationInputsCard(
                                pickup = pickup,
                                destination = destination,
                                userLocation = userLocation,
                                onPickupChange = {
                                    pickup = it
                                    pickupPoint = pickupPoint.copy(title = it)
                                },
                                onDestinationChange = {
                                    destination = it
                                    destinationPoint = destinationPoint.copy(title = it)
                                },
                                onUseCurrentGps = {
                                    pickup = userLocation.fullLabel
                                    pickupPoint = MapLocationPoint(userLocation.areaName, userLocation.latitude, userLocation.longitude)
                                },
                                onSelectLandmark = { preset ->
                                    destination = preset.name
                                    destinationPoint = MapLocationPoint(preset.name, preset.latitude, preset.longitude)
                                    benchmarkNote = preset.defaultBenchmark
                                }
                            )
                        }

                        item {
                            BookingBenchmarkCard(
                                benchmarkNote = benchmarkNote,
                                onBenchmarkChange = { benchmarkNote = it }
                            )
                        }

                        item {
                            BookingSuburbanZoneCard(
                                selectedZone = selectedZone,
                                suburbanZones = suburbanZones,
                                indonesianFormat = indonesianFormat,
                                onSelectZone = { zone ->
                                    selectedZone = zone
                                    if (zone != null) destination = zone.name
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Modular Reusable Components for Adaptive Display
// -------------------------------------------------------------

@Composable
fun BookingServicesCard(
    activeService: ServiceType,
    availableServices: List<ServiceType>,
    estimatedMinutes: Int,
    indonesianFormat: NumberFormat,
    calculateFareFor: (ServiceType) -> Int,
    onSelectService: (ServiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Layanan GoPace",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Tarif disesuaikan rute",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableServices.forEach { svc ->
                    val isSelected = svc == activeService
                    val fare = calculateFareFor(svc)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GoPaceGreenContainer else SoftBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GoPaceGreen else SoftCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectService(svc) }
                            .testTag("service_option_${svc.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (svc) {
                                    ServiceType.PACE_RIDE -> Icons.Default.TwoWheeler
                                    ServiceType.PACE_CAR -> Icons.Default.DirectionsCar
                                    ServiceType.PACE_SEND -> Icons.Default.LocalShipping
                                    ServiceType.PACE_MART -> Icons.Default.ShoppingBag
                                    else -> Icons.Default.DirectionsBike
                                },
                                contentDescription = svc.title,
                                tint = if (isSelected) GoPaceGreenDark else TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = svc.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GoPaceGreenDark else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Rp ${indonesianFormat.format(fare)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) GoPaceGreenDark else TextSecondary
                            )
                            Text(
                                text = "~${(estimatedMinutes / 3).coerceAtLeast(2)} mnt",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingMapCard(
    pickupPoint: MapLocationPoint,
    destinationPoint: MapLocationPoint,
    mapSelectionMode: MapPinSelectionMode,
    calculatedDistance: Double,
    estimatedMinutes: Int,
    mapHeight: Dp,
    onModeChange: (MapPinSelectionMode) -> Unit,
    onPointSelected: (MapLocationPoint, MapPinSelectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoPaceGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            tint = GoPaceGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Peta Rute Merauke",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                // Distance and estimated time pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoPaceGreenContainer
                ) {
                    Text(
                        text = "📍 $calculatedDistance km • ~$estimatedMinutes mnt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoPaceGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                GoogleMapView(
                    pickupPoint = pickupPoint,
                    destinationPoint = destinationPoint,
                    selectionMode = mapSelectionMode,
                    onModeChange = onModeChange,
                    onPointSelected = onPointSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun BookingLocationInputsCard(
    pickup: String,
    destination: String,
    userLocation: UserLocationInfo,
    onPickupChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onUseCurrentGps: () -> Unit,
    onSelectLandmark: (DestinationLandmarkPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Titik Penjemputan & Tujuan",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Capture Current GPS Pickup Location Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoPaceGreenContainer.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoPaceGreen.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onUseCurrentGps)
                    .testTag("use_current_location_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = GoPaceGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Gunakan Lokasi GPS Saya Saat Ini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoPaceGreenDark
                            )
                            Text(
                                text = userLocation.areaName,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GoPaceGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup Input Field
            OutlinedTextField(
                value = pickup,
                onValueChange = onPickupChange,
                label = { Text("Titik Jemput Anda") },
                leadingIcon = {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = GoPaceGreen)
                },
                trailingIcon = {
                    if (pickup.isNotEmpty()) {
                        IconButton(onClick = { onPickupChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pickup_address_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Destination Input Field
            OutlinedTextField(
                value = destination,
                onValueChange = onDestinationChange,
                label = { Text("Mau Ke Mana? (Tujuan Pengantaran)") },
                leadingIcon = {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFFDE1C24))
                },
                trailingIcon = {
                    if (destination.isNotEmpty()) {
                        IconButton(onClick = { onDestinationChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("destination_address_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Destination Quick Suggestions
            Text(
                text = "Rekomendasi Tujuan Populer Merauke:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MERAUKE_DESTINATIONS) { preset ->
                    val isSelected = destination == preset.name
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFFEF2F2) else SoftBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFDE1C24) else SoftCardBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectLandmark(preset) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFDE1C24) else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = preset.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFDE1C24) else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingBenchmarkCard(
    benchmarkNote: String,
    onBenchmarkChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan Patokan Lokasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFFBEB)
                ) {
                    Text(
                        text = "Khas Merauke",
                        fontSize = 10.sp,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Alamat di Merauke sering menggunakan patokan pohon pinang, warna pagar, atau kios mama.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = benchmarkNote,
                onValueChange = onBenchmarkChange,
                placeholder = { Text("Contoh: Pagar biru samping kios mama Papua, ada pohon pinang") },
                leadingIcon = {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = GoPaceGreen)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick benchmark chips
            val sampleBenchmarks = listOf(
                "Depan pagar kayu biru dekat pohon pinang",
                "Samping kios mama Papua",
                "Masuk lorong samping gereja",
                "Dekat gapura gang"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(sampleBenchmarks) { note ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onBenchmarkChange(note) }
                    ) {
                        Text(
                            text = note,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingSuburbanZoneCard(
    selectedZone: SuburbanZone?,
    suburbanZones: List<SuburbanZone>,
    indonesianFormat: NumberFormat,
    onSelectZone: (SuburbanZone?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Tujuan Menuju Luar Kota / Distrik Pinggiran?",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Text(
                text = "Tarif flat tambahan untuk wilayah Semangga, Tanah Miring, Kurik, & Kumbe",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedZone == null,
                        onClick = { onSelectZone(null) },
                        label = { Text("Dalam Kota Merauke", fontSize = 11.sp) }
                    )
                }
                items(suburbanZones) { zone ->
                    FilterChip(
                        selected = selectedZone?.id == zone.id,
                        onClick = {
                            val newZone = if (selectedZone?.id == zone.id) null else zone
                            onSelectZone(newZone)
                        },
                        label = {
                            Text("${zone.name} (+Rp ${indonesianFormat.format(zone.surcharge)})", fontSize = 11.sp)
                        }
                    )
                }
            }
        }
    }
}
