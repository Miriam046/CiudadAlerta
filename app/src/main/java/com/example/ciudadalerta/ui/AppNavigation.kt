package com.example.ciudadalerta.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instanciamos el ViewModel aquí para que viva durante toda la navegación
    val reportViewModel: ReportViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = reportViewModel,
                onNavigateToCreate = { navController.navigate("create") }
            )
        }
        composable("create") {
            CreateReportScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}