package cl.duoc.usuarios.dto;

public record LoginResponse(

    String accessToken,
    String tokenType,
    long expiresIn,
    String rol

) {
}