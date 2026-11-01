package cl.duoc.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashGeneratorTest {

    @Test
    void generarHashesDePrueba() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "DemoForo2026!";

        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);

            assertTrue(encoder.matches(password, hash));

            System.out.println("HASH_USUARIO_" + i + ": " + hash);
        }
    }
}