package com.example.ciudadalerta.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.domain.model.TicketCategory
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.ui.components.CategoryBadge
import com.example.ciudadalerta.ui.components.CategorySquircleIcon
import com.example.ciudadalerta.ui.components.CleanDonutRing
import com.example.ciudadalerta.ui.components.MiniGpsMapCanvas
import com.example.ciudadalerta.ui.components.MinimalHeader
import com.example.ciudadalerta.ui.components.PhotoEvidenceCard
import com.example.ciudadalerta.ui.components.SoftIconButton
import com.example.ciudadalerta.ui.components.StatusChip
import com.example.ciudadalerta.ui.components.StatusTimelineCard
import com.example.ciudadalerta.ui.components.SyncBadge
import com.example.ciudadalerta.ui.components.TicketCardItem
import com.example.ciudadalerta.ui.components.UrgentBadge
import com.example.ciudadalerta.ui.components.formatDateFull
import com.example.ciudadalerta.ui.components.getCategoryVisual
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.PrimaryCivicDarkBlue
import com.example.ciudadalerta.ui.theme.PrimaryLightBlue
import com.example.ciudadalerta.ui.theme.SquircleIndigo
import com.example.ciudadalerta.ui.theme.SyncPendingIndigo
import com.example.ciudadalerta.ui.viewmodels.FeedUiState
import com.example.ciudadalerta.ui.viewmodels.FeedViewModel

