package com.example.ciudadalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ciudadalerta.ui.navigation.CiudadAlertaApp
import com.example.ciudadalerta.ui.theme.CiudadAlertaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager = (application as CiudadAlertaApplication).container.sessionManager
        setContent {
            val isDarkMode by sessionManager.isDarkMode.collectAsState()
            CiudadAlertaTheme(darkTheme = isDarkMode) {
                CiudadAlertaApp()
            }
        }
    }
}
