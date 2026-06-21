-- ==========================================
-- Datos de prueba - ReportaYA
-- Alineado a los JSON del front (assets/data/)
-- ==========================================
-- IDs fijos para coincidir con cuentas.json:
--   1 = Ana Quispe (CIUDADANO)
--   2 = Jose Rios (CIUDADANO)
--   3 = Maria Rojas (OPERADOR_MUNICIPAL)
--   4 = Carlos Mendoza (TECNICO)
--   5 = Lucia Paredes (TECNICO)
--   6 = Maria Torres (TECNICO)
-- ==========================================

-- Limpiar todo
TRUNCATE TABLE
  token_notificaciones,
  historial_estados,
  fotos,
  reportes,
  ubicaciones,
  operadores_municipales,
  tecnicos,
  ciudadanos,
  cuentas,
  personas
RESTART IDENTITY CASCADE;

-- ==========================================
-- PERSONAS (mismos datos que cuentas.json)
-- ==========================================
INSERT INTO personas (id, nombres, apellidos, dni, telefono, correo) VALUES
(1, 'Ana',    'Quispe',  '45781230', '987654321', 'ana.quispe@mail.com'),
(2, 'Jose',   'Rios',    '41239876', '986112233', 'jose.rios@mail.com'),
(3, 'Maria',  'Rojas',   '09887766', '955443322', 'maria.rojas@muni.gob.pe'),
(4, 'Carlos', 'Mendoza', '70123456', '933221100', 'carlos.mendoza@muni.gob.pe'),
(5, 'Lucia',  'Paredes', '70988111', '933000111', 'lucia.paredes@muni.gob.pe'),
(6, 'Maria',  'Torres',  '70555222', '933444555', 'maria.torres@muni.gob.pe');

SELECT setval('personas_id_seq', 6);

-- ==========================================
-- CUENTAS (contrasena en texto plano: el backend la migra a BCrypt en el primer login)
-- ==========================================
INSERT INTO cuentas (id, usuario, contrasena_hash, persona_id, fecha_creacion, fecha_actualizacion, activo) VALUES
(1, 'ciudadano',  '123456', 1, NOW(), NOW(), true),
(2, 'jose',       '123456', 2, NOW(), NOW(), true),
(3, 'op.maria',   '123456', 3, NOW(), NOW(), true),
(4, 'tec.carlos', '123456', 4, NOW(), NOW(), true),
(5, 'tec.lucia',  '123456', 5, NOW(), NOW(), true),
(6, 'tec.maria',  '123456', 6, NOW(), NOW(), true);

SELECT setval('cuentas_id_seq', 6);

-- ==========================================
-- SUBCLASES (herencia JOINED)
-- ==========================================
INSERT INTO ciudadanos (id) VALUES (1), (2);
INSERT INTO operadores_municipales (id) VALUES (3);
INSERT INTO tecnicos (id) VALUES (4), (5), (6);

-- ==========================================
-- UBICACIONES (de reportes.json)
-- ==========================================
INSERT INTO ubicaciones (id, direccion, latitud, longitud) VALUES
(1, 'Av. Arequipa 1234, Lince',            -12.08530, -77.03760),
(2, 'Jr. Las Begonias 245, San Isidro',     -12.09650, -77.03200),
(3, 'Av. Brasil 800, Pueblo Libre',         -12.07210, -77.06320),
(4, 'Av. Javier Prado 4500, Surco',        -12.08900, -76.99100),
(5, 'Parque Reducto, Miraflores',           -12.12050, -77.02980),
(6, 'Av. Petit Thouars 3300, San Isidro',   -12.09980, -77.03450);

SELECT setval('ubicaciones_id_seq', 6);

-- ==========================================
-- REPORTES (de reportes.json — mismos IDs)
-- ==========================================
INSERT INTO reportes (id, titulo, descripcion, cuenta_id, ubicacion_id, tecnico_id, tipo_problema, estado, fecha_creacion, fecha_actualizacion, comentario_resolucion, fecha_cierre) VALUES
(1042, 'Hueco profundo en la pista',
       'Hueco de unos 40 cm en el carril derecho. Ya provoco dos llantas pinchadas esta semana. Pasa frente a la entrada del colegio.',
       1, 1, 4, 'INFRAESTRUCTURA', 'REVISION',
       '2026-04-27 08:42:00', '2026-04-27 10:20:00', NULL, NULL),

(1041, 'Poste sin alumbrado',
       'El poste lleva 5 dias sin luz, la cuadra queda totalmente a oscuras de noche.',
       1, 2, 5, 'ALUMBRADO', 'REVISION',
       '2026-04-26 19:02:00', '2026-04-26 21:12:00', NULL, NULL),

(1040, 'Acumulacion de basura',
       'Punto de acopio improvisado lleva 3 dias sin recojo. Mal olor y presencia de roedores.',
       1, 3, NULL, 'RESIDUOS', 'PENDIENTE',
       '2026-04-26 14:05:00', '2026-04-26 14:05:00', NULL, NULL),

(1039, 'Semaforo intermitente',
       'El semaforo de la interseccion quedo en amarillo intermitente, riesgo de choques.',
       1, 4, 4, 'SEGURIDAD', 'FINALIZADO',
       '2026-04-24 07:30:00', '2026-04-25 16:40:00',
       'Se reemplazo el modulo de control del semaforo y se verifico el ciclo completo. Operativo.',
       '2026-04-25 16:40:00'),

