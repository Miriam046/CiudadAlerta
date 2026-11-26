package com.example.ciudadalerta.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.CiudadAlertaApplication
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.DefaultAppContainer
import com.example.ciudadalerta.ui.screens.DetalleTicketScreen
import com.example.ciudadalerta.ui.screens.EstadisticasScreen
import com.example.ciudadalerta.ui.screens.FeedReportesScreen
import com.example.ciudadalerta.ui.screens.LoginEncargadoScreen
import com.example.ciudadalerta.ui.screens.MapaIncidenciasScreen
import com.example.ciudadalerta.ui.screens.ReportarScreen
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModel
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.EstadisticasViewModel
import com.example.ciudadalerta.ui.viewmodels.EstadisticasViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.FeedViewModel
import com.example.ciudadalerta.ui.viewmodels.FeedViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.LoginViewModel
import com.example.ciudadalerta.ui.viewmodels.LoginViewModelFactory
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModel
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModelFactory

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Feed : AppRoute("feed_reportes")
    data object Map : AppRoute("mapa_urbano")
    data object Stats : AppRoute("estadisticas")
    data object CreateReport : AppRoute("reportar")
    data object TicketDetail : AppRoute("detalle/{ticketId}") {
        fun createRoute(ticketId: Int) = "detalle/$ticketId"
    }
}

private data class BottomDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(AppRoute.Feed.route, R.string.nav_home, Icons.Default.Home),
    BottomDestination(AppRoute.Map.route, R.string.nav_map, Icons.Default.Map),
    BottomDestination(AppRoute.Stats.route, R.string.nav_stats, Icons.Default.PieChart)
)

@Composable
fun CiudadAlertaApp(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val container = remember(context) {
        (context.applicationContext as? CiudadAlertaApplication)?.container
            ?: DefaultAppContainer(context.applicationContext)
    }
    val repository = container.ticketRepository
    val sessionManager = container.sessionManager

    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showMainChrome = bottomDestinations.any { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(AppRoute.Feed.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val openCreateReport: () -> Unit = {
        navController.navigate(AppRoute.CreateReport.route) {
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isExpandedScreen && showMainChrome) {
                MinimalFloatingBottomBar(
                    currentRoute = currentDestination?.route,
                    onSelectRoute = navigateToTab,
                    onCreateReport = openCreateReport
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isExpandedScreen && showMainChrome) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        FloatingActionButton(
                            onClick = openCreateReport,
                            containerColor = PrimaryCivicBlue,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nav_create))
                        }
                    }
                ) {
                    bottomDestinations.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTab(dest.route) },
                            icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                            label = {
                                Text(
                                    text = stringResource(dest.labelRes),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Login.route
                ) {
                    composable(AppRoute.Login.route) {
                        val viewModel: LoginViewModel = viewModel(
                            factory = LoginViewModelFactory(sessionManager, repository)
                        )
                        LoginEncargadoScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate(AppRoute.Feed.route) {
                                    popUpTo(AppRoute.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(AppRoute.Feed.route) {
                        val viewModel: FeedViewModel = viewModel(
                            factory = FeedViewModelFactory(repository, sessionManager)
                        )
                        FeedReportesScreen(
                            viewModel = viewModel,
                            isExpandedScreen = isExpandedScreen,
                            onNavigateToCreate = openCreateReport,
                            onNavigateToDetail = { ticketId ->
                                navController.navigate(AppRoute.TicketDetail.createRoute(ticketId)) {
                                    launchSingleTop = true
                                }
                            },
                            onSwitchAccount = {
                                sessionManager.logout()
                                navController.navigate(AppRoute.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(AppRoute.Map.route) {
                        val viewModel: FeedViewModel = viewModel(
                            factory = FeedViewModelFactory(repository, sessionManager)
                        )
                        MapaIncidenciasScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { ticketId ->
                                navController.navigate(AppRoute.TicketDetail.createRoute(ticketId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(AppRoute.Stats.route) {
                        val viewModel: EstadisticasViewModel = viewModel(
                            factory = EstadisticasViewModelFactory(repository)
                        )
                        EstadisticasScreen(viewModel = viewModel)
                    }

                    composable(AppRoute.CreateReport.route) {
                        val viewModel: ReportarViewModel = viewModel(
                            factory = ReportarViewModelFactory(repository)
                        )
                        ReportarScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = AppRoute.TicketDetail.route,
                        arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 0
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
        }
    }
}

@Composable
private fun MinimalFloatingBottomBar(
    currentRoute: String?,
    onSelectRoute: (String) -> Unit,
    onCreateReport: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                val tint = if (selected) PrimaryCivicBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectRoute(dest.route) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = stringResource(dest.labelRes),
                        tint = tint,
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        text = stringResource(dest.labelRes),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = tint
                    )
                }
            }

            Surface(
                onClick = onCreateReport,
                shape = CircleShape,
                color = PrimaryCivicBlue,
                shadowElevation = 4.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_create),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}