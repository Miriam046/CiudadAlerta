package com.example.ciudadalerta.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.EvidenceMediaHelper
import com.example.ciudadalerta.data.TicketEntity
import com.example.ciudadalerta.domain.model.TicketStatus
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.PrimaryLightBlue
import com.example.ciudadalerta.ui.theme.SquircleCoral
import com.example.ciudadalerta.ui.theme.SquircleIndigo
import com.example.ciudadalerta.ui.theme.SquircleRose
import com.example.ciudadalerta.ui.theme.SquircleSky
import com.example.ciudadalerta.ui.theme.SquircleViolet
import com.example.ciudadalerta.ui.theme.StatusEvaluacionOrange
import com.example.ciudadalerta.ui.theme.StatusRegistradoRed
import com.example.ciudadalerta.ui.theme.StatusReparacionBlue
import com.example.ciudadalerta.ui.theme.StatusSolucionadoGreen
import com.example.ciudadalerta.ui.theme.SyncPendingIndigo
import com.example.ciudadalerta.ui.theme.UrgentRoseRed
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CategoryVisual(
    val labelRes: Int,
    val fullLabelRes: Int,
    val icon: ImageVector,
    val color: Color
)

fun getCategoryVisual(categoriaId: Int): CategoryVisual = when (categoriaId) {
    1 -> CategoryVisual(R.string.cat_pothole, R.string.cat_pothole_full, Icons.Default.Construction, SquircleCoral)
    2 -> CategoryVisual(R.string.cat_lighting, R.string.cat_lighting_full, Icons.Default.Lightbulb, SquircleIndigo)
    3 -> CategoryVisual(R.string.cat_water, R.string.cat_water_full, Icons.Default.WaterDrop, SquircleSky)
    4 -> CategoryVisual(R.string.cat_security, R.string.cat_security_full, Icons.Default.Security, SquircleRose)
    5 -> CategoryVisual(R.string.cat_trash, R.string.cat_trash_full, Icons.Default.Delete, SquircleViolet)
    else -> CategoryVisual(R.string.cat_general, R.string.cat_general, Icons.Default.Build, SquircleIndigo)
}

fun getStatusColor(estatusId: Int): Color = when (estatusId) {
    TicketStatus.REGISTRADO.id -> StatusRegistradoRed
    TicketStatus.EN_EVALUACION.id -> StatusEvaluacionOrange
    TicketStatus.EN_REPARACION.id -> StatusReparacionBlue
    TicketStatus.SOLUCIONADO.id -> StatusSolucionadoGreen
    else -> StatusRegistradoRed
}

fun getStatusIcon(estatusId: Int): ImageVector = when (estatusId) {
    TicketStatus.REGISTRADO.id -> Icons.Default.ReportProblem
    TicketStatus.EN_EVALUACION.id -> Icons.AutoMirrored.Filled.ManageSearch
    TicketStatus.EN_REPARACION.id -> Icons.Default.Engineering
    TicketStatus.SOLUCIONADO.id -> Icons.Default.CheckCircle
    else -> Icons.Default.ReportProblem
}

/**
 * Cabecera minimalista sobre fondo limpio con título centrado y botones circulares suaves.
 */
@Composable
fun MinimalHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingAction: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
            leadingAction?.invoke()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterEnd) {
            trailingAction?.invoke()
        }
    }
}

@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BackHeaderButton(onClick: () -> Unit) {
    SoftIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.action_back),
        onClick = onClick
    )
}

/**
 * Selector segmentado estilo cápsula limpia (inspirado en pantalla central de referencia).
 */
