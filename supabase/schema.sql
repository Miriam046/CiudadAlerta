-- ============================================================================
-- CIUDAD ALERTA - ESQUEMA COMPLETO PARA SUPABASE (PostgreSQL + Storage)
-- Basado en el Diagrama Entidad-Relación (ER) y Flujo de 4 Fases del proyecto.
--
-- INSTRUCCIONES:
-- 1. Crea tu proyecto en https://supabase.com
-- 2. Ve a "SQL Editor" -> "New query"
-- 3. Pega TODO este archivo y presiona "Run"
-- 4. Copia tu Project URL y tu anon/public key en local.properties
-- ============================================================================

-- 1. Catálogo de Roles de Acceso
CREATE TABLE IF NOT EXISTS public.rol_acceso (
    id_rol INT PRIMARY KEY,
    tipo_privilegio VARCHAR(60) NOT NULL UNIQUE
);

INSERT INTO public.rol_acceso (id_rol, tipo_privilegio) VALUES
    (1, 'Ciudadano'),
    (2, 'Técnico / Cuadrilla'),
    (3, 'Gestor Municipal')
ON CONFLICT (id_rol) DO NOTHING;

-- 2. Cuentas de Usuario
CREATE TABLE IF NOT EXISTS public.cuenta_usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    correo_contacto VARCHAR(150) NOT NULL UNIQUE,
    clave_hash VARCHAR(255) DEFAULT 'demo_no_validation',
    id_rol INT NOT NULL REFERENCES public.rol_acceso(id_rol) DEFAULT 1,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Catálogo de Categorías de Problemas
CREATE TABLE IF NOT EXISTS public.categoria_problema (
    id_categoria INT PRIMARY KEY,
    tipo_categoria VARCHAR(80) NOT NULL UNIQUE
);

INSERT INTO public.categoria_problema (id_categoria, tipo_categoria) VALUES
    (1, 'Bache / Vialidad'),
    (2, 'Alumbrado Público'),
    (3, 'Agua / Fuga'),
    (4, 'Seguridad Ciudadana'),
    (5, 'Acumulación de Basura')
ON CONFLICT (id_categoria) DO NOTHING;

-- 4. Catálogo de Estatus de Progreso (4 Fases del Diagrama de Flujo)
CREATE TABLE IF NOT EXISTS public.estatus_progreso (
    id_estatus INT PRIMARY KEY,
    fase_actual VARCHAR(80) NOT NULL UNIQUE
);

INSERT INTO public.estatus_progreso (id_estatus, fase_actual) VALUES
    (1, 'Registrado'),
    (2, 'En Evaluación Técnica'),
    (3, 'Mantenimiento en Progreso'),
    (4, 'Solucionado')
ON CONFLICT (id_estatus) DO NOTHING;

-- 5. Tabla Central: Tickets de Incidencia
CREATE TABLE IF NOT EXISTS public.ticket_incidencia (
    id_ticket BIGSERIAL PRIMARY KEY,
    descripcion TEXT NOT NULL,
    ruta_evidencia_url TEXT,
    coordenada_lat DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    coordenada_lng DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    registro_fecha BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    actualizacion_fecha BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    id_categoria INT NOT NULL REFERENCES public.categoria_problema(id_categoria) DEFAULT 1,
    id_estatus INT NOT NULL REFERENCES public.estatus_progreso(id_estatus) DEFAULT 1,
    es_urgente BOOLEAN NOT NULL DEFAULT FALSE,
    cuadrilla_asignada VARCHAR(120),
    solucionado_por VARCHAR(150),
    creado_por_correo VARCHAR(150)
);

CREATE INDEX IF NOT EXISTS idx_ticket_estatus ON public.ticket_incidencia(id_estatus);
CREATE INDEX IF NOT EXISTS idx_ticket_categoria ON public.ticket_incidencia(id_categoria);
CREATE INDEX IF NOT EXISTS idx_ticket_urgente_fecha ON public.ticket_incidencia(es_urgente DESC, registro_fecha DESC);

