package com.example.redcurtainapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Polls : Screen("polls")
    object Profile : Screen("profile")
    object MovieDetail : Screen("movieDetail/{movieId}")
    object Seating : Screen("seating/{movieId}/{movieTitle}")
    object BookingSummary : Screen("bookingSummary/{movieId}/{movieTitle}/{selectedSeats}/{selectedDate}/{selectedTime}/{totalPrice}")
    object Settings : Screen("settings")
    object Search : Screen("search")
}
