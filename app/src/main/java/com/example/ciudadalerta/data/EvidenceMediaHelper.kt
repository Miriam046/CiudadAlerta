package com.example.ciudadalerta.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Helper nativo para persistir y cargar evidencia fotográfica:
 * - Captura real de cámara (FileProvider)
 * - Copia desde galería a almacenamiento interno
 * - Descarga y caché local de imágenes alojadas en Supabase Storage (`https://...`)
 */
object EvidenceMediaHelper {

    private const val EVIDENCE_DIR = "evidence_photos"

    private fun getEvidenceDir(context: Context): File {
        val dir = File(context.filesDir, EVIDENCE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createCameraCaptureFile(context: Context): Pair<File, Uri> {
        val file = File(getEvidenceDir(context), "IMG_CAM_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    suspend fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val destFile = File(getEvidenceDir(context), "IMG_GAL_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (destFile.exists() && destFile.length() > 0L) destFile.absolutePath else null
            } catch (_: Exception) {
                null
            }
        }

    suspend fun loadSampledBitmap(
        pathOrUrl: String?,
        cacheDir: File? = null,
        maxDimension: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (pathOrUrl.isNullOrBlank()) return@withContext null

        val targetFile: File = if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            val dir = cacheDir ?: return@withContext null
            val remoteCache = File(dir, "remote_evidence")
            if (!remoteCache.exists()) remoteCache.mkdirs()
            val cached = File(remoteCache, "${pathOrUrl.hashCode()}.jpg")
            if (!cached.exists() || cached.length() == 0L) {
                try {
                    URL(pathOrUrl).openStream().use { input ->
                        FileOutputStream(cached).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (_: Exception) {
                    return@withContext null
                }
            }
            cached
        } else {
            File(pathOrUrl)
        }

        if (!targetFile.exists() || !targetFile.isFile) return@withContext null
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(targetFile.absolutePath, bounds)
            var sampleSize = 1
            while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(targetFile.absolutePath, opts)
        } catch (_: Exception) {
            null
        }
    }
}
