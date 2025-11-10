package com.example.redcurtainapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY booking_date DESC")
    fun getAllBookings(): Flow<List<Booking>>
    
    @Query("SELECT * FROM bookings WHERE user_email = :email ORDER BY booking_date DESC")
    fun getBookingsByUser(email: String): Flow<List<Booking>>
    
    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: Long): Booking?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long
    
    @Delete
    suspend fun deleteBooking(booking: Booking)
    
    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()
}

@Dao
interface SeatBookingDao {
    @Query("SELECT * FROM seat_bookings WHERE movie_id = :movieId AND selected_date = :date AND selected_time = :time")
    suspend fun getBookedSeatsForShow(movieId: String, date: String, time: String): List<SeatBooking>
    
    @Query("SELECT seat_id FROM seat_bookings WHERE movie_id = :movieId AND selected_date = :date AND selected_time = :time")
    suspend fun getBookedSeatIdsForShow(movieId: String, date: String, time: String): List<String>
    
    @Query("SELECT * FROM seat_bookings WHERE booking_id = :bookingId")
    suspend fun getSeatBookingsByBookingId(bookingId: Long): List<SeatBooking>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeatBooking(seatBooking: SeatBooking)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeatBookings(seatBookings: List<SeatBooking>)
    
    @Query("DELETE FROM seat_bookings WHERE booking_id = :bookingId")
    suspend fun deleteSeatBookingsByBookingId(bookingId: Long)
    
    @Query("DELETE FROM seat_bookings")
    suspend fun deleteAllSeatBookings()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transaction_date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE user_email = :email ORDER BY transaction_date DESC")
    fun getTransactionsByUser(email: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE booking_id = :bookingId")
    suspend fun getTransactionByBookingId(bookingId: Long): Transaction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

