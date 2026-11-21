package cl.duoc.usuarios.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.UsuarioMapper;
import cl.duoc.usuarios.dto.UsuarioResponse;
import cl.duoc.usuarios.exception.ConflictoException;
import cl.duoc.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.usuarios.model.Rol;
import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.RolRepository;
import cl.duoc.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            UsuarioMapper mapper) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorEmail(String email) {
        return mapper.toResponse(buscarPorEmail(email));
    }

    // =====================================================
    // REGISTRO (PUBLICO): SIEMPRE CON ROL USUARIO
    // =====================================================

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {

        String email = normalizar(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("El correo ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(buscarRol("USUARIO"));
        usuario.setEstado("ACTIVO");

        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    // =====================================================
    // ACTUALIZACION
    // =====================================================

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, ActualizarUsuarioRequest request) {
        return actualizarDatos(buscarPorId(id), request);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarPerfil(String email, ActualizarUsuarioRequest request) {
        return actualizarDatos(buscarPorEmail(email), request);
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, CambiarPasswordRequest request) {

        Usuario usuario = buscarPorEmail(email);

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena actual no es correcta");
        }
        if (passwordEncoder.matches(request.passwordNueva(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contrasena debe ser distinta de la actual");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarRol(Long id, String rol) {
        Usuario usuario = buscarPorId(id);
        usuario.setRol(buscarRol(rol));
        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, String estado) {
        Usuario usuario = buscarPorId(id);
        usuario.setEstado(estado);
        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    // =====================================================
    // ELIMINACION
    // =====================================================

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {

        Usuario usuario = buscarPorId(id);

        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoException(
                "No se puede eliminar el usuario porque tiene publicaciones o comentarios asociados", ex);
        }
    }

    // =====================================================
    // METODOS DE APOYO
    // =====================================================

    private UsuarioResponse actualizarDatos(Usuario usuario, ActualizarUsuarioRequest request) {

        String email = normalizar(request.email());

        usuarioRepository.findByEmail(email).ifPresent(existente -> {
            if (!existente.getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new ConflictoException("El correo ya pertenece a otro usuario");
            }
        });

        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con ID: " + id));
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizar(email))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + email));
    }

    private Rol buscarRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol " + nombre));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
