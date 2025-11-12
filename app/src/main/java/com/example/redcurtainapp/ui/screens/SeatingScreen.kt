package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.redcurtainapp.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.net.URLEncoder
import com.example.redcurtainapp.MovieDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatingScreen(
    movieId: String,
    movieTitle: String,
    navController: NavHostController? = null
) {
    var selectedSeats by remember { mutableStateOf<List<Seat>>(emptyList()) }
    var cinemaHall by remember { mutableStateOf(createSampleCinemaHall()) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var bookedSeatIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMissingDateTimeDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { MovieDatabase.getDatabase(context) }
    
    val totalPrice = selectedSeats.sumOf { it.price }
    val scrollState = rememberLazyListState()
    
    // Load booked seats when date/time/movieId changes
    LaunchedEffect(movieId, selectedDate, selectedTime) {
        if (movieId.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
            try {
                val booked = withContext(Dispatchers.IO) {
                    database.seatBookingDao().getBookedSeatIdsForShow(movieId, selectedDate, selectedTime)
                }
                val bookedSet = booked.toSet()
                bookedSeatIds = bookedSet
                
                // Remove any booked seats from selectedSeats
                selectedSeats = selectedSeats.filter { seat ->
                    seat.id !in bookedSet
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bookedSeatIds = emptySet()
            }
        } else {
            bookedSeatIds = emptySet()
        }
    }
    
    // Scroll to top when screen is first displayed
    LaunchedEffect(Unit) {
        scrollState.scrollToItem(0)
    }
    
    Scaffold(
        topBar = {
            Box(modifier = Modifier.padding(top = 32.dp)) {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Choose Seats",
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
        },
        bottomBar = {
            if (selectedSeats.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Seats: ${selectedSeats.joinToString(", ") { "${it.row}${it.number}" }}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total: $${String.format("%.2f", totalPrice)}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedDate.isBlank() || selectedTime.isBlank()) {
                                    // Show prompt
                                    showMissingDateTimeDialog = true
                                } else if (selectedSeats.isEmpty()) {
                                    // Show prompt for no seats selected
                                    android.widget.Toast.makeText(
                                        context,
                                        "Please select at least one seat",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Validate that selected seats are not already booked
                                    val selectedSeatIds = selectedSeats.map { "${it.row}${it.number}" }.toSet()
                                    val conflictingSeats = selectedSeatIds.intersect(bookedSeatIds)
                                    
                                    if (conflictingSeats.isNotEmpty()) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Seat(s) ${conflictingSeats.joinToString(", ")} are no longer available. Please select different seats.",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        // Remove conflicting seats from selection
                                        selectedSeats = selectedSeats.filter { "${it.row}${it.number}" !in conflictingSeats }
                                    } else {
                                        // All seats are available - navigate to booking summary
                                        val seatsString = selectedSeats.joinToString(",") { "${it.row}${it.number}" }
                                        navController?.navigate(
                                            "bookingSummary/$movieId/${URLEncoder.encode(movieTitle, "UTF-8")}/$seatsString/${URLEncoder.encode(selectedDate, "UTF-8")}/${URLEncoder.encode(selectedTime, "UTF-8")}/$totalPrice"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6200EE)
                            )
                        ) {
                            Text(
                                text = "Continue Booking",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFF1A1A1A))
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Movie Info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = movieTitle,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cinema Hall: ${cinemaHall.name}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            
                            // Date and Time Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Date Picker
                                Button(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6200EE)
                                    )
                                ) {
                                    Text(
                                        text = if (selectedDate.isNotEmpty()) selectedDate else "Select Date",
                                        fontSize = 12.sp
                                    )
                                }
                                
                                // Time Picker
                                Button(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6200EE)
                                    )
                                ) {
                                    Text(
                                        text = if (selectedTime.isNotEmpty()) selectedTime else "Select Time",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            
            // Screen Indicator
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SCREEN",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Seat Grid
            items(cinemaHall.rows) { row ->
                SeatRow(
                    row = row,
                    seatsPerRow = cinemaHall.seatsPerRow,
                    premiumRows = cinemaHall.premiumRows,
                    basePrice = cinemaHall.basePrice,
                    premiumPrice = cinemaHall.premiumPrice,
                    selectedSeats = selectedSeats,
                    bookedSeatIds = bookedSeatIds,
                    onSeatClick = { seat ->
                        // Prevent selecting booked seats
                        if (seat.type != SeatType.OCCUPIED && seat.isSelectable) {
                            selectedSeats = if (selectedSeats.any { it.id == seat.id }) {
                                selectedSeats.filter { it.id != seat.id }
                            } else {
                                selectedSeats + seat
                            }
                        }
                    }
                )
            }
            
            // Seat Legend
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeatLegendItem(
                            color = Color.Green,
                            label = "Available"
                        )
                        SeatLegendItem(
                            color = Color.Red,
                            label = "Occupied"
                        )
                        SeatLegendItem(
                            color = Color.Blue,
                            label = "Selected"
                        )
                        SeatLegendItem(
                            color = Color.Yellow,
                            label = "Premium"
                        )
                    }
                }
            }
            }
            
            // Date Picker Dialog - outside LazyColumn but inside Box
            if (showDatePicker) {
                DatePickerDialog(
                    onDateSelected = { date ->
                        selectedDate = date
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
            
            // Time Picker Dialog - outside LazyColumn but inside Box
            if (showTimePicker) {
                TimePickerDialog(
                    onTimeSelected = { time ->
                        selectedTime = time
                        showTimePicker = false
                    },
                    onDismiss = { showTimePicker = false }
                )
            }

            // Missing date/time dialog
            if (showMissingDateTimeDialog) {
                AlertDialog(
                    onDismissRequest = { showMissingDateTimeDialog = false },
                    title = { Text("Select date & time") },
                    text = { Text("Please select a date and time before continuing.") },
                    confirmButton = {
                        TextButton(onClick = { showMissingDateTimeDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SeatRow(
    row: String,
    seatsPerRow: Int,
    premiumRows: List<String>,
    basePrice: Double,
    premiumPrice: Double,
    selectedSeats: List<Seat>,
    bookedSeatIds: Set<String>,
    onSeatClick: (Seat) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Row Label
        Text(
            text = row,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Seats
        repeat(seatsPerRow) { seatNumber ->
            val seatId = "${row}${seatNumber + 1}"
            val isPremium = row in premiumRows
            
            // Check if seat is booked
            val isBooked = seatId in bookedSeatIds
            
            // Determine seat type - booked seats should always be OCCUPIED, never SELECTED
            val baseSeatType = when {
                isBooked -> SeatType.OCCUPIED
                isPremium -> SeatType.PREMIUM
                else -> SeatType.AVAILABLE
            }
            
            val seat = Seat(
                id = seatId,
                row = row,
                number = seatNumber + 1,
                type = baseSeatType,
                price = if (isPremium) premiumPrice else basePrice
            )
            
            // Check if this seat is currently selected AND not booked
            val finalSeat = if (!isBooked && selectedSeats.any { it.id == seatId }) {
                seat.copy(type = SeatType.SELECTED)
            } else {
                seat
            }
            
            SeatItem(
                seat = finalSeat,
                onClick = { onSeatClick(finalSeat) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun SeatItem(
    seat: Seat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (seat.type) {
        SeatType.AVAILABLE -> Color.Green.copy(alpha = 0.7f)
        SeatType.SELECTED -> Color.Blue.copy(alpha = 0.7f)
        SeatType.OCCUPIED -> Color.Red.copy(alpha = 0.7f)
        SeatType.PREMIUM -> Color.Yellow.copy(alpha = 0.7f)
        SeatType.DISABLED -> Color.Gray.copy(alpha = 0.7f)
    }
    
    val textColor = when (seat.type) {
        SeatType.AVAILABLE, SeatType.SELECTED, SeatType.PREMIUM -> Color.White
        SeatType.OCCUPIED, SeatType.DISABLED -> Color.Black
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(enabled = seat.isSelectable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.number.toString(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SeatLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

private fun createSampleCinemaHall(): CinemaHall {
    return CinemaHall(
        id = "hall1",
        name = "Screen 1",
        rows = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"),
        seatsPerRow = 9,
        premiumRows = listOf("A", "B"),
        basePrice = 12.0,
        premiumPrice = 18.0
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Get current date and set time to start of today
    val today = remember { 
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    
    // Calculate tomorrow (minimum selectable date - after current date)
    val tomorrow = remember {
        Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = tomorrow.timeInMillis,
        yearRange = IntRange(
            today.get(Calendar.YEAR),
            today.get(Calendar.YEAR) + 1 // Allow up to 1 year in the future
        )
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = Date(millis)
                    val tomorrowStart = tomorrow.timeInMillis
                    
                    // Validate that selected date is after today (tomorrow or later)
                    if (millis >= tomorrowStart) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        onDateSelected(dateFormat.format(selectedDate))
                        onDismiss()
                    } else {
                        // This shouldn't happen if date picker is configured correctly,
                        // but add validation as a safety measure
                        onDismiss()
                    }
                } ?: run {
                    onDismiss()
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            // Some library versions don't support dateValidator; we validate on confirm instead.
        )
    }
}

@Composable
private fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column {
                HourMinutePicker(
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(timeString)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun HourMinutePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hour", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(24) { hour ->
                    Box(
                        modifier = Modifier
                            .clickable { onHourChange(hour) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", hour),
                            color = if (hour == selectedHour) Color(0xFF6200EE) else Color.Gray,
                            fontWeight = if (hour == selectedHour) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Minute", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                items(60) { minute ->
                    Box(
                        modifier = Modifier
                            .clickable { onMinuteChange(minute) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", minute),
                            color = if (minute == selectedMinute) Color(0xFF6200EE) else Color.Gray,
                            fontWeight = if (minute == selectedMinute) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}   
