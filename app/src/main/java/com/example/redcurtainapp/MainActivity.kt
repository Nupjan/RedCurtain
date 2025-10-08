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
import com.example.redcurtainapp.ui.screens.MovieDetailScreen
import com.example.redcurtainapp.ui.screens.SettingsScreen
import com.example.redcurtainapp.ui.screens.SplashScreen
import com.example.redcurtainapp.ui.screens.SearchScreen
import com.example.redcurtainapp.ui.theme.RedCurtainAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedCurtainAppTheme {
                RedCurtainApp()
            }
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } })
            }
            composable(Screen.Home.route) { MovieGridScreen(navController) }
            composable(Screen.Polls.route) { PollsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.MovieDetail.route) { backStackEntry ->
                val raw = backStackEntry.arguments?.getString("title") ?: ""
                val title = java.net.URLDecoder.decode(raw, "UTF-8")
                MovieDetailScreen(titleArg = title)
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Search.route) {
                SearchScreen(onSelect = { movie ->
                    val encoded = java.net.URLEncoder.encode(movie.title, "UTF-8")
                    navController.navigate("movieDetail/$encoded")
                })
            }
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
