package cl.duoc.usuarios.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.usuarios.dto.LoginRequest;
import cl.duoc.usuarios.dto.LoginResponse;
import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.UsuarioRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final long expirationMinutes;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            JwtEncoder jwtEncoder,
            PasswordEncoder passwordEncoder,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.usuarioRepository = usuarioRepository;
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    // =====================================================
    // LOGIN: AUTENTICAR USUARIO Y GENERAR JWT
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // Buscar usuario en Oracle (el correo se compara sin mayusculas)
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        // Verificar contrasena BCrypt
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Verificar que el usuario este activo
        if (!"ACTIVO".equals(usuario.getEstado())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String rol = usuario.getRol().getNombre();
        Instant ahora = Instant.now();

        // Patron Builder: construccion de los claims del JWT
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("usuarios-service")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(usuario.getEmail())
                .claim("userId", usuario.getIdUsuario())
                .claim("nombre", usuario.getNombre())
                .claim("role", rol)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, "Bearer", expirationMinutes * 60, rol);
    }
}
