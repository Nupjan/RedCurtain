package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.redcurtainapp.Movie
import com.example.redcurtainapp.MovieDatabase
import com.example.redcurtainapp.AuthManager
import com.example.redcurtainapp.PollManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsScreen() {
    val context = LocalContext.current
    val db = remember(context) { MovieDatabase.getDatabase(context) }
    val moviesFlow = remember(db) { db.movieDao().getAllMoviesRandom() }
    val movies by moviesFlow.collectAsState(initial = emptyList())
    val options = remember(movies) { movies.take(6).map(Movie::title) }
    val isAdmin = remember { AuthManager.isAdmin(context) }
    val userEmail = remember { AuthManager.getUserEmail(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedOption by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var voteCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var hasVoted by remember { mutableStateOf(false) }
    var userVote by remember { mutableStateOf<String?>(null) }
    
    // Load vote counts for admin and check if user has voted
    LaunchedEffect(isAdmin, options, userEmail, submitted) {
        // Always refresh vote counts for admin, especially after voting
        if (isAdmin) {
            voteCounts = PollManager.getAllVotes(context)
        }
        try {
            hasVoted = PollManager.hasUserVoted(context, userEmail)
            userVote = PollManager.getUserVote(context, userEmail)
        } catch (e: Exception) {
            // Handle any errors gracefully
            hasVoted = false
            userVote = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("🗳 Movie Polls" + if (isAdmin) " (Admin View)" else "") 
                }
            )
        }
    ) { padding ->
        if (submitted || hasVoted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (hasVoted && userVote != null) 
                        "You voted for: $userVote" 
                    else 
                        "Thank you for the vote! Please wait for the result.",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "📊 Vote Counts (Admin View)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                            if (options.isNotEmpty()) {
                                options.forEach { option ->
                                    val count = voteCounts[option] ?: 0
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(option, color = Color.White)
                                        Text(
                                            "$count votes",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6200EE)
                                        )
                                    }
                                }
                            } else {
                                Text("No votes yet", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        } else if (options.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No movies available for polls.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Which movie do you want to be screened?", 
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (isAdmin) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "📊 Vote Counts (Admin View)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                            options.forEach { option ->
                                val count = voteCounts[option] ?: 0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(option, color = Color.White)
                                    Text(
                                        "$count votes",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6200EE)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Text("Cast your vote:", style = MaterialTheme.typography.bodyMedium)
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedOption == option),
                                onClick = { selectedOption = option }
                            )
                            Text(option, modifier = Modifier.padding(start = 8.dp))
                        }
                        if (isAdmin) {
                            Text(
                                "${voteCounts[option] ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { 
                        if (selectedOption.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val success = PollManager.submitVote(context, selectedOption, userEmail)
                                    if (success) {
                                        submitted = true
                                        hasVoted = true
                                        userVote = selectedOption
                                        // Force refresh vote counts for admin
                                        if (isAdmin) {
                                            voteCounts = PollManager.getAllVotes(context)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Handle error - could show a toast here
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    enabled = selectedOption.isNotBlank(),
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Submit Vote")
                }
            }
        }
    }
}
