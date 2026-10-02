package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PacePayBar(
    balance: Int,
    points: Int,
    onTopUp: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showQrisDialog by remember { mutableStateOf(false) }
    val indonesianFormat = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Balance Info
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .clickable { showTopUpDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(GoPaceBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PacePay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Rp ${indonesianFormat.format(balance)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${indonesianFormat.format(points)} PacePoin",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoPaceGreenDark
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(Color(0xFFE5E7EB))
            )

            // Right: Actions
            Row(
                modifier = Modifier.weight(1.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PacePayActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Bayar / QR",
                    tint = GoPaceBlue,
                    onClick = { showQrisDialog = true }
                )
                PacePayActionButton(
                    icon = Icons.Default.AddCircleOutline,
                    label = "Top Up",
                    tint = GoPaceGreen,
                    onClick = { showTopUpDialog = true }
                )
                PacePayActionButton(
                    icon = Icons.Default.ReceiptLong,
                    label = "Riwayat",
                    tint = TextSecondary,
                    onClick = onOpenHistory
                )
            }
        }
    }

    // Top Up Dialog
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = {
                Text(
                    text = "Top Up Saldo PacePay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pilih nominal pengisian saldo instan:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    val nominals = listOf(20000, 50000, 100000, 200000)
                    nominals.forEach { amount ->
                        Button(
                            onClick = {
                                onTopUp(amount)
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoPaceGreenContainer,
                                contentColor = GoPaceGreenDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "+ Rp ${indonesianFormat.format(amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    // QRIS Dialog
    if (showQrisDialog) {
        AlertDialog(
            onDismissRequest = { showQrisDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = GoPaceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QRIS GoPace Merauke",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scan untuk bayar di mitra warung, ojek, atau minimarket di Merauke",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.size(130.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NMID: ID1029384756-GOPACE-MRK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Standar Pembayaran Nasional QRIS Bank Indonesia",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrisDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Selesai")
                }
            }
        )
    }
}

@Composable
private fun PacePayActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
