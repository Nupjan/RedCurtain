package com.example.redcurtainapp.ui.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val userEmail = remember { AuthManager.getUserEmail(context) }
    
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
                            onClick = { /* Account details - could expand this */ }
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
                if (success) {
                    feedbackText = ""
                    showFeedbackDialog = false
                    // Show success toast
                    android.widget.Toast.makeText(
                        context,
                        "Thank you for your feedback!",
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