@Composable
fun FeedReportesScreen(
    viewModel: FeedViewModel,
    isExpandedScreen: Boolean = false,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onSwitchAccount: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var searchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.syncMessage) {
        val message = state.syncMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message.asString(context))
            viewModel.clearSyncMessage()
        }
    }

    val filteredTickets = state.tickets.filter { ticket ->
        val matchesCategory = state.selectedCategoryFilter == null || ticket.categoriaId == state.selectedCategoryFilter
        val matchesUrgent = !state.onlyUrgentFilter || (ticket.esUrgente && ticket.estatusId != TicketStatus.SOLUCIONADO.id)
        val matchesSearch = state.searchQuery.isBlank() ||
            ticket.descripcion.contains(state.searchQuery, ignoreCase = true) ||
            (ticket.cuadrillaAsignada?.contains(state.searchQuery, ignoreCase = true) == true)
        matchesCategory && matchesUrgent && matchesSearch
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
            // Clean Minimal Header (Reference style)
            MinimalHeader(
                title = stringResource(R.string.nav_home),
                leadingAction = {
                    SoftIconButton(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.action_logout),
                        onClick = {
                            viewModel.logout()
                            onSwitchAccount()
                        }
                    )
                },
                trailingAction = {
                    SoftIconButton(
                        icon = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_search),
                        onClick = {
                            if (searchExpanded) {
                                viewModel.onSearchQueryChanged("")
                            }
                            searchExpanded = !searchExpanded
                        }
                    )
                }
            )

            // Expandable Search Bar (keeps default screen clean)
            AnimatedVisibility(visible = searchExpanded) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = PrimaryCivicBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                )
            }

            if (isExpandedScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    FeedHomeContent(
                        state = state,
                        filteredTickets = filteredTickets,
                        viewModel = viewModel,
                        isExpandedScreen = true,
                        onNavigateToCreate = onNavigateToCreate,
                        onTicketClick = { viewModel.onSelectTicket(it) },
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    val selected = filteredTickets.firstOrNull { it.id == state.selectedTicketId }
                        ?: filteredTickets.firstOrNull()
                    TabletPreviewPane(
                        ticket = selected,
                        onOpenFullDetail = onNavigateToDetail,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    )
                }
            } else {
                FeedHomeContent(
                    state = state,
                    filteredTickets = filteredTickets,
                    viewModel = viewModel,
                    isExpandedScreen = false,
                    onNavigateToCreate = onNavigateToCreate,
                    onTicketClick = onNavigateToDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FeedHomeContent(
    state: FeedUiState,
    filteredTickets: List<TicketEntity>,
    viewModel: FeedViewModel,
    isExpandedScreen: Boolean,
    onNavigateToCreate: () -> Unit,
    onTicketClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Role + Subtle Utility Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val roleTitle = stringResource(state.currentRole.titleRes)
                val badgeText = if (
                    state.currentRole.isDefaultName(state.userName) ||
                    state.userName.equals(roleTitle, ignoreCase = true)
                ) {
                    roleTitle
                } else {
                    "${state.userName} • $roleTitle"
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.syncPending() },
                        enabled = !state.isSyncing,
                        modifier = Modifier.size(34.dp)
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = stringResource(R.string.action_sync_now),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(R.string.action_dark_mode),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // Hero Rounded Donut Card (Reference Screen 1 signature card, in Indigo)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryCivicBlue, PrimaryLightBlue)
                        )
                    )
            ) {
                // Subtle diagonal glass highlight like reference card
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sheen = Path().apply {
                        moveTo(size.width * 0.42f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height)
                        lineTo(size.width * 0.68f, size.height)
                        close()
                    }
                    drawPath(sheen, color = Color.White.copy(alpha = 0.08f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CleanDonutRing(
                        percentage = state.tasaResolucion,
                        size = 86.dp,
                        strokeWidth = 10.dp
                    )
                    Spacer(modifier = Modifier.width(22.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.dashboard_impact_title),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.solucionadasCount} / ${state.totalIncidencias} ${stringResource(R.string.label_reports_unit)}",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${state.urgentesCount} ${stringResource(R.string.metric_urgent).lowercase()} • ${state.enProcesoCount} ${stringResource(R.string.metric_in_process).lowercase()}",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Pastel Category Squircle Row (Reference Screen 1 signature row)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CategorySquircleItem(
                        label = stringResource(R.string.cat_all),
                        icon = Icons.Default.GridView,
                        color = SquircleIndigo,
                        selected = state.selectedCategoryFilter == null,
                        onClick = { viewModel.onCategoryFilterSelected(null) }
                    )
                }
                items(TicketCategory.entries) { cat ->
                    val visual = getCategoryVisual(cat.id)
                    CategorySquircleItem(
                        label = stringResource(cat.shortLabelRes),
                        icon = visual.icon,
                        color = visual.color,
                        selected = state.selectedCategoryFilter == cat.id,
                        onClick = { viewModel.onCategoryFilterSelected(cat.id) }
                    )
                }
            }
        }

        // Subtle Offline Sync Pill (only when needed)
        if (state.pendientesSyncCount > 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SyncPendingIndigo.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = SyncPendingIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.sync_banner_pending, state.pendientesSyncCount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SyncPendingIndigo
                            )
                        }
                        TextButton(
                            onClick = { viewModel.syncPending() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_sync),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SyncPendingIndigo
                            )
                        }
                    }
                }
            }
        }

        // Clean Section Header ("Recent reports" + "Urgent / All" toggle)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.feed_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (state.onlyUrgentFilter) {
                        stringResource(R.string.feed_see_all)
                    } else {
                        "${stringResource(R.string.tab_urgent_reports)} (${state.urgentesCount})"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryCivicBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.onUrgentFilterSelected(!state.onlyUrgentFilter) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryCivicBlue)
                }
            }
        } else if (filteredTickets.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CategorySquircleIcon(
                            icon = Icons.Default.ReportProblem,
                            color = PrimaryCivicBlue.copy(alpha = 0.14f),
                            size = 54.dp,
                            iconSize = 26.dp,
                            cornerRadius = 16.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.empty_feed_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.empty_feed_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToCreate,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCivicBlue)
                        ) {
                            Text(stringResource(R.string.btn_create_report), color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                TicketCardItem(
                    ticket = ticket,
                    isSelected = isExpandedScreen && state.selectedTicketId == ticket.id,
                    onClick = { onTicketClick(ticket.id) }
                )
            }
        }
    }
}

@Composable
private fun CategorySquircleItem(
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (selected) {
                        Modifier.border(2.5.dp, color, RoundedCornerShape(18.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(if (selected) 3.dp else 0.dp)
        ) {
            CategorySquircleIcon(
                icon = icon,
                color = color,
                size = 52.dp,
                iconSize = 24.dp,
                cornerRadius = 15.dp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TabletPreviewPane(
    ticket: TicketEntity?,
    onOpenFullDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (ticket == null) {
        Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.select_ticket_tablet),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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

        Text(
            text = "${stringResource(R.string.ticket_number, ticket.id)} • ${formatDateFull(ticket.fecha)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = ticket.descripcion,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        SyncBadge(isSynced = ticket.isSynced)
        StatusTimelineCard(estatusId = ticket.estatusId)

        val photoSource = ticket.bestPhotoSource
        if (photoSource != null) {
            PhotoEvidenceCard(fotoRuta = photoSource, categoriaId = ticket.categoriaId)
        }

        MiniGpsMapCanvas(
            latitud = ticket.latitud,
            longitud = ticket.longitud,
            categoriaId = ticket.categoriaId
        )

        OutlinedButton(
            onClick = { onOpenFullDetail(ticket.id) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.map_view_detail), fontWeight = FontWeight.SemiBold)
        }
    }
}