(1038, 'Vidrio roto en parque infantil',
       'Botellas rotas en la zona de juegos para ninos.',
       2, 5, NULL, 'SEGURIDAD', 'PENDIENTE',
       '2026-04-26 11:20:00', '2026-04-26 11:20:00', NULL, NULL),

(1037, 'Rampa rota para sillas de ruedas',
       'La rampa de acceso esta fracturada y es peligrosa.',
       1, 6, NULL, 'INFRAESTRUCTURA', 'RECHAZADO',
       '2026-04-23 10:00:00', '2026-04-23 15:30:00',
       'Rechazado: la rampa pertenece a un predio privado, no corresponde a la via publica municipal.',
       '2026-04-23 15:30:00');

SELECT setval('reportes_id_seq', 1042);

-- ==========================================
-- FOTOS (de fotos.json — mismos IDs)
-- ==========================================
INSERT INTO fotos (id, reporte_id, url, tipo, descripcion, fecha_carga) VALUES
(1, 1042, 'assets/img/sample/hueco_1.png',        'INICIAL', 'Vista del hueco',        '2026-04-27 08:42:00'),
(2, 1042, 'assets/img/sample/hueco_2.png',        'INICIAL', 'Detalle del dano',       '2026-04-27 08:42:00'),
(3, 1041, 'assets/img/sample/poste_1.png',        'INICIAL', 'Poste apagado',          '2026-04-26 19:02:00'),
(4, 1040, 'assets/img/sample/basura_1.png',       'INICIAL', 'Acumulacion',            '2026-04-26 14:05:00'),
(5, 1039, 'assets/img/sample/semaforo_1.png',     'INICIAL', 'Semaforo intermitente',  '2026-04-24 07:30:00'),
(6, 1039, 'assets/img/sample/semaforo_fin_1.png', 'FINAL',   'Semaforo reparado',      '2026-04-25 16:40:00'),
(7, 1038, 'assets/img/sample/vidrio_1.png',       'INICIAL', 'Vidrios rotos',          '2026-04-26 11:20:00');

SELECT setval('fotos_id_seq', 7);

-- ==========================================
-- HISTORIAL DE ESTADOS (de historial.json — mismos IDs)
-- ==========================================
INSERT INTO historial_estados (id, reporte_id, estado_anterior, estado_nuevo, fecha_cambio) VALUES
( 1, 1042, NULL,        'PENDIENTE',  '2026-04-27 08:42:00'),
( 2, 1042, 'PENDIENTE', 'REVISION',   '2026-04-27 10:15:00'),
( 3, 1041, NULL,        'PENDIENTE',  '2026-04-26 19:02:00'),
( 4, 1041, 'PENDIENTE', 'REVISION',   '2026-04-26 21:10:00'),
( 5, 1040, NULL,        'PENDIENTE',  '2026-04-26 14:05:00'),
( 6, 1039, NULL,        'PENDIENTE',  '2026-04-24 07:30:00'),
( 7, 1039, 'PENDIENTE', 'REVISION',   '2026-04-24 09:00:00'),
( 8, 1039, 'REVISION',  'FINALIZADO', '2026-04-25 16:40:00'),
( 9, 1038, NULL,        'PENDIENTE',  '2026-04-26 11:20:00'),
(10, 1037, NULL,        'PENDIENTE',  '2026-04-23 10:00:00'),
(11, 1037, 'PENDIENTE', 'RECHAZADO',  '2026-04-23 15:30:00');

SELECT setval('historial_estados_id_seq', 11);

-- ==========================================
-- VERIFICACION
-- ==========================================
SELECT '=== CUENTAS ===' AS seccion;
SELECT c.id, c.usuario, p.nombres, p.apellidos,
       CASE WHEN ci.id IS NOT NULL THEN 'CIUDADANO'
            WHEN o.id  IS NOT NULL THEN 'OPERADOR_MUNICIPAL'
            WHEN t.id  IS NOT NULL THEN 'TECNICO'
       END AS tipo_cuenta
FROM cuentas c
JOIN personas p ON p.id = c.persona_id
LEFT JOIN ciudadanos ci ON ci.id = c.id
LEFT JOIN operadores_municipales o ON o.id = c.id
LEFT JOIN tecnicos t ON t.id = c.id
ORDER BY c.id;

SELECT '=== REPORTES ===' AS seccion;
SELECT r.id, r.titulo, r.estado, r.tipo_problema, r.tecnico_id,
       p.nombres || ' ' || p.apellidos AS ciudadano
FROM reportes r
JOIN cuentas c ON c.id = r.cuenta_id
JOIN personas p ON p.id = c.persona_id
ORDER BY r.id DESC;

SELECT '=== FOTOS ===' AS seccion;
SELECT id, reporte_id, tipo, descripcion FROM fotos ORDER BY reporte_id, id;

SELECT '=== HISTORIAL ===' AS seccion;
SELECT id, reporte_id, estado_anterior, estado_nuevo, fecha_cambio
FROM historial_estados ORDER BY reporte_id, fecha_cambio;
