package com.example.ciudadalerta.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.LocationHelper
import com.example.ciudadalerta.domain.model.MaintenanceCrews
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.ui.components.BackHeaderButton
import com.example.ciudadalerta.ui.components.CategoryBadge
import com.example.ciudadalerta.ui.components.MiniGpsMapCanvas
import com.example.ciudadalerta.ui.components.MinimalHeader
import com.example.ciudadalerta.ui.components.PhotoEvidenceCard
import com.example.ciudadalerta.ui.components.SoftIconButton
import com.example.ciudadalerta.ui.components.StatusChip
import com.example.ciudadalerta.ui.components.StatusTimelineCard
import com.example.ciudadalerta.ui.components.SyncBadge
import com.example.ciudadalerta.ui.components.UrgentBadge
import com.example.ciudadalerta.ui.components.formatDateFull
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.StatusEvaluacionOrange
import com.example.ciudadalerta.ui.theme.StatusRegistradoRed
import com.example.ciudadalerta.ui.theme.StatusReparacionBlue
import com.example.ciudadalerta.ui.theme.StatusSolucionadoGreen
import com.example.ciudadalerta.ui.viewmodels.DetalleViewModel
import java.util.Locale

@Composable
fun DetalleTicketScreen(
    viewModel: DetalleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.actionFeedback, state.errorMessage) {
        val msg = state.actionFeedback ?: state.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReport(onSuccess = onBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRegistradoRed)
                ) {
                    Text(stringResource(R.string.dialog_confirm_delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            MinimalHeader(
                title = if (state.ticket != null) "Ticket #${state.ticket!!.id}" else stringResource(R.string.detail_title_fallback),
                leadingAction = { BackHeaderButton(onBack) },
                trailingAction = {
                    if (state.ticket != null) {
                        SoftIconButton(
                            icon = Icons.Default.CloudSync,
                            contentDescription = stringResource(R.string.action_sync_now),
                            onClick = { viewModel.syncWithCloud() }
                        )
                    }
                }
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCivicBlue)
                }
            } else if (state.ticket == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.detail_not_found))
                }
            } else {
                val ticket = state.ticket!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Summary Card
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryBadge(categoriaId = ticket.categoriaId)
                                    if (ticket.esUrgente) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        UrgentBadge()
                                    }
                                }
                                StatusChip(estatusId = ticket.estatusId)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = ticket.descripcion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SyncBadge(isSynced = ticket.isSynced)
                        }
                    }

                    // 4-Phase Timeline
                    StatusTimelineCard(estatusId = ticket.estatusId)

                    // Photo Evidence (local o URL de Supabase Storage)
                    val photoSource = ticket.bestPhotoSource
                    if (photoSource != null) {
                        PhotoEvidenceCard(fotoRuta = photoSource, categoriaId = ticket.categoriaId)
                    }

                    // Map + Open Maps
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniGpsMapCanvas(
                            latitud = ticket.latitud,
                            longitud = ticket.longitud,
                            categoriaId = ticket.categoriaId
                        )
                        FilledTonalButton(
                            onClick = {
                                LocationHelper.openInExternalMaps(
                                    context = context,
                                    lat = ticket.latitud,
                                    lon = ticket.longitud,
                                    label = "Ticket #${ticket.id}"
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.detail_open_maps), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Details Card
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.detail_info_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            DetailInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = stringResource(R.string.detail_registered_at),
                                value = formatDateFull(ticket.fecha)
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Update,
                                label = stringResource(R.string.detail_updated_at),
                                value = formatDateFull(ticket.fechaActualizacion)
                            )
                            DetailInfoRow(
                                icon = Icons.Default.LocationOn,
                                label = stringResource(R.string.detail_gps_coords),
                                value = "%.5f, %.5f".format(Locale.US, ticket.latitud, ticket.longitud)
                            )
                            if (!ticket.cuadrillaAsignada.isNullOrBlank()) {
                                DetailInfoRow(
                                    icon = Icons.Default.Groups,
                                    label = stringResource(R.string.detail_assigned_crew),
                                    value = ticket.cuadrillaAsignada,
                                    valueColor = StatusReparacionBlue
                                )
                            }
                            if (ticket.estatusId == TicketStatus.SOLUCIONADO.id && !ticket.solucionadoPor.isNullOrBlank()) {
                                DetailInfoRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = stringResource(R.string.detail_solved_by),
                                    value = ticket.solucionadoPor,
                                    valueColor = PrimaryCivicBlue
                                )
                            }
                        }
                    }

                    // Role-based Operations Panel (Técnico de Reparación & Gestor Municipal)
                    if (state.currentRole.canUpdateRepairStatus) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, PrimaryCivicBlue.copy(alpha = 0.28f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        if (state.currentRole.canManageAndDelete) {
                                            R.string.admin_panel_title
                                        } else {
                                            R.string.tech_panel_title
                                        }
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCivicBlue
                                )
                                Text(
                                    text = stringResource(R.string.admin_panel_subtitle),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Crew selector (Manager or Technician)
                                Text(
                                    text = stringResource(R.string.admin_select_crew),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(MaintenanceCrews.availableCrews) { crew ->
                                        FilterChip(
                                            selected = state.selectedCrew == crew,
                                            onClick = { viewModel.onCrewSelected(crew) },
                                            label = { Text(crew, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                // Manager can move to Technical Evaluation
                                if (state.currentRole.canManageAndDelete) {
                                    Button(
                                        onClick = { viewModel.markAsEnEvaluacion() },
                                        enabled = !state.isUpdatingStatus && ticket.estatusId != TicketStatus.EN_EVALUACION.id,
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusEvaluacionOrange),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.admin_btn_evaluate), fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }

                                // Technician & Manager: In Repair
                                Button(
                                    onClick = { viewModel.markAsEnReparacion() },
                                    enabled = !state.isUpdatingStatus && ticket.estatusId != TicketStatus.EN_REPARACION.id,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusReparacionBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.admin_btn_repair), fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                // Technician & Manager: Mark Resolved
                                Button(
                                    onClick = { viewModel.markAsSolucionado() },
                                    enabled = !state.isUpdatingStatus && ticket.estatusId != TicketStatus.SOLUCIONADO.id,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSolucionadoGreen),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.admin_btn_solve), fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                // Only Manager can delete
                                if (state.currentRole.canManageAndDelete) {
                                    OutlinedButton(
                                        onClick = { showDeleteDialog = true },
                                        enabled = !state.isUpdatingStatus,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRegistradoRed),
                                        border = BorderStroke(1.dp, StatusRegistradoRed.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.admin_btn_delete), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryCivicBlue,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = valueColor
            )
        }
    }
}
