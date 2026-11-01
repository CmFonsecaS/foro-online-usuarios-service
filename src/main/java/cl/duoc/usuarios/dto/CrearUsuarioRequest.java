package cl.duoc.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    String email,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    String password

) {
}