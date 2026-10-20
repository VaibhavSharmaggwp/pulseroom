package com.workspace.pulseroom.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workspace.pulseroom.ui.main.screens.DashboardScreen
import com.workspace.pulseroom.ui.main.screens.LiveRoomScreen
import com.workspace.pulseroom.ui.main.viewmodels.RoomsViewModel

@Composable
fun AppNavigation(viewModel: RoomsViewModel) {
    // NavController driver for screen switching
    val navController = rememberNavController()

    // NavHost defines all application routes
    NavHost(navController = navController, startDestination = "dashboard") {
        // Route 1: Dashboard
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onJoinRoom = { code ->
                    // Jab user code dale, usko live_room ke url par bhej do
                    navController.navigate("live_room/$code")
                },
                onCreateRoom = {
                    // Random code generate karke room bana denge
                    val newCode = (1000..9999).random().toString()
                    navController.navigate("live_room/ROOM_$newCode")
                }
            )
        }

        // Route 2: The Live Canvas (with dynamic ID)
        composable(
            route = "live_room/{roomId}",
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.StringType
                    defaultValue = "error_room"
                }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "error_room"

            LiveRoomScreen(
                roomId = roomId,
                onBackClick = {
                    viewModel.leaveRoom()
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
