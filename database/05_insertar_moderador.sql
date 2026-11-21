-- =====================================================
-- PROYECTO: SISTEMA DE FOROS EN LINEA
-- SCRIPT: 05_insertar_moderador.sql
-- Crea el usuario de prueba con rol MODERADOR.
-- Usa la misma contrasena de demostracion que admin@foro.test.
-- =====================================================

INSERT INTO USUARIOS (NOMBRE, EMAIL, PASSWORD_HASH, ID_ROL)
SELECT 'Moderadora Foro',
       'moderador@foro.test',
       (SELECT PASSWORD_HASH FROM USUARIOS WHERE EMAIL = 'admin@foro.test'),
       (SELECT ID_ROL FROM ROLES WHERE NOMBRE = 'MODERADOR')
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM USUARIOS WHERE EMAIL = 'moderador@foro.test');

COMMIT;
