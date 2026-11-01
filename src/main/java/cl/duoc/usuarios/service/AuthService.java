package cl.duoc.usuarios.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.usuarios.dto.LoginRequest;
import cl.duoc.usuarios.dto.LoginResponse;
import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtEncoder jwtEncoder;
    private final BCryptPasswordEncoder passwordEncoder;
    private final long expirationMinutes;

    // =====================================================
    // CONSTRUCTOR E INYECCION DE DEPENDENCIAS
    // =====================================================

    public AuthService(
            UsuarioRepository usuarioRepository,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.usuarioRepository = usuarioRepository;
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.expirationMinutes = expirationMinutes;
    }

    // =====================================================
    // LOGIN: AUTENTICAR USUARIO Y GENERAR JWT
    // =====================================================

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // Buscar usuario en Oracle
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() ->
                    new BadCredentialsException(
                        "Credenciales incorrectas"
                    )
                );

        // Verificar contrasena BCrypt
        if (!passwordEncoder.matches(
                request.password(),
                usuario.getPasswordHash())) {

            throw new BadCredentialsException(
                "Credenciales incorrectas"
            );
        }

        // Verificar que el usuario este activo
        if (!"ACTIVO".equals(usuario.getEstado())) {

            throw new BadCredentialsException(
                "Credenciales incorrectas"
            );
        }

        // Obtener rol desde Oracle
        String rol = usuario.getRol().getNombre();

        // Definir fechas del token
        Instant ahora = Instant.now();

        Instant expiracion = ahora.plus(
                expirationMinutes,
                ChronoUnit.MINUTES
        );

        // Construir informacion del JWT
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("usuarios-service")
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(usuario.getEmail())
                .claim("userId", usuario.getIdUsuario())
                .claim("role", rol)
                .build();

        // Firmar token con algoritmo HS256
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        // Devolver respuesta
        return new LoginResponse(
                token,
                "Bearer",
                expirationMinutes * 60,
                rol
        );
    }
}
