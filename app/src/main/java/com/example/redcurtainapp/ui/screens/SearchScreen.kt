package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redcurtainapp.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onSelect: (Movie) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🔍 Search Movies") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search movies...") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { 
                    // TODO: Implement search functionality
                    // For now, just show a placeholder
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }
            
            if (searchResults.isNotEmpty()) {
                Text(
                    text = "Search results:",
                    style = MaterialTheme.typography.titleMedium
                )
                searchResults.forEach { movie ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onSelect(movie) }
                    ) {
                        Text(
                            text = movie.title,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
