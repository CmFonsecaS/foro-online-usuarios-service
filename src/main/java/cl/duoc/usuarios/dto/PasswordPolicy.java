package cl.duoc.usuarios.dto;

/**
 * Politica de contrasenas (6 validaciones de seguridad):
 * 1. Longitud minima de 8 caracteres
 * 2. Longitud maxima de 20 caracteres
 * 3. Al menos una letra mayuscula
 * 4. Al menos una letra minuscula
 * 5. Al menos un numero
 * 6. Al menos un caracter especial y sin espacios
 */
public final class PasswordPolicy {

    public static final String REGEX =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,20}$";

    public static final String MENSAJE =
        "La contrasena debe tener entre 8 y 20 caracteres, una mayuscula, una minuscula, "
        + "un numero, un caracter especial y no contener espacios";

    private PasswordPolicy() {
    }
}
