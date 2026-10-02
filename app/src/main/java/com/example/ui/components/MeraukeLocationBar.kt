package com.example.ui.components

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.MeraukeLocationManager
import com.example.location.UserLocationInfo
import com.example.ui.theme.*

/**
 * Clickable location chip in TopBar or Header showing current city/area in Merauke
 */
@Composable
fun MeraukeLocationChip(
    locationInfo: UserLocationInfo,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAreaDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GoPaceGreenContainer.copy(alpha = 0.7f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showAreaDialog = true }
            .testTag("location_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (locationInfo.isPermissionGranted) GoPaceGreen else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Lokasi Saya",
                tint = GoPaceGreenDark,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = locationInfo.areaName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoPaceGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${locationInfo.district}, Merauke",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GoPaceGreenDark,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showAreaDialog) {
        MeraukeAreaSelectionDialog(
            currentInfo = locationInfo,
            onDismiss = { showAreaDialog = false },
            onDetectGps = {
                onRefreshLocation()
                showAreaDialog = false
            },
            onSelectArea = { area ->
                MeraukeLocationManager.setManualArea(area)
                showAreaDialog = false
            }
        )
    }
}

/**
 * Permission handler & Area Selection Dialog
 */
@Composable
fun MeraukeAreaSelectionDialog(
    currentInfo: UserLocationInfo,
    onDismiss: () -> Unit,
    onDetectGps: () -> Unit,
    onSelectArea: (MeraukeLocationManager.MeraukeReferenceArea) -> Unit
) {
    val context = LocalContext.current
    var isDetecting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            isDetecting = true
            MeraukeLocationManager.requestCurrentLocation(context) { info ->
                isDetecting = false
                Toast.makeText(context, "Lokasi terdeteksi: ${info.areaName}", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        } else {
            Toast.makeText(context, "Izin lokasi tidak diberikan. Anda dapat memilih wilayah secara manual.", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GoPaceGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = GoPaceGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Lokasi Saya di Merauke",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Deteksi GPS & Wilayah Jemput",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Current detected location card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (currentInfo.isPermissionGranted) GoPaceGreen else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentInfo.isPermissionGranted) "Terdeteksi Aktif" else "Izin Lokasi Belum Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentInfo.isPermissionGranted) GoPaceGreenDark else Color(0xFFB45309)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentInfo.areaName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${currentInfo.district}, Merauke (${String.format("%.4f, %.4f", currentInfo.latitude, currentInfo.longitude)})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GPS Auto Detect Button
                Button(
                    onClick = {
                        if (!MeraukeLocationManager.hasLocationPermission(context)) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            isDetecting = true
                            MeraukeLocationManager.requestCurrentLocation(context) { info ->
                                isDetecting = false
                                Toast.makeText(context, "Lokasi diperbarui: ${info.areaName}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoPaceGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("detect_gps_button")
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deteksi Otomatis Lokasi GPS Saya", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Atau Pilih Titik Wilayah / Distrik Merauke:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(MeraukeLocationManager.REFERENCE_AREAS) { area ->
                        val isSelected = area.name == currentInfo.areaName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GoPaceGreenContainer else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoPaceGreen else SoftCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectArea(area) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = area.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) GoPaceGreenDark else TextPrimary
                                    )
                                    Text(
                                        text = area.district,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GoPaceGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.Bold, color = GoPaceGreen)
            }
        }
    )
}
