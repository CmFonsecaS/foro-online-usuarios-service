package cl.duoc.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CambiarEstadoRequest;
import cl.duoc.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.usuarios.dto.CambiarRolRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.UsuarioResponse;
import cl.duoc.usuarios.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // =====================================================
    // REGISTRO (PUBLICO)
    // =====================================================

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    // =====================================================
    // PERFIL DEL USUARIO AUTENTICADO
    // =====================================================

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtenerPorEmail(jwt.getSubject());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        return usuarioService.actualizarPerfil(jwt.getSubject(), request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarMiPassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CambiarPasswordRequest request) {

        usuarioService.cambiarPassword(jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ADMINISTRACION DE USUARIOS (ROL ADMIN)
    // =====================================================

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        return usuarioService.actualizarUsuario(id, request);
    }

    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest request) {
        return usuarioService.cambiarRol(id, request.rol());
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return usuarioService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
