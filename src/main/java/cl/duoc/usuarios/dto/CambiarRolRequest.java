package cl.duoc.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarRolRequest(

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "ADMIN|MODERADOR|USUARIO", message = "El rol debe ser ADMIN, MODERADOR o USUARIO")
    String rol

) {
}
