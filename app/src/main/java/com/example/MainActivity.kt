package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.BookingEntity
import com.example.data.ProviderEntity
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OmniServiceApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniServiceApp(viewModel: MainViewModel = viewModel()) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val providerStats by viewModel.providerStats.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showBookingDialogFor by remember { mutableStateOf<ProviderEntity?>(null) }
    var activeProgressingBooking by remember { mutableStateOf<BookingEntity?>(null) }
    var activeReviewsFormBooking by remember { mutableStateOf<BookingEntity?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(VibrantHeaderBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Vibrant Palette Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "● ",
                                color = VibrantTealPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CURRENT LOCATION",
                                color = VibrantTextMedium,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Skyview Towers, Block A",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantTextDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "▾",
                                color = VibrantTealPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Light Mode Dual switcher
                        Row(
                            modifier = Modifier
                                .background(VibrantBgSlate, RoundedCornerShape(20.dp))
                                .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (currentMode == "customer") VibrantTealPrimary else Color.Transparent)
                                    .clickable { viewModel.setMode("customer") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("toggle_customer_mode")
                            ) {
                                Text(
                                    text = "Customer",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMode == "customer") Color.White else VibrantTextMedium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (currentMode == "provider") VibrantTealPrimary else Color.Transparent)
                                    .clickable { viewModel.setMode("provider") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("toggle_provider_mode")
                            ) {
                                Text(
                                    text = "Provider",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMode == "provider") Color.White else VibrantTextMedium
                                )
                            }
                        }

                        // MR Initials Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VibrantTealLight)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MR",
                                color = VibrantTealPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Optional system nav spacer safely
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(SpaceBlack)
        ) {
            when (currentMode) {
                "customer" -> {
                    CustomerHub(
                        viewModel = viewModel,
                        providers = providers,
                        bookings = bookings,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onOpenBookingDialog = { showBookingDialogFor = it },
                        onTrackBooking = { activeProgressingBooking = it },
                        onReviewBooking = { activeReviewsFormBooking = it }
                    )
                }
                "provider" -> {
                    ProviderHub(
                        viewModel = viewModel,
                        bookings = bookings,
                        stats = providerStats,
                        onProgressBooking = { activeProgressingBooking = it }
                    )
                }
            }

            // Booking Modal dialog
            showBookingDialogFor?.let { provider ->
                BookingDialog(
                    provider = provider,
                    viewModel = viewModel,
                    onDismiss = { showBookingDialogFor = null },
                    onConfirmed = { address, date, time, notes, dynamicPrice ->
                        viewModel.createBooking(
                            providerId = provider.id,
                            providerName = provider.name,
                            providerCategory = provider.category,
                            serviceName = provider.specialization,
                            address = address,
                            date = date,
                            timeSlot = time,
                            notes = notes,
                            price = dynamicPrice
                        )
                        showBookingDialogFor = null
                        Toast.makeText(context, "Booking requested successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Live Tracking state modal
            activeProgressingBooking?.let { booking ->
                LiveTrackingModal(
                    booking = booking,
                    viewModel = viewModel,
                    onDismiss = { activeProgressingBooking = null }
                )
            }

            // Review submission dialog
            activeReviewsFormBooking?.let { booking ->
                ReviewRatingDialog(
                    booking = booking,
                    onDismiss = { activeReviewsFormBooking = null },
                    onSubmitFeedback = { rating, comments ->
                        viewModel.submitCustomerFeedback(booking, rating, comments)
                        activeReviewsFormBooking = null
                        Toast.makeText(context, "Thank you for rating!',", Toast.LENGTH_SHORT).show()
                    },
                    onSubmitDispute = { comments ->
                        viewModel.submitDispute(booking, comments)
                        activeReviewsFormBooking = null
                        Toast.makeText(context, "Dispute registered. Our ops will review this.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ==================== CUSTOMER HUB ====================
@Composable
fun CustomerHub(
    viewModel: MainViewModel,
    providers: List<ProviderEntity>,
    bookings: List<BookingEntity>,
    selectedCategory: String,
    searchQuery: String,
    onOpenBookingDialog: (ProviderEntity) -> Unit,
    onTrackBooking: (BookingEntity) -> Unit,
    onReviewBooking: (BookingEntity) -> Unit
) {
    var isChatExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Vibrant Gradient Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isChatExpanded = !isChatExpanded }
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VibrantTealPrimary, VibrantEmerald)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = "What can I help you\nwith today?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Elegant Translucent Input Mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "\"My sink is leaking...\"",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                color = VibrantTealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // AI Concierge collapsible drawer trigger
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
                    .clickable { isChatExpanded = !isChatExpanded }
                    .testTag("ai_concierge_bar")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VibrantVioletBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "AI face icon",
                            tint = VibrantVioletText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Omni AI Concierge",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = VibrantTextDark
                        )
                        Text(
                            text = "Ask me: 'My sink is leaking' or 'I need a clean'",
                            fontSize = 11.sp,
                            color = VibrantTextMedium
                        )
                    }
                    Icon(
                        imageVector = if (isChatExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand icon",
                        tint = VibrantTextMuted
                    )
                }
            }
        }

        // Expanded Chat Panel
        if (isChatExpanded) {
            item {
                AiConciergeChatPanel(viewModel = viewModel)
            }
        }

        // Active bookings ticker (if any exist)
        val activeBookings = bookings.filter { it.state != "COMPLETED" && it.state != "CANCELLED" }
        if (activeBookings.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE TRANSACTIONS (${activeBookings.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal,
                    letterSpacing = 1.sp
                )
            }
            items(activeBookings) { booking ->
                BookingStatusCard(
                    booking = booking,
                    onTrack = { onTrackBooking(booking) }
                )
            }
        }

        // Historical Bookings needing feedback
        val idleCompletedBookings = bookings.filter { it.state == "COMPLETED" && it.customerRating == null }
        if (idleCompletedBookings.isNotEmpty()) {
            item {
                Text(
                    text = "PENDING RATING REVIEWS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunsetOrange,
                    letterSpacing = 1.sp
                )
            }
            items(idleCompletedBookings) { booking ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Service by ${booking.providerName}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${booking.serviceName} • Completed",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { onReviewBooking(booking) },
                            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange, contentColor = SpaceBlack),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("rate_booking_btn")
                        ) {
                            Text("Rate / Dispute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Keyword Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search services/skills...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = BorderLight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CosmicNavy,
                    unfocusedContainerColor = CosmicNavy
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Categories selector scroll
        item {
            val categories = listOf("All", "Salon", "Electrical", "Cleaning", "AC Repair", "Painting")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    
                    // Pastel styling mapping matching the Vibrant Palette specifications
                    val (bg, fg, border) = when (category) {
                        "Salon" -> if (isSelected) Triple(VibrantVioletText, Color.White, VibrantVioletText) else Triple(VibrantVioletBg, VibrantVioletText, VibrantVioletBg)
                        "AC Repair" -> if (isSelected) Triple(VibrantAmberText, Color.White, VibrantAmberText) else Triple(VibrantAmberBg, VibrantAmberText, VibrantAmberBg)
                        "Cleaning" -> if (isSelected) Triple(VibrantBlueText, Color.White, VibrantBlueText) else Triple(VibrantBlueBg, VibrantBlueText, VibrantBlueBg)
                        "Painting" -> if (isSelected) Triple(VibrantRoseText, Color.White, VibrantRoseText) else Triple(VibrantRoseBg, VibrantRoseText, VibrantRoseBg)
                        "Electrical" -> if (isSelected) Triple(VibrantTealPrimary, Color.White, VibrantTealPrimary) else Triple(VibrantTealLight, VibrantTealPrimary, VibrantTealLight)
                        else -> if (isSelected) Triple(VibrantTealPrimary, Color.White, VibrantTealPrimary) else Triple(VibrantCardBg, VibrantTextDark, VibrantBorder)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("category_pill_$category")
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = fg
                        )
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "NEARBY REGISTERED PROFESSIONALS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        // Providers list filtering
        val filteredProviders = providers.filter {
            val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
            val matchesQuery = searchQuery.isEmpty() || it.name.contains(searchQuery, true) || it.specialization.contains(searchQuery, true)
            matchesCategory && matchesQuery
        }

        if (filteredProviders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "no providers",
                            tint = TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No providers matching filter currently.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredProviders) { provider ->
                ProviderCard(
                    provider = provider,
                    onBook = { onOpenBookingDialog(provider) }
                )
            }
        }
    }
}

// ==================== PROVIDER HUB ====================
@Composable
fun ProviderHub(
    viewModel: MainViewModel,
    bookings: List<BookingEntity>,
    stats: com.example.data.ProviderStatsEntity?,
    onProgressBooking: (BookingEntity) -> Unit
) {
    val activeProviderJobs = bookings.filter {
        it.providerId == "prov_david" && it.state != "COMPLETED" && it.state != "CANCELLED"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & online toggler header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(BorderLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Provider Avatar placeholder",
                                tint = AccentTeal,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "David Chen (You)",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Premium AC Service Expert",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Online/Offline neon toggler
                    stats?.let { stat ->
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (stat.isOnline) AccentTeal else SunsetOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (stat.isOnline) "ONLINE" else "OFFLINE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (stat.isOnline) AccentTeal else SunsetOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Switch(
                                checked = stat.isOnline,
                                onCheckedChange = { viewModel.toggleProviderOnline() },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = AccentTeal,
                                    checkedThumbColor = SpaceBlack,
                                    uncheckedTrackColor = BorderLight
                                ),
                                modifier = Modifier.scale(0.85f).testTag("online_switch")
                            )
                        }
                    }
                }
            }
        }

        // Performance Dashboard Cards
        item {
            stats?.let { currentStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicNavy)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Wallet Balance", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${currentStats.walletBalance}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                            Text("Instant Payout", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicNavy)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Completed Jobs", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentStats.completedJobs}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Tier Level: Elite", fontSize = 9.sp, color = ElectricViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Secondary metrics dashboard row
        item {
            stats?.let { currentStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicNavy)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Acceptance Rate", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentStats.acceptanceRate}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicNavy)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Cancellation Rate", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentStats.cancellationRate}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicNavy)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Avg Rating", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${currentStats.customerRating} ⭐", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SunsetOrange)
                        }
                    }
                }
            }
        }

        // Active jobs listing
        item {
            Text(
                text = "MY ASSIGNED DISPATCH JOBS (${activeProviderJobs.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricViolet,
                letterSpacing = 1.sp
            )
        }

        if (activeProviderJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "no jobs icon",
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending service dispatch jobs currently.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeProviderJobs) { job ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricViolet.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = job.state,
                                        color = ElectricViolet,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Client Request: ${job.serviceName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "₹${job.price}",
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = BorderLight, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.LocationOn, contentDescription = "address", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = job.customerAddress,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onProgressBooking(job) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet, contentColor = SlateLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("dispatch_work_progress")
                        ) {
                            Text("Execute Dispatch Actions", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== COMPOSE WIDGETS ====================

@Composable
fun AiConciergeChatPanel(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }

    val scrollState = remember { mutableStateListOf<ChatMessage>() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicNavy),
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .background(SlateDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time AI Concierge Routing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Clear history",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .clickable { viewModel.clearChat() }
                        .padding(4.dp)
                )
            }

            // Msg Lists
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    reverseLayout = false
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (msg.isUser) AccentTeal else SlateDark)
                                    .padding(10.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = if (msg.isUser) SpaceBlack else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SlateDark)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🤖 Omni AI is routing...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("What needs fixing at home?", fontSize = 12.sp, color = TextSecondary) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("ai_chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendAiMessage(messageText)
                            messageText = ""
                        }
                    })
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendAiMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(AccentTeal, RoundedCornerShape(12.dp))
                        .testTag("send_ai_message_btn")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = SpaceBlack)
                }
            }
        }
    }
}

