-- ============================================================
--  DATOS INICIALES - NauticaApi (Club Náutico Poseidón)
--  Spring Boot ejecuta este script al iniciar, después de que
--  Hibernate crea las tablas (spring.sql.init.mode=always).
--  Todas las sentencias son idempotentes: se pueden ejecutar
--  muchas veces sin duplicar registros.
-- ============================================================

-- ------------------------------------------------------------
-- 1. Catálogo de estados de la solicitud
-- ------------------------------------------------------------
INSERT INTO estado_solicitud (codigo, descripcion, orden) VALUES
    ('PENDIENTE',   'Pendiente de verificación', 1),
    ('VERIFICADA',  'Pendiente de aprobación',   2),
    ('NO_APTA',     'No apta (no pagador)',      3),
    ('APROBADA',    'Aprobada',                  4),
    ('DESAPROBADA', 'Desaprobada',               5)
ON CONFLICT (codigo) DO UPDATE
    SET descripcion = EXCLUDED.descripcion, orden = EXCLUDED.orden;

-- ------------------------------------------------------------
-- 2. Catálogo de clasificación de pago (verificación con otros clubes)
-- ------------------------------------------------------------
INSERT INTO clasificacion_pago (codigo, descripcion, detalle, apto, orden) VALUES
    ('PAGADOR',            'Pagador',            'Paga puntualmente. Pasa a aprobación del Jefe de Atención al Cliente.', TRUE,  1),
    ('PAGADOR_ESPORADICO', 'Pagador esporádico', 'Paga con atrasos. La solicitud queda como NO APTA.',                    FALSE, 2),
    ('RENUENTE',           'Renuente a pago',    'No paga sus obligaciones. La solicitud queda como NO APTA.',            FALSE, 3)
ON CONFLICT (codigo) DO UPDATE
    SET descripcion = EXCLUDED.descripcion, detalle = EXCLUDED.detalle,
        apto = EXCLUDED.apto, orden = EXCLUDED.orden;

-- Llaves foráneas de solicitudes_socio hacia los catálogos
ALTER TABLE solicitudes_socio DROP CONSTRAINT IF EXISTS fk_solicitud_estado;
ALTER TABLE solicitudes_socio ADD CONSTRAINT fk_solicitud_estado
    FOREIGN KEY (estado) REFERENCES estado_solicitud (codigo);
ALTER TABLE solicitudes_socio DROP CONSTRAINT IF EXISTS fk_solicitud_clasificacion;
ALTER TABLE solicitudes_socio ADD CONSTRAINT fk_solicitud_clasificacion
    FOREIGN KEY (clasificacion) REFERENCES clasificacion_pago (codigo);

-- Columna del formato antiguo de código de seguimiento (sprint 3)
ALTER TABLE solicitudes_socio DROP COLUMN IF EXISTS codigo_seguimiento;
UPDATE solicitudes_socio SET codigo = gen_random_uuid() WHERE codigo IS NULL;

