package com.example.ui.screens.customer

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaceFoodScreen(
    merchants: List<Merchant>,
    menuItems: List<MenuItem>,
    selectedMerchantInitial: Merchant?,
    onBack: () -> Unit,
    onCheckoutFoodOrder: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var currentMerchant by remember {
        mutableStateOf(selectedMerchantInitial ?: merchants.firstOrNull { it.id == "m1" } ?: merchants.first())
    }
    var selectedCategory by remember { mutableStateOf("Semua") }
    val cart = remember { mutableStateMapOf<String, Int>() }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    val filteredItems = remember(currentMerchant, selectedCategory, menuItems) {
        menuItems.filter { it.merchantId == currentMerchant.id }
            .filter { selectedCategory == "Semua" || it.category == selectedCategory }
    }

    val totalItemsCount = cart.values.sum()
    val totalFoodPrice = cart.entries.sumOf { (id, qty) ->
        val item = menuItems.find { it.id == id }
        (item?.price ?: 0) * qty
    }
    val deliveryFee = 9000
    val totalAll = if (totalFoodPrice > 0) totalFoodPrice + deliveryFee else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pace-Food Merauke", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Kuliner Lokal, Sate Rusa & Tenda Malam", fontSize = 11.sp, color = TextSecondary)
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
            if (totalItemsCount > 0) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$totalItemsCount Menu Dipilih",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Rp ${indonesianFormat.format(totalAll)} (Termasuk Ongkir)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = GoPaceGreenDark
                            )
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoPaceOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lanjut Pesan", fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Select Merchant Horizontal Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Pilih Warung / Resto Merauke:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(merchants.filter { it.id != "m5" }) { m ->
                            val isSelected = m.id == currentMerchant.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GoPaceOrangeContainer else SoftBackground,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoPaceOrange) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        currentMerchant = m
                                        cart.clear()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (isSelected) GoPaceOrange else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = m.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GoPaceOrange else TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${m.specialBadge} • ⭐ ${m.rating}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Current Merchant Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoPaceOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = GoPaceOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentMerchant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentMerchant.address,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Status: ${if (currentMerchant.isOpen) "Buka Sekarang 🟢" else "Tutup 🔴"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentMerchant.isOpen) GoPaceGreenDark else Color.Red
                                )
                            }
                        }
                    }
                }
            }

            // Menu Items List
            item {
                Text(
                    text = "Daftar Menu Makanan & Minuman",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            items(filteredItems) { item ->
                val qty = cart[item.id] ?: 0
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (item.isLocalPapuaSpecialty) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoPaceOrangeContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "KHAS MERAUKE / PAPUA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoPaceOrange
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${indonesianFormat.format(item.price)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = GoPaceGreenDark
                            )
                        }

                        // Qty Controller
                        if (item.isAvailable) {
                            if (qty == 0) {
                                Button(
                                    onClick = { cart[item.id] = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(SoftBackground, RoundedCornerShape(8.dp))
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (qty > 1) cart[item.id] = qty - 1 else cart.remove(item.id)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { cart[item.id] = qty + 1 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Habis",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Checkout Confirmation Dialog
    if (showCheckoutDialog) {
        var userAddress by remember { mutableStateOf("Jl. Kamizaun, Rimba Jaya (Kampus Musamus)") }
        var userBenchmark by remember { mutableStateOf("Depan gerbang timur kampus, samping pos satpam") }
        var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }

        val orderItems = cart.mapNotNull { (id, qty) ->
            menuItems.find { it.id == id }?.let { CartItem(it, qty) }
        }
        val summaryText = orderItems.joinToString(", ") { "${it.quantity}x ${it.menuItem.name}" }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Text("Konfirmasi Pesanan Pace-Food", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Warung: ${currentMerchant.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GoPaceOrange
                    )
                    Text(text = "Pesanan: $summaryText", fontSize = 12.sp, color = TextPrimary)

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = userAddress,
                        onValueChange = { userAddress = it },
                        label = { Text("Alamat Antar") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = userBenchmark,
                        onValueChange = { userBenchmark = it },
                        label = { Text("Catatan Patokan (Penting)") },
                        placeholder = { Text("Samping pohon pinang / depan pagar...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cara Bayar: ${paymentMethod.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                paymentMethod = if (paymentMethod == PaymentMethod.CASH) PaymentMethod.QRIS else PaymentMethod.CASH
                            }
                        ) {
                            Text("Ganti", fontSize = 11.sp, color = GoPaceGreen)
                        }
                    }

                    Text(
                        text = "Total Bayar: Rp ${indonesianFormat.format(totalAll)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = GoPaceGreenDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newOrder = Order(
                            serviceType = ServiceType.PACE_FOOD,
                            customerName = "Mace Maria Kaize",
                            customerPhone = "081248001122",
                            pickupLocation = currentMerchant.name + " (" + currentMerchant.address + ")",
                            destinationLocation = userAddress,
                            benchmarkNote = userBenchmark,
                            distanceKm = currentMerchant.distanceKm,
                            itemsSummary = summaryText,
                            itemsList = orderItems,
                            baseFare = totalFoodPrice,
                            distanceFare = deliveryFee,
                            suburbanSurcharge = 0,
                            discount = 0,
                            totalFare = totalAll,
                            paymentMethod = paymentMethod,
                            status = OrderStatus.SEARCHING
                        )
                        showCheckoutDialog = false
                        onCheckoutFoodOrder(newOrder)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen)
                ) {
                    Text("Kirim Pesanan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
