package com.example.ui.screens.customer

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PacePayBar
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    walletBalance: Int,
    points: Int,
    landmarks: List<MeraukeLandmark>,
    suburbanZones: List<SuburbanZone>,
    merchants: List<Merchant>,
    promos: List<PromoBroadcast>,
    activeOrder: Order?,
    onTopUp: (Int) -> Unit,
    onSelectService: (ServiceType) -> Unit,
    onSelectLandmark: (MeraukeLandmark) -> Unit,
    onSelectSuburbanZone: (SuburbanZone) -> Unit,
    onSelectMerchant: (Merchant) -> Unit,
    onViewActiveOrder: () -> Unit,
    onOpenOrderHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftBackground,
        bottomBar = {
            if (activeOrder != null && activeOrder.status != OrderStatus.COMPLETED && activeOrder.status != OrderStatus.CANCELLED) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 840.dp)) {
                        ActiveOrderBottomBanner(
                            order = activeOrder,
                            onClick = onViewActiveOrder
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            // Search Bar & Greeting
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Selamat Datang di Merauke,",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Mau kemana atau pesan apa hari ini?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Cari sate rusa, kampus Unmus, toko sembako...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = GoPaceGreen
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SoftBackground,
                            unfocusedContainerColor = SoftBackground,
                            focusedIndicatorColor = GoPaceGreen,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Wallet Bar (PacePay)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    PacePayBar(
                        balance = walletBalance,
                        points = points,
                        onTopUp = onTopUp,
                        onOpenHistory = onOpenOrderHistory
                    )
                }
            }

            // Core Services Grid (5 Core Services in Modern Flat Cards)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Layanan Utama GoPace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Semua Wilayah Merauke",
                                fontSize = 11.sp,
                                color = GoPaceGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ServiceGridItem(
                                title = "Pace-Ride",
                                subtitle = "Motor",
                                icon = Icons.Default.TwoWheeler,
                                bgTint = GoPaceGreen,
                                onClick = { onSelectService(ServiceType.PACE_RIDE) }
                            )
                            ServiceGridItem(
                                title = "Pace-Car",
                                subtitle = "Mobil",
                                icon = Icons.Default.DirectionsCar,
                                bgTint = GoPaceGreenLight,
                                onClick = { onSelectService(ServiceType.PACE_CAR) }
                            )
                            ServiceGridItem(
                                title = "Pace-Food",
                                subtitle = "Kuliner",
                                icon = Icons.Default.Restaurant,
                                bgTint = GoPaceOrange,
                                onClick = { onSelectService(ServiceType.PACE_FOOD) }
                            )
                            ServiceGridItem(
                                title = "Pace-Send",
                                subtitle = "Kurir",
                                icon = Icons.Default.LocalShipping,
                                bgTint = GoPaceBlue,
                                onClick = { onSelectService(ServiceType.PACE_SEND) }
                            )
                            ServiceGridItem(
                                title = "Pace-Mart",
                                subtitle = "Jastip",
                                icon = Icons.Default.ShoppingBag,
                                bgTint = Color(0xFF009688),
                                onClick = { onSelectService(ServiceType.PACE_MART) }
                            )
                        }
                    }
                }
            }

            // Merauke-Specific Feature: Zona Khusus Pinggiran
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(GoPaceGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tarif Zona Khusus Pinggiran Merauke",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GoPaceGreenDark
                                )
                                Text(
                                    text = "Tarif transparan flat agar driver antusias ambil rute jauh",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(suburbanZones) { zone ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectSuburbanZone(zone) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = zone.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "+Rp ${indonesianFormat.format(zone.surcharge)} flat",
                                            fontSize = 11.sp,
                                            color = GoPaceGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Shortcuts Lokasi Populer & Bandara/Kampus (Merauke Specific)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shortcut Lokasi Populer Merauke",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "1-Tap Booking",
                            fontSize = 11.sp,
                            color = GoPaceGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(landmarks) { landmark ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                modifier = Modifier
                                    .width(200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectLandmark(landmark) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (landmark.id) {
                                                "unmus" -> Icons.Default.School
                                                "mopah_airport" -> Icons.Default.FlightTakeoff
                                                "pelabuhan_mrk" -> Icons.Default.DirectionsBoat
                                                "rsud_mrk" -> Icons.Default.LocalHospital
                                                "pantai_lampu_satu" -> Icons.Default.WbSunny
                                                else -> Icons.Default.Place
                                            },
                                            contentDescription = null,
                                            tint = GoPaceGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = landmark.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = landmark.area,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Patokan: ${landmark.defaultBenchmark}",
                                        fontSize = 10.sp,
                                        color = GoPaceGreenDark,
                                        lineHeight = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Kuliner Khas Merauke (Pace-Food Highlights)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kuliner Khas Merauke (Pace-Food)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Olahan rusa lokal, kopi Papua, seafood pantai",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(onClick = { onSelectService(ServiceType.PACE_FOOD) }) {
                            Text("Lihat Semua", color = GoPaceGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    merchants.take(4).forEach { merchant ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectMerchant(merchant) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GoPaceOrangeContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = GoPaceOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = merchant.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = merchant.category,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = " ${merchant.rating} • ${merchant.distanceKm} km",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = merchant.specialBadge,
                                            fontSize = 10.sp,
                                            color = GoPaceGreenDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Pesan",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Promo Banners
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Promo & Info Warga Merauke",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(promos) { promo ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = GoPaceGreenContainer),
                                modifier = Modifier.width(260.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoPaceGreen)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "KODE: ${promo.promoCode}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = promo.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GoPaceGreenDark
                                    )
                                    Text(
                                        text = promo.content,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ServiceGridItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun ActiveOrderBottomBanner(
    order: Order,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GoPaceGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (order.serviceType) {
                        ServiceType.PACE_RIDE -> Icons.Default.TwoWheeler
                        ServiceType.PACE_CAR -> Icons.Default.DirectionsCar
                        ServiceType.PACE_FOOD -> Icons.Default.Restaurant
                        ServiceType.PACE_SEND -> Icons.Default.LocalShipping
                        ServiceType.PACE_MART -> Icons.Default.ShoppingBag
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${order.serviceType.title} Aktif",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoPaceGreenContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoPaceGreenDark
                        )
                    }
                }
                Text(
                    text = "Tujuan: ${order.destinationLocation}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Pantau", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
