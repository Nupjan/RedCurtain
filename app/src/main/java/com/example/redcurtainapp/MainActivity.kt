package com.example.redcurtainapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.redcurtainapp.navigation.Screen
import com.example.redcurtainapp.ui.screens.BookingSummaryScreen
import com.example.redcurtainapp.ui.screens.MovieDetailScreen
import com.example.redcurtainapp.ui.screens.PollsScreen
import com.example.redcurtainapp.ui.screens.ProfileScreen
import com.example.redcurtainapp.ui.screens.SeatingScreen
import com.example.redcurtainapp.ui.screens.SplashScreen
import com.example.redcurtainapp.ui.theme.RedCurtainAppTheme
import java.net.URLDecoder
import com.example.redcurtainapp.model.Seat
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to sign-in if not logged in
        if (!AuthManager.isLoggedIn(this)) {
            redirectToSignIn()
            return
        }

        setContent {
            RedCurtainAppTheme {
                AppNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If user logged out from a screen, return to sign-in
        if (!AuthManager.isLoggedIn(this)) {
            redirectToSignIn()
        }
    }

    private fun redirectToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Polls, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    val label = when (screen) {
                        Screen.Home -> "Home"
                        Screen.Polls -> "Polls"
                        Screen.Profile -> "Profile"
                        else -> screen.route
                    }
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(screen.route) == true,
                        onClick = {
                            if (currentRoute?.startsWith(screen.route) != true) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = { Text(label) },
                        icon = { /* simple text labels only */ }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
        composable(Screen.Home.route) {
            MovieGridScreen(navController)
        }
            // Search and Settings are removed from bottom navigation and routes
        composable(Screen.Polls.route) {
            PollsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailScreen(movieId = movieId, navController = navController)
        }
        composable(
            route = Screen.Seating.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("movieTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            val movieTitle = backStackEntry.arguments?.getString("movieTitle").orEmpty()
            SeatingScreen(
                movieId = movieId,
                movieTitle = URLDecoder.decode(movieTitle, "UTF-8"),
                navController = navController
            )
        }
        composable(
            route = Screen.BookingSummary.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("movieTitle") { type = NavType.StringType },
                navArgument("selectedSeats") { type = NavType.StringType },
                navArgument("selectedDate") { type = NavType.StringType },
                navArgument("selectedTime") { type = NavType.StringType },
                navArgument("totalPrice") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            val movieTitle = URLDecoder.decode(backStackEntry.arguments?.getString("movieTitle").orEmpty(), "UTF-8")
            val seatsString = backStackEntry.arguments?.getString("selectedSeats").orEmpty()
            val selectedDate = URLDecoder.decode(backStackEntry.arguments?.getString("selectedDate").orEmpty(), "UTF-8")
            val selectedTime = URLDecoder.decode(backStackEntry.arguments?.getString("selectedTime").orEmpty(), "UTF-8")
            val totalPrice = backStackEntry.arguments?.getString("totalPrice")?.toDoubleOrNull() ?: 0.0
            
            // Parse seats
            val selectedSeats = if (seatsString.isNotEmpty()) {
                seatsString.split(",").mapNotNull { seatString ->
                    if (seatString.length >= 2) {
                        val row = seatString[0].toString()
                        val number = seatString.substring(1).toIntOrNull()
                        if (number != null) {
                            Seat(
                                id = seatString,
                                row = row,
                                number = number,
                                type = com.example.redcurtainapp.model.SeatType.SELECTED,
                                price = if (row in listOf("A", "B")) 18.0 else 12.0
                            )
                        } else null
                    } else null
                }
            } else emptyList()
            
            BookingSummaryScreen(
                movieId = movieId,
                movieTitle = movieTitle,
                selectedSeats = selectedSeats,
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                totalPrice = totalPrice,
                navController = navController
            )
        }
        }
    }
}
