package com.example.ui.screens.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun ActiveOrderTrackingScreen(
    order: Order,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onCancelOrder: (String) -> Unit,
    onAdvanceSimulatedStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    fun dialDriver(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Membuka panggilan ke: $phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(waNumber: String, orderId: String) {
        try {
            val text = "Halo Pace driver GoPace (Order $orderId), saya customer. Posisi saya sesuai patokan ya."
            val url = "https://wa.me/$waNumber?text=${Uri.encode(text)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Membuka WhatsApp driver: $waNumber", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lacak ${order.serviceType.title}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "No. Pesanan: ${order.id}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Demo status stepper button to simulate driver journey in prototype
                    if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
                        FilledTonalButton(
                            onClick = onAdvanceSimulatedStatus,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Simulasi Maju", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SoftBackground
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
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Live Status Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (order.status == OrderStatus.COMPLETED) GoPaceGreenContainer else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (order.status == OrderStatus.COMPLETED) GoPaceGreen else GoPaceGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (order.status == OrderStatus.COMPLETED) Icons.Default.Check else Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = order.status.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (order.status == OrderStatus.COMPLETED) GoPaceGreenDark else TextPrimary
                                )
                                Text(
                                    text = when (order.status) {
                                        OrderStatus.SEARCHING -> "Sistem sedang mencarikan mitra driver terdekat di Merauke..."
                                        OrderStatus.ACCEPTED -> "Pace driver siap meluncur menuju titik jemput Anda."
                                        OrderStatus.ARRIVED -> "Driver sudah sampai di titik jemput sesuai patokan!"
                                        OrderStatus.ON_THE_WAY -> "Dalam perjalanan aman menuju lokasi tujuan."
                                        OrderStatus.COMPLETED -> "Pesanan telah selesai. Terima kasih sudah menggunakan GoPace!"
                                        OrderStatus.CANCELLED -> "Pesanan dibatalkan."
                                    },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stepper Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentStep = order.status.step
                            val steps = listOf("Dipesan", "Dijemput", "Tiba", "Jalan", "Selesai")
                            steps.forEachIndexed { index, stepName ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index <= currentStep) GoPaceGreen else Color(0xFFD1D5DB)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (index <= currentStep) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stepName,
                                        fontSize = 10.sp,
                                        fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                                        color = if (index <= currentStep) GoPaceGreenDark else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Driver Card with Direct Communication (Chat, Telp GSM, WhatsApp)
            if (order.driver != null) {
                item {
                    val driver = order.driver
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(GoPaceGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GoPaceGreenDark,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = driver.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${driver.vehicleModel} • ${driver.plateNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoPaceGreenDark
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = " ${driver.rating} • ${driver.totalTrips} Trip Selesai",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // DIRECT COMMUNICATION ROW (Requested feature: In-App Chat, Direct Call, Direct WhatsApp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // In-App Chat
                                Button(
                                    onClick = onOpenChat,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct WhatsApp
                                Button(
                                    onClick = { openWhatsApp(driver.whatsappNumber, order.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct Phone Call (GSM)
                                OutlinedButton(
                                    onClick = { dialDriver(driver.phone) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.9f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoPaceGreen)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Telepon", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Location & Landmark Guide Details
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Rute & Panduan Lokasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pickup
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = GoPaceGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Titik Jemput:", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = order.pickupLocation,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Local Benchmark (Patokan)
                        if (order.benchmarkNote.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEFCE8))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Patokan Khusus dari Anda:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "\"${order.benchmarkNote}\"",
                                            fontSize = 12.sp,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Destination
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Tujuan Pengantaran:", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = order.destinationLocation,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (order.suburbanZone != null) {
                                    Text(
                                        text = "Wilayah Pinggiran: ${order.suburbanZone.name}",
                                        fontSize = 11.sp,
                                        color = GoPaceGreenDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (order.itemsSummary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rincian Pesanan / Titipan:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = order.itemsSummary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Payment Summary
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Metode Pembayaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoPaceGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = order.paymentMethod.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoPaceGreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Ongkos / Biaya", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = "Rp ${indonesianFormat.format(order.totalFare)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = GoPaceGreenDark
                            )
                        }
                    }
                }
            }

            // Cancel Button if still searching
            if (order.status == OrderStatus.SEARCHING || order.status == OrderStatus.ACCEPTED) {
                item {
                    OutlinedButton(
                        onClick = { onCancelOrder(order.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batalkan Pesanan")
                    }
                }
            }
        }
    }
}
}

