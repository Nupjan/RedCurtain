package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsScreen() {
    var selectedOption by remember { mutableStateOf("") }
    val options = listOf("Great!", "Good", "Average", "Poor")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🗳 Movie Polls") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("How was your last movie experience?", style = MaterialTheme.typography.titleMedium)
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
                onClick = { /* handle submission */ },
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            ) {
                Text("Submit Vote")
            }
        }
    }
}
