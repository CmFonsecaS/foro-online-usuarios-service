package cl.duoc.usuarios.service;

import cl.duoc.usuarios.dto.LoginRequest;
import cl.duoc.usuarios.dto.LoginResponse;

/**
 * Patron Service Layer: autenticacion y emision de JWT.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);
}
