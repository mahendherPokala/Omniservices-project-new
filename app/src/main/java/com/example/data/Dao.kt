package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OmniDao {
    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Int)

    @Query("SELECT * FROM provider_stats WHERE providerId = :providerId LIMIT 1")
    fun getProviderStats(providerId: String): Flow<ProviderStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviderStats(stats: ProviderStatsEntity)

    @Update
    suspend fun updateProviderStats(stats: ProviderStatsEntity)
}
