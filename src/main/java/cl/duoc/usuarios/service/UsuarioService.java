package cl.duoc.usuarios.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.UsuarioResponse;
import cl.duoc.usuarios.model.Rol;
import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.RolRepository;
import cl.duoc.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // =====================================================
    // CONSTRUCTOR E INYECCION DE DEPENDENCIAS
    // =====================================================

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // =====================================================
    // GET: CONSULTAR TODOS LOS USUARIOS
    // =====================================================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // =====================================================
    // GET: CONSULTAR USUARIO POR ID
    // =====================================================

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "No existe un usuario con ID: " + id
                    )
                );

        return convertirAResponse(usuario);
    }

    // =====================================================
    // POST: CREAR NUEVO USUARIO
    // =====================================================

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {

        // Verificar si el correo ya existe
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "El correo ya se encuentra registrado"
            );
        }

        // Buscar el rol USUARIO en Oracle
        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseThrow(() ->
                    new IllegalStateException(
                        "No existe el rol USUARIO en Oracle"
                    )
                );

        // Crear nueva entidad Usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());

        // Proteger la contrasena mediante BCrypt
        usuario.setPasswordHash(
            passwordEncoder.encode(request.password())
        );

        // Asignar rol y estado
        usuario.setRol(rolUsuario);
        usuario.setEstado("ACTIVO");

        // Guardar usuario en Oracle
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Retornar respuesta sin contrasena
        return convertirAResponse(usuarioGuardado);
    }

    // =====================================================
    // PUT: ACTUALIZAR USUARIO EXISTENTE
    // =====================================================

    @Transactional
    public UsuarioResponse actualizarUsuario(
            Long id,
            ActualizarUsuarioRequest request) {

        // Buscar usuario por ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "No existe un usuario con ID: " + id
                    )
                );

        // Comprobar que el correo no pertenezca a otro usuario
        usuarioRepository.findByEmail(request.email())
                .ifPresent(usuarioExistente -> {

                    if (!usuarioExistente.getIdUsuario().equals(id)) {
                        throw new IllegalArgumentException(
                            "El correo ya pertenece a otro usuario"
                        );
                    }
                });

        // Modificar los datos permitidos
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());

        // Guardar cambios en Oracle
        Usuario usuarioActualizado =
                usuarioRepository.save(usuario);

        // Retornar respuesta sin contrasena
        return convertirAResponse(usuarioActualizado);
    }

    // =====================================================
    // DELETE: ELIMINAR USUARIO
    // =====================================================

    @Transactional
    public void eliminarUsuario(Long id) {

        // Verificar que el usuario exista
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "No existe un usuario con ID: " + id
                    )
                );

        try {
            // Eliminar usuario de Oracle
            usuarioRepository.delete(usuario);

            // Ejecutar la operacion DELETE
            usuarioRepository.flush();

        } catch (DataIntegrityViolationException ex) {

            // Evitar eliminar usuarios con registros asociados
            throw new IllegalStateException(
                "No se puede eliminar el usuario porque tiene publicaciones o comentarios asociados",
                ex
            );
        }
    }

    // =====================================================
    // CONVERTIR ENTIDAD A DTO
    // =====================================================

    private UsuarioResponse convertirAResponse(Usuario usuario) {

        return new UsuarioResponse(
            usuario.getIdUsuario(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol().getNombre(),
            usuario.getEstado(),
            usuario.getFechaCreacion()
        );
    }
}