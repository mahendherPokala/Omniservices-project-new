package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class OmniRepository(private val omniDao: OmniDao) {

    val allProviders: Flow<List<ProviderEntity>> = omniDao.getAllProviders()
    val allBookings: Flow<List<BookingEntity>> = omniDao.getAllBookings()

    fun getProviderStats(providerId: String): Flow<ProviderStatsEntity?> {
        return omniDao.getProviderStats(providerId)
    }

    suspend fun insertBooking(booking: BookingEntity): Long {
        return omniDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: BookingEntity) {
        omniDao.updateBooking(booking)
    }

    suspend fun deleteBookingById(id: Int) {
        omniDao.deleteBookingById(id)
    }

    suspend fun updateProviderStats(stats: ProviderStatsEntity) {
        omniDao.updateProviderStats(stats)
    }

    suspend fun preseedInitialDataIfNecessary() {
        val existing = omniDao.getAllProviders().first()
        if (existing.isEmpty()) {
            val initial = listOf(
                ProviderEntity(
                    id = "prov_john",
                    name = "John Miller",
                    photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                    category = "Salon",
                    specialization = "Men's Haircut, Spa & Styling",
                    rating = 4.8,
                    reviewsCount = 142,
                    experienceYears = 6,
                    isVerified = true,
                    basePrice = 499.0,
                    distanceKm = 1.8,
                    trustScore = 98,
                    responseTimeMin = 15,
                    completedJobs = 312
                ),
                ProviderEntity(
                    id = "prov_bob",
                    name = "Robert Carter",
                    photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                    category = "Electrical",
                    specialization = "Short Circuit Repairs & Wiring",
                    rating = 4.9,
                    reviewsCount = 389,
                    experienceYears = 9,
                    isVerified = true,
                    basePrice = 599.0,
                    distanceKm = 2.4,
                    trustScore = 99,
                    responseTimeMin = 12,
                    completedJobs = 582
                ),
                ProviderEntity(
                    id = "prov_alice",
                    name = "Alice Simmons",
                    photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
                    category = "Cleaning",
                    specialization = "Deep Home Vacuuming & Sanitization",
                    rating = 4.7,
                    reviewsCount = 211,
                    experienceYears = 4,
                    isVerified = true,
                    basePrice = 899.0,
                    distanceKm = 3.2,
                    trustScore = 95,
                    responseTimeMin = 20,
                    completedJobs = 278
                ),
                ProviderEntity(
                    id = "prov_david",
                    name = "David Chen",
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                    category = "AC Repair",
                    specialization = "Inverter AC Gas Refill & General Service",
                    rating = 4.9,
                    reviewsCount = 428,
                    experienceYears = 8,
                    isVerified = true,
                    basePrice = 1299.0,
                    distanceKm = 4.1,
                    trustScore = 98,
                    responseTimeMin = 10,
                    completedJobs = 620
                ),
                ProviderEntity(
                    id = "prov_painter",
                    name = "Sarah Williams",
                    photoUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=200&q=80",
                    category = "Painting",
                    specialization = "Wall Textured Painting & Consultation",
                    rating = 4.6,
                    reviewsCount = 89,
                    experienceYears = 5,
                    isVerified = false,
                    basePrice = 1499.0,
                    distanceKm = 5.5,
                    trustScore = 92,
                    responseTimeMin = 25,
                    completedJobs = 110
                )
            )
            omniDao.insertProviders(initial)

            omniDao.insertProviderStats(
                ProviderStatsEntity(
                    providerId = "prov_david",
                    isOnline = true,
                    walletBalance = 3850.0,
                    completedJobs = 620,
                    acceptanceRate = 97.4,
                    cancellationRate = 1.1,
                    customerRating = 4.9
                )
            )
        }
    }
}
