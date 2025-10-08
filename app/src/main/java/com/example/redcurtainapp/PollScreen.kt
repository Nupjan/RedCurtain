package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.redcurtainapp.Movie
import com.example.redcurtainapp.MovieDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsScreen() {
    val context = LocalContext.current
    val db = remember(context) { MovieDatabase.getDatabase(context) }
    val moviesFlow = remember(db) { db.movieDao().getAllMoviesRandom() }
    val movies by moviesFlow.collectAsState(initial = emptyList())
    val options = remember(movies) { movies.take(6).map(Movie::title) }

    var selectedOption by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🗳 Movie Polls") })
        }
    ) { padding ->
        if (submitted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Thank you for the vote! Please wait for the result.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Which movie do you want to be screened?", style = MaterialTheme.typography.titleMedium)
                options.forEach { option ->
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { selectedOption = option }
                        )
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Button(
                    onClick = { if (selectedOption.isNotBlank()) submitted = true },
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
