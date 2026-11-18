-- ==========================================
-- Cuenta de servicio para el dispositivo IoT (ESP32 "Poste Civico")
-- ReportaYA — integracion IoT
-- ==========================================
-- Crea una cuenta CIUDADANO *activa* que el ESP32 usa para autenticarse
-- (POST /api/auth/login) y crear reportes automaticos (POST /api/reportes).
--
-- Por que CIUDADANO: crear reportes (POST /api/reportes) esta permitido solo a
-- CIUDADANO, y el dueño de un reporte puede consultar su detalle. El registro publico
-- (POST /api/cuenta) dejaria la cuenta INACTIVA hasta verificar el correo, por
-- eso la insertamos directo con activo=true (mismo criterio que operadores/tecnicos).
--
-- Contrasena: poste123, guardada como hash BCrypt (el backend ya no acepta
-- contrasenas en texto plano; si encuentra alguna al arrancar, la convierte).
--
-- Idempotente: si la cuenta 'poste_iot' ya existe, no hace nada.
-- Ejecutar en pgAdmin (Query Tool) sobre la BD del proyecto, DESPUES de
-- crear-TABLAS.sql (y, si aplica, llenar-TABLAS.sql).
-- ==========================================

DO $$
DECLARE
  v_persona_id BIGINT;
  v_cuenta_id  BIGINT;
BEGIN
  IF EXISTS (SELECT 1 FROM cuentas WHERE usuario = 'poste_iot') THEN
    RAISE NOTICE 'La cuenta de servicio "poste_iot" ya existe; no se vuelve a crear.';
    RETURN;
  END IF;

  -- 1) Persona del dispositivo
  INSERT INTO personas (nombres, apellidos, dni, telefono, correo)
  VALUES ('Poste', 'Civico IoT', '00000001', '900000001', 'poste.iot@reportaya.demo')
  RETURNING id INTO v_persona_id;

  -- 2) Cuenta activa. Contrasena: poste123 (guardada como hash BCrypt).
  INSERT INTO cuentas (usuario, contrasena_hash, persona_id, fecha_creacion, fecha_actualizacion, activo)
  VALUES ('poste_iot', '$2a$10$456qiCCG68hlCWEqvCp8sOqHYVMgdMAMaIgnjCknXZpxDXWcxlRry', v_persona_id, NOW(), NOW(), true)
  RETURNING id INTO v_cuenta_id;

  -- 3) Marca de subclase (herencia JOINED)
  INSERT INTO ciudadanos (id) VALUES (v_cuenta_id);

  RAISE NOTICE 'Cuenta de servicio creada. usuario=poste_iot  cuenta_id=%  (usar como SVC_CUENTA_ID en el firmware)', v_cuenta_id;
END $$;

-- Muestra el cuenta_id a copiar en el firmware (constante SVC_CUENTA_ID)
SELECT c.id AS svc_cuenta_id, c.usuario, c.activo, p.correo
FROM cuentas c
JOIN personas p ON p.id = c.persona_id
WHERE c.usuario = 'poste_iot';

-- ==========================================
-- (Opcional) Limpieza — descomentar para borrar la cuenta de servicio:
-- ==========================================
/*
DELETE FROM ciudadanos WHERE id IN (SELECT id FROM cuentas WHERE usuario = 'poste_iot');
DELETE FROM cuentas    WHERE usuario = 'poste_iot';
DELETE FROM personas   WHERE correo  = 'poste.iot@reportaya.demo';
*/