-- ------------------------------------------------------------
-- 3. Módulos del menú dinámico
-- ------------------------------------------------------------
INSERT INTO modulos (codigo, nombre, ruta, icono, orden, activo) VALUES
    ('DASHBOARD',             'Dashboard',             '/panel/dashboard',             'bi-graph-up',        1, TRUE),
    ('SOLICITUDES_VERIFICAR', 'Verificar solicitudes', '/panel/solicitudes/verificar', 'bi-clipboard-check', 2, TRUE),
    ('SOLICITUDES_APROBAR',   'Aprobar solicitudes',   '/panel/solicitudes/aprobar',   'bi-check2-circle',   3, TRUE),
    ('AUDITORIA',             'Auditoría',             '/panel/auditoria',             'bi-shield-check',    4, TRUE),
    ('MIS_EMBARCACIONES',     'Mis embarcaciones',     '/panel/mis-embarcaciones',     'bi-water',           5, TRUE),
    ('EMBARCACIONES_VERIFICAR','Verificar embarcaciones','/panel/embarcaciones/verificar','bi-patch-check',   6, TRUE),
    ('RADAS',                 'Asignar radas',         '/panel/radas',                 'bi-signpost-split',  7, TRUE),
    ('PERFIL',                'Mi perfil',             '/panel/perfil',                'bi-person-circle',   9, TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- ------------------------------------------------------------
-- 4. Roles (perfiles de usuario)
-- ------------------------------------------------------------
INSERT INTO roles (codigo, nombre, activo) VALUES
    ('GERENTE',            'Gerente General',     TRUE),
    ('ATENCION_CLIENTE',   'Atención al Cliente', TRUE),
    ('SERVICIOS_NAVIEROS', 'Servicios Navieros',  TRUE),
    ('ADMINISTRACION',     'Administración',      TRUE),
    ('SOCIO',              'Socio',               TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- ------------------------------------------------------------
-- 5. Módulos asignados a cada rol
-- ------------------------------------------------------------
INSERT INTO rol_modulo (rol_id, modulo_id)
SELECT r.id, m.id
FROM (VALUES
        ('GERENTE', 'DASHBOARD'), ('GERENTE', 'SOLICITUDES_VERIFICAR'), ('GERENTE', 'SOLICITUDES_APROBAR'),
        ('GERENTE', 'AUDITORIA'), ('GERENTE', 'PERFIL'),
        ('ATENCION_CLIENTE', 'SOLICITUDES_VERIFICAR'), ('ATENCION_CLIENTE', 'SOLICITUDES_APROBAR'),
        ('ATENCION_CLIENTE', 'PERFIL'),
        ('GERENTE', 'EMBARCACIONES_VERIFICAR'), ('GERENTE', 'RADAS'),
        ('SERVICIOS_NAVIEROS', 'EMBARCACIONES_VERIFICAR'), ('SERVICIOS_NAVIEROS', 'RADAS'),
        ('SERVICIOS_NAVIEROS', 'PERFIL'),
        ('ADMINISTRACION', 'DASHBOARD'), ('ADMINISTRACION', 'PERFIL'),
        ('SOCIO', 'MIS_EMBARCACIONES'), ('SOCIO', 'PERFIL')
     ) AS v (rol, modulo)
JOIN roles r ON r.codigo = v.rol
JOIN modulos m ON m.codigo = v.modulo
WHERE NOT EXISTS (SELECT 1 FROM rol_modulo x WHERE x.rol_id = r.id AND x.modulo_id = m.id);

-- ------------------------------------------------------------
-- 6. Usuarios de prueba (contraseña: Poseidon2026, encriptada con BCrypt)
-- ------------------------------------------------------------
INSERT INTO usuarios (nombres, apellidos, correo, password, rol_id, activo, fecha_creacion)
SELECT v.nombres, v.apellidos, v.correo,
       '$2a$10$B0jA6.CLeBQj3x3RLqeXXOpR6sslfQAs/sfccYt6Ms1EtbR9DeZxW',
       r.id, TRUE, NOW()
FROM (VALUES
        ('Gerente',   'General',        'gerente@poseidon.pe',        'GERENTE'),
        ('Atención',  'Cliente',        'atencion@poseidon.pe',       'ATENCION_CLIENTE'),
        ('Servicios', 'Navieros',       'navieros@poseidon.pe',       'SERVICIOS_NAVIEROS'),
        ('Personal',  'Administración', 'administracion@poseidon.pe', 'ADMINISTRACION'),
        ('Socio',     'De Prueba',      'socio@poseidon.pe',          'SOCIO')
     ) AS v (nombres, apellidos, correo, rol)
JOIN roles r ON r.codigo = v.rol
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.correo = v.correo);

-- ------------------------------------------------------------
-- 7. Solicitudes de ejemplo para el dashboard (180 postulantes, últimos 120 días)
--    Se generan una sola vez: si ya existen correos @demo.poseidon.pe no se vuelven a insertar.
-- ------------------------------------------------------------
INSERT INTO solicitudes_socio (
    codigo, nombres, apellidos, dni, fecha_nacimiento, correo, celular, direccion,
    posee_embarcacion, cantidad_embarcaciones, password_hash, estado, clasificacion,
    resultado_verificacion, motivo_desaprobacion, fecha_solicitud, fecha_verificacion,
    fecha_resolucion, verificado_por, resuelto_por)
SELECT
    gen_random_uuid(),
    b.nombre,
    b.apellido,
    b.dni,
    b.fecha_nacimiento,
    'postulante' || LPAD(CAST(b.g AS text), 3, '0') || '@demo.poseidon.pe',
    '9' || LPAD(CAST((b.g * 7919) % 100000000 AS text), 8, '0'),
    b.direccion,
    b.embarcaciones > 0,
    b.embarcaciones,
    '$2a$10$B0jA6.CLeBQj3x3RLqeXXOpR6sslfQAs/sfccYt6Ms1EtbR9DeZxW',
    b.estado,
    b.clasificacion,
    CASE b.clasificacion
        WHEN 'PAGADOR'            THEN 'Los clubes consultados confirman pagos puntuales de cuotas.'
        WHEN 'PAGADOR_ESPORADICO' THEN 'Un club consultado reporta pagos con atrasos frecuentes.'
        WHEN 'RENUENTE'           THEN 'Un club consultado reporta deuda impaga de más de 6 meses.'
    END,
    CASE WHEN b.estado = 'DESAPROBADA' THEN
        (ARRAY['Documento de identidad ilegible. Debe adjuntar copia legible del DNI.',
               'La dirección declarada no coincide con el recibo de servicios presentado.',
               'Falta la carta de presentación de dos socios del club.'])[1 + b.g % 3]
    END,
    b.fecha_solicitud,
    CASE WHEN b.clasificacion IS NOT NULL THEN b.fecha_solicitud + b.horas_verificacion END,
    CASE WHEN b.estado IN ('APROBADA', 'DESAPROBADA')
         THEN LEAST(b.fecha_solicitud + b.horas_verificacion + b.horas_resolucion, LOCALTIMESTAMP) END,
    CASE WHEN b.clasificacion IS NOT NULL THEN 'atencion@poseidon.pe' END,
    CASE WHEN b.estado IN ('APROBADA', 'DESAPROBADA') THEN 'gerente@poseidon.pe' END
FROM (
    SELECT a.*,
           CASE
               WHEN a.dias_atras <= 5 THEN 'PENDIENTE'
               WHEN a.clasif_base <> 'PAGADOR' THEN 'NO_APTA'
               WHEN a.dias_atras <= 14 THEN 'VERIFICADA'
               WHEN a.g % 7 = 0 THEN 'DESAPROBADA'
               ELSE 'APROBADA'
           END AS estado,
           CASE WHEN a.dias_atras <= 5 THEN NULL ELSE a.clasif_base END AS clasificacion,
           -- Verificar a un renuente o esporádico toma más llamadas a otros clubes
           MAKE_INTERVAL(hours => CASE a.clasif_base WHEN 'PAGADOR' THEN 4 + (a.g * 11) % 30
                                                     ELSE 20 + (a.g * 13) % 50 END) AS horas_verificacion,
           MAKE_INTERVAL(hours => 6 + (a.g * 17) % 90) AS horas_resolucion
    FROM (
        SELECT g,
               (ARRAY['Carlos','Lucía','Jorge','María','Andrés','Valeria','Ricardo','Daniela','Fernando','Camila',
                      'Miguel','Sofía','Luis','Gabriela','Pedro','Alejandra','Héctor','Paola','Raúl','Elena',
                      'Martín','Natalia','Óscar','Rosa','Iván','Patricia','Diego','Carmen','Javier','Silvia'])[1 + (g * 7) % 30] AS nombre,
               (ARRAY['Mendoza','Paredes','Salazar','Quispe','Torres','Gutiérrez','Flores','Rojas','Castillo','Vargas',
                      'Herrera','Morales','Ramírez','Núñez','Aguilar','Silva','Cárdenas','Espinoza','Navarro','Ponce'])[1 + (g * 3) % 20]
               || ' ' ||
               (ARRAY['Ríos','Soto','Vega','Huamán','Lazo','Pinto','Chávez','Medina','Ramos','León',
                      'Díaz','Cruz','Ortiz','Campos','Reyes','Romero','Loayza','Arias','Salas','Villanueva'])[1 + (g * 11) % 20] AS apellido,
               -- Tres postulantes no aptos vuelven a postular con el mismo DNI: alerta de doble inscripción
               CASE WHEN g IN (47, 97, 147) THEN CAST(41000000 + ((g - 30) * 104729) % 8999999 AS text)
                    ELSE CAST(41000000 + (g * 104729) % 8999999 AS text) END AS dni,
               DATE '1958-01-01' + (g * 2917) % 16000 AS fecha_nacimiento,
               (ARRAY['Av. La Marina','Jr. Los Pinos','Calle Las Gaviotas','Av. Costanera','Jr. Bolognesi','Av. Brasil'])[1 + g % 6]
               || ' ' || (100 + (g * 37) % 900) || ', ' ||
               (ARRAY['San Miguel','Miraflores','La Punta','Barranco','Chorrillos','San Isidro','Magdalena','Surco'])[1 + (g * 5) % 8]
               || ', Lima' AS direccion,
               -- Los postulantes de más edad suelen tener más embarcaciones
               CASE WHEN g % 4 = 0 THEN 0
                    ELSE 1 + (2026 - (1958 + ((g * 2917) % 16000) / 365)) / 18 + g % 2 END AS embarcaciones,
               CASE WHEN g % 10 IN (7, 8) THEN 'PAGADOR_ESPORADICO'
                    WHEN g % 10 = 9 OR g % 13 = 0 THEN 'RENUENTE'
                    ELSE 'PAGADOR' END AS clasif_base,
               CASE WHEN g IN (47, 97, 147) THEN g % 3 ELSE (g * 37) % 120 END AS dias_atras,
               -- Fecha y hora de registro: más solicitudes en horario de oficina
               LEAST(CAST(CURRENT_DATE - CASE WHEN g IN (47, 97, 147) THEN g % 3 ELSE (g * 37) % 120 END AS timestamp)
                   + MAKE_INTERVAL(hours => (ARRAY[8,9,9,10,10,10,11,11,12,13,14,15,15,16,16,17,18,19])[1 + (g * 5 + g / 4) % 18],
                                   mins  => (g * 13) % 60), LOCALTIMESTAMP) AS fecha_solicitud
        FROM generate_series(1, 180) AS g
    ) a
) b
WHERE NOT EXISTS (SELECT 1 FROM solicitudes_socio s WHERE s.correo LIKE '%@demo.poseidon.pe');

-- Usuarios con rol Socio para las solicitudes de ejemplo aprobadas
INSERT INTO usuarios (nombres, apellidos, correo, password, rol_id, activo, fecha_creacion)
SELECT s.nombres, s.apellidos, s.correo, s.password_hash, r.id, TRUE, s.fecha_resolucion
FROM solicitudes_socio s
JOIN roles r ON r.codigo = 'SOCIO'
WHERE s.estado = 'APROBADA'
  AND s.correo LIKE '%@demo.poseidon.pe'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.correo = s.correo);

