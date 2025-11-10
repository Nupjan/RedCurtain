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
import com.example.redcurtainapp.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(navController: NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { MovieDatabase.getDatabase(context) }
    val userEmail = remember { com.example.redcurtainapp.AuthManager.getUserEmail(context) }
    
    // Use Flow to observe transactions reactively
    val transactionsFlow = remember(userEmail) {
        if (userEmail != null) {
            database.transactionDao().getTransactionsByUser(userEmail)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList<Transaction>())
        }
    }
    
    val transactions by transactionsFlow.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Transaction History",
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
                transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "💳",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "No transactions yet",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your transaction history will appear here",
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
                        items(transactions) { transaction ->
                            TransactionHistoryItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Movie Title and Transaction Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.movieTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTransactionDate(transaction.transactionDate),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            
            // Payment Method and Card Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "Payment Method",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = transaction.paymentMethod,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (transaction.cardLastFour != null) {
                    Column {
                        Text(
                            text = "Card",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "**** ${transaction.cardLastFour}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            
            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "$${String.format("%.2f", transaction.amount)}",
                    color = Color(0xFF6200EE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTransactionDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

