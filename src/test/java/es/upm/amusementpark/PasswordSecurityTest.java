package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.invoice.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class PasswordSecurityTest extends ParkTestSupport {

    @Test
    void fileStoresOnlyBcryptHash() throws Exception {
        User ana = user(1);
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertTrue(ana.getPasswordHash().startsWith("$2a$"));
        assertTrue(content.contains(ana.getPasswordHash()));
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("Clave1!", ana.getPasswordHash()));
    }

    @Test
    void passwordChangeIsRehashed() {
        User ana = user(1);
        String before = ana.getPasswordHash();
        users.update(email(1), "Clave1!", UserField.PASSWORD, "Nueva2#");
        assertNotEquals(before, ana.getPasswordHash());
        assertThrows(AuthenticationException.class, () -> users.update(email(1), "Clave1!", UserField.NAME, "X"));
        users.update(email(1), "Nueva2#", UserField.NAME, "X");
        restart();
        users.delete(email(1), "Nueva2#");
    }

    @Test
    void invalidPasswordRejectedBeforeHashing() {
        user(1);
        assertThrows(InvalidDataException.class, () -> users.update(email(1), "Clave1!", UserField.PASSWORD, "debil"));
    }

    @Test
    void wrongPasswordRejected() {
        user(1);
        assertThrows(AuthenticationException.class, () -> users.delete(email(1), "Clave1?"));
        assertNotNull(park.getUsers().findByEmail(email(1)));
    }

    @Test
    void plainTextPasswordFromOldFileDoesNotAuthenticate() throws Exception {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456\n");
        restart();
        assertThrows(AuthenticationException.class, () -> users.delete("ana@correo.com", "Clave1!"));
    }
}
