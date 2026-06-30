package com.pulse.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pulse.app.ui.chores.ChoresScreen
import com.pulse.app.ui.clock.ClockScreen
import com.pulse.app.ui.habits.HabitsScreen
import com.pulse.app.ui.pomodoro.PomodoroScreen

@Composable
fun PulseNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "clock",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("clock") { ClockScreen() }
            composable("pomodoro") { PomodoroScreen() }
            composable("habits") { HabitsScreen() }
            composable("chores") { ChoresScreen() }
        }
    }
}
