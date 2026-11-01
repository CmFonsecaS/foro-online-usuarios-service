-- =====================================================
-- PROYECTO: SISTEMA DE FOROS EN LINEA
-- ASIGNATURA: DESARROLLO FULL STACK III
-- SCRIPT: 03_insertar_datos.sql
-- ESQUEMA: FORO_APP
-- =====================================================


-- =====================================================
-- 1. INSERTAR ROLES
-- =====================================================

INSERT INTO ROLES (NOMBRE)
VALUES ('ADMIN');

INSERT INTO ROLES (NOMBRE)
VALUES ('USUARIO');

INSERT INTO ROLES (NOMBRE)
VALUES ('MODERADOR');

-- =====================================================
-- 2. INSERTAR CATEGORIAS
-- =====================================================

INSERT INTO CATEGORIAS (NOMBRE, DESCRIPCION)
VALUES (
    'Tecnologia',
    'Noticias y debates sobre tecnologia'
);

INSERT INTO CATEGORIAS (NOMBRE, DESCRIPCION)
VALUES (
    'Programacion',
    'Consultas sobre lenguajes y desarrollo de software'
);

INSERT INTO CATEGORIAS (NOMBRE, DESCRIPCION)
VALUES (
    'Videojuegos',
    'Discusiones sobre videojuegos y plataformas'
);

INSERT INTO CATEGORIAS (NOMBRE, DESCRIPCION)
VALUES (
    'Educacion',
    'Recursos de aprendizaje y experiencias educativas'
);

INSERT INTO CATEGORIAS (NOMBRE, DESCRIPCION)
VALUES (
    'Ciencia',
    'Noticias y descubrimientos cientificos'
);

-- =====================================================
-- 3. INSERTAR USUARIOS DE PRUEBA
-- =====================================================

-- Usuario administrador
INSERT INTO USUARIOS (
    NOMBRE,
    EMAIL,
    PASSWORD_HASH,
    ID_ROL
)
VALUES (
    'Administrador Foro',
    'admin@foro.test',
    '$2a$10$Qbq5H30EKULMjO9IyerpE.f4QdVrcI6EJnDRDhs/jLmGQOHMQvlGO',
    (SELECT ID_ROL FROM ROLES WHERE NOMBRE = 'ADMIN')
);


-- Usuario normal 1
INSERT INTO USUARIOS (
    NOMBRE,
    EMAIL,
    PASSWORD_HASH,
    ID_ROL
)
VALUES (
    'Ana Garcia',
    'ana@foro.test',
    '$2a$10$bOpzikMf9p7N3/76CssDluaRk2AkPKhSdO7knXLREtsbaYYMhE5AO',
    (SELECT ID_ROL FROM ROLES WHERE NOMBRE = 'USUARIO')
);


-- Usuario normal 2
INSERT INTO USUARIOS (
    NOMBRE,
    EMAIL,
    PASSWORD_HASH,
    ID_ROL
)
VALUES (
    'Carlos Perez',
    'carlos@foro.test',
    '$2a$10$z7Um2Flt87JZ5z3a8aPC8OTAKp76qgx.05xbz6UvUxWMCERmHSuiq',
    (SELECT ID_ROL FROM ROLES WHERE NOMBRE = 'USUARIO')
);

-- =====================================================
-- 4. INSERTAR PUBLICACIONES
-- =====================================================

INSERT INTO PUBLICACIONES (
    TITULO,
    CONTENIDO,
    ID_USUARIO,
    ID_CATEGORIA
)
VALUES (
    'Bienvenidos al foro de tecnologia',
    'Este espacio permite compartir noticias y experiencias sobre tecnologia.',
    (SELECT ID_USUARIO FROM USUARIOS WHERE EMAIL = 'ana@foro.test'),
    (SELECT ID_CATEGORIA FROM CATEGORIAS WHERE NOMBRE = 'Tecnologia')
);


INSERT INTO PUBLICACIONES (
    TITULO,
    CONTENIDO,
    ID_USUARIO,
    ID_CATEGORIA
)
VALUES (
    'Primeros pasos con Java',
    'Compartamos recomendaciones para aprender Java y Spring Boot.',
    (SELECT ID_USUARIO FROM USUARIOS WHERE EMAIL = 'carlos@foro.test'),
    (SELECT ID_CATEGORIA FROM CATEGORIAS WHERE NOMBRE = 'Programacion')
);


INSERT INTO PUBLICACIONES (
    TITULO,
    CONTENIDO,
    ID_USUARIO,
    ID_CATEGORIA
)
VALUES (
    'Videojuegos favoritos',
    'Cuales son sus videojuegos favoritos y por que los recomiendan?',
    (SELECT ID_USUARIO FROM USUARIOS WHERE EMAIL = 'ana@foro.test'),
    (SELECT ID_CATEGORIA FROM CATEGORIAS WHERE NOMBRE = 'Videojuegos')
);

-- =====================================================
-- 5. INSERTAR COMENTARIOS
-- =====================================================

INSERT INTO COMENTARIOS (
    CONTENIDO,
    ID_PUBLICACION,
    ID_USUARIO
)
VALUES (
    'Excelente iniciativa para compartir conocimientos.',
    (SELECT ID_PUBLICACION FROM PUBLICACIONES
     WHERE TITULO = 'Bienvenidos al foro de tecnologia'),
    (SELECT ID_USUARIO FROM USUARIOS
     WHERE EMAIL = 'carlos@foro.test')
);


INSERT INTO COMENTARIOS (
    CONTENIDO,
    ID_PUBLICACION,
    ID_USUARIO
)
VALUES (
    'Recomiendo comenzar con programacion orientada a objetos.',
    (SELECT ID_PUBLICACION FROM PUBLICACIONES
     WHERE TITULO = 'Primeros pasos con Java'),
    (SELECT ID_USUARIO FROM USUARIOS
     WHERE EMAIL = 'ana@foro.test')
);


INSERT INTO COMENTARIOS (
    CONTENIDO,
    ID_PUBLICACION,
    ID_USUARIO
)
VALUES (
    'Me gustan los videojuegos MMORPG como el WOW.',
    (SELECT ID_PUBLICACION FROM PUBLICACIONES
     WHERE TITULO = 'Videojuegos favoritos'),
    (SELECT ID_USUARIO FROM USUARIOS
     WHERE EMAIL = 'carlos@foro.test')
);

COMMIT;