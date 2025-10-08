package com.example.redcurtainapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("👤 My Profile") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Name: John Doe", style = MaterialTheme.typography.bodyLarge)
            Text("Email: johndoe@example.com", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { /* TODO: Edit profile */ }) {
                Text("Edit Profile")
            }
        }
    }
}
