package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.redcurtainapp.model.Booking
import com.example.redcurtainapp.model.SeatBooking
import com.example.redcurtainapp.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentConfirmationActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)
        
        // Get booking data from intent
        val movieId = intent.getStringExtra("movieId") ?: ""
        val movieTitle = intent.getStringExtra("movieTitle") ?: "Movie"
        val selectedSeats = intent.getStringExtra("selectedSeats") ?: ""
        val selectedDate = intent.getStringExtra("selectedDate") ?: ""
        val selectedTime = intent.getStringExtra("selectedTime") ?: ""
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        val cardNumber = intent.getStringExtra("cardNumber") ?: "****"
        
        // Save booking and transaction to database
        saveBookingAndTransaction(movieId, movieTitle, selectedSeats, selectedDate, selectedTime, totalPrice, cardNumber)
        
        // Display booking details
        val details = """
            Movie: $movieTitle
            Seats: $selectedSeats
            Date: $selectedDate
            Time: $selectedTime
            Total: $${String.format("%.2f", totalPrice)}
            Paid with: $cardNumber
        """.trimIndent()
        
        findViewById<TextView>(R.id.booking_details_text).text = details
        
        // Back to home button
        findViewById<Button>(R.id.back_to_home_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun saveBookingAndTransaction(
        movieId: String,
        movieTitle: String,
        selectedSeats: String,
        selectedDate: String,
        selectedTime: String,
        totalPrice: Double,
        cardNumber: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MovieDatabase.getDatabase(this@PaymentConfirmationActivity)
                val userEmail = AuthManager.getUserEmail(this@PaymentConfirmationActivity)
                
                // Create booking
                val booking = Booking(
                    movieId = movieId,
                    movieTitle = movieTitle,
                    selectedDate = selectedDate,
                    selectedTime = selectedTime,
                    totalPrice = totalPrice,
                    userEmail = userEmail
                )
                
                // Insert booking and get the generated ID
                val bookingId = database.bookingDao().insertBooking(booking)
                
                // Parse seats and create seat bookings
                val seatBookings = mutableListOf<SeatBooking>()
                val basePrice = 12.0
                val premiumPrice = 18.0
                val premiumRows = setOf("A", "B")
                
                if (selectedSeats.isNotEmpty()) {
                    selectedSeats.split(",").forEach { seatString ->
                        val trimmedSeat = seatString.trim()
                        if (trimmedSeat.isNotEmpty() && trimmedSeat.length >= 2) {
                            val row = trimmedSeat[0].toString().uppercase()
                            val number = trimmedSeat.substring(1).toIntOrNull()
                            if (number != null) {
                                val seatId = "${row}${number}"
                                val price = if (row in premiumRows) premiumPrice else basePrice
                                
                                seatBookings.add(
                                    SeatBooking(
                                        bookingId = bookingId,
                                        movieId = movieId,
                                        seatId = seatId,
                                        seatRow = row,
                                        seatNumber = number,
                                        selectedDate = selectedDate,
                                        selectedTime = selectedTime,
                                        price = price
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Insert seat bookings
                if (seatBookings.isNotEmpty()) {
                    database.seatBookingDao().insertSeatBookings(seatBookings)
                }
                
                // Create transaction
                val transaction = Transaction(
                    bookingId = bookingId,
                    movieTitle = movieTitle,
                    amount = totalPrice,
                    paymentMethod = "Card",
                    cardLastFour = if (cardNumber.length >= 4) cardNumber.takeLast(4) else null,
                    userEmail = userEmail
                )
                
                // Insert transaction
                database.transactionDao().insertTransaction(transaction)
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Log error but don't crash the app
            }
        }
    }
}
