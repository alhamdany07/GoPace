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
import androidx.compose.runtime.*
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
fun PaceMartScreen(
    onBack: () -> Unit,
    onCheckoutMartOrder: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var selectedMarket by remember { mutableStateOf("Pasar Wamanggu (Pasar Sentral Merauke)") }
    var customShoppingList by remember {
        mutableStateOf("Beras Merauke 5kg, Sayur kangkung 2 ikat, Ikan asin 1/2 kg, Telur ayam 10 butir")
    }
    var deliveryAddress by remember { mutableStateOf("Jl. Kamizaun, Rimba Jaya, Merauke") }
    var benchmarkNote by remember { mutableStateOf("Depan pohon pinang, rumah pagar biru") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var estimatedBudget by remember { mutableStateOf("120000") }

    val jastipServiceFee = 12000
    val totalEst = (estimatedBudget.toIntOrNull() ?: 100000) + jastipServiceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pace-Mart / Jastip Pasar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Jasa Titip Belanja Pasar Wamanggu & Minimarket", fontSize = 11.sp, color = TextSecondary)
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
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Estimasi Total Belanja + Jasa", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "Rp ${indonesianFormat.format(totalEst)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = GoPaceGreenDark
                            )
                        }

                        Button(
                            onClick = {
                                val order = Order(
                                    serviceType = ServiceType.PACE_MART,
                                    customerName = "Mace Maria Kaize",
                                    customerPhone = "081248001122",
                                    pickupLocation = selectedMarket,
                                    destinationLocation = deliveryAddress,
                                    benchmarkNote = benchmarkNote,
                                    distanceKm = 3.8,
                                    itemsSummary = "Titipan Jastip: $customShoppingList",
                                    baseFare = jastipServiceFee,
                                    distanceFare = 5000,
                                    suburbanSurcharge = 0,
                                    discount = 0,
                                    totalFare = totalEst,
                                    paymentMethod = paymentMethod,
                                    status = OrderStatus.SEARCHING
                                )
                                onCheckoutMartOrder(order)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pesan Jastip Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = SoftBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Target Market Selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pilih Pasar / Lokasi Belanja di Merauke:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val markets = listOf(
                            "Pasar Wamanggu (Pasar Sentral Merauke)",
                            "Pasar Mopah Baru (Sentra Pagi)",
                            "Mega Mall Merauke (Supermarket)",
                            "Toko Sembako Barokah Ampera"
                        )

                        markets.forEach { market ->
                            val isSelected = selectedMarket == market
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFE0F2F1) else SoftBackground,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF009688)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedMarket = market }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMarket = market }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = market,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom Shopping List Text Area
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Catatan Daftar Belanja Titipan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tuliskan barang yang mau dititip (sembako, sayur, ikan, bumbu dapur, dll):",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = customShoppingList,
                            onValueChange = { customShoppingList = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = estimatedBudget,
                            onValueChange = { estimatedBudget = it },
                            label = { Text("Estimasi Budget Uang Belanja (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Delivery & Benchmark
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Alamat Pengantaran & Patokan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Alamat Rumah / Kantor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = benchmarkNote,
                            onValueChange = { benchmarkNote = it },
                            label = { Text("Catatan Patokan (Samping pohon pinang, kios dll)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
    }
}
