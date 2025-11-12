package com.example.redcurtainapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redcurtainapp.model.SeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ChooseSeatsActivity : AppCompatActivity() {
    
    private val seatViews = mutableMapOf<String, ImageView>()
    private val selectedSeats = mutableSetOf<String>()
    private val reservedSeats = mutableSetOf<String>()
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var movieId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)
        
        // Get movieId from intent
        movieId = intent.getStringExtra("movieId") ?: ""
        
        // Ensure ScrollView starts at the top
        val scrollView = findViewById<ScrollView>(R.id.scroll_view_seats)
        scrollView?.post {
            scrollView.scrollTo(0, 0)
        }
        
        setupSeatViews()
        setupClickListeners()
        updateSeatDisplay()
    }
    
    override fun onResume() {
        super.onResume()
        // Load booked seats when date/time changes
        if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
            loadBookedSeats()
        }
    }
    
    private fun loadBookedSeats() {
        if (movieId.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MovieDatabase.getDatabase(this@ChooseSeatsActivity)
                val bookedSeatIds = database.seatBookingDao().getBookedSeatIdsForShow(movieId, selectedDate, selectedTime)
                
                // Convert seat IDs like "A1" to format "seat_a1"
                val formattedBookedSeats = bookedSeatIds.map { seatId ->
                    if (seatId.length >= 2) {
                        val row = seatId[0].lowercase()
                        val number = seatId.substring(1)
                        "seat_${row}${number}"
                    } else {
                        ""
                    }
                }.filter { it.isNotEmpty() }.toSet()
                
                runOnUiThread {
                    reservedSeats.clear()
                    reservedSeats.addAll(formattedBookedSeats)
                    updateSeatDisplay()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupSeatViews() {
        // Initialize all seat views - now supports rows A through K
        val rows = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")
        val seatNumbers = (1..9).toList()
        
        rows.forEach { row ->
            seatNumbers.forEach { number ->
                val seatId = "seat_${row}${number}"
                val resourceId = resources.getIdentifier(seatId, "id", packageName)
                val imageView = findViewById<ImageView>(resourceId)
                if (imageView != null) {
                    seatViews[seatId] = imageView
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        seatViews.forEach { (seatId, imageView) ->
            imageView.setOnClickListener {
                handleSeatClick(seatId)
            }
        }
        
        // Back button
        findViewById<ImageView>(R.id.back_button_seats).setOnClickListener {
            finish()
        }
        
        // Calendar icon - show date/time picker
        findViewById<ImageView>(R.id.calendar_icon).setOnClickListener {
            showDateTimePicker()
        }
        
        // Continue booking button
        findViewById<Button>(R.id.continue_booking_button_seats).setOnClickListener {
            handleContinueBooking()
        }
    }
    
    private fun handleSeatClick(seatId: String) {
        // Convert seatId from "seat_a1" to "A1" format for checking
        val seatIdFormatted = seatId.replace("seat_", "").uppercase().let { 
            if (it.length >= 2) {
                it[0].uppercase() + it.substring(1)
            } else {
                it
            }
        }
        
        // Check if seat is disabled by admin
        if (AdminSeatManagementActivity.isSeatDisabled(this, seatIdFormatted)) {
            Toast.makeText(this, "This seat is not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if seat is reserved (only after booking)
        if (reservedSeats.contains(seatId)) {
            Toast.makeText(this, "This seat is already reserved", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Toggle seat selection
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId)
        } else {
            selectedSeats.add(seatId)
        }
        
        updateSeatDisplay()
    }
    
    private fun updateSeatDisplay() {
        seatViews.forEach { (seatId, imageView) ->
            // Convert seatId from "seat_a1" to "A1" format for checking
            val seatIdFormatted = seatId.replace("seat_", "").uppercase().let { 
                if (it.length >= 2) {
                    it[0].uppercase() + it.substring(1)
                } else {
                    it
                }
            }
            
            val isDisabled = AdminSeatManagementActivity.isSeatDisabled(this, seatIdFormatted)
            
            when {
                isDisabled -> {
                    imageView.setImageResource(R.drawable.seat_reserved) // Use reserved drawable for disabled
                    imageView.alpha = 0.4f
                }
                reservedSeats.contains(seatId) -> {
                    imageView.setImageResource(R.drawable.seat_reserved)
                    imageView.alpha = 0.6f
                }
                selectedSeats.contains(seatId) -> {
                    imageView.setImageResource(R.drawable.seat_selected)
                    imageView.alpha = 1.0f
                }
                else -> {
                    imageView.setImageResource(R.drawable.seat_available)
                    imageView.alpha = 1.0f
                }
            }
        }
        
        // Update continue button state
        val continueButton = findViewById<Button>(R.id.continue_booking_button_seats)
        continueButton.isEnabled = selectedSeats.isNotEmpty()
        continueButton.alpha = if (selectedSeats.isNotEmpty()) 1.0f else 0.5f
    }
    
    private fun handleContinueBooking() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate seats are not already booked before proceeding
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MovieDatabase.getDatabase(this@ChooseSeatsActivity)
                val bookedSeatIds = database.seatBookingDao().getBookedSeatIdsForShow(movieId, selectedDate, selectedTime)
                val bookedSet = bookedSeatIds.toSet()
                
                // Convert selected seats to seat IDs (format: "A1", "B2", etc.)
                val selectedSeatIds = selectedSeats.map { seatId ->
                    val formatted = seatId.replace("seat_", "").uppercase()
                    if (formatted.length >= 2) {
                        formatted[0].uppercase() + formatted.substring(1)
                    } else {
                        formatted
                    }
                }
                
                // Check for conflicts
                val conflictingSeats = selectedSeatIds.filter { it in bookedSet }
                
                runOnUiThread {
                    if (conflictingSeats.isNotEmpty()) {
                        Toast.makeText(
                            this@ChooseSeatsActivity,
                            "Seat(s) ${conflictingSeats.joinToString(", ")} are no longer available. Please select different seats.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Remove conflicting seats from selection
                        selectedSeats.removeAll { seatId ->
                            val formatted = seatId.replace("seat_", "").uppercase()
                            val seatIdFormatted = if (formatted.length >= 2) {
                                formatted[0].uppercase() + formatted.substring(1)
                            } else {
                                formatted
                            }
                            seatIdFormatted in conflictingSeats
                        }
                        updateSeatDisplay()
                    } else {
                        // All seats are available - proceed with booking
                        proceedToBookingSummary()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ChooseSeatsActivity, "Error checking seat availability: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun proceedToBookingSummary() {
        // Get movie info from intent
        val movieTitle = intent.getStringExtra("movieTitle") ?: "Movie"
        val finalDate = if (selectedDate.isNotEmpty()) selectedDate else "Not selected"
        val finalTime = if (selectedTime.isNotEmpty()) selectedTime else "Not selected"
        
        // Calculate total price
        val basePrice = 12.0
        val premiumPrice = 18.0
        val premiumRows = setOf("a", "b") // Rows A and B are premium
        
        var totalPrice = 0.0
        selectedSeats.forEach { seatId ->
            val row = seatId.substring(5, 6) // Extract row letter
            val price = if (premiumRows.contains(row)) premiumPrice else basePrice
            totalPrice += price
        }
        
        // Format seats list
        val seatList = selectedSeats.joinToString(", ") { it.substring(5).uppercase() }
        
        // Get movieId from intent
        val movieId = intent.getStringExtra("movieId") ?: ""
        
        // Navigate to booking summary
        val intent = Intent(this, BookingSummaryActivity::class.java)
        intent.putExtra("movieId", movieId)
        intent.putExtra("movieTitle", movieTitle)
        intent.putExtra("selectedSeats", seatList)
        intent.putExtra("selectedDate", finalDate)
        intent.putExtra("selectedTime", finalTime)
        intent.putExtra("totalPrice", totalPrice)
        startActivity(intent)
    }
    
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        
        // Show date picker first
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format("%d/%d/%d", month + 1, dayOfMonth, year)
                
                // After selecting date, show time options
                showTimeOptionsDialog()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun showTimeOptionsDialog() {
        // Get showtimes from admin settings or use defaults
        val times = AdminMovieTimeActivity.getShowTimes(this).toTypedArray()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Show Time")
            .setItems(times) { _, which ->
                selectedTime = times[which]
                Toast.makeText(
                    this, 
                    "Selected: $selectedDate at $selectedTime", 
                    Toast.LENGTH_SHORT
                ).show()
                // Reload booked seats when time changes
                loadBookedSeats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
