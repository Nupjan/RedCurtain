package com.example.redcurtainapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "movie_id")
    val movieId: String,
    
    @ColumnInfo(name = "movie_title")
    val movieTitle: String,
    
    @ColumnInfo(name = "selected_date")
    val selectedDate: String,
    
    @ColumnInfo(name = "selected_time")
    val selectedTime: String,
    
    @ColumnInfo(name = "total_price")
    val totalPrice: Double,
    
    @ColumnInfo(name = "booking_date")
    val bookingDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "user_email")
    val userEmail: String? = null
)

@Entity(tableName = "seat_bookings")
data class SeatBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "booking_id")
    val bookingId: Long,
    
    @ColumnInfo(name = "movie_id")
    val movieId: String,
    
    @ColumnInfo(name = "seat_id")
    val seatId: String, // Format: "A1", "B2", etc.
    
    @ColumnInfo(name = "seat_row")
    val seatRow: String,
    
    @ColumnInfo(name = "seat_number")
    val seatNumber: Int,
    
    @ColumnInfo(name = "selected_date")
    val selectedDate: String,
    
    @ColumnInfo(name = "selected_time")
    val selectedTime: String,
    
    @ColumnInfo(name = "price")
    val price: Double
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "booking_id")
    val bookingId: Long,
    
    @ColumnInfo(name = "movie_title")
    val movieTitle: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Card",
    
    @ColumnInfo(name = "card_last_four")
    val cardLastFour: String? = null,
    
    @ColumnInfo(name = "user_email")
    val userEmail: String? = null
)

