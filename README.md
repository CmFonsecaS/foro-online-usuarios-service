# Microservicio de Gestión de Usuarios

## Sistema de Foros en Línea

**Asignatura:** Desarrollo Full Stack III

**Institución:** Duoc UC

**Proyecto:** Sistema de Foros en Línea

**Microservicio:** usuarios-service

## 1. Descripción

Microservicio backend desarrollado con Java y Spring Boot para gestionar los usuarios de un sistema de foros en línea.

Permite realizar operaciones CRUD sobre usuarios, autenticar credenciales mediante JWT y restringir el acceso a determinados endpoints según el rol del usuario.

Los datos se almacenan en Oracle Autonomous AI Database.

Este proyecto corresponde al microservicio de usuarios y se desarrolla de manera independiente del microservicio de administración.

## 2. Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JSON Web Token (JWT)
- BCrypt
- Oracle Autonomous AI Database
- Maven
- Postman
- Git y GitHub

## 3. Arquitectura

El proyecto utiliza una arquitectura organizada por capas:

- **Controller:** expone los endpoints REST.
- **Service:** contiene la lógica de negocio.
- **Repository:** gestiona el acceso a Oracle mediante JPA.
- **Model:** representa las entidades de la base de datos.
- **DTO:** define los datos recibidos y enviados por la API.
- **Config:** contiene la configuración de seguridad y JWT.

## 4. Base de datos

Se utiliza Oracle Autonomous AI Database.

**Esquema:** `FORO_APP`

Tablas utilizadas directamente por este microservicio:

### ROLES

Contiene los roles disponibles:

- ADMIN
- USUARIO
- MODERADOR

### USUARIOS

Almacena:

- ID del usuario
- Nombre
- Correo electrónico
- Hash de contraseña
- Rol
- Estado
- Fecha de creación

Las contraseñas se almacenan utilizando BCrypt.

La API no devuelve los hashes de contraseña en las respuestas JSON.

## 5. Configuración

El proyecto utiliza el archivo:

`src/main/resources/application.properties`

Las credenciales y los datos de conexión se obtienen mediante variables de entorno.

### Variables requeridas

| Variable | Descripción |
|----------|-------------|
| DB_URL | URL JDBC de conexión a Oracle, incluida la ruta local del wallet |
| DB_USERNAME | Usuario del esquema Oracle |
| DB_PASSWORD | Contraseña de Oracle |
| JWT_SECRET | Clave secreta para firmar y validar JWT |

### Configuración de Oracle

La URL de conexión se configura mediante la variable de entorno `DB_URL`.

Ejemplo para Windows PowerShell:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@crisdevdb_medium?TNS_ADMIN=C:/ruta/local/oracle-wallet/CrisDevDB"
```

Cada usuario debe reemplazar la ruta de ejemplo por la ubicación de su wallet de Oracle.

El usuario y la contraseña de Oracle se configuran mediante las variables `DB_USERNAME` y `DB_PASSWORD`.

### Configuración de JWT

La variable `JWT_SECRET` debe contener una clave Base64 que represente al menos 32 bytes aleatorios.

El microservicio utiliza JWT para autenticar solicitudes y controlar el acceso según el rol del usuario.

**Importante:** las contraseñas, claves JWT y archivos del wallet no deben publicarse en GitHub.

## 6. Ejecución

### Requisitos

- JDK 21
- Conexión a Oracle Autonomous AI Database
- Wallet de Oracle configurado
- Variables de entorno configuradas

### Iniciar el microservicio

Desde la carpeta `usuarios-service`, ejecutar en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

El servicio utiliza el puerto:

`8081`

URL base:

`http://localhost:8081`

### Ejecutar pruebas automatizadas

```powershell
.\mvnw.cmd clean test
```

## 7. Endpoints REST

### Autenticación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/auth/login | Autenticar usuario y obtener JWT |

### Registro y perfil

| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| POST | /api/usuarios | Registrar usuario (rol USUARIO) | Público |
| GET | /api/usuarios/me | Consultar el perfil propio | Autenticado |
| PUT | /api/usuarios/me | Modificar el perfil propio | Autenticado |
| PUT | /api/usuarios/me/password | Cambiar la contraseña propia | Autenticado |

### Gestión de usuarios

| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | /api/usuarios | Listar usuarios | ADMIN |
| GET | /api/usuarios/{id} | Consultar usuario por ID | ADMIN |
| PUT | /api/usuarios/{id} | Actualizar usuario | ADMIN |
| PATCH | /api/usuarios/{id}/rol | Cambiar rol (ADMIN, MODERADOR, USUARIO) | ADMIN |
| PATCH | /api/usuarios/{id}/estado | Activar o desactivar usuario | ADMIN |
| DELETE | /api/usuarios/{id} | Eliminar usuario | ADMIN |

## 8. Autenticación JWT

Para iniciar sesión, realizar:

**POST**

`http://localhost:8081/api/auth/login`

Body JSON:

```json
{
  "email": "admin@foro.test",
  "password": "DemoForo2026!"
}
```

La respuesta contiene:

- accessToken
- tokenType
- expiresIn
- rol

El token debe enviarse en los endpoints protegidos mediante:

`Authorization: Bearer <TOKEN>`

Los usuarios con rol ADMIN pueden acceder a los endpoints protegidos de administración de usuarios.

La creación de usuarios y el inicio de sesión están configurados como endpoints públicos.

Las credenciales mostradas corresponden únicamente a datos de demostración y deben cambiarse fuera del entorno de pruebas.

## 9. Ejemplo de creación de usuarios

**POST**

`http://localhost:8081/api/usuarios`

Body JSON:

