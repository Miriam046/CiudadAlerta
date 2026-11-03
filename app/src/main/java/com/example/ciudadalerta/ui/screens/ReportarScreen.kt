package com.example.ciudadalerta.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ciudadalerta.ui.components.getCategoryInfo
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.StatusRegistradoRed
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarScreen(
    viewModel: ReportarViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Reporte Ciudadano",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCivicBlue
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Error alert if present
            if (state.errorMessage != null) {
                Surface(
                    color = StatusRegistradoRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusRegistradoRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.errorMessage!!,
                            color = StatusRegistradoRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category Selection Header
            Text(
                text = "1. Selecciona la Categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val categories = listOf(
                1 to "Bache / Calle",
                2 to "Alumbrado Público",
                3 to "Fuga de Agua",
                4 to "Seguridad Ciudadana",
                5 to "Acumulación de Basura"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (catId, catName) ->
                    val isSelected = state.categoriaId == catId
                    val (label, icon, color) = getCategoryInfo(catId)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onCategoriaSelected(catId) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = catName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Problem Description Header
            Text(
                text = "2. Descripción del Problema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                placeholder = { Text("Describe brevemente la ubicación exacta y detalles de la incidencia...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Location Section
            Text(
                text = "3. Ubicación Geográfica (GPS)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryCivicBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Latitud: %.4f, Longitud: %.4f".format(state.latitud, state.longitud),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Ubicación fijada automáticamente",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val randomLat = 19.4326 + (Random.nextDouble() - 0.5) * 0.05
                            val randomLon = -99.1332 + (Random.nextDouble() - 0.5) * 0.05
                            viewModel.onLocationChanged(randomLat, randomLon)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Refrescar GPS",
                            tint = PrimaryCivicBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Urgency Checkbox Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.isUrgent,
                        onCheckedChange = { viewModel.onUrgentToggled(it) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Marcar como Atención Urgente / Crítica",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (state.isUrgent) StatusRegistradoRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Priorizará el ticket en el tablero ciudadano",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.submitReport(onSuccess = onBack)
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCivicBlue)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar y Enviar Reporte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}