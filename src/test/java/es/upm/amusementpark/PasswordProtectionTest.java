package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.ElementNotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.security.PasswordProtector;
import es.upm.amusementpark.persistence.TextFileRepositories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordProtectionTest {

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(new User("ana@correo.com", "12345678Z"));
    }

    @Test
    void protectorUsesBcrypt() {
        PasswordProtector protector = new PasswordProtector();
        String hash = protector.protect("Clave1!");
        assertTrue(hash.startsWith("$2a$"));
        assertNotEquals(hash, protector.protect("Clave1!"));
        assertTrue(protector.matches("Clave1!", hash));
        assertFalse(protector.matches("clave1!", hash));
        assertFalse(protector.matches("Clave1!", "not-a-hash"));
    }

    @Test
    void storedValueIsProtected() {
        park.setPassword("ana@correo.com", "Clave1!");
        String hash = park.findUser("ana@correo.com").getPasswordHash();
        assertNotEquals("Clave1!", hash);
        assertFalse(hash.contains("Clave1!"));
    }

    @Test
    void verificationAcceptsOnlyTheRightPassword() {
        park.setPassword("ana@correo.com", "Clave1!");
        assertEquals("ana@correo.com", park.verifyPassword("ana@correo.com", "Clave1!").getEmail());
        assertThrows(OperationNotAllowedException.class, () -> park.verifyPassword("ana@correo.com", "clave1!"));
        assertThrows(ElementNotFoundException.class, () -> park.verifyPassword("nadie@correo.com", "Clave1!"));
    }

    @Test
    void changeGeneratesNewValue() {
        park.setPassword("ana@correo.com", "Clave1!");
        String first = park.findUser("ana@correo.com").getPasswordHash();
        park.setPassword("ana@correo.com", "Nueva2#");
        assertNotEquals(first, park.findUser("ana@correo.com").getPasswordHash());
        park.verifyPassword("ana@correo.com", "Nueva2#");
        assertThrows(OperationNotAllowedException.class, () -> park.verifyPassword("ana@correo.com", "Clave1!"));
    }

    @Test
    void userWithoutPasswordIsRejected() {
        assertNull(park.findUser("ana@correo.com").getPasswordHash());
        assertThrows(OperationNotAllowedException.class, () -> park.verifyPassword("ana@correo.com", ""));
    }

    @Test
    void persistenceKeepsOnlyTheHash() throws IOException {
        park.setPassword("ana@correo.com", "Clave1!");
        park.configure(TextFileRepositories.create(dir));
        park.persist();
        String file = Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        assertFalse(file.contains("Clave1!"));
        assertTrue(file.startsWith("ana@correo.com;12345678Z;$2a$"));

        park.reset();
        park.configure(TextFileRepositories.create(dir));
        park.load();
        park.verifyPassword("ana@correo.com", "Clave1!");
    }

    @Test
    void legacyUserLinesStillLoad() throws IOException {
        Files.writeString(dir.resolve("users.txt"), "pedro@correo.com;87654321X\n", StandardCharsets.UTF_8);
        park.reset();
        park.configure(TextFileRepositories.create(dir));
        assertTrue(park.load().isEmpty());
        assertNull(park.findUser("pedro@correo.com").getPasswordHash());
    }
}
