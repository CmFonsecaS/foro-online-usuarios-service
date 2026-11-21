package cl.duoc.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarPasswordRequest(

    @NotBlank(message = "La contrasena actual es obligatoria")
    String passwordActual,

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MENSAJE)
    String passwordNueva

) {
}
