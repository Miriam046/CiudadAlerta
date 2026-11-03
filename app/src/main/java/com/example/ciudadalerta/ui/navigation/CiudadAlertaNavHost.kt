package com.example.ciudadalerta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ciudadalerta.data.AppDatabase
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.ui.screens.DashboardScreen
import com.example.ciudadalerta.ui.screens.DetalleTicketScreen
import com.example.ciudadalerta.ui.screens.ReportarScreen
import com.example.ciudadalerta.ui.viewmodels.DashboardViewModel
import com.example.ciudadalerta.ui.viewmodels.DashboardViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModel
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModel
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModelFactory

@Composable
fun CiudadAlertaApp(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = TicketRepository(database.ticketDao())

    NavHost(navController = navController, startDestination = "dashboard") {

        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate("reportar") },
                onNavigateToDetail = { ticketId -> navController.navigate("detalle/$ticketId") }
            )
        }

        composable("reportar") {
            val viewModel: ReportarViewModel = viewModel(
                factory = ReportarViewModelFactory(repository)
            )
            ReportarScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("detalle/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId")?.toIntOrNull() ?: 0
            val viewModel: DetalleViewModel = viewModel(
                factory = DetalleViewModelFactory(repository, ticketId)
            )
            DetalleTicketScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}