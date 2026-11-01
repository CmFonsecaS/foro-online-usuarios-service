package cl.duoc.usuarios.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
    Long idUsuario,
    String nombre,
    String email,
    String rol,
    String estado,
    LocalDateTime fechaCreacion
) {
}