UPDATE solicitudes_socio s
SET usuario_id = u.id
FROM usuarios u
WHERE u.correo = s.correo
  AND s.estado = 'APROBADA'
  AND s.usuario_id IS NULL;

-- Datos de ejemplo del sprint 3: se reemplazan nombres de clubes reales por un texto genérico
UPDATE solicitudes_socio
SET resultado_verificacion = CASE clasificacion
        WHEN 'PAGADOR'            THEN 'Los clubes consultados confirman pagos puntuales de cuotas.'
        WHEN 'PAGADOR_ESPORADICO' THEN 'Un club consultado reporta pagos con atrasos frecuentes.'
        ELSE 'Un club consultado reporta deuda impaga de más de 6 meses.'
    END
WHERE resultado_verificacion LIKE '%Club Regatas Lima%'
   OR resultado_verificacion LIKE '%Club Náutico Ancón%';

-- ============================================================
--  SPRINT 4: EMBARCACIONES Y RADAS (HU11, HU12, HU13)
-- ============================================================

-- ------------------------------------------------------------
-- 8. Catálogos de embarcaciones
-- ------------------------------------------------------------
INSERT INTO tipo_embarcacion (codigo, descripcion, orden) VALUES
    ('MOTO_ACUATICA', 'Moto acuática', 1),
    ('BOTE',          'Bote',          2),
    ('LANCHA',        'Lancha',        3),
    ('VELERO',        'Velero',        4),
    ('CATAMARAN',     'Catamarán',     5),
    ('YATE',          'Yate',          6)
