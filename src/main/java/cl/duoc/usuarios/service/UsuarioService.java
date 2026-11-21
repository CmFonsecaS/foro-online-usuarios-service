package cl.duoc.usuarios.service;

import java.util.List;

import cl.duoc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.usuarios.dto.CrearUsuarioRequest;
import cl.duoc.usuarios.dto.UsuarioResponse;

/**
 * Patron Service Layer: contrato de la gestion de usuarios.
 */
public interface UsuarioService {

    List<UsuarioResponse> listarUsuarios();

    UsuarioResponse obtenerUsuarioPorId(Long id);

    UsuarioResponse obtenerPorEmail(String email);

    UsuarioResponse crearUsuario(CrearUsuarioRequest request);

    UsuarioResponse actualizarUsuario(Long id, ActualizarUsuarioRequest request);

    UsuarioResponse actualizarPerfil(String email, ActualizarUsuarioRequest request);

    void cambiarPassword(String email, CambiarPasswordRequest request);

    UsuarioResponse cambiarRol(Long id, String rol);

    UsuarioResponse cambiarEstado(Long id, String estado);

    void eliminarUsuario(Long id);
}
