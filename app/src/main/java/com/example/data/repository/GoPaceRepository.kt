package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GoPaceRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: GoPaceRepository? = null

        fun getInstance(): GoPaceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoPaceRepository().also { INSTANCE = it }
            }
        }
    }

    // Role switcher state
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Customer Wallet & Points
    private val _customerWalletBalance = MutableStateFlow(125000)
    val customerWalletBalance: StateFlow<Int> = _customerWalletBalance.asStateFlow()

    private val _customerPoints = MutableStateFlow(3400)
    val customerPoints: StateFlow<Int> = _customerPoints.asStateFlow()

    // Active Driver Profile (Mitra)
    private val _driverProfile = MutableStateFlow(
        DriverProfile(
            id = "DRV-7701",
            name = "Pace Yakob Gebze",
            phone = "081248112233",
            whatsappNumber = "6281248112233",
            vehicleModel = "Honda Beat Street Hitam",
            plateNumber = "PA 4821 GC",
            rating = 4.9,
            totalTrips = 342,
            isOnline = true,
            walletBalance = 245000,
            isVerified = true
        )
    )
    val driverProfile: StateFlow<DriverProfile> = _driverProfile.asStateFlow()

    // Orders state
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _currentActiveOrderId = MutableStateFlow<String?>(null)
    val currentActiveOrderId: StateFlow<String?> = _currentActiveOrderId.asStateFlow()

    // Chat history for current order
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                senderName = "Pace Yakob Gebze",
                isFromCustomer = false,
                text = "Halo pace/mace! Posisi aman, saya langsung gas meluncur ke titik jemput ya."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Merauke Shortcuts & Popular Landmarks
    val meraukeLandmarks = listOf(
        MeraukeLandmark(
            id = "unmus",
            name = "Kampus Musamus (Unmus)",
            area = "Jl. Kamizaun, Rimba Jaya",
            description = "Universitas Negeri Musamus Merauke",
            defaultBenchmark = "Gerbang utama depan pos sekuriti kampus"
        ),
        MeraukeLandmark(
            id = "mopah_airport",
            name = "Bandara Mopah Merauke",
            area = "Jl. Poros Mopah",
            description = "Bandar Udara Utama Papua Selatan",
            defaultBenchmark = "Pintu kedatangan penumpang / samping drop-off"
        ),
        MeraukeLandmark(
            id = "pelabuhan_mrk",
            name = "Pelabuhan Merauke Yos Sudarso",
            area = "Dermaga Yos Sudarso",
            description = "Pelabuhan Kapal Pelni & Peti Kemas",
            defaultBenchmark = "Depan terminal penumpang Pelni"
        ),
        MeraukeLandmark(
            id = "rsud_mrk",
            name = "RSUD Merauke",
            area = "Jl. Soekarjo Wiryopranoto",
            description = "Rumah Sakit Umum Daerah Merauke",
            defaultBenchmark = "Lobby IGD pintu depan"
        ),
        MeraukeLandmark(
            id = "kapsul_waktu",
            name = "Monumen Kapsul Waktu",
            area = "Pusat Kota Merauke",
            description = "Landmark Ikonik Mirip Markas Avengers",
            defaultBenchmark = "Plaza barat dekat parkiran sepeda motor"
        ),
        MeraukeLandmark(
            id = "pantai_lampu_satu",
            name = "Pantai Lampu Satu",
            area = "Pesisir Buti, Merauke",
            description = "Wisata Sunset & Sentra Ikan Bakar",
            defaultBenchmark = "Depan gapura pantai dekat warung ikan bakar"
        ),
        MeraukeLandmark(
            id = "pasar_wamanggu",
            name = "Pasar Wamanggu",
            area = "Pusat Niaga Merauke",
            description = "Pasar Sentral Sembako & Sayur Mayur",
            defaultBenchmark = "Depan kios mama Papua blok timur"
        ),
        MeraukeLandmark(
            id = "tugu_lingkar",
            name = "Tugu Lingkar Brawijaya",
            area = "Brawijaya - Sp. Lima",
            description = "Titik Nol Keramaian Kota Merauke",
            defaultBenchmark = "Samping Pos Polisi Lingkar"
        )
    )

    // Merauke Remote / Suburban Zones with transparent flat surcharge
    val suburbanZones = listOf(
        SuburbanZone(
            id = "mopah_lama",
            name = "Mopah Lama",
            distanceApproxKm = 8.5,
            surcharge = 6000,
            note = "Wilayah perumahan ujung timur bandara lama"
        ),
        SuburbanZone(
            id = "semangga",
            name = "Semangga (SP 1 - SP 2)",
            distanceApproxKm = 14.0,
            surcharge = 12000,
            note = "Jalur transmigrasi sentra pertanian & hortikultura"
        ),
        SuburbanZone(
            id = "tanah_miring",
            name = "Tanah Miring (SP 3 - SP 9)",
            distanceApproxKm = 24.0,
            surcharge = 20000,
            note = "Kawasan lumbung padi Merauke rute jalan poros"
        ),
        SuburbanZone(
            id = "kurik",
            name = "Kurik (SP 4 - Kumbe Baru)",
            distanceApproxKm = 36.0,
            surcharge = 30000,
            note = "Rute sentra perkebunan & pesawahan utara"
        ),
        SuburbanZone(
            id = "kumbe",
            name = "Kumbe / Malind",
            distanceApproxKm = 42.0,
            surcharge = 35000,
            note = "Wilayah muara Sungai Kumbe jalur lintas"
        ),
        SuburbanZone(
            id = "wasur",
            name = "Taman Nasional Wasur",
            distanceApproxKm = 28.0,
            surcharge = 22000,
            note = "Rute perbatasan savana & habitat rusa lokal"
        )
    )

    // Merchants List
    private val _merchants = MutableStateFlow(
        listOf(
            Merchant(
                id = "m1",
                name = "Warung Sate Rusa Barokah Merauke",
                category = "Kuliner Khas Papua",
                address = "Jl. Brawijaya No. 45, Merauke",
                rating = 4.9,
                distanceKm = 1.2,
                isOpen = true,
                specialBadge = "Juara Sate Rusa"
            ),
            Merchant(
                id = "m2",
                name = "Kedai Kopi Ampera & Roti Panggang",
                category = "Kopi & Tenda Malam",
                address = "Jl. Ampera IV dekat Pelabuhan",
                rating = 4.8,
                distanceKm = 2.1,
                isOpen = true,
                specialBadge = "Favorit Nongkrong"
            ),
            Merchant(
                id = "m3",
                name = "RM Seafood Ikan Bakar Lampu Satu",
                category = "Seafood Lokal Segar",
                address = "Pesisir Buti Lampu Satu",
                rating = 4.9,
                distanceKm = 3.5,
                isOpen = true,
                specialBadge = "Ikan Kakap Segar"
            ),
            Merchant(
                id = "m4",
                name = "Warung Tenda Yos Sudarso Merauke",
                category = "Kuliner Tenda Malam",
                address = "Jl. Yos Sudarso Depan Dermaga",
                rating = 4.7,
                distanceKm = 1.8,
                isOpen = true,
                specialBadge = "Porsi Mantap"
            ),
            Merchant(
                id = "m5",
                name = "Pasar Wamanggu Sembako Jaya (Pace-Mart)",
                category = "Jastip Sembako & Pasar",
                address = "Los Sembako Blok A Pasar Wamanggu",
                rating = 4.9,
                distanceKm = 1.5,
                isOpen = true,
                specialBadge = "Beras Merauke Asli"
            )
        )
    )
    val merchants: StateFlow<List<Merchant>> = _merchants.asStateFlow()

    // Menu Catalog for Merchants
    private val _menuItems = MutableStateFlow(
        listOf(
            MenuItem(
                id = "f1",
                merchantId = "m1",
                name = "Sate Daging Rusa Khas Merauke (10 Tusuk)",
                description = "Daging rusa hutan lokal empuk dibakar bumbu kecap rempah khas Papua Selatan",
                price = 35000,
                category = "Makanan Utama",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            ),
            MenuItem(
                id = "f2",
                merchantId = "m1",
                name = "Rendang Daging Rusa Merauke",
                description = "Olahan daging rusa berempah legit kaya bumbu santan kelapa lokal",
                price = 38000,
                category = "Makanan Utama",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            ),
            MenuItem(
                id = "f3",
                merchantId = "m1",
                name = "Sop Tulang Rusa Kuah Bening",
                description = "Kuah rempah segar hangat, wortel, kentang & taburan daun bawang",
                price = 30000,
                category = "Sup & Kuah",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            ),
            MenuItem(
                id = "f4",
                merchantId = "m2",
                name = "Kopi Senang Robusta Merauke Dingin",
                description = "Kopi khas seduh manual dengan aroma cokelat karamel khas tanah Anim Ha",
                price = 15000,
                category = "Minuman Kopi",
                isAvailable = true
            ),
            MenuItem(
                id = "f5",
                merchantId = "m2",
                name = "Pisang Goreng Sambal Roa / Petis",
                description = "Pisang kepok goreng renyah disajikan dengan cocolan sambal pedas gurih",
                price = 18000,
                category = "Camilan",
                isAvailable = true
            ),
            MenuItem(
                id = "f6",
                merchantId = "m3",
                name = "Ikan Kakap Merah Bakar Bumbu Rica",
                description = "Ikan segar tangkapan nelayan Merauke dibakar arang kelapa dengan bumbu rica",
                price = 45000,
                category = "Seafood",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            ),
            MenuItem(
                id = "f7",
                merchantId = "m3",
                name = "Udang Laut Bakar Madu Pedas",
                description = "Udang tambak lokal Merauke ukuran besar gurih manis",
                price = 40000,
                category = "Seafood",
                isAvailable = true
            ),
            MenuItem(
                id = "f8",
                merchantId = "m4",
                name = "Nasi Goreng Merauke Porsi Kuli",
                description = "Nasi goreng telor ceplok + ayam suwir + kerupuk porsi mengenyangkan",
                price = 22000,
                category = "Makanan Utama",
                isAvailable = true
            ),
            MenuItem(
                id = "f9",
                merchantId = "m5",
                name = "Beras Merauke Super Cap Kapsul (5 Kg)",
                description = "Beras lokal hasil panen sawah Semangga & Kurik, pulen dan harum alami",
                price = 75000,
                category = "Sembako",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            ),
            MenuItem(
                id = "f10",
                merchantId = "m5",
                name = "Sagu Asli Muting Basah (1 Kg)",
                description = "Sagu olahan asli masyarakat lokal Papua untuk papeda lezat",
                price = 20000,
                category = "Sembako",
                isAvailable = true,
                isLocalPapuaSpecialty = true
            )
        )
    )
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    // Dynamic Fare Settings (Controlled by Admin)
    data class DynamicFareConfig(
        val rideBasePrice: Int = 8000,
        val ridePerKm: Int = 2500,
        val carBasePrice: Int = 18000,
        val carPerKm: Int = 5000,
        val rainSurcharge: Int = 3000,
        val nightSurcharge: Int = 4000,
        val platformFeePercent: Int = 12,
        val isRainSurgeActive: Boolean = false,
        val isNightSurgeActive: Boolean = false
    )

    private val _fareConfig = MutableStateFlow(DynamicFareConfig())
    val fareConfig: StateFlow<DynamicFareConfig> = _fareConfig.asStateFlow()

    // Driver Document Applications (Admin verification)
    private val _driverApplications = MutableStateFlow(
        listOf(
            DriverDocumentApplication(
                id = "APP-101",
                driverName = "Marten Mahuze",
                phone = "082199887766",
                ktpNumber = "93010219950001",
                simNumber = "SIM-C-992100",
                vehicleType = "Yamaha NMAX",
                vehiclePlate = "PA 5219 GK",
                area = "Merauke Kota - Mopah",
                isApproved = true,
                status = "Terverifikasi"
            ),
            DriverDocumentApplication(
                id = "APP-102",
                driverName = "Silvester Basik-Basik",
                phone = "081344556677",
                ktpNumber = "93010419970002",
                simNumber = "SIM-C-881203",
                vehicleType = "Honda Vario 160",
                vehiclePlate = "PA 3901 GA",
                area = "Semangga SP 1",
                isApproved = false,
                status = "Menunggu Verifikasi"
            ),
            DriverDocumentApplication(
                id = "APP-103",
                driverName = "Elias Ndiken",
                phone = "081240112299",
                ktpNumber = "93010319930004",
                simNumber = "SIM-A-771920",
                vehicleType = "Toyota Avanza Silver",
                vehiclePlate = "PA 1823 GB",
                area = "Bandara Mopah - RSUD",
                isApproved = false,
                status = "Menunggu Verifikasi"
            )
        )
    )
    val driverApplications: StateFlow<List<DriverDocumentApplication>> = _driverApplications.asStateFlow()

    // Promo Broadcasts
    private val _promos = MutableStateFlow(
        listOf(
            PromoBroadcast(
                id = "PRM1",
                title = "PACE-UNMUS DISKON 50%",
                content = "Khusus mahasiswa & dosen rute Kampus Musamus potongan s/d Rp 6.000!",
                discountPercent = 50,
                maxDiscount = 6000,
                promoCode = "UNMUSAHEE",
                targetArea = "Kampus Rimba Jaya"
            ),
            PromoBroadcast(
                id = "PRM2",
                title = "ONGKIR PINGGIRAN HEMAT",
                content = "Diskon ongkir flat rute Semangga & Tanah Miring agar makin terjangkau.",
                discountPercent = 30,
                maxDiscount = 8000,
                promoCode = "PACEPINGGIRAN",
                targetArea = "Semangga & SP 1 - SP 9"
            ),
            PromoBroadcast(
                id = "PRM3",
                title = "KULINER RUSA MERAUKE PROMO",
                content = "Gratis ongkir Pace-Food belanja kuliner khas lokal minimal Rp 30.000.",
                discountPercent = 100,
                maxDiscount = 9000,
                promoCode = "RUSASEGAR",
                targetArea = "Semua Wilayah Merauke"
            )
        )
    )
    val promos: StateFlow<List<PromoBroadcast>> = _promos.asStateFlow()

    init {
        // Create an initial sample order for immediate realistic demonstration
        val initialOrder = Order(
            id = "GPC-9824",
            serviceType = ServiceType.PACE_RIDE,
            customerName = "Mace Maria Kaize",
            customerPhone = "081248001122",
            pickupLocation = "Kampus Musamus (Unmus)",
            destinationLocation = "Monumen Kapsul Waktu",
            benchmarkNote = "Depan gerbang utama kampus samping pos satpam timur",
            distanceKm = 4.2,
            baseFare = 8000,
            distanceFare = 10500,
            suburbanSurcharge = 0,
            discount = 4000,
            totalFare = 14500,
            driverCommission = 12760,
            paymentMethod = PaymentMethod.CASH,
            status = OrderStatus.ACCEPTED,
            driver = _driverProfile.value
        )
        _orders.value = listOf(initialOrder)
        _currentActiveOrderId.value = initialOrder.id
    }

    // Role switcher
    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    // Wallet actions
    fun topUpCustomerWallet(amount: Int) {
        _customerWalletBalance.update { it + amount }
        _customerPoints.update { it + (amount / 100) }
    }

    fun deductCustomerWallet(amount: Int): Boolean {
        if (_customerWalletBalance.value >= amount) {
            _customerWalletBalance.update { it - amount }
            return true
        }
        return false
    }

    fun topUpDriverWallet(amount: Int) {
        _driverProfile.update { it.copy(walletBalance = it.walletBalance + amount) }
    }

    fun withdrawDriverWallet(amount: Int): Boolean {
        if (_driverProfile.value.walletBalance >= amount) {
            _driverProfile.update { it.copy(walletBalance = it.walletBalance - amount) }
            return true
        }
        return false
    }

    fun toggleDriverOnline() {
        _driverProfile.update { it.copy(isOnline = !it.isOnline) }
    }

    // Order operations
    fun createOrder(order: Order): Order {
        val assignedOrder = if (_driverProfile.value.isOnline) {
            order.copy(
                status = OrderStatus.ACCEPTED,
                driver = _driverProfile.value
            )
        } else {
            order.copy(status = OrderStatus.SEARCHING)
        }
        _orders.update { listOf(assignedOrder) + it }
        _currentActiveOrderId.value = assignedOrder.id
        return assignedOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { list ->
            list.map {
                if (it.id == orderId) {
                    it.copy(status = newStatus)
                } else it
            }
        }
        if (newStatus == OrderStatus.COMPLETED) {
            val order = _orders.value.find { it.id == orderId }
            if (order != null) {
                // Add driver commission to driver wallet
                _driverProfile.update {
                    it.copy(
                        walletBalance = it.walletBalance + order.driverCommission,
                        totalTrips = it.totalTrips + 1
                    )
                }
            }
        }
    }

    fun sendChatMessage(text: String, isCustomer: Boolean) {
        val sender = if (isCustomer) "Pelanggan" else _driverProfile.value.name
        val newMsg = ChatMessage(
            senderName = sender,
            isFromCustomer = isCustomer,
            text = text
        )
        _chatMessages.update { it + newMsg }
    }

    // Merchant management
    fun toggleMerchantOpen(merchantId: String) {
        _merchants.update { list ->
            list.map {
                if (it.id == merchantId) it.copy(isOpen = !it.isOpen) else it
            }
        }
    }

    fun toggleMenuItemAvailability(menuId: String) {
        _menuItems.update { list ->
            list.map {
                if (it.id == menuId) it.copy(isAvailable = !it.isAvailable) else it
            }
        }
    }

    fun updateMenuItemPrice(menuId: String, newPrice: Int) {
        _menuItems.update { list ->
            list.map {
                if (it.id == menuId) it.copy(price = newPrice) else it
            }
        }
    }

    fun addMenuItem(item: MenuItem) {
        _menuItems.update { it + item }
    }

    // Admin operations
    fun updateFareConfig(config: DynamicFareConfig) {
        _fareConfig.value = config
    }

    fun toggleRainSurge() {
        _fareConfig.update { it.copy(isRainSurgeActive = !it.isRainSurgeActive) }
    }

    fun toggleNightSurge() {
        _fareConfig.update { it.copy(isNightSurgeActive = !it.isNightSurgeActive) }
    }

    fun approveDriverApplication(appId: String) {
        _driverApplications.update { list ->
            list.map {
                if (it.id == appId) it.copy(isApproved = true, status = "Terverifikasi") else it
            }
        }
    }

    fun rejectDriverApplication(appId: String) {
        _driverApplications.update { list ->
            list.map {
                if (it.id == appId) it.copy(isApproved = false, status = "Ditolak") else it
            }
        }
    }

    fun addPromoBroadcast(promo: PromoBroadcast) {
        _promos.update { listOf(promo) + it }
    }
}
