package com.example.ui.screens.driver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    driverProfile: DriverProfile,
    orders: List<Order>,
    onToggleOnline: () -> Unit,
    onTopUpCommission: (Int) -> Unit,
    onWithdrawEarnings: (Int, String) -> Boolean,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onOpenChat: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }

    // Active order for driver
    val activeDriverOrder = orders.firstOrNull {
        (it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.ARRIVED || it.status == OrderStatus.ON_THE_WAY)
    }

    // Pending incoming order awaiting acceptance
    val pendingOrder = orders.firstOrNull { it.status == OrderStatus.SEARCHING }

    fun openExternalMap(locationName: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode("$locationName, Merauke")}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Membuka peta navigasi: $locationName", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Driver Header & Online/Offline Switch
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GoPaceBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = GoPaceBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = driverProfile.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${driverProfile.vehicleModel} (${driverProfile.plateNumber})",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Online / Offline Switch
                        Switch(
                            checked = driverProfile.isOnline,
                            onCheckedChange = { onToggleOnline() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoPaceGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (driverProfile.isOnline) StatusOnline else StatusOffline)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (driverProfile.isOnline) "STATUS ONLINE - Siap Terima Order" else "STATUS OFFLINE - Istirahat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (driverProfile.isOnline) GoPaceGreenDark else TextMuted
                            )
                        }

                        // Battery Saver Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔋 Mode Hemat Daya",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Driver Wallet & Performance Metrics
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dompet Mitra & Pendapatan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Saldo Komisi / Pendapatan", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "Rp ${indonesianFormat.format(driverProfile.walletBalance)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = GoPaceGreenDark
                            )
                            Text(
                                text = "Komisi sistem 12% per order",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { showTopUpDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Top Up Saldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showWithdrawDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Tarik Dana", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        DriverMetricItem(
                            label = "Total Trip",
                            value = "${driverProfile.totalTrips}",
                            icon = Icons.Default.Speed
                        )
                        DriverMetricItem(
                            label = "Rating Mitra",
                            value = "⭐ ${driverProfile.rating}",
                            icon = Icons.Default.Star
                        )
                        DriverMetricItem(
                            label = "Wilayah",
                            value = "Merauke",
                            icon = Icons.Default.LocationCity
                        )
                    }
                }
            }
        }

        // PENDING INCOMING ORDER POPUP (Order Matching)
        if (pendingOrder != null && driverProfile.isOnline) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ORDER BARU MASUK! (${pendingOrder.serviceType.title})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF92400E)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pelanggan: ${pendingOrder.customerName} (${pendingOrder.distanceKm} km)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Jemput: ${pendingOrder.pickupLocation}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tujuan: ${pendingOrder.destinationLocation}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )

                        if (pendingOrder.benchmarkNote.isNotEmpty()) {
                            Text(
                                text = "Patokan: \"${pendingOrder.benchmarkNote}\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Pendapatan Bersih Mitra:", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "Rp ${indonesianFormat.format(pendingOrder.driverCommission)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = GoPaceGreenDark
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onUpdateOrderStatus(pendingOrder.id, OrderStatus.CANCELLED) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Lewati", color = Color.Red, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onUpdateOrderStatus(pendingOrder.id, OrderStatus.ACCEPTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("TERIMA ORDER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACTIVE ORDER EXECUTION & NAVIGATION
        if (activeDriverOrder != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Orderan Sedang Berjalan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoPaceBlueContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = activeDriverOrder.status.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoPaceBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Customer: ${activeDriverOrder.customerName} (${activeDriverOrder.customerPhone})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Jemput: ${activeDriverOrder.pickupLocation}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tujuan: ${activeDriverOrder.destinationLocation}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        if (activeDriverOrder.benchmarkNote.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEFCE8),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Patokan: ${activeDriverOrder.benchmarkNote}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SHORTCUT BUTTONS: Buka Peta Navigasi & Chat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val target = if (activeDriverOrder.status == OrderStatus.ACCEPTED) {
                                        activeDriverOrder.pickupLocation
                                    } else {
                                        activeDriverOrder.destinationLocation
                                    }
                                    openExternalMap(target)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoPaceBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Peta Navigasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { onOpenChat(activeDriverOrder) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat Cepat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // STEP UPDATE BUTTONS
                        when (activeDriverOrder.status) {
                            OrderStatus.ACCEPTED -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(activeDriverOrder.id, OrderStatus.ARRIVED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Saya Sudah di Titik Jemput", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.ARRIVED -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(activeDriverOrder.id, OrderStatus.ON_THE_WAY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mulai Antar Customer / Paket", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.ON_THE_WAY -> {
                                Button(
                                    onClick = { onUpdateOrderStatus(activeDriverOrder.id, OrderStatus.COMPLETED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Selesai Antar & Terima Rp ${indonesianFormat.format(activeDriverOrder.totalFare)}", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Recent Trips Summary
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Riwayat Trip & Order Mitra",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
                    if (completedOrders.isEmpty()) {
                        Text(
                            text = "Belum ada trip selesai hari ini. Aktifkan status online untuk mulai terima order!",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        completedOrders.take(5).forEach { ord ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${ord.serviceType.title} • ${ord.destinationLocation}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Customer: ${ord.customerName} (${ord.paymentMethod.label})",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "+Rp ${indonesianFormat.format(ord.driverCommission)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoPaceGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Top-up Commission Dialog
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Top Up Saldo Komisi Mitra", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih nominal top up saldo komisi agar bisa terus terima order:", fontSize = 12.sp, color = TextSecondary)
                    listOf(50000, 100000, 200000).forEach { amt ->
                        FilledTonalButton(
                            onClick = {
                                onTopUpCommission(amt)
                                showTopUpDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Rp ${indonesianFormat.format(amt)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Withdraw Earnings Dialog
    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("100000") }
        var selectedBank by remember { mutableStateOf("Bank Papua") }

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Pencairan Saldo (Tarik Dana)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Rekening Tujuan:", fontSize = 12.sp, color = TextSecondary)
                    listOf("Bank Papua", "Bank BRI Merauke", "Bank Mandiri", "DANA / GoPay").forEach { b ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBank = b }
                        ) {
                            RadioButton(selected = selectedBank == b, onClick = { selectedBank = b })
                            Text(b, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Nominal Penarikan (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmount.toIntOrNull() ?: 50000
                        val success = onWithdrawEarnings(amt, selectedBank)
                        if (success) {
                            Toast.makeText(context, "Pencairan Rp ${indonesianFormat.format(amt)} ke $selectedBank berhasil!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Saldo tidak mencukupi!", Toast.LENGTH_SHORT).show()
                        }
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen)
                ) {
                    Text("Cairkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun DriverMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = GoPaceGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}
