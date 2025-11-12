package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.redcurtainapp.model.UserProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {
    
    // Sample card database - In production, this would be stored securely
    private val validCards = mapOf(
        "1234567890123456" to CardDetails("1234567890123456", "John Doe", "12/25", "123"),
        "1111222233334444" to CardDetails("1111222233334444", "Jane Smith", "03/24", "456"),
        "9999888877776666" to CardDetails("9999888877776666", "Bob Johnson", "06/26", "789"),
        "5555444433332222" to CardDetails("5555444433332222", "Alice Williams", "09/25", "321"),
        "7777666655554444" to CardDetails("7777666655554444", "Charlie Brown", "11/24", "654")
    )
    
    private data class CardDetails(
        val cardNumber: String,
        val cardholderName: String,
        val expiry: String,
        val cvv: String
    )
    
    private lateinit var cardNumberInput: EditText
    private lateinit var cardholderNameInput: EditText
    private lateinit var expiryInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var paymentErrorText: TextView
    private lateinit var loyaltyPointsText: TextView
    private lateinit var pointsErrorText: TextView
    private lateinit var payWithPointsButton: Button

    private var availablePoints: Int = 0
    private var totalAmount: Double = 0.0
    private var movieTitle: String = "Movie"
    private var selectedSeats: String = ""
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var movieId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        // Get booking data from intent
        movieTitle = intent.getStringExtra("movieTitle") ?: "Movie"
        selectedSeats = intent.getStringExtra("selectedSeats") ?: ""
        selectedDate = intent.getStringExtra("selectedDate") ?: ""
        selectedTime = intent.getStringExtra("selectedTime") ?: ""
        totalAmount = intent.getDoubleExtra("totalPrice", 0.0)
        movieId = intent.getStringExtra("movieId") ?: ""
        
        // Set up UI elements
        findViewById<TextView>(R.id.movie_title_payment).text = movieTitle
        
        if (selectedDate.isNotEmpty()) {
            findViewById<TextView>(R.id.booking_date_payment).text = "Date: $selectedDate"
        }
        if (selectedTime.isNotEmpty()) {
            findViewById<TextView>(R.id.booking_time_payment).text = "Time: $selectedTime"
        }
        
        findViewById<TextView>(R.id.selected_seats_payment).text = "Seats: $selectedSeats"
        findViewById<TextView>(R.id.total_price_payment).text = "Total: $${String.format("%.2f", totalAmount)}"

        // Loyalty UI
        loyaltyPointsText = findViewById(R.id.loyalty_points_text)
        pointsErrorText = findViewById(R.id.points_error_text)
        payWithPointsButton = findViewById(R.id.pay_with_points_button)
        payWithPointsButton.isEnabled = false
        payWithPointsButton.setOnClickListener {
            hidePointsError()
            val required = Math.ceil(totalAmount * 10.0).toInt() // 10 points per $1
            if (availablePoints >= required) {
                navigateToConfirmationWithPoints()
            } else {
                showPointsError("Not enough points to cover $$totalAmount")
            }
        }
        loadLoyaltyPoints()
        
        // Initialize input fields
        cardNumberInput = findViewById(R.id.card_number_input)
        cardholderNameInput = findViewById(R.id.cardholder_name_input)
        expiryInput = findViewById(R.id.expiry_input)
        cvvInput = findViewById(R.id.cvv_input)
        paymentErrorText = findViewById(R.id.payment_error_text)
        
        // Set up button listeners
        findViewById<ImageView>(R.id.back_button_payment).setOnClickListener {
            finish()
        }
        
        findViewById<Button>(R.id.process_payment_button).setOnClickListener {
            if (movieId.isBlank()) {
                showError("Invalid movie information. Please select a movie again.")
                return@setOnClickListener
            }
            processPayment(movieTitle, selectedSeats, selectedDate, selectedTime, totalAmount, movieId)
        }
        
        // Format card number input as user types
        cardNumberInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s.toString().replace(" ", "")
                if (text.isNotEmpty() && text.length <= 16) {
                    val formatted = formatCardNumber(text)
                    if (s.toString() != formatted) {
                        cardNumberInput.setText(formatted)
                        cardNumberInput.setSelection(formatted.length)
                    }
                }
            }
        })
        
        // Format expiry input automatically
        expiryInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s.toString().replace("/", "")
                if (text.length == 2 && !s.toString().contains("/")) {
                    s?.insert(2, "/")
                }
            }
        })
    }

    private fun loadLoyaltyPoints() {
        val email = AuthManager.getUserEmail(this) ?: return
        val database = MovieDatabase.getDatabase(this)
        val dao: UserProfileDao = database.userProfileDao()
        CoroutineScope(Dispatchers.IO).launch {
            val points = dao.getLoyaltyPoints(email) ?: 0
            withContext(Dispatchers.Main) {
                availablePoints = points
                loyaltyPointsText.text = "Points: $availablePoints"
                val required = Math.ceil(totalAmount * 10.0).toInt()
                payWithPointsButton.isEnabled = availablePoints >= required
                if (!payWithPointsButton.isEnabled) {
                    showPointsError("You need $required points to cover $$totalAmount")
                } else {
                    hidePointsError()
                }
            }
        }
    }

    private fun showPointsError(message: String) {
        pointsErrorText.text = message
        pointsErrorText.visibility = TextView.VISIBLE
    }

    private fun hidePointsError() {
        pointsErrorText.visibility = TextView.GONE
        pointsErrorText.text = ""
    }
    
    private fun formatCardNumber(cardNumber: String): String {
        val cleaned = cardNumber.replace(" ", "")
        return cleaned.chunked(4).joinToString(" ")
    }
    
    private fun processPayment(
        movieTitle: String,
        selectedSeats: String,
        selectedDate: String,
        selectedTime: String,
        totalPrice: Double,
        movieId: String
    ) {
        // Get user input
        val cardNumber = cardNumberInput.text?.toString()?.replace(" ", "") ?: ""
        val cardholderName = cardholderNameInput.text?.toString() ?: ""
        val expiry = expiryInput.text?.toString() ?: ""
        val cvv = cvvInput.text?.toString() ?: ""
        
        // Validate input
        if (cardNumber.length < 16) {
            showError("Please enter a valid 16-digit card number")
            return
        }
        
        if (cardholderName.isBlank()) {
            showError("Please enter the cardholder name")
            return
        }
        
        if (expiry.length < 5) {
            showError("Please enter a valid expiry date (MM/YY)")
            return
        }
        
        if (cvv.length < 3) {
            showError("Please enter a valid 3-digit CVV")
            return
        }
        
        // Check against sample database
        val cardDetails = validCards[cardNumber]
        
        if (cardDetails == null) {
            showError("Card not found. Please use one of the test cards.\nExample: 1234567890123456")
            return
        }
        
        // Verify card details match - case insensitive for name
        if (cardDetails.cardholderName.uppercase() != cardholderName.trim().uppercase()) {
            showError("Cardholder name does not match. Expected: ${cardDetails.cardholderName}")
            return
        }
        
        if (cardDetails.expiry != expiry.trim()) {
            showError("Expiry date does not match. Expected: ${cardDetails.expiry}")
            return
        }
        
        if (cardDetails.cvv != cvv.trim()) {
            showError("CVV does not match. Expected: ${cardDetails.cvv}")
            return
        }
        
        // Payment successful
        hideError()
        
        // Navigate to confirmation screen
        val intent = Intent(this, PaymentConfirmationActivity::class.java)
        intent.putExtra("movieId", movieId)
        intent.putExtra("movieTitle", movieTitle)
        intent.putExtra("selectedSeats", selectedSeats)
        intent.putExtra("selectedDate", selectedDate)
        intent.putExtra("selectedTime", selectedTime)
        intent.putExtra("totalPrice", totalPrice)
        intent.putExtra("cardNumber", "**** **** **** ${cardNumber.takeLast(4)}")
        intent.putExtra("paymentMethod", "Card")
        startActivity(intent)
        finish()
    }

    private fun navigateToConfirmationWithPoints() {
        val intent = Intent(this, PaymentConfirmationActivity::class.java)
        intent.putExtra("movieId", movieId)
        intent.putExtra("movieTitle", movieTitle)
        intent.putExtra("selectedSeats", selectedSeats)
        intent.putExtra("selectedDate", selectedDate)
        intent.putExtra("selectedTime", selectedTime)
        intent.putExtra("totalPrice", totalAmount)
        intent.putExtra("paymentMethod", "Points")
        startActivity(intent)
        finish()
    }
    
    private fun showError(message: String) {
        paymentErrorText.text = message
        paymentErrorText.visibility = TextView.VISIBLE
    }
    
    private fun hideError() {
        paymentErrorText.visibility = TextView.GONE
        paymentErrorText.text = ""
    }
}
