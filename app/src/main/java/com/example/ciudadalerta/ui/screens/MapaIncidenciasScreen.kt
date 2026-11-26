package com.example.ciudadalerta.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.LocationHelper
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.ui.components.CategoryBadge
import com.example.ciudadalerta.ui.components.MinimalHeader
import com.example.ciudadalerta.ui.components.PhotoEvidenceCard
import com.example.ciudadalerta.ui.components.SegmentedPillToggle
import com.example.ciudadalerta.ui.components.StatusChip
import com.example.ciudadalerta.ui.components.UrgentBadge
import com.example.ciudadalerta.ui.components.getCategoryVisual
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.UrgentRoseRed
import com.example.ciudadalerta.ui.viewmodels.FeedViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MapaIncidenciasScreen(
    viewModel: FeedViewModel,
    isExpandedScreen: Boolean = false,
    onNavigateToDetail: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showUrgentOnly by remember { mutableStateOf(false) }
    var selectedPinId by remember { mutableStateOf<Int?>(null) }

    val mapTickets = state.tickets.filter { ticket ->
        !showUrgentOnly || (ticket.esUrgente && ticket.estatusId != TicketStatus.SOLUCIONADO.id)
    }
    val selectedTicket = mapTickets.firstOrNull { it.id == selectedPinId } ?: mapTickets.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (isExpandedScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.map_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                SegmentedPillToggle(
                    firstLabel = stringResource(R.string.tab_all_reports),
                    secondLabel = stringResource(R.string.tab_urgent_reports),
                    selectedIndex = if (showUrgentOnly) 1 else 0,
                    onSelect = { showUrgentOnly = it == 1 },
                    modifier = Modifier.widthIn(max = 340.dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MapCanvasContainer(
                    tickets = mapTickets,
                    selectedTicketId = selectedTicket?.id,
                    onPinClick = { selectedPinId = it },
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTicket != null) {
                        SelectedMapTicketCard(
                            ticket = selectedTicket,
                            showPhotoPreview = true,
                            onViewDetail = { onNavigateToDetail(selectedTicket.id) },
                            onOpenMaps = {
                                LocationHelper.openInExternalMaps(
                                    context = context,
                                    lat = selectedTicket.latitud,
                                    lon = selectedTicket.longitud,
                                    label = context.getString(R.string.ticket_number, selectedTicket.id)
                                )
                            }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.map_tap_pin_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            MinimalHeader(title = stringResource(R.string.map_title))

            SegmentedPillToggle(
                firstLabel = stringResource(R.string.tab_all_reports),
                secondLabel = stringResource(R.string.tab_urgent_reports),
                selectedIndex = if (showUrgentOnly) 1 else 0,
                onSelect = { showUrgentOnly = it == 1 },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            MapCanvasContainer(
                tickets = mapTickets,
                selectedTicketId = selectedTicket?.id,
                onPinClick = { selectedPinId = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            if (selectedTicket != null) {
                SelectedMapTicketCard(
                    ticket = selectedTicket,
                    showPhotoPreview = false,
                    onViewDetail = { onNavigateToDetail(selectedTicket.id) },
                    onOpenMaps = {
                        LocationHelper.openInExternalMaps(
                            context = context,
                            lat = selectedTicket.latitud,
                            lon = selectedTicket.longitud,
                            label = context.getString(R.string.ticket_number, selectedTicket.id)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MapCanvasContainer(
    tickets: List<TicketEntity>,
    selectedTicketId: Int?,
    onPinClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    ) {
        InteractiveUrbanMap(
            tickets = tickets,
            selectedTicketId = selectedTicketId,
            onPinClick = onPinClick,
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.78f),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.map_subtitle, tickets.size),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun SelectedMapTicketCard(
    ticket: TicketEntity,
    showPhotoPreview: Boolean,
    onViewDetail: () -> Unit,
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(categoriaId = ticket.categoriaId)
                    if (ticket.esUrgente) {
                        Spacer(modifier = Modifier.width(6.dp))
                        UrgentBadge()
                    }
                }
                StatusChip(estatusId = ticket.estatusId)
            }

            Text(
                text = ticket.descripcion,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = if (showPhotoPreview) 4 else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (showPhotoPreview) {
                val photoSource = ticket.bestPhotoSource
                if (photoSource != null) {
                    PhotoEvidenceCard(fotoRuta = photoSource, categoriaId = ticket.categoriaId)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryCivicBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stringResource(R.string.ticket_number, ticket.id)} • %.4f, %.4f".format(
                        Locale.US,
                        ticket.latitud,
                        ticket.longitud
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onViewDetail,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCivicBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.map_view_detail),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = onOpenMaps,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_open_maps_short), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InteractiveUrbanMap(
    tickets: List<TicketEntity>,
    selectedTicketId: Int?,
    onPinClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val minLat = (tickets.minOfOrNull { it.latitud } ?: 19.420) - 0.004
        val maxLat = (tickets.maxOfOrNull { it.latitud } ?: 19.445) + 0.004
        val minLon = (tickets.minOfOrNull { it.longitud } ?: -99.148) - 0.004
        val maxLon = (tickets.maxOfOrNull { it.longitud } ?: -99.120) + 0.004
        val latSpan = (maxLat - minLat).coerceAtLeast(0.01)
        val lonSpan = (maxLon - minLon).coerceAtLeast(0.01)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(color = Color(0xFFEEF2F9))

            val blockColor = Color(0xFFE2E8F4)
            drawRect(blockColor, Offset(w * 0.05f, h * 0.08f), Size(w * 0.24f, h * 0.22f))
            drawRect(blockColor, Offset(w * 0.36f, h * 0.12f), Size(w * 0.26f, h * 0.20f))
            drawRect(blockColor, Offset(w * 0.68f, h * 0.36f), Size(w * 0.26f, h * 0.22f))
            drawRect(blockColor, Offset(w * 0.08f, h * 0.64f), Size(w * 0.28f, h * 0.26f))

            val zoneColor = Color(0xFFDCE4FA)
            drawRect(zoneColor, Offset(w * 0.66f, h * 0.08f), Size(w * 0.26f, h * 0.22f))
            drawRect(zoneColor, Offset(w * 0.40f, h * 0.66f), Size(w * 0.24f, h * 0.22f))

            val streetColor = Color(0xFFD5DEEE)
            for (i in 1..6) {
                val x = w * (i / 7f)
                drawLine(streetColor, Offset(x, 0f), Offset(x, h), strokeWidth = 4f)
            }
            for (j in 1..6) {
                val y = h * (j / 7f)
                drawLine(streetColor, Offset(0f, y), Offset(w, y), strokeWidth = 4f)
            }

            drawLine(Color.White, Offset(0f, h * 0.44f), Offset(w, h * 0.40f), strokeWidth = 14f)
            drawLine(Color.White, Offset(w * 0.34f, 0f), Offset(w * 0.38f, h), strokeWidth = 14f)
            drawLine(Color.White, Offset(w * 0.66f, 0f), Offset(w * 0.62f, h), strokeWidth = 11f)
        }

        tickets.forEachIndexed { idx, ticket ->
            val normX = (((ticket.longitud - minLon) / lonSpan).toFloat()).coerceIn(0.12f, 0.88f)
            val normY = (1f - ((ticket.latitud - minLat) / latSpan).toFloat()).coerceIn(0.16f, 0.86f)
            val visual = getCategoryVisual(ticket.categoriaId)
            val isSelected = ticket.id == selectedTicketId
            val isUrgentActive = ticket.esUrgente && ticket.estatusId != TicketStatus.SOLUCIONADO.id
            val pinSizeDp = if (isSelected) 46.dp else 36.dp
            val halfPinPx = if (isSelected) 23.dp else 18.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset {
                        val offsetX = (widthPx * normX - halfPinPx.toPx()).roundToInt()
                        val offsetY = (heightPx * normY - halfPinPx.toPx()).roundToInt()
                        IntOffset(offsetX, offsetY)
                    }
                    .size(pinSizeDp)
                    .clickable { onPinClick(ticket.id) }
            ) {
                if (isSelected || isUrgentActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                (if (isUrgentActive) UrgentRoseRed else visual.color).copy(alpha = 0.24f)
                            )
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 34.dp else 28.dp)
                        .shadow(elevation = if (isSelected) 6.dp else 2.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(visual.color)
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = "Pin #${idx + 1}",
                        tint = Color.White,
                        modifier = Modifier.size(if (isSelected) 18.dp else 14.dp)
                    )
                }
            }
        }
    }
}
