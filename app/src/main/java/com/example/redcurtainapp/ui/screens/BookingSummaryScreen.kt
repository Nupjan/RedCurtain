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
import com.example.redcurtainapp.model.Seat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    movieId: String,
    movieTitle: String,
    selectedSeats: List<Seat>,
    selectedDate: String,
    selectedTime: String,
    totalPrice: Double,
    navController: NavHostController? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Booking Summary",
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1A1A)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Movie Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Movie Details",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(color = Color.Gray)
                        
                        Text(
                            text = movieTitle,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (selectedDate.isNotEmpty()) {
                            Text(
                                text = "Date: $selectedDate",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        
                        if (selectedTime.isNotEmpty()) {
                            Text(
                                text = "Time: $selectedTime",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Seats Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Selected Seats",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(color = Color.Gray)
                        
                        Text(
                            text = selectedSeats.joinToString(", ") { "${it.row}${it.number}" },
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        
                        Text(
                            text = "Quantity: ${selectedSeats.size}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Pricing Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pricing Summary",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(color = Color.Gray)
                        
                        selectedSeats.forEach { seat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Seat ${seat.row}${seat.number}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "$${String.format("%.2f", seat.price)}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Divider(color = Color.Gray)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${String.format("%.2f", totalPrice)}",
                                color = Color(0xFF6200EE),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Continue Payment Button
            item {
                Button(
                    onClick = { 
                        // TODO: Navigate to payment screen
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Complete Payment",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Cancel Button
            item {
                TextButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel Booking",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
