package cl.duoc.usuarios.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.UsuarioResponse;
import cl.duoc.usuarios.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // =====================================================
    // GET: CONSULTAR TODOS LOS USUARIOS
    // =====================================================

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // =====================================================
    // GET: CONSULTAR USUARIO POR ID
    // =====================================================

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuarioPorId(
            @PathVariable Long id) {

        try {
            return usuarioService.obtenerUsuarioPorId(id);

        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            );
        }
    }

    // =====================================================
    // POST: CREAR NUEVO USUARIO
    // =====================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request) {

        try {
            return usuarioService.crearUsuario(request);

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                ex.getMessage()
            );
        }
    }

    // =====================================================
    // PUT: ACTUALIZAR USUARIO EXISTENTE
    // =====================================================

    @PutMapping("/{id}")
    public UsuarioResponse actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        try {
            return usuarioService.actualizarUsuario(id, request);

        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            );

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                ex.getMessage()
            );
        }
    }

    // =====================================================
    // DELETE: ELIMINAR USUARIO
    // =====================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {

        try {
            usuarioService.eliminarUsuario(id);

        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            );

        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                ex.getMessage()
            );
        }
    }
}