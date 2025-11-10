package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.redcurtainapp.MovieDatabase
import com.example.redcurtainapp.model.Booking
import com.example.redcurtainapp.model.SeatBooking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { MovieDatabase.getDatabase(context) }
    val userEmail = remember { com.example.redcurtainapp.AuthManager.getUserEmail(context) }
    
    // Use Flow to observe bookings reactively
    val bookingsFlow = remember(userEmail) {
        if (userEmail != null) {
            database.bookingDao().getBookingsByUser(userEmail)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList<Booking>())
        }
    }
    
    val bookings by bookingsFlow.collectAsState(initial = emptyList())
    
    var seatBookingsMap by remember { mutableStateOf<Map<Long, List<SeatBooking>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Load seat bookings when bookings change
    LaunchedEffect(bookings) {
        isLoading = true
        error = null
        try {
            val seatMap = mutableMapOf<Long, List<SeatBooking>>()
            withContext(Dispatchers.IO) {
                bookings.forEach { booking ->
                    val seats = database.seatBookingDao().getSeatBookingsByBookingId(booking.id)
                    seatMap[booking.id] = seats
                }
            }
            seatBookingsMap = seatMap
        } catch (e: Exception) {
            error = e.message ?: "Failed to load seat details"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Booking History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1A1A1A))
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF6200EE))
                            Text(
                                text = "Loading booking history...",
                                color = Color.White
                            )
                        }
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "Error loading bookings",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = error ?: "Unknown error",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                bookings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🎬",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "No bookings yet",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your booking history will appear here",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingHistoryItem(
                                booking = booking,
                                seatBookings = seatBookingsMap[booking.id] ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingHistoryItem(
    booking: Booking,
    seatBookings: List<SeatBooking>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Movie Title and Booking Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.movieTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatBookingDate(booking.bookingDate),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            
            // Show Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "Show Date",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = booking.selectedDate,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = "Show Time",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = booking.selectedTime,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Seats
            if (seatBookings.isNotEmpty()) {
                Column {
                    Text(
                        text = "Seats",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = seatBookings.joinToString(", ") { "${it.seatRow}${it.seatNumber}" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            
            // Total Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "$${String.format("%.2f", booking.totalPrice)}",
                    color = Color(0xFF6200EE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatBookingDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

