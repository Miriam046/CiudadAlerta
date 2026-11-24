package com.example.ciudadalerta.data
import com.example.ciudadalerta.BuildConfig

/**
 * Configuración centralizada de Supabase (leída desde local.properties -> BuildConfig).
 */
object SupabaseConfig {
    val rawUrl: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean
        get() = rawUrl.startsWith("https://") && anonKey.isNotBlank()

    val restBaseUrl: String
        get() = if (isConfigured) "$rawUrl/rest/v1/" else "https://example.supabase.co/rest/v1/"

    fun storageUploadUrl(fileName: String): String =
        "$rawUrl/storage/v1/object/evidencias/$fileName"

    fun storagePublicUrl(fileName: String): String =
        "$rawUrl/storage/v1/object/public/evidencias/$fileName"
}
