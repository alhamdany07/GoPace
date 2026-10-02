package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.repository.GoPaceRepository
import com.example.location.MeraukeLocationManager
import com.example.ui.components.GoPaceTopBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.customer.*
import com.example.ui.screens.driver.DriverDashboardScreen
import com.example.ui.screens.merchant.MerchantDashboardScreen
import com.example.ui.theme.MyApplicationTheme

sealed class CustomerSubScreen {
    object Home : CustomerSubScreen()
    data class Booking(
        val serviceType: ServiceType,
        val destination: String = "",
        val suburbanZone: SuburbanZone? = null
    ) : CustomerSubScreen()
    data class Food(val initialMerchant: Merchant? = null) : CustomerSubScreen()
    object Mart : CustomerSubScreen()
    data class ActiveTracking(val orderId: String) : CustomerSubScreen()
    data class Chat(val orderId: String, val isFromCustomer: Boolean = true) : CustomerSubScreen()
    object History : CustomerSubScreen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = GoPaceRepository.getInstance()

        setContent {
            MyApplicationTheme {
                val currentRole by repository.currentRole.collectAsStateWithLifecycle()
                val walletBalance by repository.customerWalletBalance.collectAsStateWithLifecycle()
                val customerPoints by repository.customerPoints.collectAsStateWithLifecycle()
                val driverProfile by repository.driverProfile.collectAsStateWithLifecycle()
                val orders by repository.orders.collectAsStateWithLifecycle()
                val currentActiveOrderId by repository.currentActiveOrderId.collectAsStateWithLifecycle()
                val chatMessages by repository.chatMessages.collectAsStateWithLifecycle()
                val merchants by repository.merchants.collectAsStateWithLifecycle()
                val menuItems by repository.menuItems.collectAsStateWithLifecycle()
                val fareConfig by repository.fareConfig.collectAsStateWithLifecycle()
                val driverApplications by repository.driverApplications.collectAsStateWithLifecycle()
                val promos by repository.promos.collectAsStateWithLifecycle()

                val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(auth) {
                    val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fbAuth ->
                        currentUser = fbAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                if (currentUser == null) {
                    com.example.ui.screens.auth.PhoneAuthScreen(
                        onAuthSuccess = { user ->
                            currentUser = user
                        }
                    )
                } else {
                    val context = LocalContext.current
                    val userLocation by MeraukeLocationManager.currentLocation.collectAsStateWithLifecycle()

                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        if (fineGranted || coarseGranted) {
                            MeraukeLocationManager.requestCurrentLocation(context)
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (MeraukeLocationManager.hasLocationPermission(context)) {
                            MeraukeLocationManager.requestCurrentLocation(context)
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }

                    var customerScreen by remember { mutableStateOf<CustomerSubScreen>(CustomerSubScreen.Home) }

                    // The currently tracked order for Customer
                    val activeOrder = orders.firstOrNull { it.id == currentActiveOrderId } ?: orders.firstOrNull {
                        it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            GoPaceTopBar(
                                currentRole = currentRole,
                                onRoleSelected = { role ->
                                    repository.setRole(role)
                                    if (role == AppRole.CUSTOMER) {
                                        customerScreen = CustomerSubScreen.Home
                                    }
                                },
                                locationInfo = userLocation,
                                onRefreshLocation = {
                                    if (!MeraukeLocationManager.hasLocationPermission(context)) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        MeraukeLocationManager.requestCurrentLocation(context)
                                    }
                                },
                                userPhone = currentUser?.phoneNumber ?: currentUser?.email ?: "User GoPace",
                                onSignOut = {
                                    auth.signOut()
                                    currentUser = null
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 1040.dp)
                            ) {
                                val screenModifier = Modifier.fillMaxSize()

                                when (currentRole) {
                        AppRole.CUSTOMER -> {
                            when (val sub = customerScreen) {
                                is CustomerSubScreen.Home -> {
                                    CustomerHomeScreen(
                                        walletBalance = walletBalance,
                                        points = customerPoints,
                                        landmarks = repository.meraukeLandmarks,
                                        suburbanZones = repository.suburbanZones,
                                        merchants = merchants,
                                        promos = promos,
                                        activeOrder = activeOrder,
                                        onTopUp = { amount -> repository.topUpCustomerWallet(amount) },
                                        onSelectService = { service ->
                                            when (service) {
                                                ServiceType.PACE_FOOD -> customerScreen = CustomerSubScreen.Food()
                                                ServiceType.PACE_MART -> customerScreen = CustomerSubScreen.Mart
                                                else -> customerScreen = CustomerSubScreen.Booking(serviceType = service)
                                            }
                                        },
                                        onSelectLandmark = { landmark ->
                                            customerScreen = CustomerSubScreen.Booking(
                                                serviceType = ServiceType.PACE_RIDE,
                                                destination = "${landmark.name} (${landmark.area})"
                                            )
                                        },
                                        onSelectSuburbanZone = { zone ->
                                            customerScreen = CustomerSubScreen.Booking(
                                                serviceType = ServiceType.PACE_RIDE,
                                                destination = "Wilayah ${zone.name}, Merauke",
                                                suburbanZone = zone
                                            )
                                        },
                                        onSelectMerchant = { merchant ->
                                            customerScreen = CustomerSubScreen.Food(initialMerchant = merchant)
                                        },
                                        onViewActiveOrder = {
                                            activeOrder?.let {
                                                customerScreen = CustomerSubScreen.ActiveTracking(it.id)
                                            }
                                        },
                                        onOpenOrderHistory = {
                                            customerScreen = CustomerSubScreen.History
                                        },
                                        modifier = screenModifier
                                    )
                                }

                                is CustomerSubScreen.Booking -> {
                                    BookingOrderScreen(
                                        serviceType = sub.serviceType,
                                        landmarks = repository.meraukeLandmarks,
                                        suburbanZones = repository.suburbanZones,
                                        walletBalance = walletBalance,
                                        initialDestination = sub.destination,
                                        initialSuburbanZone = sub.suburbanZone,
                                        onBack = { customerScreen = CustomerSubScreen.Home },
                                        onOrderCreated = { newOrder ->
                                            val created = repository.createOrder(newOrder)
                                            customerScreen = CustomerSubScreen.ActiveTracking(created.id)
                                        },
                                        modifier = screenModifier
                                    )
                                }

                                is CustomerSubScreen.Food -> {
                                    PaceFoodScreen(
                                        merchants = merchants,
                                        menuItems = menuItems,
                                        selectedMerchantInitial = sub.initialMerchant,
                                        onBack = { customerScreen = CustomerSubScreen.Home },
                                        onCheckoutFoodOrder = { foodOrder ->
                                            val created = repository.createOrder(foodOrder)
                                            customerScreen = CustomerSubScreen.ActiveTracking(created.id)
                                        },
                                        modifier = screenModifier
                                    )
                                }

                                is CustomerSubScreen.Mart -> {
                                    PaceMartScreen(
                                        onBack = { customerScreen = CustomerSubScreen.Home },
                                        onCheckoutMartOrder = { martOrder ->
                                            val created = repository.createOrder(martOrder)
                                            customerScreen = CustomerSubScreen.ActiveTracking(created.id)
                                        },
                                        modifier = screenModifier
                                    )
                                }

                                is CustomerSubScreen.ActiveTracking -> {
                                    val orderToTrack = orders.find { it.id == sub.orderId } ?: activeOrder
                                    if (orderToTrack != null) {
                                        ActiveOrderTrackingScreen(
                                            order = orderToTrack,
                                            onBack = { customerScreen = CustomerSubScreen.Home },
                                            onOpenChat = {
                                                customerScreen = CustomerSubScreen.Chat(orderId = orderToTrack.id, isFromCustomer = true)
                                            },
                                            onCancelOrder = { id ->
                                                repository.updateOrderStatus(id, OrderStatus.CANCELLED)
                                                customerScreen = CustomerSubScreen.Home
                                            },
                                            onAdvanceSimulatedStatus = {
                                                val nextStatus = when (orderToTrack.status) {
                                                    OrderStatus.SEARCHING -> OrderStatus.ACCEPTED
                                                    OrderStatus.ACCEPTED -> OrderStatus.ARRIVED
                                                    OrderStatus.ARRIVED -> OrderStatus.ON_THE_WAY
                                                    OrderStatus.ON_THE_WAY -> OrderStatus.COMPLETED
                                                    else -> OrderStatus.COMPLETED
                                                }
                                                repository.updateOrderStatus(orderToTrack.id, nextStatus)
                                            },
                                            modifier = screenModifier
                                        )
                                    } else {
                                        customerScreen = CustomerSubScreen.Home
                                    }
                                }

                                is CustomerSubScreen.Chat -> {
                                    val targetOrder = orders.find { it.id == sub.orderId }
                                    val driver = targetOrder?.driver ?: driverProfile
                                    ChatScreen(
                                        messages = chatMessages,
                                        driverName = driver.name,
                                        driverPhone = driver.phone,
                                        driverWhatsapp = driver.whatsappNumber,
                                        isCurrentCustomerView = sub.isFromCustomer,
                                        onSendMessage = { text ->
                                            repository.sendChatMessage(text, isCustomer = sub.isFromCustomer)
                                        },
                                        onBack = {
                                            customerScreen = if (targetOrder != null) {
                                                CustomerSubScreen.ActiveTracking(targetOrder.id)
                                            } else {
                                                CustomerSubScreen.Home
                                            }
                                        },
                                        modifier = screenModifier
                                    )
                                }

                                is CustomerSubScreen.History -> {
                                    OrderHistoryScreen(
                                        orders = orders,
                                        onSelectOrder = { ord ->
                                            customerScreen = CustomerSubScreen.ActiveTracking(ord.id)
                                        },
                                        onBack = { customerScreen = CustomerSubScreen.Home },
                                        modifier = screenModifier
                                    )
                                }
                            }
                        }

                        AppRole.DRIVER -> {
                            DriverDashboardScreen(
                                driverProfile = driverProfile,
                                orders = orders,
                                onToggleOnline = { repository.toggleDriverOnline() },
                                onTopUpCommission = { amt -> repository.topUpDriverWallet(amt) },
                                onWithdrawEarnings = { amt, _ -> repository.withdrawDriverWallet(amt) },
                                onUpdateOrderStatus = { orderId, newStatus ->
                                    repository.updateOrderStatus(orderId, newStatus)
                                },
                                onOpenChat = { order ->
                                    // Open chat in customer/driver view
                                    repository.setRole(AppRole.CUSTOMER)
                                    customerScreen = CustomerSubScreen.Chat(orderId = order.id, isFromCustomer = false)
                                },
                                modifier = screenModifier
                            )
                        }

                        AppRole.MERCHANT -> {
                            MerchantDashboardScreen(
                                merchants = merchants,
                                menuItems = menuItems,
                                orders = orders,
                                onToggleMerchantOpen = { mId -> repository.toggleMerchantOpen(mId) },
                                onToggleItemAvailability = { itemId -> repository.toggleMenuItemAvailability(itemId) },
                                onUpdateItemPrice = { itemId, newPrice -> repository.updateMenuItemPrice(itemId, newPrice) },
                                onAddNewItem = { newItem -> repository.addMenuItem(newItem) },
                                modifier = screenModifier
                            )
                        }

                        AppRole.ADMIN -> {
                            AdminDashboardScreen(
                                orders = orders,
                                driverProfile = driverProfile,
                                fareConfig = fareConfig,
                                driverApplications = driverApplications,
                                promos = promos,
                                onToggleRainSurge = { repository.toggleRainSurge() },
                                onToggleNightSurge = { repository.toggleNightSurge() },
                                onUpdateFareConfig = { config -> repository.updateFareConfig(config) },
                                onApproveDriver = { appId -> repository.approveDriverApplication(appId) },
                                onRejectDriver = { appId -> repository.rejectDriverApplication(appId) },
                                onAddPromo = { promo -> repository.addPromoBroadcast(promo) },
                                modifier = screenModifier
                            )
                        }
                    }
                }
            }
        }
    }
}
}
}
}
