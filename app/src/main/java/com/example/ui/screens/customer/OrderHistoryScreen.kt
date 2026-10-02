package com.example.ui.screens.customer

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    orders: List<Order>,
    onSelectOrder: (Order) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan GoPace", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SoftBackground
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat pesanan.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders) { ord ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectOrder(ord) }
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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (ord.serviceType) {
                                                    ServiceType.PACE_RIDE -> GoPaceGreenContainer
                                                    ServiceType.PACE_CAR -> GoPaceGreenContainer
                                                    ServiceType.PACE_FOOD -> GoPaceOrangeContainer
                                                    ServiceType.PACE_SEND -> GoPaceBlueContainer
                                                    ServiceType.PACE_MART -> Color(0xFFE0F2F1)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (ord.serviceType) {
                                                ServiceType.PACE_RIDE -> Icons.Default.TwoWheeler
                                                ServiceType.PACE_CAR -> Icons.Default.DirectionsCar
                                                ServiceType.PACE_FOOD -> Icons.Default.Restaurant
                                                ServiceType.PACE_SEND -> Icons.Default.LocalShipping
                                                ServiceType.PACE_MART -> Icons.Default.ShoppingBag
                                            },
                                            contentDescription = null,
                                            tint = when (ord.serviceType) {
                                                ServiceType.PACE_RIDE -> GoPaceGreenDark
                                                ServiceType.PACE_CAR -> GoPaceGreenDark
                                                ServiceType.PACE_FOOD -> GoPaceOrange
                                                ServiceType.PACE_SEND -> GoPaceBlue
                                                ServiceType.PACE_MART -> Color(0xFF00796B)
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = ord.serviceType.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "#${ord.id}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (ord.status == OrderStatus.COMPLETED) GoPaceGreenContainer else Color(0xFFF3F4F6)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = ord.status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ord.status == OrderStatus.COMPLETED) GoPaceGreenDark else TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Tujuan: ${ord.destinationLocation}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (ord.benchmarkNote.isNotEmpty()) {
                                Text(
                                    text = "Patokan: ${ord.benchmarkNote}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ord.paymentMethod.label,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Rp ${indonesianFormat.format(ord.totalFare)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = GoPaceGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