-- 6. Permisos de esquema/tablas/secuencias + Row Level Security (RLS) para demo
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT ON public.rol_acceso, public.categoria_problema, public.estatus_progreso TO anon, authenticated;
GRANT ALL ON public.cuenta_usuario, public.ticket_incidencia TO anon, authenticated;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;

ALTER TABLE public.rol_acceso ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cuenta_usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categoria_problema ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.estatus_progreso ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ticket_incidencia ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "demo_read_roles" ON public.rol_acceso;
CREATE POLICY "demo_read_roles" ON public.rol_acceso FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "demo_all_usuarios" ON public.cuenta_usuario;
CREATE POLICY "demo_all_usuarios" ON public.cuenta_usuario FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "demo_read_categorias" ON public.categoria_problema;
CREATE POLICY "demo_read_categorias" ON public.categoria_problema FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "demo_read_estatus" ON public.estatus_progreso;
CREATE POLICY "demo_read_estatus" ON public.estatus_progreso FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "demo_all_tickets" ON public.ticket_incidencia;
CREATE POLICY "demo_all_tickets" ON public.ticket_incidencia FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 7. Bucket de Supabase Storage para Evidencias Fotográficas (público)
INSERT INTO storage.buckets (id, name, public)
VALUES ('evidencias', 'evidencias', true)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "evidencias_public_select" ON storage.objects;
CREATE POLICY "evidencias_public_select" ON storage.objects
    FOR SELECT TO anon, authenticated
    USING (bucket_id = 'evidencias');

DROP POLICY IF EXISTS "evidencias_public_insert" ON storage.objects;
CREATE POLICY "evidencias_public_insert" ON storage.objects
    FOR INSERT TO anon, authenticated
    WITH CHECK (bucket_id = 'evidencias');

DROP POLICY IF EXISTS "evidencias_public_update" ON storage.objects;
CREATE POLICY "evidencias_public_update" ON storage.objects
    FOR UPDATE TO anon, authenticated
    USING (bucket_id = 'evidencias');

DROP POLICY IF EXISTS "evidencias_public_delete" ON storage.objects;
CREATE POLICY "evidencias_public_delete" ON storage.objects
    FOR DELETE TO anon, authenticated
    USING (bucket_id = 'evidencias');

-- 8. Datos iniciales de demostración (solo si la tabla está vacía)
INSERT INTO public.ticket_incidencia
    (descripcion, coordenada_lat, coordenada_lng, id_categoria, id_estatus, es_urgente, cuadrilla_asignada, solucionado_por)
SELECT * FROM (VALUES
    ('Bache profundo en carril central de Av. Insurgentes frente a la parada de autobús.', 19.4342, -99.1386, 1, 1, TRUE, NULL, NULL),
    ('3 luminarias LED inoperantes en el andador peatonal del Parque Municipal.', 19.4289, -99.1274, 2, 2, FALSE, 'Cuadrilla Alumbrado Público', NULL),
    ('Semáforo peatonal sin funcionar en cruce escolar de Primaria Benito Juárez.', 19.4265, -99.1350, 4, 3, TRUE, 'Patrulla Seguridad Urbana', NULL),
    ('Fuga de agua potable en toma principal sobre Calle Reforma esq. Juárez.', 19.4378, -99.1412, 3, 4, TRUE, 'Brigada Hidráulica Municipal', 'gestor@ciudadalerta.gob'),
    ('Acumulación de basura y escombros en esquina del Mercado Municipal.', 19.4310, -99.1295, 5, 4, FALSE, 'Brigada Limpia y Residuos', 'cuadrilla@ciudadalerta.gob')
) AS seed(descripcion, coordenada_lat, coordenada_lng, id_categoria, id_estatus, es_urgente, cuadrilla_asignada, solucionado_por)
WHERE NOT EXISTS (SELECT 1 FROM public.ticket_incidencia LIMIT 1);