ON CONFLICT (codigo) DO UPDATE SET descripcion = EXCLUDED.descripcion, orden = EXCLUDED.orden;

INSERT INTO estado_embarcacion (codigo, descripcion, orden) VALUES
    ('PENDIENTE',  'Pendiente de verificación', 1),
    ('VERIFICADA', 'Verificada',                2),
    ('OBSERVADA',  'Observada',                 3)
ON CONFLICT (codigo) DO UPDATE SET descripcion = EXCLUDED.descripcion, orden = EXCLUDED.orden;

ALTER TABLE embarcaciones DROP CONSTRAINT IF EXISTS fk_embarcacion_tipo;
ALTER TABLE embarcaciones ADD CONSTRAINT fk_embarcacion_tipo
    FOREIGN KEY (tipo) REFERENCES tipo_embarcacion (codigo);
ALTER TABLE embarcaciones DROP CONSTRAINT IF EXISTS fk_embarcacion_estado;
ALTER TABLE embarcaciones ADD CONSTRAINT fk_embarcacion_estado
    FOREIGN KEY (estado) REFERENCES estado_embarcacion (codigo);

-- ------------------------------------------------------------
-- 9. Radas: muelle A (naves pequeñas), B (medianas) y C (grandes)
-- ------------------------------------------------------------
INSERT INTO radas (codigo, muelle, eslora_max, manga_max, calado_max, tarifa_mensual, estado)
SELECT 'A-' || LPAD(CAST(n AS text), 2, '0'), 'A',
       CASE WHEN n <= 5 THEN 8.00 ELSE 6.00 END,
       CASE WHEN n <= 5 THEN 3.00 ELSE 2.50 END,
       1.50,
       CASE WHEN n <= 5 THEN 380.00 ELSE 300.00 END,
       'LIBRE'
