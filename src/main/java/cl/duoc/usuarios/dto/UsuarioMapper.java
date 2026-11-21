package cl.duoc.usuarios.dto;

import org.springframework.stereotype.Component;

import cl.duoc.usuarios.model.Usuario;

/**
 * Patron DTO + Mapper: la API nunca expone la entidad ni el hash de la contrasena.
 */
@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
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
