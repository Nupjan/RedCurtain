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
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "Card"
        val cardNumber = intent.getStringExtra("cardNumber") ?: "****"
        
        // Save booking and transaction to database
        saveBookingAndTransaction(movieId, movieTitle, selectedSeats, selectedDate, selectedTime, totalPrice, paymentMethod, cardNumber)
        
        // Display booking details
        val details = """
            Movie: $movieTitle
            Seats: $selectedSeats
            Date: $selectedDate
            Time: $selectedTime
            Total: $${String.format("%.2f", totalPrice)}
            Paid with: ${if (paymentMethod == "Points") "Loyalty Points" else cardNumber}
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
        paymentMethod: String,
        cardNumber: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MovieDatabase.getDatabase(this@PaymentConfirmationActivity)
                val userEmail = AuthManager.getUserEmail(this@PaymentConfirmationActivity)
                
                // CRITICAL: Check if any selected seats are already booked before proceeding
                val alreadyBookedSeats = database.seatBookingDao().getBookedSeatIdsForShow(movieId, selectedDate, selectedTime)
                val bookedSeatSet = alreadyBookedSeats.toSet()
                
                // Parse selected seats to check
                val selectedSeatIds = mutableListOf<String>()
                if (selectedSeats.isNotEmpty()) {
                    selectedSeats.split(",").forEach { seatString ->
                        val trimmedSeat = seatString.trim()
                        if (trimmedSeat.isNotEmpty() && trimmedSeat.length >= 2) {
                            val row = trimmedSeat[0].toString().uppercase()
                            val number = trimmedSeat.substring(1).toIntOrNull()
                            if (number != null) {
                                selectedSeatIds.add("${row}${number}")
                            }
                        }
                    }
                }
                
                // Check for conflicts
                val conflictingSeats = selectedSeatIds.filter { it in bookedSeatSet }
                if (conflictingSeats.isNotEmpty()) {
                    // Some seats are already booked - show error and don't proceed
                    runOnUiThread {
                        android.widget.Toast.makeText(
                            this@PaymentConfirmationActivity,
                            "Seat(s) ${conflictingSeats.joinToString(", ")} are no longer available. Please select different seats.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        // Navigate back to seat selection
                        finish()
                    }
                    return@launch
                }
                
                // All seats are available - proceed with booking
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
                
                selectedSeatIds.forEach { seatId ->
                    val row = seatId[0].toString()
                    val number = seatId.substring(1).toIntOrNull()
                    if (number != null) {
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
                
                // Insert seat bookings - double-check each seat is still available
                val finalSeatBookings = mutableListOf<SeatBooking>()
                val unavailableSeats = mutableListOf<String>()
                
                for (seatBooking in seatBookings) {
                    val isBooked = database.seatBookingDao().isSeatBooked(
                        seatBooking.movieId,
                        seatBooking.selectedDate,
                        seatBooking.selectedTime,
                        seatBooking.seatId
                    ) > 0
                    
                    if (isBooked) {
                        unavailableSeats.add(seatBooking.seatId)
                    } else {
                        finalSeatBookings.add(seatBooking)
                    }
                }
                
                if (unavailableSeats.isNotEmpty()) {
                    // Some seats became unavailable during booking process
                    runOnUiThread {
                        android.widget.Toast.makeText(
                            this@PaymentConfirmationActivity,
                            "Seat(s) ${unavailableSeats.joinToString(", ")} were just booked by another user. Please try again.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    return@launch
                }
                
                // Insert only available seat bookings
                if (finalSeatBookings.isNotEmpty()) {
                    database.seatBookingDao().insertSeatBookings(finalSeatBookings)
                }
                
                // Create transaction
                val transaction = Transaction(
                    bookingId = bookingId,
                    movieTitle = movieTitle,
                    amount = totalPrice,
                    paymentMethod = if (paymentMethod == "Points") "Points" else "Card",
                    cardLastFour = if (cardNumber.length >= 4) cardNumber.takeLast(4) else null,
                    userEmail = userEmail
                )
                
                // Insert transaction
                database.transactionDao().insertTransaction(transaction)

                // Update loyalty points
                val email = userEmail
                if (email != null) {
                    val userDao = database.userProfileDao()
                    val now = System.currentTimeMillis()
                    // Ensure user profile exists
                    val existing = userDao.getUserProfileByEmail(email)
                    if (existing == null) {
                        userDao.insertUserProfile(
                            com.example.redcurtainapp.model.UserProfile(
                                email = email,
                                loyaltyPoints = 0,
                                needsSync = true
                            )
                        )
                    }
                    if (paymentMethod == "Points") {
                        val required = kotlin.math.ceil(totalPrice * 10.0).toInt() // 10 points per $
                        userDao.addLoyaltyPoints(email, -required, now)
                    } else {
                        val earned = kotlin.math.floor(totalPrice).toInt() // 1 point per $
                        if (earned > 0) {
                            userDao.addLoyaltyPoints(email, earned, now)
                        }
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Log error but don't crash the app
            }
        }
    }
}
