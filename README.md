# Ciudad Alerta

Aplicación Android nativa (Kotlin + Jetpack Compose) para el reporte y seguimiento de incidencias urbanas con arquitectura **Offline-First** (Room + Retrofit + Supabase + WorkManager).

## Arquitectura del proyecto

Flujo de dependencias:

```text
CiudadAlertaApplication (AppContainer)
  -> MainActivity
    -> CiudadAlertaApp (NavHost)
      -> ViewModels (LoginViewModel, FeedViewModel, ReportarViewModel, DetalleViewModel, EstadisticasViewModel)
        -> TicketRepository (Offline-First + SyncWorker)
          ├── LocalTicketRepository -> TicketDao -> AppDatabase (Room / SQLite)
          └── NetworkTicketRepository -> CiudadAlertaApi (Retrofit + OkHttp -> Supabase PostgREST & Storage)
```

## Configuración de Supabase y `local.properties`

1. Crea o abre un proyecto en [Supabase](https://supabase.com).
2. Ejecuta el script [`supabase/schema.sql`](supabase/schema.sql) en el **SQL Editor** de Supabase para crear:
   - Las tablas `rol_acceso`, `cuenta_usuario`, `categoria_problema`, `estatus_progreso` y `ticket_incidencia`.
   - Permisos de Data API y políticas RLS.
   - El bucket público `evidencias` en Supabase Storage para fotografías.
   - Datos iniciales de catálogos, cuentas de prueba y tickets de ejemplo.
3. Crea un archivo `local.properties` en la raíz del proyecto (usando [`local.properties.example`](local.properties.example) como plantilla) con tu ruta de Android SDK y tus credenciales de Supabase:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://TU-PROYECTO.supabase.co
SUPABASE_ANON_KEY=TU_PUBLISHABLE_OR_ANON_KEY
```

> `local.properties` está excluido en `.gitignore` para evitar subir rutas locales y llaves privadas al repositorio.

## Compilación

En Android Studio: **Sync Project with Gradle Files** → **Run** (`Shift+F10`).

Desde PowerShell (usando el JBR incluido en Android Studio):

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```
