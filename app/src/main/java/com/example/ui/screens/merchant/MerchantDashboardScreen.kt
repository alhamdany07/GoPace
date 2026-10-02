package com.example.ui.screens.merchant

import android.content.Context
import android.os.Vibrator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(
    merchants: List<Merchant>,
    menuItems: List<MenuItem>,
    orders: List<Order>,
    onToggleMerchantOpen: (String) -> Unit,
    onToggleItemAvailability: (String) -> Unit,
    onUpdateItemPrice: (String, Int) -> Unit,
    onAddNewItem: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var selectedMerchantId by remember { mutableStateOf("m1") }
    val currentMerchant = merchants.find { it.id == selectedMerchantId } ?: merchants.first()

    val currentMenuItems = menuItems.filter { it.merchantId == currentMerchant.id }

    var showEditPriceDialog by remember { mutableStateOf<MenuItem?>(null) }
    var showAddMenuDialog by remember { mutableStateOf(false) }

    // Food orders related to this merchant
    val foodOrders = orders.filter { it.serviceType == ServiceType.PACE_FOOD }

    fun triggerOrderAlert() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(500)
            Toast.makeText(context, "🔔 NOTIFIKASI ORDER BARU DITERIMA! Silakan siapkan pesanan.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {}
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Merchant Header & Store Open/Close Toggle
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GoPaceOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = GoPaceOrange,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentMerchant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentMerchant.category,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Open / Close Toggle
                        Switch(
                            checked = currentMerchant.isOpen,
                            onCheckedChange = { onToggleMerchantOpen(currentMerchant.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoPaceGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentMerchant.isOpen) "Status: Warung BUKA (Menerima Pesanan)" else "Status: Warung TUTUP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (currentMerchant.isOpen) GoPaceGreenDark else Color.Red
                        )

                        FilledTonalButton(
                            onClick = { triggerOrderAlert() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Tes Bunyi Notif 🔔", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Daily / Weekly Sales Recap
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Laporan Transaksi & Omzet Warung",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Omzet Hari Ini", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "Rp 385.000",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = GoPaceGreenDark
                            )
                            Text(text = "12 Porsi Terjual", fontSize = 10.sp, color = TextSecondary)
                        }

                        Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFFE5E7EB)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Omzet Minggu Ini", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "Rp 2.450.000",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = GoPaceOrange
                            )
                            Text(text = "78 Porsi Terjual", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Live Food Orders to Prepare
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
                            text = "Pesanan Masuk (Dapur)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoPaceOrangeContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${foodOrders.size} Pesanan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoPaceOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (foodOrders.isEmpty()) {
                        Text(
                            text = "Belum ada pesanan aktif. Pesanan customer akan otomatis muncul di sini.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        foodOrders.forEach { ord ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Order #${ord.id}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = GoPaceOrange
                                        )
                                        Text(
                                            text = ord.status.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GoPaceGreenDark
                                        )
                                    }
                                    Text(
                                        text = ord.itemsSummary.ifEmpty { "Menu Pesanan Siap Antar" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tujuan: ${ord.destinationLocation}",
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
                                }
                            }
                        }
                    }
                }
            }
        }

        // Menu Catalogue & Stock Control
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Katalog Menu & Kontrol Stok",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Button(
                    onClick = { showAddMenuDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Menu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(currentMenuItems) { item ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            if (item.isLocalPapuaSpecialty) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoPaceOrangeContainer)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Papua Khas", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GoPaceOrange)
                                }
                            }
                        }

                        Text(
                            text = "Rp ${indonesianFormat.format(item.price)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = GoPaceGreenDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Instant In-Stock / Out-of-Stock Switch
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { onToggleItemAvailability(item.id) },
                                modifier = Modifier.height(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.isAvailable) "Stok Tersedia" else "STOK HABIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isAvailable) GoPaceGreenDark else Color.Red
                            )
                        }
                    }

                    // Edit Price Button
                    IconButton(
                        onClick = { showEditPriceDialog = item }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Harga", tint = GoPaceBlue)
                    }
                }
            }
        }
    }

    // Edit Price Dialog
    if (showEditPriceDialog != null) {
        val item = showEditPriceDialog!!
        var newPriceInput by remember { mutableStateOf(item.price.toString()) }

        AlertDialog(
            onDismissRequest = { showEditPriceDialog = null },
            title = { Text("Ubah Harga Menu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Menu: ${item.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = newPriceInput,
                        onValueChange = { newPriceInput = it },
                        label = { Text("Harga Baru (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newPriceInput.toIntOrNull() ?: item.price
                        onUpdateItemPrice(item.id, parsed)
                        showEditPriceDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen)
                ) {
                    Text("Simpan Harga")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPriceDialog = null }) { Text("Batal") }
            }
        )
    }

    // Add Menu Dialog
    if (showAddMenuDialog) {
        var menuName by remember { mutableStateOf("") }
        var menuDesc by remember { mutableStateOf("") }
        var menuPrice by remember { mutableStateOf("25000") }
        var isSpecialty by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddMenuDialog = false },
            title = { Text("Tambah Menu Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = menuName,
                        onValueChange = { menuName = it },
                        label = { Text("Nama Menu Makanan/Minuman") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuDesc,
                        onValueChange = { menuDesc = it },
                        label = { Text("Deskripsi Porsi / Rasa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuPrice,
                        onValueChange = { menuPrice = it },
                        label = { Text("Harga (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSpecialty, onCheckedChange = { isSpecialty = it })
                        Text("Kuliner Khas Merauke / Papua", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (menuName.isNotBlank()) {
                            val newItem = MenuItem(
                                id = "f" + System.currentTimeMillis().toString().takeLast(4),
                                merchantId = currentMerchant.id,
                                name = menuName.trim(),
                                description = menuDesc.trim(),
                                price = menuPrice.toIntOrNull() ?: 20000,
                                category = "Makanan Utama",
                                isAvailable = true,
                                isLocalPapuaSpecialty = isSpecialty
                            )
                            onAddNewItem(newItem)
                            showAddMenuDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen)
                ) {
                    Text("Tambah ke Katalog")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMenuDialog = false }) { Text("Batal") }
            }
        )
    }
}