```json
{
  "nombre": "Maria Lopez",
  "email": "maria@foro.test",
  "password": "DemoForo2026!"
}
```

Respuesta esperada:

`201 Created`

El usuario se guarda en Oracle con el rol USUARIO y la contraseña protegida con BCrypt.

## 10. Pruebas realizadas

Se verificaron los siguientes escenarios mediante Postman:

| Prueba | Resultado |
|--------|-----------|
| Login correcto | 200 OK |
| Listar usuarios con JWT ADMIN | 200 OK |
| Listar usuarios sin autenticación | 401 Unauthorized |
| Listar usuarios con JWT sin permisos | 403 Forbidden |
| Crear usuario | 201 Created |
| Actualizar usuario | 200 OK |
| Eliminar usuario | 204 No Content |

También se verificó la persistencia en Oracle SQL Developer.

### Pruebas Maven

Se ejecutó:

```powershell
.\mvnw.cmd clean test
```

Resultado:

- Tests ejecutados: 10 (1 omitido: prueba de contexto que requiere conexión a Oracle)
- Fallos: 0
- Errores: 0
- BUILD SUCCESS

Las pruebas unitarias cubren el servicio de usuarios y la autenticación con JWT.

## 11. Scripts SQL

Los scripts SQL se encuentran en la carpeta `database/` del repositorio.

| Script | Descripción |
|--------|-------------|
| 01_crear_usuario.sql | Creación del usuario y asignación de permisos en Oracle |
| 02_crear_tablas.sql | Creación de las tablas y sus relaciones |
| 03_insertar_datos.sql | Inserción de datos iniciales |
| 04_consultas_verificacion.sql | Consultas para verificar los datos almacenados |
| 05_insertar_moderador.sql | Inserción del usuario de demostración con rol MODERADOR |

Los scripts deben ejecutarse en el orden indicado y utilizando las credenciales y permisos correspondientes.

El script de creación del usuario contiene una contraseña de ejemplo que debe reemplazarse por una contraseña segura antes de ejecutarlo.

## 12. Despliegue en AWS

El microservicio se ejecuta como contenedor Docker en una instancia Amazon EC2, junto con los demás
microservicios y el frontend. Nginx actúa como proxy inverso y expone todo bajo una única URL pública;
las solicitudes a `/api/auth/** y /api/usuarios/**` se envían a este servicio (puerto interno `8081`).

| Componente | Uso |
|---|---|
| Amazon ECR | Repositorio de imágenes `foro-online/usuarios-service` |
| Amazon EC2 | Host Docker + Nginx, creado con CloudFormation (`foro-frontend/ec2/foro-online-ec2-stack.yml`) |
| AWS Systems Manager | Despliegue remoto en la instancia, sin SSH |
| Amazon S3 | Bucket privado con el wallet de Oracle |
| AWS Secrets Manager | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` |

El workflow `.github/workflows/publish-ecr.yml` se ejecuta en cada push a `main` que modifica código
(los cambios solo de documentación se omiten):

1. Ejecuta las pruebas con Maven.
2. Construye la imagen Docker y la publica en ECR con las etiquetas `latest` y el SHA del commit.
3. Despliega en EC2 mediante Systems Manager: descarga el wallet desde S3, lee las credenciales desde
   Secrets Manager, reemplaza el contenedor y verifica `/actuator/health`. Si el servicio no queda
   disponible, restaura la versión anterior.

Configuración del repositorio en GitHub:

- **Secrets:** `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
- **Variables:** `AWS_REGION`, `EC2_INSTANCE_ID`, `ORACLE_CREDENTIALS_SECRET_ARN`,
  `ORACLE_WALLET_S3_BUCKET`, `ORACLE_WALLET_S3_KEY`.

En AWS Academy las credenciales del laboratorio son temporales, por lo que los secrets deben
actualizarse al iniciar cada sesión.

## 13. Pruebas con Postman

La colección `postman/Foro-Online.postman_collection.json` cubre los tres microservicios del sistema:
39 solicitudes y 41 validaciones automáticas, incluidos los casos de error 400, 401, 403, 404 y 409.

| Carpeta | Contenido |
|---|---|
| 0. Autenticación | Login de ADMIN, MODERADOR y USUARIO; credenciales incorrectas |
| 1. usuarios-service | Registro, perfil, listado, cambio de rol, estado y eliminación |
| 2. foro-service | Búsqueda, detalle, publicación, edición y comentarios |
| 3. administracion-service | Banear y reactivar publicaciones y comentarios; CRUD de categorías |
| 4. Limpieza | Elimina los datos creados durante la prueba |

Uso:

1. Importar la colección en Postman.
2. Ajustar la variable de colección `baseUrl` con la URL pública de la instancia EC2
   (la IP cambia si la instancia se reinicia).
3. Ejecutar primero los logins de la carpeta `0. Autenticación`: guardan los tokens en variables
   que usan las demás solicitudes. También se puede ejecutar la colección completa con *Run collection*.

Desde la línea de comandos:

```bash
npx newman run postman/Foro-Online.postman_collection.json
```

## 14. Flujo de trabajo Git

- `main` es la rama estable: cada cambio de código integrado en ella se despliega en AWS.
- Los cambios se desarrollan en ramas de corta duración con un prefijo según su tipo:
  `feature/`, `fix/`, `docs/`, `ci/`, `refactor/`.
- Cada rama se integra a `main` mediante un Pull Request (merge commit) y se elimina después del merge.
- Los mensajes de commit siguen la convención *Conventional Commits*: `feat:`, `fix:`, `docs:`,
  `test:`, `ci:`, `refactor:`.
