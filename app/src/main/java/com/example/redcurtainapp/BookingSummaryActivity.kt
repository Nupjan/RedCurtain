package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BookingSummaryActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_summary)
        
        // Get data from intent
        val movieTitle = intent.getStringExtra("movieTitle") ?: "Movie Title"
        val selectedSeats = intent.getStringExtra("selectedSeats") ?: ""
        val selectedDate = intent.getStringExtra("selectedDate") ?: ""
        val selectedTime = intent.getStringExtra("selectedTime") ?: ""
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        
        // Set up UI elements
        findViewById<TextView>(R.id.movie_title_summary).text = movieTitle
        
        if (selectedDate.isNotEmpty()) {
            findViewById<TextView>(R.id.movie_date_summary).text = "Date: $selectedDate"
        }
        if (selectedTime.isNotEmpty()) {
            findViewById<TextView>(R.id.movie_time_summary).text = "Time: $selectedTime"
        }
        
        // Display selected seats
        findViewById<TextView>(R.id.selected_seats_summary).text = selectedSeats
        val seatCount = selectedSeats.split(",").size
        findViewById<TextView>(R.id.seat_quantity_summary).text = "Quantity: $seatCount"
        
        // Calculate and display seat breakdown
        val seats = selectedSeats.split(",")
        val basePrice = 12.0
        val premiumPrice = 18.0
        var breakdown = StringBuilder()
        
        seats.forEach { seat ->
            if (seat.isNotEmpty()) {
                val row = seat[0].toString()
                val isPremium = row in listOf("A", "B")
                val price = if (isPremium) premiumPrice else basePrice
                breakdown.append("Seat $seat: $${String.format("%.2f", price)}\n")
            }
        }
        
        findViewById<TextView>(R.id.seat_breakdown_summary).text = breakdown.toString().trim()
        findViewById<TextView>(R.id.total_price_summary).text = "$${String.format("%.2f", totalPrice)}"
        
        // Set up button listeners
        findViewById<ImageView>(R.id.back_button_summary).setOnClickListener {
            finish()
        }
        
        findViewById<Button>(R.id.complete_payment_button).setOnClickListener {
            // Navigate to payment screen
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("movieTitle", movieTitle)
            intent.putExtra("selectedSeats", selectedSeats)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedTime", selectedTime)
            intent.putExtra("totalPrice", totalPrice)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.cancel_booking_button).setOnClickListener {
            finish()
        }
    }
}