FROM generate_series(1, 10) AS n
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO radas (codigo, muelle, eslora_max, manga_max, calado_max, tarifa_mensual, estado)
SELECT 'B-' || LPAD(CAST(n AS text), 2, '0'), 'B',
       CASE WHEN n <= 4 THEN 15.00 ELSE 12.00 END,
       CASE WHEN n <= 4 THEN 5.00 ELSE 4.20 END,
       2.50,
       CASE WHEN n <= 4 THEN 820.00 ELSE 680.00 END,
       'LIBRE'
FROM generate_series(1, 10) AS n
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO radas (codigo, muelle, eslora_max, manga_max, calado_max, tarifa_mensual, estado)
SELECT 'C-' || LPAD(CAST(n AS text), 2, '0'), 'C',
       CASE WHEN n <= 2 THEN 30.00 ELSE 22.00 END,
       CASE WHEN n <= 2 THEN 8.00 ELSE 7.00 END,
       CASE WHEN n <= 2 THEN 4.00 ELSE 3.20 END,
       CASE WHEN n <= 2 THEN 1650.00 ELSE 1250.00 END,
       'LIBRE'
FROM generate_series(1, 6) AS n
ON CONFLICT (codigo) DO NOTHING;

-- ------------------------------------------------------------
-- 10. Embarcaciones de ejemplo (matrícula terminada en -DM). Se insertan una sola vez.
-- ------------------------------------------------------------
-- Tres naves del socio de prueba (socio@poseidon.pe): verificada, observada y pendiente
INSERT INTO embarcaciones (socio_id, nombre, matricula, tipo, eslora, manga, calado, anio_fabricacion,
                           estado, observaciones, fecha_registro, fecha_verificacion, verificado_por)
