package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val photoUrl: String,
    val category: String,
    val specialization: String,
    val rating: Double,
    val reviewsCount: Int,
    val experienceYears: Int,
    val isVerified: Boolean,
    val basePrice: Double,
    val distanceKm: Double,
    val trustScore: Int,
    val responseTimeMin: Int,
    val completedJobs: Int
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: String,
    val providerName: String,
    val providerCategory: String,
    val serviceName: String,
    val customerAddress: String,
    val bookingDate: String,
    val bookingTimeSlot: String,
    val notes: String,
    val price: Double,
    val state: String = "CONFIRMED", // CONFIRMED, ON_THE_WAY, ARRIVED, WORK_STARTED, COMPLETED, CANCELLED, DISPUTED
    val otpCode: String = "1234",
    val timestamp: Long = System.currentTimeMillis(),
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val customerFeedback: String? = null,
    val customerRating: Int? = null
)

@Entity(tableName = "provider_stats")
data class ProviderStatsEntity(
    @PrimaryKey val providerId: String,
    val isOnline: Boolean = true,
    val walletBalance: Double = 3500.0,
    val completedJobs: Int = 42,
    val acceptanceRate: Double = 98.0,
    val cancellationRate: Double = 2.0,
    val customerRating: Double = 4.9
)
