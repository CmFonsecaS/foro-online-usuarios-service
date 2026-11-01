package cl.duoc.usuarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.usuarios.dto.LoginRequest;
import cl.duoc.usuarios.dto.LoginResponse;
import cl.duoc.usuarios.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =====================================================
    // POST: INICIAR SESION Y OBTENER JWT
    // =====================================================

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        try {
            return authService.login(request);

        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
            );
        }
    }
}
