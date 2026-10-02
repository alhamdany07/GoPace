package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.GoPaceRepository
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    orders: List<Order>,
    driverProfile: DriverProfile,
    fareConfig: GoPaceRepository.DynamicFareConfig,
    driverApplications: List<DriverDocumentApplication>,
    promos: List<PromoBroadcast>,
    onToggleRainSurge: () -> Unit,
    onToggleNightSurge: () -> Unit,
    onUpdateFareConfig: (GoPaceRepository.DynamicFareConfig) -> Unit,
    onApproveDriver: (String) -> Unit,
    onRejectDriver: (String) -> Unit,
    onAddPromo: (PromoBroadcast) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var selectedTab by remember { mutableStateOf(0) }
    var showPromoDialog by remember { mutableStateOf(false) }

    val activeOrdersCount = orders.count {
        it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
    }
    val completedOrdersCount = orders.count { it.status == OrderStatus.COMPLETED }
    val totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalFare }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = GoPaceGreen
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Monitor", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Tarif Dinamis", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Verifikasi Mitra", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Broadcast Promo", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: LIVE MONITORING
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Monitoring Armada & Transaksi Merauke",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    AdminMetricCard(
                                        title = "Driver Online",
                                        value = if (driverProfile.isOnline) "1 Aktif" else "0 Standby",
                                        icon = Icons.Default.TwoWheeler,
                                        tint = GoPaceGreen
                                    )
                                    AdminMetricCard(
                                        title = "Order Berjalan",
                                        value = "$activeOrdersCount Berlangsung",
                                        icon = Icons.Default.AltRoute,
                                        tint = GoPaceBlue
                                    )
                                    AdminMetricCard(
                                        title = "Trip Selesai",
                                        value = "$completedOrdersCount Sukses",
                                        icon = Icons.Default.CheckCircle,
                                        tint = GoPaceGreenDark
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Daftar Pesanan Real-Time di Kota Merauke",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    if (orders.isEmpty()) {
                        item {
                            Text("Belum ada antrean order.", fontSize = 12.sp, color = TextMuted)
                        }
                    } else {
                        items(orders) { ord ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${ord.serviceType.title} (#${ord.id})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GoPaceGreenDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GoPaceGreenContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ord.status.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoPaceGreenDark
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pelanggan: ${ord.customerName} (${ord.customerPhone})",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Dari: ${ord.pickupLocation}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Ke: ${ord.destinationLocation}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    if (ord.benchmarkNote.isNotEmpty()) {
                                        Text(
                                            text = "Patokan: ${ord.benchmarkNote}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Metode: ${ord.paymentMethod.label}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Total: Rp ${indonesianFormat.format(ord.totalFare)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: DYNAMIC FARE CONFIGURATION
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Pengaturan Tarif Dinamis GoPace",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Sesuaikan tarif dasar buka pintu, tarif per KM & kompensasi cuaca",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Surcharge Toggles (Rain & Night)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "🌧️ Surcharge Cuaca Hujan (+Rp 3.000)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "Ekstra insentif driver saat hujan deras Merauke", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = fareConfig.isRainSurgeActive,
                                        onCheckedChange = { onToggleRainSurge() }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "🌙 Surcharge Jam Malam (+Rp 4.000)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "Kompensasi rute malam di atas jam 21.00 WIT", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = fareConfig.isNightSurgeActive,
                                        onCheckedChange = { onToggleNightSurge() }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(14.dp))

                                Text(text = "Parameter Tarif Saat Ini:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "• Tarif Dasar Pace-Ride: Rp ${indonesianFormat.format(fareConfig.rideBasePrice)}", fontSize = 12.sp)
                                Text(text = "• Tarif Per KM Motor: Rp ${indonesianFormat.format(fareConfig.ridePerKm)}/km", fontSize = 12.sp)
                                Text(text = "• Tarif Dasar Pace-Car: Rp ${indonesianFormat.format(fareConfig.carBasePrice)}", fontSize = 12.sp)
                                Text(text = "• Tarif Per KM Mobil: Rp ${indonesianFormat.format(fareConfig.carPerKm)}/km", fontSize = 12.sp)
                                Text(text = "• Komisi Platform: ${fareConfig.platformFeePercent}% (88% untuk driver)", fontSize = 12.sp, color = GoPaceGreenDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: VERIFIKASI DOKUMEN MITRA (KTP, SIM, STNK)
                    item {
                        Text(
                            text = "Verifikasi Berkas Calon Mitra Driver & Kendaraan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    items(driverApplications) { app ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = app.driverName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (app.isApproved) GoPaceGreenContainer else Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = app.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (app.isApproved) GoPaceGreenDark else Color(0xFFB45309)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(text = "No. HP / WA: ${app.phone}", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "NIK KTP: ${app.ktpNumber}", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "No. SIM: ${app.simNumber}", fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Kendaraan: ${app.vehicleType} (${app.vehiclePlate})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Domisili Operasi: ${app.area}", fontSize = 12.sp, color = TextSecondary)

                                if (!app.isApproved) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onRejectDriver(app.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tolak", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onApproveDriver(app.id)
                                                Toast.makeText(context, "Mitra ${app.driverName} berhasil diverifikasi!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Setujui & Verifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: BROADCAST PROMO / PENGUMUMAN
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kelola Broadcast Promo & Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Button(
                                onClick = { showPromoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buat Promo Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(promos) { p ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoPaceGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(p.promoCode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = p.targetArea,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = p.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = p.content, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Promo Dialog
    if (showPromoDialog) {
        var promoTitle by remember { mutableStateOf("") }
        var promoContent by remember { mutableStateOf("") }
        var promoCodeInput by remember { mutableStateOf("") }
        var discountPct by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showPromoDialog = false },
            title = { Text("Broadcast Promo Diskon Lokal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("Judul Promo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = promoContent,
                        onValueChange = { promoContent = it },
                        label = { Text("Isi Pesan / Info Diskon") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it },
                        label = { Text("Kode Promo (Misal: DISKONMERAUKE)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoTitle.isNotBlank()) {
                            val newP = PromoBroadcast(
                                title = promoTitle.trim(),
                                content = promoContent.trim(),
                                discountPercent = discountPct.toIntOrNull() ?: 20,
                                maxDiscount = 5000,
                                promoCode = promoCodeInput.ifBlank { "PROMO" + System.currentTimeMillis().toString().takeLast(3) }
                            )
                            onAddPromo(newP)
                            showPromoDialog = false
                            Toast.makeText(context, "Promo berhasil di-broadcast ke pelanggan!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen)
                ) {
                    Text("Broadcast Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
        Text(text = title, fontSize = 10.sp, color = TextSecondary)
    }
}
