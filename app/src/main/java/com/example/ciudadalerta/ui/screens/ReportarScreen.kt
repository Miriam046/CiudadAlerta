package com.example.ciudadalerta.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ciudadalerta.R
import com.example.ciudadalerta.data.EvidenceMediaHelper
import com.example.ciudadalerta.data.LocationHelper
import com.example.ciudadalerta.domain.model.TicketCategory
import com.example.ciudadalerta.ui.components.BackHeaderButton
import com.example.ciudadalerta.ui.components.CategorySquircleIcon
import com.example.ciudadalerta.ui.components.MiniGpsMapCanvas
import com.example.ciudadalerta.ui.components.MinimalHeader
import com.example.ciudadalerta.ui.components.PhotoEvidenceCard
import com.example.ciudadalerta.ui.components.getCategoryVisual
import com.example.ciudadalerta.ui.theme.PrimaryCivicBlue
import com.example.ciudadalerta.ui.theme.StatusRegistradoRed
import com.example.ciudadalerta.ui.theme.UrgentRoseRed
import com.example.ciudadalerta.ui.viewmodels.ReportarUiState
import com.example.ciudadalerta.ui.viewmodels.ReportarViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.random.Random

@Composable
fun ReportarScreen(
    viewModel: ReportarViewModel,
    isExpandedScreen: Boolean = false,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraFile
        if (success && captured != null && captured.exists()) {
            viewModel.onFotoSelected(captured.absolutePath)
        }
        pendingCameraFile = null
    }

    val launchCamera: () -> Unit = {
        val (file, uri) = EvidenceMediaHelper.createCameraCaptureFile(context)
        pendingCameraFile = file
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val savedPath = EvidenceMediaHelper.copyUriToInternalStorage(context, uri)
                if (savedPath != null) {
                    viewModel.onFotoSelected(savedPath)
                }
            }
        }
    }

    val refreshLocation: () -> Unit = {
        val hardware = LocationHelper.getLastKnownHardwareLocation(context)
        if (hardware != null) {
            viewModel.onLocationChanged(hardware.latitude, hardware.longitude, fromHardware = true)
        } else {
            val lat = 19.4326 + (Random.nextDouble() - 0.5) * 0.03
            val lon = -99.1332 + (Random.nextDouble() - 0.5) * 0.03
            viewModel.onLocationChanged(lat, lon, fromHardware = false)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshLocation()
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            LocationHelper.getLastKnownHardwareLocation(context)?.let { reading ->
                viewModel.onLocationChanged(reading.latitude, reading.longitude, fromHardware = true)
            }
        }
    }

    val onTakePhotoClick: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val onPickGalleryClick: () -> Unit = {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val onGpsClick: () -> Unit = {
        if (LocationHelper.hasLocationPermission(context)) {
            refreshLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        MinimalHeader(
            title = stringResource(R.string.report_title),
            leadingAction = { BackHeaderButton(onBack) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (state.errorMessage != null) {
                ReportErrorBanner(message = state.errorMessage!!.asString())
            }

            if (isExpandedScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(0.54f),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        CategoryStepSection(
                            selectedCategoryId = state.categoriaId,
                            onSelectCategory = { viewModel.onCategoriaSelected(it) }
                        )
                        DescriptionStepSection(
                            description = state.descripcion,
                            onDescriptionChanged = { viewModel.onDescripcionChanged(it) },
                            minHeight = 160.dp
                        )
                        UrgencyToggleCard(
                            isUrgent = state.isUrgent,
                            onToggle = { viewModel.onUrgentToggled(it) }
                        )
                    }

                    Column(
                        modifier = Modifier.weight(0.46f),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        PhotoEvidenceStepSection(
                            fotoRuta = state.fotoRuta,
                            categoriaId = state.categoriaId,
                            onRemovePhoto = { viewModel.onFotoSelected(null) },
                            onTakePhoto = onTakePhotoClick,
                            onPickGallery = onPickGalleryClick
                        )
                        LocationStepSection(
                            state = state,
                            onRefreshGps = onGpsClick
                        )
                        SubmitReportButton(
                            isSaving = state.isSaving,
                            onClick = { viewModel.submitReport(onSuccess = onBack) }
                        )
                    }
                }
            } else {
                CategoryStepSection(
                    selectedCategoryId = state.categoriaId,
                    onSelectCategory = { viewModel.onCategoriaSelected(it) }
                )
                DescriptionStepSection(
                    description = state.descripcion,
                    onDescriptionChanged = { viewModel.onDescripcionChanged(it) }
                )
                PhotoEvidenceStepSection(
                    fotoRuta = state.fotoRuta,
                    categoriaId = state.categoriaId,
                    onRemovePhoto = { viewModel.onFotoSelected(null) },
                    onTakePhoto = onTakePhotoClick,
                    onPickGallery = onPickGalleryClick
                )
                LocationStepSection(
                    state = state,
                    onRefreshGps = onGpsClick
                )
                UrgencyToggleCard(
                    isUrgent = state.isUrgent,
                    onToggle = { viewModel.onUrgentToggled(it) }
                )
                SubmitReportButton(
                    isSaving = state.isSaving,
                    onClick = { viewModel.submitReport(onSuccess = onBack) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportErrorBanner(message: String) {
    Surface(
        color = StatusRegistradoRed.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRegistradoRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = StatusRegistradoRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryStepSection(
    selectedCategoryId: Int,
    onSelectCategory: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.report_step_category),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(TicketCategory.entries) { cat ->
                val visual = getCategoryVisual(cat.id)
                val isSelected = selectedCategoryId == cat.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { onSelectCategory(cat.id) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.5.dp, visual.color, RoundedCornerShape(18.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .padding(if (isSelected) 3.dp else 0.dp)
                    ) {
                        CategorySquircleIcon(
                            icon = visual.icon,
                            color = visual.color,
                            size = 52.dp,
                            iconSize = 24.dp,
                            cornerRadius = 15.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(cat.shortLabelRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionStepSection(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 120.dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.report_step_description),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text(stringResource(R.string.lbl_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight),
            shape = RoundedCornerShape(18.dp),
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryCivicBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
private fun PhotoEvidenceStepSection(
    fotoRuta: String?,
    categoriaId: Int,
    onRemovePhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.report_step_evidence),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (fotoRuta != null) {
                TextButton(onClick = onRemovePhoto) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = StatusRegistradoRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_remove_photo), color = StatusRegistradoRed)
                }
            }
        }

        if (fotoRuta != null) {
            PhotoEvidenceCard(fotoRuta = fotoRuta, categoriaId = categoriaId)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryCivicBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.photo_captured_ok),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryCivicBlue
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.photo_placeholder_hint),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onTakePhoto,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_take_photo), fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onPickGallery,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_pick_gallery), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationStepSection(
    state: ReportarUiState,
    onRefreshGps: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.report_step_location),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        MiniGpsMapCanvas(
            latitud = state.latitud,
            longitud = state.longitud,
            categoriaId = state.categoriaId
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryCivicBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "%.5f, %.5f".format(Locale.US, state.latitud, state.longitud),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = stringResource(
                                if (state.isRealGps) R.string.gps_source_real else R.string.gps_source_preset
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onRefreshGps,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.action_gps_short),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgencyToggleCard(
    isUrgent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isUrgent) 1.5.dp else 1.dp,
            color = if (isUrgent) UrgentRoseRed else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isUrgent) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategorySquircleIcon(
                    icon = Icons.Default.PriorityHigh,
                    color = UrgentRoseRed,
                    size = 38.dp,
                    iconSize = 19.dp,
                    cornerRadius = 11.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.chk_urgent),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.chk_urgent_sub),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isUrgent,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = UrgentRoseRed
                )
            )
        }
    }
}

@Composable
private fun SubmitReportButton(
    isSaving: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCivicBlue)
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(
                text = stringResource(R.string.btn_save),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