@Composable
fun ProviderCard(provider: ProviderEntity, onBook: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicNavy),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, BorderLight, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo load
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderLight)
            ) {
                AsyncImage(
                    model = provider.photoUrl,
                    contentDescription = provider.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    if (provider.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Partner",
                            tint = AccentTeal,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = provider.specialization,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = SunsetOrange, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${provider.rating}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                    }
                    Text(text = "${provider.completedJobs} jobs Completed", color = TextSecondary, fontSize = 10.sp)
                    Text(text = "${provider.distanceKm} km away", color = TextSecondary, fontSize = 10.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Starts at",
                    fontSize = 9.sp,
                    color = TextSecondary
                )
                Text(
                    text = "₹${provider.basePrice}",
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = SpaceBlack),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("book_now_btn_${provider.id}")
                ) {
                    Text("Book", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BookingStatusCard(booking: BookingEntity, onTrack: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicNavy),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${booking.providerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(text = booking.serviceName, fontSize = 11.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (booking.state) {
                                "COMPLETED" -> AccentTeal.copy(alpha = 0.2f)
                                "CANCELLED" -> Color.Red.copy(alpha = 0.2f)
                                "DISPUTED" -> SunsetOrange.copy(alpha = 0.2f)
                                else -> ElectricViolet.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.state,
                        color = when (booking.state) {
                            "COMPLETED" -> AccentTeal
                            "CANCELLED" -> Color.Red
                            "DISPUTED" -> SunsetOrange
                            else -> ElectricViolet
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Scheduled Info:", fontSize = 10.sp, color = TextSecondary)
                    Text(text = "${booking.bookingDate} at ${booking.bookingTimeSlot}", fontSize = 11.sp, color = TextPrimary)
                }
                Button(
                    onClick = onTrack,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet, contentColor = SlateLight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("track_booking_btn_${booking.id}")
                ) {
                    Text("Track / Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== DIALOGS & OVERLAYS ====================

@Composable
fun BookingDialog(
    provider: ProviderEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onConfirmed: (address: String, date: String, time: String, notes: String, finalPrice: Double) -> Unit
) {
    var address by remember { mutableStateOf("12, High Rise residency, New Delhi") }
    var date by remember { mutableStateOf("Tomorrow") }
    var timeSlot by remember { mutableStateOf("Evening (5:00 PM - 7:00 PM)") }
    var notes by remember { mutableStateOf("") }
    var dynamicSurgeEnabled by remember { mutableStateOf(false) }

    val dynamicPrice = viewModel.calculateDynamicPrice(provider.basePrice, provider.distanceKm, dynamicSurgeEnabled)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Verify & Book Professional Service",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info Summary Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicNavy, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Expert: ", fontSize = 11.sp, color = TextSecondary)
                    Text(provider.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                }

                // Address Text field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Service Venue Address", fontSize = 11.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal)
                )

                // Date Time Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date Selection", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal)
                    )
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("Time Slot", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add instructions (e.g. 'Please bring ladder')", fontSize = 12.sp, color = TextSecondary) },
                    label = { Text("Instructions ", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal)
                )

                // Peak pricing surge Simulator (Section 25: Dynamic surge multiplier)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Simulate Surge Pricing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Applies 25% extra peak-hour multipliers", fontSize = 9.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = dynamicSurgeEnabled,
                        onCheckedChange = { dynamicSurgeEnabled = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }

                // Pricing Formula breakdown block
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicNavy)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Base Wage", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${provider.basePrice}", fontSize = 11.sp, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Dynamic Dispatch distance fee", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${String.format("%.1f", provider.distanceKm * 15)}", fontSize = 11.sp, color = TextPrimary)
                        }
                        if (dynamicSurgeEnabled) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Peak Hour Multiplier", fontSize = 11.sp, color = SunsetOrange)
                                Text("+ 25%", fontSize = 11.sp, color = SunsetOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Platform overhead & taxes", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${Math.round(49.0 + (provider.basePrice + provider.distanceKm*15)*0.18)}", fontSize = 11.sp, color = TextPrimary)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = BorderLight)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Outlay", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentTeal)
                            Text("₹$dynamicPrice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentTeal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmed(address, date, timeSlot, notes, dynamicPrice) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = SpaceBlack),
                modifier = Modifier.testTag("confirm_booking_confirm_btn")
            ) {
                Text("Lock Booking (₹$dynamicPrice)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SlateDark
    )
}

@Composable
fun LiveTrackingModal(
    booking: BookingEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Dispatch Monitor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = "gps", tint = AccentTeal, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PROVIDER STATUS: ${booking.state}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentTeal
                        )
                        Text(
                            text = "ETA: ${if (booking.state == "ON_THE_WAY") "8 mins away" else if (booking.state == "ARRIVED") "Arrived at location" else "In Progress"}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Security OTP (Section 33: Arrival OTP, Trust & Safety)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicNavy, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Secure Booking Arrival Verification OTP Code", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        booking.otpCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = SunsetOrange,
                        letterSpacing = 4.sp
                    )
                    Text("Provide this code to your specialist once they arrive.", fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }

                // Ops Progression Sandbox simulation (Section 67 & 68: Operations and admin controls)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SunsetOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🛠️ MOCK OPERATIONS MANAGER SIMULATOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunsetOrange
                    )
                    Text("Force provider dispatch progression status changes:", fontSize = 9.sp, color = TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewModel.advanceBookingState(booking, "ON_THE_WAY") },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderLight, contentColor = TextPrimary),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("On Way", fontSize = 9.sp)
                        }
                        Button(
                            onClick = { viewModel.advanceBookingState(booking, "ARRIVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderLight, contentColor = TextPrimary),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Arrived", fontSize = 9.sp)
                        }
                        Button(
                            onClick = { viewModel.advanceBookingState(booking, "WORK_STARTED") },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderLight, contentColor = TextPrimary),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Start", fontSize = 9.sp)
                        }
                        Button(
                            onClick = { viewModel.advanceBookingState(booking, "COMPLETED") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = SpaceBlack),
                            modifier = Modifier.weight(1.1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Complete", fontSize = 9.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Monitor", color = AccentTeal, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SlateDark
    )
}

