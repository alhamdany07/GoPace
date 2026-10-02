package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.*

data class UserLocationInfo(
    val areaName: String = "Rimba Jaya (Kampus Musamus)",
    val district: String = "Distrik Merauke",
    val fullLabel: String = "Rimba Jaya, Merauke, Papua Selatan",
    val latitude: Double = -8.5132,
    val longitude: Double = 140.4285,
    val isAutoDetected: Boolean = false,
    val isPermissionGranted: Boolean = false
)

object MeraukeLocationManager {

    private val _currentLocation = MutableStateFlow(UserLocationInfo())
    val currentLocation: StateFlow<UserLocationInfo> = _currentLocation.asStateFlow()

    // Known Merauke Reference Areas with Centroid Coordinates
    data class MeraukeReferenceArea(
        val name: String,
        val district: String,
        val lat: Double,
        val lng: Double,
        val radiusKm: Double
    )

    val REFERENCE_AREAS = listOf(
        MeraukeReferenceArea("Rimba Jaya (Kampus Musamus)", "Distrik Merauke", -8.5132, 140.4285, 3.0),
        MeraukeReferenceArea("Pusat Kota (Kapsul Waktu / Mandala)", "Distrik Merauke", -8.4947, 140.4012, 2.5),
        MeraukeReferenceArea("Pasar Wamanggu & Pelabuhan Yos Sudarso", "Distrik Merauke", -8.4850, 140.3900, 2.5),
        MeraukeReferenceArea("Kelapa Lima / Jl. Brawijaya", "Distrik Merauke", -8.5020, 140.4120, 2.5),
        MeraukeReferenceArea("Mopah Lama & Sekitar Bandara", "Distrik Merauke", -8.5201, 140.4172, 3.0),
        MeraukeReferenceArea("Pantai Lampu Satu & Buti", "Distrik Merauke", -8.5220, 140.3750, 3.5),
        MeraukeReferenceArea("Semangga (SP 1 Muram Sari & SP 2)", "Distrik Semangga", -8.4500, 140.4500, 6.0),
        MeraukeReferenceArea("Tanah Miring (SP 3 s/d SP 6)", "Distrik Tanah Miring", -8.3800, 140.5200, 8.0),
        MeraukeReferenceArea("Distrik Kurik", "Distrik Kurik", -8.3000, 140.2500, 10.0),
        MeraukeReferenceArea("Distrik Kumbe", "Distrik Malind", -8.3500, 140.2200, 10.0),
        MeraukeReferenceArea("Taman Nasional Wasur", "Distrik Sota / Naukenjerai", -8.5800, 140.6000, 15.0)
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        context: Context,
        onComplete: (UserLocationInfo) -> Unit = {}
    ) {
        if (!hasLocationPermission(context)) {
            val unpermitted = _currentLocation.value.copy(isPermissionGranted = false)
            _currentLocation.value = unpermitted
            onComplete(unpermitted)
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationSource = CancellationTokenSource()

        try {
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val resolvedInfo = resolveMeraukeArea(context, location.latitude, location.longitude)
                    _currentLocation.value = resolvedInfo
                    onComplete(resolvedInfo)
                } else {
                    // Fallback to last known location
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        val resolved = if (lastLoc != null) {
                            resolveMeraukeArea(context, lastLoc.latitude, lastLoc.longitude)
                        } else {
                            // Default to Merauke Campus area if location is not populated yet
                            _currentLocation.value.copy(
                                isAutoDetected = true,
                                isPermissionGranted = true
                            )
                        }
                        _currentLocation.value = resolved
                        onComplete(resolved)
                    }.addOnFailureListener {
                        onComplete(_currentLocation.value)
                    }
                }
            }.addOnFailureListener { e ->
                Log.w("MeraukeLocation", "Failed to retrieve location: ${e.message}")
                onComplete(_currentLocation.value)
            }
        } catch (e: SecurityException) {
            Log.e("MeraukeLocation", "Security exception requesting location", e)
            onComplete(_currentLocation.value)
        }
    }

    /**
     * Resolves coordinates to human-readable Merauke area, district, and landmark info.
     */
    fun resolveMeraukeArea(context: Context, latitude: Double, longitude: Double): UserLocationInfo {
        // Find closest Merauke reference area
        var closestArea = REFERENCE_AREAS.first()
        var minDistance = Double.MAX_VALUE

        for (area in REFERENCE_AREAS) {
            val dist = calculateDistanceKm(latitude, longitude, area.lat, area.lng)
            if (dist < minDistance) {
                minDistance = dist
                closestArea = area
            }
        }

        // Try Geocoder for street/sublocality if available
        var streetOrSubLocality: String? = null
        try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val subLoc = addr.subLocality ?: addr.locality ?: addr.thoroughfare
                if (!subLoc.isNullOrBlank()) {
                    streetOrSubLocality = subLoc
                }
            }
        } catch (e: Exception) {
            // Geocoder service may be offline or in emulator
        }

        val areaName = if (!streetOrSubLocality.isNullOrBlank() && minDistance < 5.0) {
            "$streetOrSubLocality (${closestArea.name})"
        } else {
            closestArea.name
        }

        return UserLocationInfo(
            areaName = areaName,
            district = closestArea.district,
            fullLabel = "$areaName, ${closestArea.district}, Merauke",
            latitude = latitude,
            longitude = longitude,
            isAutoDetected = true,
            isPermissionGranted = true
        )
    }

    fun setManualArea(area: MeraukeReferenceArea) {
        _currentLocation.value = UserLocationInfo(
            areaName = area.name,
            district = area.district,
            fullLabel = "${area.name}, ${area.district}, Merauke",
            latitude = area.lat,
            longitude = area.lng,
            isAutoDetected = true,
            isPermissionGranted = true
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in KM
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
