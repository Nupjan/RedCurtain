package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Initialize views
        nameEditText = findViewById(R.id.editTextName)
        phoneEditText = findViewById(R.id.editTextPhone)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.signup)
        signInLink = findViewById(R.id.signin_link)

        // Set up keyboard navigation
        setupKeyboardNavigation()

        // Sign Up button click
        signUpButton.setOnClickListener {
            performSignUp()
        }

        // Sign In link click
        signInLink.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupKeyboardNavigation() {
        // Name field - move to phone on Enter
        nameEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                phoneEditText.requestFocus()
                true
            } else {
                false
            }
        }

        // Phone field - move to email on Enter
        phoneEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                emailEditText.requestFocus()
                true
            } else {
                false
            }
        }

        // Email field - move to password on Enter
        emailEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                passwordEditText.requestFocus()
                true
            } else {
                false
            }
        }

        // Password field - perform sign up on Enter
        passwordEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSignUp()
                true
            } else {
                false
            }
        }

        // Real-time validation
        setupRealTimeValidation()
    }

    private fun setupRealTimeValidation() {
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && nameEditText.text.isNotEmpty()) {
                validateNameField()
            }
        }

        phoneEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && phoneEditText.text.isNotEmpty()) {
                validatePhoneField()
            }
        }

        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && emailEditText.text.isNotEmpty()) {
                validateEmailField()
            }
        }

        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && passwordEditText.text.isNotEmpty()) {
                validatePasswordField()
            }
        }
    }

    private fun performSignUp() {
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Clear previous errors
        clearFieldErrors()

        // Validate inputs
        if (!validateInput(name, phone, email, password)) {
            return
        }

        // Show loading state
        signUpButton.isEnabled = false
        signUpButton.text = "Creating Account..."

        // Simulate registration delay for better UX
        nameEditText.postDelayed({
            val registered = AuthManager.registerUser(this, email, password)
            if (registered) {
                Toast.makeText(this, "Account created successfully! You can sign in now.", Toast.LENGTH_LONG).show()
                // Navigate back to sign in with pre-filled email
                val intent = Intent(this, SignInActivity::class.java)
                intent.putExtra("prefilled_email", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "User already exists. Try signing in.", Toast.LENGTH_SHORT).show()
                emailEditText.requestFocus()
                emailEditText.selectAll()
            }
            
            // Reset button state
            signUpButton.isEnabled = true
            signUpButton.text = "Sign Up"
        }, 800)
    }

    private fun validateInput(name: String, phone: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            showFieldError(nameEditText, "Name is required")
            isValid = false
        } else if (name.length < 2) {
            showFieldError(nameEditText, "Name must be at least 2 characters")
            isValid = false
        }

        if (phone.isEmpty()) {
            showFieldError(phoneEditText, "Phone number is required")
            isValid = false
        } else if (!isValidPhoneNumber(phone)) {
            showFieldError(phoneEditText, "Please enter a valid phone number")
            isValid = false
        }

        if (email.isEmpty()) {
            showFieldError(emailEditText, "Email is required")
            isValid = false
        } else if (!isValidEmail(email)) {
            showFieldError(emailEditText, "Please enter a valid email address")
            isValid = false
        }

        if (password.isEmpty()) {
            showFieldError(passwordEditText, "Password is required")
            isValid = false
        } else if (password.length < 6) {
            showFieldError(passwordEditText, "Password must be at least 6 characters")
            isValid = false
        }

        return isValid
    }

    private fun validateNameField(): Boolean {
        val name = nameEditText.text.toString().trim()
        return if (name.isNotEmpty() && name.length < 2) {
            showFieldError(nameEditText, "Name must be at least 2 characters")
            false
        } else {
            clearFieldError(nameEditText)
            true
        }
    }

    private fun validatePhoneField(): Boolean {
        val phone = phoneEditText.text.toString().trim()
        return if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) {
            showFieldError(phoneEditText, "Please enter a valid phone number")
            false
        } else {
            clearFieldError(phoneEditText)
            true
        }
    }

    private fun validateEmailField(): Boolean {
        val email = emailEditText.text.toString().trim()
        return if (email.isNotEmpty() && !isValidEmail(email)) {
            showFieldError(emailEditText, "Please enter a valid email address")
            false
        } else {
            clearFieldError(emailEditText)
            true
        }
    }

    private fun validatePasswordField(): Boolean {
        val password = passwordEditText.text.toString().trim()
        return if (password.isNotEmpty() && password.length < 6) {
            showFieldError(passwordEditText, "Password must be at least 6 characters")
            false
        } else {
            clearFieldError(passwordEditText)
            true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Allow only digits, spaces, hyphens, parentheses, and plus sign
        val phonePattern = Pattern.compile("^[0-9\\s\\-\\+\\(\\)]+$")
        return phonePattern.matcher(phone).matches() && phone.length >= 10
    }

    private fun showFieldError(editText: EditText, message: String) {
        editText.error = message
        editText.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, theme))
    }

    private fun clearFieldError(editText: EditText) {
        editText.error = null
        editText.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
    }

    private fun clearFieldErrors() {
        clearFieldError(nameEditText)
        clearFieldError(phoneEditText)
        clearFieldError(emailEditText)
        clearFieldError(passwordEditText)
    }
}
