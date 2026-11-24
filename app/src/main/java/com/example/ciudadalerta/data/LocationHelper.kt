package com.example.ciudadalerta.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * Helper nativo para obtener coordenadas GPS del hardware del dispositivo (LocationManager)
 * y abrir coordenadas en aplicaciones de mapas externas.
 */
object LocationHelper {

    data class GpsReading(
        val latitude: Double,
        val longitude: Double,
        val isFromHardware: Boolean
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownHardwareLocation(context: Context): GpsReading? {
        if (!hasLocationPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = try {
                manager.getLastKnownLocation(provider)
            } catch (_: Exception) {
                null
            }
            if (loc != null) {
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
        }
        return bestLocation?.let {
            GpsReading(
                latitude = it.latitude,
                longitude = it.longitude,
                isFromHardware = true
            )
        }
    }

    fun openInExternalMaps(context: Context, lat: Double, lon: Double, label: String) {
        val encodedLabel = Uri.encode(label)
        val geoUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($encodedLabel)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}
