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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.location.UserLocationInfo
import com.example.ui.theme.*

@Composable
fun GoPaceTopBar(
    currentRole: AppRole,
    onRoleSelected: (AppRole) -> Unit,
    locationInfo: UserLocationInfo = UserLocationInfo(),
    onRefreshLocation: () -> Unit = {},
    userPhone: String? = null,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRoleDialog by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 960.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            // Brand & Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoPaceGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GP",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GoPace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoPaceGreenContainer)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MERAUKE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoPaceGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    MeraukeLocationChip(
                        locationInfo = locationInfo,
                        onRefreshLocation = onRefreshLocation
                    )
                }
            }

            // Role Switcher Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (currentRole) {
                    AppRole.CUSTOMER -> GoPaceGreenContainer
                    AppRole.DRIVER -> GoPaceBlueContainer
                    AppRole.MERCHANT -> GoPaceOrangeContainer
                    AppRole.ADMIN -> Color(0xFFF3E8FF)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showRoleDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (currentRole) {
                            AppRole.CUSTOMER -> Icons.Default.Person
                            AppRole.DRIVER -> Icons.Default.TwoWheeler
                            AppRole.MERCHANT -> Icons.Default.Storefront
                            AppRole.ADMIN -> Icons.Default.AdminPanelSettings
                        },
                        contentDescription = "Role",
                        tint = when (currentRole) {
                            AppRole.CUSTOMER -> GoPaceGreenDark
                            AppRole.DRIVER -> GoPaceBlue
                            AppRole.MERCHANT -> GoPaceOrange
                            AppRole.ADMIN -> Color(0xFF6B21A8)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (currentRole) {
                            AppRole.CUSTOMER -> "Pelanggan"
                            AppRole.DRIVER -> "Mitra Driver"
                            AppRole.MERCHANT -> "Mitra Usaha"
                            AppRole.ADMIN -> "Admin"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (currentRole) {
                            AppRole.CUSTOMER -> GoPaceGreenDark
                            AppRole.DRIVER -> GoPaceBlue
                            AppRole.MERCHANT -> GoPaceOrange
                            AppRole.ADMIN -> Color(0xFF6B21A8)
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Ganti Role",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (userPhone != null) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Keluar / Ganti Akun",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = {
                Text(
                    text = "Ganti Mode / Tampilan Aplikasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih modul aplikasi untuk menguji semua fitur GoPace Merauke:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    AppRole.values().forEach { role ->
                        val isSelected = role == currentRole
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GoPaceGreenContainer else SoftBackground,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GoPaceGreen) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onRoleSelected(role)
                                    showRoleDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GoPaceGreen else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (role) {
                                            AppRole.CUSTOMER -> Icons.Default.Person
                                            AppRole.DRIVER -> Icons.Default.TwoWheeler
                                            AppRole.MERCHANT -> Icons.Default.Storefront
                                            AppRole.ADMIN -> Icons.Default.AdminPanelSettings
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else GoPaceGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = role.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) GoPaceGreenDark else TextPrimary
                                    )
                                    Text(
                                        text = role.subtitle,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Tutup", color = GoPaceGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
