package es.upm.etsisi.amusementpark.business.security;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class PasswordSecurityTest extends ParkFixture {

    private String storedField(int line) throws Exception {
        return Files.readAllLines(dir.resolve("users.txt")).get(line).split(";", -1)[4];
    }

    @Test
    void fileStoresOnlyHash() throws Exception {
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertFalse(content.contains("Otra2#"));
        assertTrue(BCrypt.checkpw("Clave1!", storedField(0)));
        assertTrue(BCrypt.checkpw("Otra2#", storedField(1)));
    }

    @Test
    void passwordChangeIsRehashedAndVerifiedAfterRestart() throws Exception {
        String before = storedField(0);
        users.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        String after = storedField(0);
        assertNotEquals(before, after);
        assertTrue(BCrypt.checkpw("Nueva2#", after));
        assertFalse(BCrypt.checkpw("Clave1!", after));

        restart();
        assertThrows(AuthenticationException.class,
                () -> users.update("ana@correo.com", "Clave1!", "PHONE", "1"));
        users.update("ana@correo.com", "Nueva2#", "PHONE", "699999999");
        assertEquals("699999999", park.users().findByEmail("ana@correo.com").orElseThrow().getPhone());
    }

    @Test
    void invalidNewPasswordIsNotStored() throws Exception {
        String before = storedField(0);
        assertThrows(Exception.class, () -> users.update("ana@correo.com", "Clave1!", "PASSWORD", "weak"));
        assertEquals(before, storedField(0));
    }

    @Test
    void defaultHasherUsesBCrypt() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", null));
    }
}
