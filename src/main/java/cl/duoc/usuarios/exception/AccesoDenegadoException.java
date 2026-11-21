package cl.duoc.usuarios.exception;

/**
 * Se lanza cuando el usuario autenticado no puede modificar el recurso (HTTP 403).
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