SELECT u.id, v.nombre, v.matricula, v.tipo, v.eslora, v.manga, v.calado, v.anio, v.estado, v.observaciones,
       LOCALTIMESTAMP - MAKE_INTERVAL(days => v.dias),
       CASE WHEN v.estado <> 'PENDIENTE' THEN LOCALTIMESTAMP - MAKE_INTERVAL(days => v.dias - 1) END,
       CASE WHEN v.estado <> 'PENDIENTE' THEN 'navieros@poseidon.pe' END
FROM (VALUES
        ('Gaviota II',   'CO-48213-DM', 'VELERO', 11.40, 3.80, 1.90, 2015, 'VERIFICADA', NULL, 20),
        ('Brisa Marina', 'CO-50921-DM', 'LANCHA',  7.60, 2.80, 0.90, 2019, 'OBSERVADA',
         'La matrícula registrada en Capitanías termina en -RE. Adjunta el certificado de matrícula vigente.', 6),
        ('Delfín',       'CO-51377-DM', 'BOTE',    5.20, 2.10, 0.60, 2021, 'PENDIENTE', NULL, 1)
     ) AS v (nombre, matricula, tipo, eslora, manga, calado, anio, estado, observaciones, dias)
JOIN usuarios u ON u.correo = 'socio@poseidon.pe'
WHERE NOT EXISTS (SELECT 1 FROM embarcaciones e WHERE e.matricula LIKE 'CO-%-DM');

-- Una nave por cada socio de ejemplo aprobado que declaró embarcaciones (máximo 45)
INSERT INTO embarcaciones (socio_id, nombre, matricula, tipo, eslora, manga, calado, anio_fabricacion,
                           estado, observaciones, fecha_registro, fecha_verificacion, verificado_por)
SELECT b.usuario_id,
       (ARRAY['Albatros','Pelícano','Neptuno','Marea Alta','Estrella del Sur','Coral','Tritón','Poseidón Jr',
              'Ola Azul','Cormorán','Viento Norte','Lobo de Mar','Andrea','Sirena','Ancla Dorada'])[1 + b.rn % 15]
           || ' ' || (ARRAY['I','II','III'])[1 + (b.rn / 15) % 3],
       'PL-' || LPAD(CAST(30000 + b.rn * 457 AS text), 5, '0') || '-DM',
       b.tipo,
       b.eslora, b.manga, b.calado,
       2000 + b.rn % 24,
       b.estado,
       CASE WHEN b.estado = 'OBSERVADA' THEN
           (ARRAY['El certificado de navegabilidad está vencido. Adjunta el certificado renovado.',
                  'La eslora declarada no coincide con el registro de Capitanías. Corrige la medida.'])[1 + b.rn % 2]
       END,
       b.fecha_registro,
       CASE WHEN b.estado <> 'PENDIENTE' THEN b.fecha_registro + MAKE_INTERVAL(hours => CAST(12 + b.rn % 30 AS integer)) END,
       CASE WHEN b.estado <> 'PENDIENTE' THEN 'navieros@poseidon.pe' END
