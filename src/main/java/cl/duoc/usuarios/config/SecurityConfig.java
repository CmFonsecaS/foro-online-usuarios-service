package cl.duoc.usuarios.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class SecurityConfig {

    // =====================================================
    // CLAVE DE FIRMA JWT
    // =====================================================

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}") String jwtSecret) {

        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "La clave JWT debe tener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // =====================================================
    // GENERADOR DE TOKENS JWT
    // =====================================================

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {

        return new NimbusJwtEncoder(
            new ImmutableSecret<>(jwtSecretKey)
        );
    }

    // =====================================================
    // VALIDADOR DE TOKENS JWT
    // =====================================================

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // =====================================================
    // CONVERTIR ROL DEL JWT A PERMISO SPRING
    // =====================================================

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter
        );

        return converter;
    }

    // =====================================================
    // CONFIGURACION DE SEGURIDAD
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Registro y login publicos
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/usuarios",
                    "/api/auth/login"
                ).permitAll()

                // Consultas y administracion de usuarios
                .requestMatchers("/api/usuarios/**")
                .hasRole("ADMIN")

                // Cualquier otra peticion requiere autenticacion
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)
                )
            );

        return http.build();
    }
}