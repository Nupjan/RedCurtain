package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.redcurtainapp.AuthManager
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var signInButton: Button
    private lateinit var registerText: TextView
    
    companion object {
        private const val TWO_FA_CODE = "839203"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        // Initialize views
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        rememberMeCheckBox = findViewById(R.id.remember_me)
        signInButton = findViewById(R.id.signin)
        registerText = findViewById(R.id.Register)

        // Load remembered email if available
        loadRememberedEmail()

        // Set up keyboard navigation and validation
        setupKeyboardNavigation()
        
        // Sign In button click
        signInButton.setOnClickListener {
            performSignIn()
        }

        // Register text click
        registerText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadRememberedEmail() {
        // Check for pre-filled email from SignUp
        val prefilledEmail = intent.getStringExtra("prefilled_email")
        if (!prefilledEmail.isNullOrEmpty()) {
            emailEditText.setText(prefilledEmail)
            passwordEditText.requestFocus()
            return
        }
        
        // Load remembered email if available
        val rememberedEmail = AuthManager.getRememberedEmail(this)
        if (!rememberedEmail.isNullOrEmpty()) {
            emailEditText.setText(rememberedEmail)
            rememberMeCheckBox.isChecked = AuthManager.isRememberMeEnabled(this)
            // Focus on password field if email is pre-filled
            passwordEditText.requestFocus()
        }
    }

    private fun setupKeyboardNavigation() {
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

        // Password field - perform sign in on Enter
        passwordEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSignIn()
                true
            } else {
                false
            }
        }

        // Real-time validation
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

    private fun performSignIn() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Clear previous errors
        clearFieldErrors()

        // Validate inputs
        if (!validateInput(email, password)) {
            return
        }

        // Show loading state
        signInButton.isEnabled = false
        signInButton.text = "Signing In..."

        // Simulate authentication delay for better UX
        emailEditText.postDelayed({
            // Check for admin credentials first
            val isAdmin = email.equals("admin@redcurtain.com", ignoreCase = true) && password == "Admin123!"
            
            if (isAdmin) {
                // Admin login - show 2FA dialog
                showTwoFactorAuthDialog(email, password, isAdmin = true)
            } else if (AuthManager.validateCredentials(this, email, password)) {
                // Regular user login - show 2FA dialog
                showTwoFactorAuthDialog(email, password)
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                passwordEditText.requestFocus()
                passwordEditText.selectAll()
            }
            
            // Reset button state
            signInButton.isEnabled = true
            signInButton.text = "Sign In"
        }, 500)
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            showFieldError(emailEditText, "Email is required")
            isValid = false
        } else if (!isValidEmail(email)) {
            showFieldError(emailEditText, "Please enter a valid email")
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

    private fun validateEmailField(): Boolean {
        val email = emailEditText.text.toString().trim()
        return if (email.isNotEmpty() && !isValidEmail(email)) {
            showFieldError(emailEditText, "Please enter a valid email")
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

    private fun showFieldError(editText: EditText, message: String) {
        editText.error = message
        editText.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, theme))
    }

    private fun clearFieldError(editText: EditText) {
        editText.error = null
        editText.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
    }

    private fun clearFieldErrors() {
        clearFieldError(emailEditText)
        clearFieldError(passwordEditText)
    }
    
    private fun showTwoFactorAuthDialog(email: String, password: String, isAdmin: Boolean = false) {
        // Create custom dialog view
        val dialogView = layoutInflater.inflate(R.layout.dialog_two_factor_auth, null)
        val codeEditText = dialogView.findViewById<EditText>(R.id.two_fa_code_input)
        val emailTextView = dialogView.findViewById<TextView>(R.id.two_fa_email_text)
        val statusTextView = dialogView.findViewById<TextView>(R.id.two_fa_status_text)
        
        // Set email
        emailTextView.text = "Email sent to: $email"
        
        // Show "Sending email..." then "Email sent"
        statusTextView.text = "📧 Sending verification code..."
        statusTextView.postDelayed({
            statusTextView.text = "✅ Verification code sent!\n\nPlease check your email and enter the 6-digit code below."
        }, 1500)
        
        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Two-Factor Authentication")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Verify", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                signInButton.isEnabled = true
                signInButton.text = "Sign In"
            }
            .create()
        
        dialog.setOnShowListener {
            val verifyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            verifyButton.setOnClickListener {
                val enteredCode = codeEditText.text.toString().trim()
                
                if (enteredCode.isEmpty()) {
                    codeEditText.error = "Please enter the verification code"
                    codeEditText.requestFocus()
                    return@setOnClickListener
                }
                
                if (enteredCode == TWO_FA_CODE) {
                    // Code is correct - complete login
                    dialog.dismiss()
                    if (isAdmin) {
                        completeAdminSignIn(email, password)
                    } else {
                        completeSignIn(email, password)
                    }
                } else {
                    // Code is incorrect
                    codeEditText.error = "Invalid code. Please try again."
                    codeEditText.text?.clear()
                    codeEditText.requestFocus()
                    Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.show()
        
        // Focus on code input after dialog is shown
        codeEditText.postDelayed({
            codeEditText.requestFocus()
        }, 2000) // Wait for "email sent" message
    }
    
    private fun completeSignIn(email: String, password: String) {
        // Complete the sign-in process
        val token = AuthManager.generateAuthToken(email)
        AuthManager.saveLoginState(
            context = this,
            email = email,
            rememberMe = rememberMeCheckBox.isChecked,
            token = token
        )
        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun completeAdminSignIn(email: String, password: String) {
        // Complete admin sign-in process
        val token = AuthManager.generateAuthToken(email)
        AuthManager.saveLoginState(
            context = this,
            email = email,
            rememberMe = rememberMeCheckBox.isChecked,
            token = token
        )
        Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
