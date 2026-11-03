package com.example.ciudadalerta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ciudadalerta.data.AppDatabase
import com.example.ciudadalerta.data.SessionManager
import com.example.ciudadalerta.data.TicketRepository
import com.example.ciudadalerta.ui.screens.DetalleTicketScreen
import com.example.ciudadalerta.ui.screens.FeedReportesScreen
import com.example.ciudadalerta.ui.screens.LoginEncargadoScreen
import com.example.ciudadalerta.ui.screens.ReportarScreen
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModel
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.FeedViewModel
import com.example.ciudadalerta.ui.viewmodels.FeedViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.LoginViewModel
import com.example.ciudadalerta.ui.viewmodels.LoginViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModel
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModelFactory

@Composable
fun CiudadAlertaApp(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val sessionManager = SessionManager.getInstance(context)
    val repository = TicketRepository(database.ticketDao())

    NavHost(navController = navController, startDestination = "feed_reportes") {

        composable("feed_reportes") {
            val viewModel: FeedViewModel = viewModel(
                factory = FeedViewModelFactory(repository, sessionManager)
            )
            FeedReportesScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate("login") { launchSingleTop = true } },
                onNavigateToCreate = { navController.navigate("reportar") { launchSingleTop = true } },
                onNavigateToDetail = { ticketId -> navController.navigate("detalle/$ticketId") { launchSingleTop = true } }
            )
        }

        composable("login") {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(sessionManager)
            )
            LoginEncargadoScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
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
                factory = DetalleViewModelFactory(repository, sessionManager, ticketId)
            )
            DetalleTicketScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}