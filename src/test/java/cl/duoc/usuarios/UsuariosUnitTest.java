package cl.duoc.usuarios;

import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import cl.duoc.usuarios.config.SecurityConfig;
import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.LoginRequest;
import cl.duoc.usuarios.dto.LoginResponse;
import cl.duoc.usuarios.dto.PasswordPolicy;
import cl.duoc.usuarios.dto.UsuarioMapper;
import cl.duoc.usuarios.dto.UsuarioResponse;
import cl.duoc.usuarios.exception.ConflictoException;
import cl.duoc.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.usuarios.model.Rol;
import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.RolRepository;
import cl.duoc.usuarios.repository.UsuarioRepository;
import cl.duoc.usuarios.service.AuthServiceImpl;
import cl.duoc.usuarios.service.UsuarioServiceImpl;

class UsuariosUnitTest {

    private static final String PASSWORD = "DemoForo2026!";

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository, encoder, new UsuarioMapper());

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(rolRepository.findByNombre("USUARIO")).thenReturn(Optional.of(new Rol("USUARIO")));
    }

    // ===== Politica de contrasenas =====

    @Test
    void politicaDeContrasenas() {
        Pattern politica = Pattern.compile(PasswordPolicy.REGEX);

        assertTrue(politica.matcher("DemoForo2026!").matches());
        assertFalse(politica.matcher("Ab1!").matches(), "muy corta");
        assertFalse(politica.matcher("Abcdefghijk1!Abcdefghij").matches(), "muy larga");
        assertFalse(politica.matcher("demoforo2026!").matches(), "sin mayuscula");
        assertFalse(politica.matcher("DEMOFORO2026!").matches(), "sin minuscula");
        assertFalse(politica.matcher("DemoForoxx!!").matches(), "sin numero");
        assertFalse(politica.matcher("DemoForo2026").matches(), "sin caracter especial");
        assertFalse(politica.matcher("Demo Foro2026!").matches(), "con espacios");
    }

    // ===== Registro =====

    @Test
    void registroGuardaHashYRolUsuario() {
        when(usuarioRepository.existsByEmail("maria@foro.test")).thenReturn(false);

        UsuarioResponse respuesta = usuarioService.crearUsuario(
                new CrearUsuarioRequest("Maria Lopez", "  Maria@Foro.test ", PASSWORD));

        assertEquals("maria@foro.test", respuesta.email());
        assertEquals("USUARIO", respuesta.rol());
        assertEquals("ACTIVO", respuesta.estado());
    }

    @Test
    void registroConCorreoDuplicadoLanza409() {
        when(usuarioRepository.existsByEmail("ana@foro.test")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> usuarioService.crearUsuario(
                new CrearUsuarioRequest("Ana", "ana@foro.test", PASSWORD)));
    }

    @Test
    void usuarioInexistenteLanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.obtenerUsuarioPorId(99L));
    }

    // ===== Perfil =====

    @Test
    void actualizarPerfilConCorreoDeOtroUsuarioLanza409() {
        Usuario ana = usuario(2L, "ana@foro.test", "USUARIO");
        Usuario carlos = usuario(3L, "carlos@foro.test", "USUARIO");
        when(usuarioRepository.findByEmail("ana@foro.test")).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByEmail("carlos@foro.test")).thenReturn(Optional.of(carlos));

        assertThrows(ConflictoException.class, () -> usuarioService.actualizarPerfil(
                "ana@foro.test", new ActualizarUsuarioRequest("Ana", "carlos@foro.test")));
    }

    @Test
    void cambiarPasswordExigeLaActualCorrecta() {
        Usuario ana = usuario(2L, "ana@foro.test", "USUARIO");
        when(usuarioRepository.findByEmail("ana@foro.test")).thenReturn(Optional.of(ana));

        assertThrows(ResponseStatusException.class, () -> usuarioService.cambiarPassword(
                "ana@foro.test", new CambiarPasswordRequest("Incorrecta1!", "NuevaClave2026!")));

        usuarioService.cambiarPassword("ana@foro.test",
                new CambiarPasswordRequest(PASSWORD, "NuevaClave2026!"));
        assertTrue(encoder.matches("NuevaClave2026!", ana.getPasswordHash()));
    }

    // ===== Login y JWT =====

    @Test
    void loginGeneraJwtConRolYUserId() {
        SecurityConfig config = new SecurityConfig();
        String secreto = Base64.getEncoder().encodeToString(new byte[32]);
        SecretKey clave = config.jwtSecretKey(secreto);

        AuthServiceImpl auth = new AuthServiceImpl(usuarioRepository, config.jwtEncoder(clave), encoder, 60);
        when(usuarioRepository.findByEmail("admin@foro.test"))
                .thenReturn(Optional.of(usuario(1L, "admin@foro.test", "ADMIN")));

        LoginResponse respuesta = auth.login(new LoginRequest("admin@foro.test", PASSWORD));
        Jwt jwt = config.jwtDecoder(clave).decode(respuesta.accessToken());

        assertNotNull(respuesta.accessToken());
        assertEquals("ADMIN", jwt.getClaimAsString("role"));
        assertEquals(1L, ((Number) jwt.getClaim("userId")).longValue());
        assertEquals(3600, respuesta.expiresIn());
    }

    @Test
    void loginConUsuarioInactivoEsRechazado() {
        AuthServiceImpl auth = new AuthServiceImpl(usuarioRepository, null, encoder, 60);
        Usuario inactivo = usuario(4L, "inactivo@foro.test", "USUARIO");
        inactivo.setEstado("INACTIVO");
        when(usuarioRepository.findByEmail("inactivo@foro.test")).thenReturn(Optional.of(inactivo));

        assertThrows(BadCredentialsException.class,
                () -> auth.login(new LoginRequest("inactivo@foro.test", PASSWORD)));
    }

    private Usuario usuario(Long id, String email, String rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre("Usuario " + id);
        usuario.setEmail(email);
        usuario.setPasswordHash(encoder.encode(PASSWORD));
        usuario.setRol(new Rol(rol));
        usuario.setEstado("ACTIVO");
        return usuario;
    }
}
