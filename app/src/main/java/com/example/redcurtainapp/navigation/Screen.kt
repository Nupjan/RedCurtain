package com.example.redcurtainapp.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Polls : Screen("polls")
    object Profile : Screen("profile")

}