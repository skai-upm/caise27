package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PasswordSecurityTest extends ParkFixture {

    private String storedPassword(String dni) throws Exception {
        for (String line : Files.readAllLines(dir.resolve("users.txt"))) {
            String[] f = line.split(";", -1);
            if (f[2].equals(dni)) {
                return f[4];
            }
        }
        return null;
    }

    @Test
    void fileContainsHashNotPassword() throws Exception {
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertFalse(content.contains("Otra2#"));
        String hash = storedPassword("12345678Z");
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(FAST_HASHER.matches("Clave1!", hash));
    }

    @Test
    void passwordChangeStoresNewHash() throws Exception {
        String before = storedPassword("12345678Z");
        park.updateUser(ANA, "Clave1!", "PASSWORD", "Nueva2#");
        String after = storedPassword("12345678Z");
        assertNotEquals(before, after);
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
        assertThrows(AuthenticationException.class, () -> park.updateUser(ANA, "Clave1!", "PHONE", "600000000"));
        restart();
        park.updateUser(ANA, "Nueva2#", "PHONE", "600000000");
        assertEquals("600000000", user(ANA).getPhone());
    }

    @Test
    void invalidNewPasswordIsRejectedBeforeHashing() throws Exception {
        String before = storedPassword("12345678Z");
        assertThrows(ValidationException.class, () -> park.updateUser(ANA, "Clave1!", "PASSWORD", "nueva"));
        assertEquals(before, storedPassword("12345678Z"));
    }

    @Test
    void wrongPasswordRejectedForDeleteAndUpdate() {
        assertThrows(AuthenticationException.class, () -> park.deleteUser(ANA, "Clave1?"));
        assertThrows(AuthenticationException.class, () -> park.updateUser(ANA, "clave1!", "NAME", "X"));
        assertNotNull(user(ANA));
    }

    @Test
    void legacyPlainTextPasswordDoesNotAuthenticate() throws Exception {
        Path users = dir.resolve("users.txt");
        Files.write(users, List.of("Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"));
        restart();
        assertThrows(AuthenticationException.class, () -> park.deleteUser("luis@correo.com", "Clave1!"));
    }

    @Test
    void productionHasherUsesBCrypt() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave1?", hash));
        assertFalse(hasher.matches("Clave1!", "not-a-hash"));
    }
}
