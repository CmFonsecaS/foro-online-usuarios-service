-- =====================================================
-- PROYECTO: SISTEMA DE FOROS EN LINEA
-- ASIGNATURA: DESARROLLO FULL STACK III
-- INSTITUCION: DUOC UC
-- BASE DE DATOS: ORACLE AUTONOMOUS AI DATABASE
-- SCRIPT: 01_crear_usuario.sql
-- =====================================================

-- IMPORTANTE:
-- Este script debe ejecutarse con el usuario ADMIN.
-- Reemplazar la contrasena de ejemplo por una segura.
-- No guardar contrasenas reales en el repositorio Git.


-- =====================================================
-- 1. CREAR USUARIO DE LA APLICACION
-- =====================================================

CREATE USER FORO_APP
IDENTIFIED BY "CAMBIAR_POR_PASSWORD_SEGURA";


-- =====================================================
-- 2. OTORGAR PERMISO DE CONEXION
-- =====================================================

GRANT CREATE SESSION TO FORO_APP;


-- =====================================================
-- 3. OTORGAR PERMISO PARA CREAR TABLAS
-- =====================================================

GRANT CREATE TABLE TO FORO_APP;


-- =====================================================
-- 4. OTORGAR PERMISO PARA CREAR SECUENCIAS
-- =====================================================

GRANT CREATE SEQUENCE TO FORO_APP;


-- =====================================================
-- 5. ASIGNAR CUOTA AL TABLESPACE DATA
-- =====================================================

ALTER USER FORO_APP
QUOTA UNLIMITED ON DATA;


-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================