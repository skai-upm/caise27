package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class PasswordTest extends ParkTestSupport {

    @Test
    void fileHoldsOnlyTheHash() throws IOException {
        User ana = ana();
        String stored = Files.readString(dir.resolve("users.txt"));
        assertFalse(stored.contains("Clave1!"));
        assertTrue(ana.getPassword().startsWith("$2a$"));
        assertTrue(HASHER.matches("Clave1!", ana.getPassword()));
    }

    @Test
    void invalidPasswordsStillRejected() {
        assertThrows(UserException.class, () ->
                users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        ana();
        assertThrows(UserException.class, () -> users.update("ana@correo.com", "Clave1!", "PASSWORD", "Clave12"));
    }

    @Test
    void onlyNewPasswordAuthenticatesAfterChange() {
        String before = ana().getPassword();
        users.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        User ana = park.findUser("ana@correo.com");
        assertFalse(before.equals(ana.getPassword()), "a new hash is generated");
        restart();
        assertThrows(UserException.class, () -> users.update("ana@correo.com", "Clave1!", "PHONE", "600"));
        users.update("ana@correo.com", "Nueva2#", "PHONE", "600");
        assertEquals("600", park.findUser("ana@correo.com").getPhone());
    }

    @Test
    void deleteVerifiesHash() {
        ana();
        assertThrows(UserException.class, () -> users.delete("ana@correo.com", "Clave1?"));
        users.delete("ana@correo.com", "Clave1!");
    }

    @Test
    void legacyPlaintextNeverAuthenticates() throws IOException {
        Files.write(dir.resolve("users.txt"), List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        restart();
        assertNotNull(park.findUser("ana@correo.com"));
        assertThrows(UserException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        assertFalse(new BCryptPasswordHasher().matches("x", null));
    }

    @Test
    void defaultCostHasherWorks() {
        BCryptPasswordHasher h = new BCryptPasswordHasher();
        assertTrue(h.hash("Clave1!").startsWith("$2a$10$"));
    }
}
