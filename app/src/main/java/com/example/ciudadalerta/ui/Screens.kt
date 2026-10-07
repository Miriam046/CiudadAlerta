package com.example.ciudadalerta.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R

@Composable
fun HomeScreen(viewModel: ReportViewModel, onNavigateToCreate: () -> Unit) {
    // Consumimos el estado del ViewModel
    val reports by viewModel.reports.collectAsState()

    // Soporte para diferentes pantallas: widthIn limita el ancho en tablets
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.nav_home), style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))

        // LISTA INTERACTIVA
        LazyColumn(modifier = Modifier.weight(1f).widthIn(max = 600.dp)) {
            items(reports) { report ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = report, modifier = Modifier.padding(16.dp))
                }
            }
        }

        // BOTÓN
        Button(onClick = onNavigateToCreate, modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)) {
            Text(text = stringResource(id = R.string.nav_create))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(viewModel: ReportViewModel, onNavigateBack: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Bache") }
    
    // Estado para un TimePicker muy básico
    var timeText by remember { mutableStateOf("12:00 PM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CAJA DE TEXTO
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(id = R.string.lbl_description)) },
            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CHECKBOX
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.widthIn(max = 600.dp)) {
            Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
            Text(stringResource(id = R.string.chk_urgent))
        }

        // RADIO BUTTONS
        val categories = listOf("Bache", "Luz", "Agua")
        Row(modifier = Modifier.widthIn(max = 600.dp)) {
            categories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (category == selectedCategory),
                        onClick = { selectedCategory = category }
                    )
                    Text(text = category)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.addReport("$selectedCategory: $description")
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
        ) {
            Text(stringResource(id = R.string.btn_save))
        }
    }
}