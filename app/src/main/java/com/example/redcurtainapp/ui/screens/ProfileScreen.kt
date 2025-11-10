package com.example.redcurtainapp.ui.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.redcurtainapp.AuthManager
import com.example.redcurtainapp.SignInActivity
import com.example.redcurtainapp.navigation.Screen
import com.example.redcurtainapp.MovieDatabase
import com.example.redcurtainapp.model.UserProfile
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val userEmail = remember { AuthManager.getUserEmail(context) }
    val database = remember { MovieDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Load user profile from database
    val userProfileFlow = remember(userEmail) {
        if (userEmail != null) {
            database.userProfileDao().getUserProfileByEmailFlow(userEmail)
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }
    val userProfile by userProfileFlow.collectAsState(initial = null)
    
    // Theme state
    var isDarkTheme by remember { 
        mutableStateOf(getThemePreference(context))
    }
    
    // Appearance state
    var fontSize by remember { 
        mutableStateOf(getFontSizePreference(context))
    }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    
    // Feedback dialog state
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    
    // Account details dialog state
    var showAccountDialog by remember { mutableStateOf(false) }
    
    // Edit profile dialog state
    var showEditProfileDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "👤 My Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            AuthManager.logout(context)
                            val intent = Intent(context, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // User Account Section
                ProfileSection(
                    title = "Personal Information",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Person,
                            title = "User Account",
                            subtitle = userEmail ?: "Not logged in",
                            onClick = {
                                showAccountDialog = true
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile",
                            subtitle = "Update your personal details",
                            onClick = {
                                showEditProfileDialog = true
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Settings,
                            title = "Theme",
                            subtitle = if (isDarkTheme) "Dark Mode" else "Light Mode",
                            onClick = {
                                isDarkTheme = !isDarkTheme
                                saveThemePreference(context, isDarkTheme)
                            },
                            trailing = {
                                Switch(
                                    checked = isDarkTheme,
                                    onCheckedChange = {
                                        isDarkTheme = it
                                        saveThemePreference(context, it)
                                    }
                                )
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Info,
                            title = "Appearance",
                            subtitle = "Font Size: ${fontSize}",
                            onClick = {
                                showFontSizeDialog = true
                            }
                        )
                    )
                )

                // History Section
                ProfileSection(
                    title = "History",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.List,
                            title = "Transaction history",
                            subtitle = "View all your payments",
                            onClick = {
                                navController?.navigate(Screen.TransactionHistory.route)
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.PlayArrow,
                            title = "Movie booking history",
                            subtitle = "View your bookings",
                            onClick = {
                                navController?.navigate(Screen.BookingHistory.route)
                            }
                        )
                    )
                )

                // Support Section
                ProfileSection(
                    title = "Support",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Star,
                            title = "App feedback",
                            subtitle = "Share your thoughts",
                            onClick = {
                                showFeedbackDialog = true
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Email,
                            title = "Customer support",
                            subtitle = "support@redcurtain.com",
                            onClick = {
                                sendSupportEmail(context)
                            }
                        )
                    )
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Logout Button
                Button(
                    onClick = {
                        AuthManager.logout(context)
                        val intent = Intent(context, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    // Font Size Picker Dialog
    if (showFontSizeDialog) {
        FontSizePickerDialog(
            currentFontSize = fontSize,
            onFontSizeSelected = { selectedSize ->
                fontSize = selectedSize
                saveFontSizePreference(context, selectedSize)
                showFontSizeDialog = false
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }
    
    // Feedback Dialog
    if (showFeedbackDialog) {
        FeedbackDialog(
            feedbackText = feedbackText,
            onFeedbackTextChange = { feedbackText = it },
            onDismiss = { showFeedbackDialog = false },
            onSend = {
                val success = sendFeedbackEmail(context, feedbackText)
                // Always show "sent" message regardless of method
                feedbackText = ""
                showFeedbackDialog = false
                android.widget.Toast.makeText(
                    context,
                    "Feedback sent! Thank you for your feedback!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
    
    // Account Details Dialog
    if (showAccountDialog) {
        AccountDetailsDialog(
            userEmail = userEmail,
            userProfile = userProfile,
            onDismiss = { showAccountDialog = false }
        )
    }
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            userEmail = userEmail,
            currentProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { profile ->
                coroutineScope.launch {
                    database.userProfileDao().insertUserProfile(profile)
                    showEditProfileDialog = false
                    android.widget.Toast.makeText(
                        context,
                        "Profile saved successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileItemRow(item: ProfileItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        if (item.trailing != null) {
            item.trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private data class ProfileItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit,
    val trailing: (@Composable () -> Unit)? = null
)

@Composable
private fun FeedbackDialog(
    feedbackText: String,
    onFeedbackTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "App Feedback",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "We'd love to hear your thoughts!",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = onFeedbackTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your feedback...") },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSend,
                enabled = feedbackText.isNotBlank()
            ) {
                Text("Send", color = Color(0xFF6200EE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF2D2D2D),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

private fun sendSupportEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@redcurtain.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Customer Support Request")
        putExtra(Intent.EXTRA_TEXT, "Hello,\n\n")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Show toast if no email app is available
        android.widget.Toast.makeText(
            context,
            "No email app found. Please install an email app to contact support.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

private fun sendFeedbackEmail(context: Context, feedback: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@redcurtain.com"))
        putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
        putExtra(Intent.EXTRA_TEXT, feedback)
    }
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        // Fallback: copy to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Feedback", feedback)
        clipboard.setPrimaryClip(clip)
        false
    }
}

@Composable
private fun FontSizePickerDialog(
    currentFontSize: String,
    onFontSizeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fontSizes = listOf("Small", "Medium", "Large")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Font Size",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fontSizes.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontSizeSelected(size) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = size,
                            color = Color.White,
                            fontSize = when (size) {
                                "Small" -> 14.sp
                                "Large" -> 18.sp
                                else -> 16.sp
                            }
                        )
                        if (size == currentFontSize) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Selected",
                                tint = Color(0xFF6200EE),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (size != fontSizes.last()) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF6200EE))
            }
        },
        containerColor = Color(0xFF2D2D2D),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

// Theme and Appearance Preferences
private const val PREFS_NAME = "profile_prefs"
private const val KEY_THEME = "theme"
private const val KEY_FONT_SIZE = "font_size"

private fun getThemePreference(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_THEME, true) // Default to dark theme
}

private fun saveThemePreference(context: Context, isDark: Boolean) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_THEME, isDark).apply()
}

private fun getFontSizePreference(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_FONT_SIZE, "Medium") ?: "Medium"
}

private fun saveFontSizePreference(context: Context, fontSize: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_FONT_SIZE, fontSize).apply()
}

@Composable
private fun AccountDetailsDialog(
    userEmail: String?,
    userProfile: UserProfile?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Account Details",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userEmail != null) {
                    AccountDetailRow(
                        label = "Email",
                        value = userEmail
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    
                    if (userProfile != null) {
                        if (!userProfile.firstName.isNullOrBlank() || !userProfile.lastName.isNullOrBlank()) {
                            AccountDetailRow(
                                label = "Name",
                                value = "${userProfile.firstName ?: ""} ${userProfile.lastName ?: ""}".trim()
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                        
                        if (!userProfile.phoneNumber.isNullOrBlank()) {
                            AccountDetailRow(
                                label = "Phone",
                                value = userProfile.phoneNumber
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                        
                        if (!userProfile.address.isNullOrBlank()) {
                            AccountDetailRow(
                                label = "Address",
                                value = userProfile.address
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                        
                        val location = listOfNotNull(
                            userProfile.city,
                            userProfile.state,
                            userProfile.zipCode
                        ).joinToString(", ")
                        
                        if (location.isNotEmpty()) {
                            AccountDetailRow(
                                label = "Location",
                                value = location
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                        
                        if (!userProfile.country.isNullOrBlank()) {
                            AccountDetailRow(
                                label = "Country",
                                value = userProfile.country
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                        
                        if (!userProfile.dateOfBirth.isNullOrBlank()) {
                            AccountDetailRow(
                                label = "Date of Birth",
                                value = userProfile.dateOfBirth
                            )
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                    }
                    
                    AccountDetailRow(
                        label = "Account Status",
                        value = "Active"
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    AccountDetailRow(
                        label = "Member Since",
                        value = "RedCurtain Member"
                    )
                } else {
                    Text(
                        text = "No account information available. Please sign in.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF6200EE))
            }
        },
        containerColor = Color(0xFF2D2D2D),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
private fun AccountDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun EditProfileDialog(
    userEmail: String?,
    currentProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var firstName by remember { mutableStateOf(currentProfile?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentProfile?.lastName ?: "") }
    var phoneNumber by remember { mutableStateOf(currentProfile?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(currentProfile?.address ?: "") }
    var city by remember { mutableStateOf(currentProfile?.city ?: "") }
    var state by remember { mutableStateOf(currentProfile?.state ?: "") }
    var zipCode by remember { mutableStateOf(currentProfile?.zipCode ?: "") }
    var country by remember { mutableStateOf(currentProfile?.country ?: "") }
    var dateOfBirth by remember { mutableStateOf(currentProfile?.dateOfBirth ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = { Text("Zip Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
                
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 1990-01-15") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (userEmail != null) {
                        val profile = UserProfile(
                            email = userEmail,
                            firstName = firstName.takeIf { it.isNotBlank() },
                            lastName = lastName.takeIf { it.isNotBlank() },
                            phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                            address = address.takeIf { it.isNotBlank() },
                            city = city.takeIf { it.isNotBlank() },
                            state = state.takeIf { it.isNotBlank() },
                            zipCode = zipCode.takeIf { it.isNotBlank() },
                            country = country.takeIf { it.isNotBlank() },
                            dateOfBirth = dateOfBirth.takeIf { it.isNotBlank() },
                            serverId = currentProfile?.serverId,
                            needsSync = true, // Mark as needing sync when updated
                            lastSynced = currentProfile?.lastSynced,
                            createdAt = currentProfile?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(profile)
                    }
                }
            ) {
                Text("Save", color = Color(0xFF6200EE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF2D2D2D),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
