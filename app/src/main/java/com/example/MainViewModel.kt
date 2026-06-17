package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "omni_service_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val dao = db.omniDao()
    val repository = OmniRepository(dao)

    // UI Navigation Mode: "customer" or "provider" or "ai_assistant"
    private val _currentMode = MutableStateFlow("customer")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    // Filters & Searches
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Providers & Bookings reactively loaded from DB
    val providers: StateFlow<List<ProviderEntity>> = repository.allProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mock provider stats (loaded/synced for David Chen prov_david)
    val providerStats: StateFlow<ProviderStatsEntity?> = repository.getProviderStats("prov_david")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI Concierge Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am your AI Concierge. Tell me what service you need (e.g. 'I need a hair styling завтра' or 'My AC is blowing hot air') and I will set up everything for you!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preseedInitialDataIfNecessary()
        }
    }

    fun setMode(mode: String) {
        _currentMode.value = mode
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Customer Actions
    fun createBooking(
        providerId: String,
        providerName: String,
        providerCategory: String,
        serviceName: String,
        address: String,
        date: String,
        timeSlot: String,
        notes: String,
        price: Double
    ) {
        viewModelScope.launch {
            val randomOtp = (1000..9999).random().toString()
            val newBooking = BookingEntity(
                providerId = providerId,
                providerName = providerName,
                providerCategory = providerCategory,
                serviceName = serviceName,
                customerAddress = address,
                bookingDate = date,
                bookingTimeSlot = timeSlot,
                notes = notes,
                price = price,
                otpCode = randomOtp,
                state = "CONFIRMED"
            )
            repository.insertBooking(newBooking)
            // Scroll to bookings
            Log.d("MainViewModel", "Created premium service booking for $providerName")
        }
    }

    // Dynamic price estimation formula based on Section 25
    fun calculateDynamicPrice(basePrice: Double, distanceKm: Double, isPeak: Boolean): Double {
        val multiplier = if (isPeak) 1.25 else 1.0
        val distanceFee = distanceKm * 15.0
        val platformFee = 49.0
        val tax = (basePrice + distanceFee) * 0.18
        return Math.round((basePrice * multiplier) + distanceFee + platformFee + tax).toDouble()
    }

    // Send AI Message
    fun sendAiMessage(message: String) {
        if (message.isBlank()) return

        val userMsg = ChatMessage(text = message, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val reply = GeminiRepository.getConcregeReply(message)
                _chatMessages.value = _chatMessages.value + ChatMessage(text = reply, isUser = false)

                // Detect if the AI suggested a booking, let's assist user in booking auto-assigned provider!
                val lower = message.lowercase()
                if (lower.contains("book") || lower.contains("hair") || lower.contains("ac") || lower.contains("clean") || lower.contains("plumb") || lower.contains("cut")) {
                    // Pre-fill a booking with auto-assigned provider based on category search
                    autoTriggerBookingSuggestion(message)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error requesting AI response", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun autoTriggerBookingSuggestion(promptText: String) {
        viewModelScope.launch {
            val list = queryMatchingProviders(promptText)
            if (list.isNotEmpty()) {
                val bestMatch = list.first()
                val priceEstimated = calculateDynamicPrice(bestMatch.basePrice, bestMatch.distanceKm, true)
                // Append AI note that they can click to finalize
                val assistantActionMsg = ChatMessage(
                    text = "🤖 *AI Concierge Suggestion:*\nI recommend **${bestMatch.name}** for your request. He has ${bestMatch.experienceYears} years experience with ${bestMatch.rating}⭐ rating. Dynamic Price: ₹${priceEstimated} (including platform fee & taxes).\n\nWould you like to complete this booking now?",
                    isUser = false,
                    isActionable = true,
                    suggestedProviderId = bestMatch.id
                )
                _chatMessages.value = _chatMessages.value + assistantActionMsg
            }
        }
    }

    private fun queryMatchingProviders(prompt: String): List<ProviderEntity> {
        val currentProviders = providers.value
        val lower = prompt.lowercase()
        return when {
            lower.contains("hair") || lower.contains("salon") || lower.contains("cut") -> currentProviders.filter { it.category == "Salon" }
            lower.contains("electric") || lower.contains("wiring") || lower.contains("short") -> currentProviders.filter { it.category == "Electrical" }
            lower.contains("clean") || lower.contains("vacuum") || lower.contains("dust") -> currentProviders.filter { it.category == "Cleaning" }
            lower.contains("ac") || lower.contains("refill") || lower.contains("cooler") -> currentProviders.filter { it.category == "AC Repair" }
            lower.contains("paint") || lower.contains("wall") -> currentProviders.filter { it.category == "Painting" }
            else -> currentProviders
        }
    }

    // Provider & Admin workflow status emulator (Section 45)
    fun advanceBookingState(booking: BookingEntity, nextState: String) {
        viewModelScope.launch {
            val updated = booking.copy(state = nextState)
            repository.updateBooking(updated)

            // If completed, add earnings to the relevant provider!
            if (nextState == "COMPLETED") {
                providerStats.value?.let { stats ->
                    if (booking.providerId == stats.providerId) {
                        val currentBalance = stats.walletBalance
                        val updatedStats = stats.copy(
                            walletBalance = currentBalance + booking.price,
                            completedJobs = stats.completedJobs + 1
                        )
                        repository.updateProviderStats(updatedStats)
                    }
                }
            }
        }
    }

    fun capturePhotoForBooking(booking: BookingEntity, isBefore: Boolean, dummyPhotoUri: String) {
        viewModelScope.launch {
            val updated = if (isBefore) {
                booking.copy(beforePhotoUri = dummyPhotoUri)
            } else {
                booking.copy(afterPhotoUri = dummyPhotoUri)
            }
            repository.updateBooking(updated)
        }
    }

    fun submitCustomerFeedback(booking: BookingEntity, rating: Int, feedback: String) {
        viewModelScope.launch {
            val updated = booking.copy(
                customerRating = rating,
                customerFeedback = feedback,
                state = "COMPLETED"
            )
            repository.updateBooking(updated)
        }
    }

    fun submitDispute(booking: BookingEntity, feedback: String) {
        viewModelScope.launch {
            val updated = booking.copy(
                customerFeedback = "[Disputed: $feedback]",
                state = "DISPUTED"
            )
            repository.updateBooking(updated)
        }
    }

    fun toggleProviderOnline() {
        viewModelScope.launch {
            providerStats.value?.let { stats ->
                val updatedStats = stats.copy(isOnline = !stats.isOnline)
                repository.updateProviderStats(updatedStats)
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "All chat cleaned. How can I help you book a household service today?",
                isUser = false
            )
        )
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isActionable: Boolean = false,
    val suggestedProviderId: String? = null
)
