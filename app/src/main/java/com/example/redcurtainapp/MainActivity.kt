package com.example.redcurtainapp
import java.util.Locale


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.redcurtainapp.navigation.Screen
import com.example.redcurtainapp.MovieGridScreen
import com.example.redcurtainapp.ui.screens.PollsScreen
import com.example.redcurtainapp.ui.screens.ProfileScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedCurtainApp()
        }
    }
}

@Composable
fun RedCurtainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { MovieGridScreen() }
            composable(Screen.Polls.route) { PollsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Home,  Screen.Polls, Screen.Profile)
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { /* TODO: add icon */ },
                label = { Text(screen.route.replaceFirstChar { it.uppercase(Locale.ROOT) }) },
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) }
            )
        }
    }
}