FROM (
    SELECT a.*,
           -- Medidas típicas de cada tipo de nave, con una pequeña variación
           CASE a.tipo WHEN 'MOTO_ACUATICA' THEN 3.20 WHEN 'BOTE' THEN 5.40 WHEN 'LANCHA' THEN 7.50
                       WHEN 'VELERO' THEN 11.20 WHEN 'CATAMARAN' THEN 12.40 ELSE 18.60 END + (a.rn % 3) * 0.10 AS eslora,
           CASE a.tipo WHEN 'MOTO_ACUATICA' THEN 1.20 WHEN 'BOTE' THEN 2.10 WHEN 'LANCHA' THEN 2.70
                       WHEN 'VELERO' THEN 3.70 WHEN 'CATAMARAN' THEN 6.20 ELSE 5.40 END AS manga,
           CASE a.tipo WHEN 'MOTO_ACUATICA' THEN 0.40 WHEN 'BOTE' THEN 0.60 WHEN 'LANCHA' THEN 0.90
                       WHEN 'VELERO' THEN 1.90 WHEN 'CATAMARAN' THEN 1.20 ELSE 2.40 END AS calado,
           CASE WHEN a.rn % 7 = 0 THEN 'OBSERVADA'
                WHEN a.rn % 7 IN (1, 2) THEN 'PENDIENTE'
                ELSE 'VERIFICADA' END AS estado
    FROM (
        SELECT s.usuario_id, s.fecha_resolucion + INTERVAL '3 days' AS fecha_registro,
               ROW_NUMBER() OVER (ORDER BY s.fecha_resolucion) AS rn,
               (ARRAY['MOTO_ACUATICA','BOTE','LANCHA','LANCHA','VELERO','VELERO','CATAMARAN','YATE'])
                   [1 + (ROW_NUMBER() OVER (ORDER BY s.fecha_resolucion)) % 8] AS tipo
        FROM solicitudes_socio s
        WHERE s.estado = 'APROBADA'
          AND s.correo LIKE '%@demo.poseidon.pe'
          AND s.cantidad_embarcaciones > 0
          AND s.usuario_id IS NOT NULL
    ) a
    WHERE a.rn <= 45
) b
WHERE NOT EXISTS (SELECT 1 FROM embarcaciones e WHERE e.matricula LIKE 'PL-%-DM');

-- Asignación inicial: la nave verificada más grande va a la rada más grande de su muelle donde quepa.
-- Una de cada cinco naves verificadas queda "por asignar" para la demostración de HU13.
WITH naves AS (
    SELECT e.id, e.eslora, e.manga, e.calado,
           CASE WHEN e.eslora <= 8 AND e.manga <= 3 AND e.calado <= 1.5 THEN 'A'
                WHEN e.eslora <= 15 AND e.manga <= 5 AND e.calado <= 2.5 THEN 'B'
                ELSE 'C' END AS muelle,
           ROW_NUMBER() OVER (ORDER BY e.id) AS orden
    FROM embarcaciones e
    WHERE e.estado = 'VERIFICADA' AND e.rada_id IS NULL AND e.matricula LIKE '%-DM'
), candidatas AS (
    SELECT n.*, ROW_NUMBER() OVER (PARTITION BY n.muelle ORDER BY n.eslora DESC, n.id) AS puesto
    FROM naves n
    WHERE n.orden % 5 <> 0
), radas_libres AS (
    SELECT r.*, ROW_NUMBER() OVER (PARTITION BY r.muelle ORDER BY r.eslora_max DESC, r.codigo) AS puesto
    FROM radas r
    WHERE r.estado = 'LIBRE'
)
INSERT INTO asignaciones_rada (rada_id, embarcacion_id, fecha_asignacion, asignado_por, activa)
SELECT r.id, c.id, LOCALTIMESTAMP - MAKE_INTERVAL(days => CAST(10 + c.id % 40 AS integer)), 'navieros@poseidon.pe', TRUE
FROM candidatas c
JOIN radas_libres r ON r.muelle = c.muelle AND r.puesto = c.puesto
WHERE r.eslora_max >= c.eslora AND r.manga_max >= c.manga AND r.calado_max >= c.calado
  AND NOT EXISTS (SELECT 1 FROM asignaciones_rada);

UPDATE embarcaciones e
SET rada_id = a.rada_id
FROM asignaciones_rada a
WHERE a.embarcacion_id = e.id AND a.activa AND e.rada_id IS NULL;

UPDATE radas r
SET estado = 'OCUPADA'
WHERE EXISTS (SELECT 1 FROM asignaciones_rada a WHERE a.rada_id = r.id AND a.activa)
  AND r.estado <> 'OCUPADA';