@Composable
fun ReviewRatingDialog(
    booking: BookingEntity,
    onDismiss: () -> Unit,
    onSubmitFeedback: (rating: Int, comments: String) -> Unit,
    onSubmitDispute: (comments: String) -> Unit
) {
    var ratingSelected by remember { mutableStateOf(5) }
    var userComments by remember { mutableStateOf("") }
    var isFilingDispute by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isFilingDispute) "File Transaction Dispute" else "Rate Specialized Service",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isFilingDispute) {
                    // Rating stars selection selectors
                    Text("Select your satisfaction star rating:", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { ratingSelected = i }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "star $i",
                                    tint = if (i <= ratingSelected) SunsetOrange else BorderLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = userComments,
                        onValueChange = { userComments = it },
                        placeholder = { Text("Write about your experience with the specialist...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal)
                    )

                    TextButton(
                        onClick = { isFilingDispute = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Something went wrong? File a dispute", color = Color.Red, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        "File structural issue dispute regarding service by ${booking.providerName}. Our ops executive team (SLA) will investigate this under 2 hours.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = userComments,
                        onValueChange = { userComments = it },
                        placeholder = { Text("What happened? (No show, overpricing, poor work Quality...)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red)
                    )

                    TextButton(
                        onClick = { isFilingDispute = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Go back to normal rating feedback", color = AccentTeal, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (!isFilingDispute) {
                Button(
                    onClick = { onSubmitFeedback(ratingSelected, userComments) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = SpaceBlack),
                    modifier = Modifier.testTag("submit_rating_feedback_btn")
                ) {
                    Text("Submit Rating", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onSubmitDispute(userComments) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = SlateLight),
                    modifier = Modifier.testTag("submit_dispute_file_btn")
                ) {
                    Text("Submit Dispute Claim", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SlateDark
    )
}

// Extension scaling helper
fun Modifier.scale(scale: Float): Modifier = this
