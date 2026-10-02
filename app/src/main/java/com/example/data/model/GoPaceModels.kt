package com.example.data.model

import java.util.UUID

enum class AppRole(val title: String, val subtitle: String) {
    CUSTOMER("Pelanggan (Customer)", "Pesan Pace-Ride, Food, Mart & Kurir"),
    DRIVER("Mitra Driver", "Online/Offline, Terima Order, Dompet"),
    MERCHANT("Mitra Usaha (Merchant)", "Kelola Menu, Stok & Pesanan Masuk"),
    ADMIN("Admin Operasional", "Monitoring, Tarif Dinamis & Verifikasi")
}

enum class ServiceType(
    val id: String,
    val title: String,
    val subtitle: String,
    val basePrice: Int,
    val perKmPrice: Int
) {
    PACE_RIDE("ride", "Pace-Ride", "Ojek Motor Gesit", 8000, 2500),
    PACE_CAR("car", "Pace-Car", "Mobil / Taksi Nyaman", 18000, 5000),
    PACE_FOOD("food", "Pace-Food", "Kuliner Khas Merauke", 9000, 2000),
    PACE_SEND("send", "Pace-Send", "Kurir Cepat & UMKM", 10000, 2500),
    PACE_MART("mart", "Pace-Mart", "Jastip Pasar & Toko", 12000, 3000)
}

enum class PaymentMethod(val label: String, val iconDesc: String) {
    CASH("Tunai / COD", "Bayar langsung uang tunai ke driver"),
    QRIS("QRIS / PacePay", "Bayar instan via saldo atau scan QR")
}

enum class OrderStatus(val label: String, val step: Int) {
    SEARCHING("Mencari Driver...", 0),
    ACCEPTED("Driver Menuju Titik Jemput", 1),
    ARRIVED("Driver Tiba di Titik Jemput", 2),
    ON_THE_WAY("Dalam Perjalanan ke Tujuan", 3),
    COMPLETED("Pesanan Selesai", 4),
    CANCELLED("Dibatalkan", -1)
}

data class MeraukeLandmark(
    val id: String,
    val name: String,
    val area: String,
    val description: String,
    val defaultBenchmark: String
)

data class SuburbanZone(
    val id: String,
    val name: String,
    val distanceApproxKm: Double,
    val surcharge: Int,
    val note: String
)

data class MenuItem(
    val id: String,
    val merchantId: String,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val isAvailable: Boolean = true,
    val isLocalPapuaSpecialty: Boolean = false
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    var notes: String = ""
)

data class Merchant(
    val id: String,
    val name: String,
    val category: String,
    val address: String,
    val rating: Double,
    val distanceKm: Double,
    val isOpen: Boolean = true,
    val specialBadge: String = "Pilihan Warga"
)

data class DriverProfile(
    val id: String,
    val name: String,
    val phone: String,
    val whatsappNumber: String,
    val vehicleModel: String,
    val plateNumber: String,
    val rating: Double,
    val totalTrips: Int,
    val isOnline: Boolean = true,
    val walletBalance: Int = 185000,
    val isVerified: Boolean = true
)

data class Order(
    val id: String = "GPC-" + UUID.randomUUID().toString().take(6).uppercase(),
    val serviceType: ServiceType,
    val customerName: String,
    val customerPhone: String,
    val pickupLocation: String,
    val destinationLocation: String,
    val benchmarkNote: String, // Catatan Patokan lokal (misal: "Samping kios mama Papua...")
    val suburbanZone: SuburbanZone? = null,
    val distanceKm: Double,
    val itemsSummary: String = "",
    val itemsList: List<CartItem> = emptyList(),
    val baseFare: Int,
    val distanceFare: Int,
    val suburbanSurcharge: Int,
    val discount: Int = 0,
    val totalFare: Int,
    val driverCommission: Int = (totalFare * 0.88).toInt(), // 88% to driver, 12% to platform
    val paymentMethod: PaymentMethod,
    val status: OrderStatus = OrderStatus.SEARCHING,
    val driver: DriverProfile? = null,
    val orderTimeMillis: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val isFromCustomer: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DriverDocumentApplication(
    val id: String = UUID.randomUUID().toString().take(6),
    val driverName: String,
    val phone: String,
    val ktpNumber: String,
    val simNumber: String,
    val vehicleType: String,
    val vehiclePlate: String,
    val area: String,
    val isApproved: Boolean = false,
    val status: String = "Menunggu Verifikasi"
)

data class PromoBroadcast(
    val id: String = UUID.randomUUID().toString().take(5),
    val title: String,
    val content: String,
    val discountPercent: Int,
    val maxDiscount: Int,
    val promoCode: String,
    val targetArea: String = "Semua Wilayah Merauke",
    val isActive: Boolean = true
)
