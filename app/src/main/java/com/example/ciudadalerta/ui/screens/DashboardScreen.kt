package com.example.ciudadalerta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.ui.components.StatCard
import com.example.ciudadalerta.ui.components.TicketCardItem
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.StatusEvaluacionOrange
import com.example.ciudadalerta.ui.theme.StatusRegistradoRed
import com.example.ciudadalerta.ui.theme.StatusSolucionadoGreen
import com.example.ciudadalerta.ui.theme.SyncPendingIndigo
import com.example.ciudadalerta.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val filteredTickets = state.tickets.filter { ticket ->
        val matchesCategory = state.selectedCategoryFilter == null || ticket.categoriaId == state.selectedCategoryFilter
        val matchesSearch = state.searchQuery.isBlank() || ticket.descripcion.contains(state.searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ciudad Alerta",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Tablero Ciudadano de Incidencias",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCivicBlue
                ),
                actions = {
                    if (state.unsyncedCount > 0) {
                        IconButton(onClick = { viewModel.syncPending() }) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sincronizar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryCivicBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Unsynced Alert Banner
            if (state.unsyncedCount > 0) {
                Surface(
                    color = SyncPendingIndigo.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = SyncPendingIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.unsyncedCount} reportes guardados offline",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SyncPendingIndigo
                            )
                        }
                        Button(
                            onClick = { viewModel.syncPending() },
                            colors = ButtonDefaults.buttonColors(containerColor = SyncPendingIndigo),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Sincronizar", fontSize = 12.sp)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Impacto en tu comunidad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid of Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Reportes",
                            value = "${state.totalAportaciones}",
                            icon = Icons.Default.FilterList,
                            accentColor = PrimaryCivicBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Críticos / Reg.",
                            value = "${state.ticketsCriticos}",
                            icon = Icons.Default.ReportProblem,
                            accentColor = StatusRegistradoRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "En Evaluación",
                            value = "${state.ticketsEnEvaluacion}",
                            icon = Icons.Default.Build,
                            accentColor = StatusEvaluacionOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Solucionados",
                            value = "${state.ticketsSolucionados}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = StatusSolucionadoGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Search & Filters
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Buscar incidencia...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips
                    val categories = listOf(
                        null to "Todos",
                        1 to "Bache",
                        2 to "Alumbrado",
                        3 to "Agua",
                        4 to "Seguridad",
                        5 to "Basura"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (id, label) ->
                            FilterChip(
                                selected = state.selectedCategoryFilter == id,
                                onClick = { viewModel.onCategoryFilterSelected(id) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Historial de Reportes (${filteredTickets.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryCivicBlue)
                        }
                    }
                } else if (filteredTickets.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryCivicBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReportProblem,
                                        contentDescription = null,
                                        tint = PrimaryCivicBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No se encontraron reportes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "¡Sé el primero en generar una alerta en tu comunidad!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onNavigateToCreate,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCivicBlue)
                                ) {
                                    Text("Reportar Incidencia")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        TicketCardItem(
                            ticket = ticket,
                            onClick = { onNavigateToDetail(ticket.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}