@Composable
fun SegmentedPillToggle(
    firstLabel: String,
    secondLabel: String,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            listOf(firstLabel, secondLabel).forEachIndexed { idx, label ->
                val isSelected = selectedIndex == idx
                Surface(
                    onClick = { onSelect(idx) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryCivicBlue else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ícono squircle pastel redondeado (inspirado en las 4 cajas de colores de la referencia).
 */
@Composable
fun CategorySquircleIcon(
    icon: ImageVector,
    color: Color,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 15.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Anillo circular limpio para mostrar porcentaje de resolución (estilo pantalla 1 y 3).
 */
@Composable
fun CleanDonutRing(
    percentage: Int,
    size: Dp = 84.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        val sweep = (percentage / 100f).coerceIn(0f, 1f) * 360f
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.28f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = if (sweep > 0f) sweep else 8f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percentage%",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (size >= 100.dp) 22.sp else 18.sp
        )
    }
}

@Composable
fun StatusChip(estatusId: Int, modifier: Modifier = Modifier) {
    val status = TicketStatus.fromId(estatusId)
    val color = getStatusColor(estatusId)

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(status.labelRes),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun UrgentBadge(modifier: Modifier = Modifier) {
    Surface(
        color = UrgentRoseRed.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = UrgentRoseRed,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.badge_urgent),
                color = UrgentRoseRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryBadge(categoriaId: Int, modifier: Modifier = Modifier) {
    val visual = getCategoryVisual(categoriaId)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CategorySquircleIcon(
            icon = visual.icon,
            color = visual.color,
            size = 28.dp,
            iconSize = 15.dp,
            cornerRadius = 9.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(visual.labelRes),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SyncBadge(isSynced: Boolean, modifier: Modifier = Modifier) {
    val label = stringResource(if (isSynced) R.string.badge_synced else R.string.badge_offline)
    val color = if (isSynced) PrimaryCivicBlue else SyncPendingIndigo
    val icon = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Tarjeta minimalista de reporte (estilo filas limpias `Preview.mp4` / `Wallpaper.jpg` de la referencia).
 */
@Composable
fun TicketCardItem(
    ticket: TicketEntity,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val visual = getCategoryVisual(ticket.categoriaId)
    val status = TicketStatus.fromId(ticket.estatusId)
    val isUrgentActive = ticket.esUrgente && ticket.estatusId != TicketStatus.SOLUCIONADO.id

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PrimaryCivicBlue else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                CategorySquircleIcon(
                    icon = visual.icon,
                    color = visual.color,
                    size = 46.dp,
                    iconSize = 22.dp,
                    cornerRadius = 14.dp
                )
                if (isUrgentActive) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(UrgentRoseRed)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.descripcion,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(visual.labelRes),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(status.labelRes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = getStatusColor(ticket.estatusId)
                    )
                    if (!ticket.isSynced) {
                        Text(
                            text = "• Offline",
                            fontSize = 11.sp,
                            color = SyncPendingIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Línea de tiempo minimalista de 4 fases.
 */
@Composable
fun StatusTimelineCard(estatusId: Int, modifier: Modifier = Modifier) {
    val steps = listOf(
        TicketStatus.REGISTRADO,
        TicketStatus.EN_EVALUACION,
        TicketStatus.EN_REPARACION,
        TicketStatus.SOLUCIONADO
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.detail_timeline_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = estatusId >= step.id
                    val isCurrent = estatusId == step.id
                    val activeColor = getStatusColor(step.id)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(
                                        when {
                                            index == 0 -> Color.Transparent
                                            estatusId >= step.id -> activeColor
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 30.dp else 26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompleted) activeColor else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted && !isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${step.id}",
                                        color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            val nextStep = steps.getOrNull(index + 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(
                                        when {
                                            nextStep == null -> Color.Transparent
                                            estatusId >= nextStep.id -> getStatusColor(nextStep.id)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(step.labelRes),
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCompleted) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mapa vectorial limpio sin verde chillón (tonos azul hielo y gris pizarra suave).
 */
@Composable
fun MiniGpsMapCanvas(
    latitud: Double,
    longitud: Double,
    categoriaId: Int,
    modifier: Modifier = Modifier
) {
    val visual = getCategoryVisual(categoriaId)
    val categoryLabel = stringResource(visual.labelRes)

    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(165.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawRect(color = Color(0xFFEEF2F9))

                val blockColor = Color(0xFFE2E8F4)
                drawRect(blockColor, Offset(w * 0.06f, h * 0.12f), Size(w * 0.24f, h * 0.34f))
                drawRect(blockColor, Offset(w * 0.36f, h * 0.10f), Size(w * 0.22f, h * 0.32f))
                drawRect(blockColor, Offset(w * 0.65f, h * 0.56f), Size(w * 0.26f, h * 0.32f))

                // Plazas en tono lavanda/azul muy tenue (sin verde)
                val plazaColor = Color(0xFFDCE4FA)
                drawRect(plazaColor, Offset(w * 0.08f, h * 0.58f), Size(w * 0.22f, h * 0.30f))
                drawRect(plazaColor, Offset(w * 0.66f, h * 0.12f), Size(w * 0.24f, h * 0.28f))

                val gridColor = Color(0xFFD5DEEE)
                for (i in 1..5) {
                    val x = w * (i / 6f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 4f)
                }
                for (j in 1..3) {
                    val y = h * (j / 4f)
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 4f)
                }

                drawLine(Color.White, Offset(0f, h * 0.50f), Offset(w, h * 0.46f), strokeWidth = 13f)
                drawLine(Color.White, Offset(w * 0.48f, 0f), Offset(w * 0.52f, h), strokeWidth = 13f)

                val fracX = ((longitud - Math.floor(longitud)) * 100.0 % 1.0).toFloat().let {
                    0.22f + (kotlin.math.abs(it) * 0.56f)
                }
                val fracY = ((latitud - Math.floor(latitud)) * 100.0 % 1.0).toFloat().let {
                    0.24f + (kotlin.math.abs(it) * 0.52f)
                }
                val pinCenter = Offset(w * fracX, h * fracY)

                drawCircle(visual.color.copy(alpha = 0.18f), radius = 38f, center = pinCenter)
                drawCircle(visual.color.copy(alpha = 0.35f), radius = 22f, center = pinCenter)
                drawCircle(Color.White, radius = 12f, center = pinCenter)
                drawCircle(visual.color, radius = 8.5f, center = pinCenter)
            }

            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.80f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$categoryLabel • %.4f, %.4f".format(Locale.US, latitud, longitud),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoEvidenceCard(
    fotoRuta: String,
    categoriaId: Int,
    modifier: Modifier = Modifier
) {
    val visual = getCategoryVisual(categoriaId)
    val categoryLabel = stringResource(visual.labelRes)
    val context = LocalContext.current
    val loadedBitmap by produceState<Bitmap?>(initialValue = null, key1 = fotoRuta) {
        value = EvidenceMediaHelper.loadSampledBitmap(fotoRuta, cacheDir = context.cacheDir)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (loadedBitmap != null) 200.dp else 138.dp)
        ) {
            if (loadedBitmap != null) {
                Image(
                    bitmap = loadedBitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.badge_evidence_verified),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.18f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryCivicBlue, PrimaryLightBlue)
                            )
                        )
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.72f)
                        lineTo(size.width * 0.35f, size.height * 0.52f)
                        lineTo(size.width * 0.68f, size.height * 0.66f)
                        lineTo(size.width, size.height * 0.42f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, color = Color.White.copy(alpha = 0.12f))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.badge_evidence_verified),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategorySquircleIcon(
                        icon = visual.icon,
                        color = Color.White.copy(alpha = 0.22f),
                        size = 38.dp,
                        iconSize = 20.dp,
                        cornerRadius = 12.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = File(fotoRuta).name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$categoryLabel • Offline-First",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateFull(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
