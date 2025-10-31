package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentConfirmationActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)
        
        // Get booking data from intent
        val movieTitle = intent.getStringExtra("movieTitle") ?: "Movie"
        val selectedSeats = intent.getStringExtra("selectedSeats") ?: ""
        val selectedDate = intent.getStringExtra("selectedDate") ?: ""
        val selectedTime = intent.getStringExtra("selectedTime") ?: ""
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        val cardNumber = intent.getStringExtra("cardNumber") ?: "****"
        
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
}
