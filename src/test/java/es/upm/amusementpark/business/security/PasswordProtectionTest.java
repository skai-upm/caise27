package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordProtectionTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private String storedPassword() {
        return Park.getInstance().users().findByDni("12345678Z").orElseThrow().getPassword();
    }

    @Test
    void persistedPasswordIsBcrypt() throws IOException {
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.files(dir));
        assertEquals("USER CREATED", p.process(ANA));
        String line = Files.readAllLines(dir.resolve("users.txt")).get(0);
        assertFalse(line.contains("Clave1!"));
        assertTrue(line.split(";")[4].startsWith("$2a$"));
        assertTrue(p.process(ANA.replace("12345678Z", "87654321X").replace("ana@", "x@").replace("Clave1!", "Clave12")).startsWith("ERROR"));
    }

    @Test
    void verificationAgainstStoredValueAndAfterRestart() {
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.files(dir));
        p.process(ANA);
        assertEquals("USER UPDATED", p.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertTrue(p.process("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR"));
        assertTrue(p.process("USER DELETE;ana@correo.com;" + storedPassword()).startsWith("ERROR"));
        CommandProcessor restarted = ApplicationInitializer.initialize(Repositories.files(dir));
        assertEquals("USER DELETED", restarted.process("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void passwordChangeGeneratesNewValue() {
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory());
        p.process(ANA);
        String before = storedPassword();
        assertEquals("USER UPDATED", p.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertNotEquals(before, storedPassword());
        assertTrue(storedPassword().startsWith("$2a$"));
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;1").startsWith("ERROR"));
        assertEquals("USER UPDATED", p.process("USER UPDATE;ana@correo.com;Nueva2#;PHONE;1"));
    }

    @Test
    void encoderRejectsNonHashes() {
        BCryptPasswordEncoder e = new BCryptPasswordEncoder();
        assertFalse(e.matches("Clave1!", "Clave1!"));
        assertTrue(e.matches("Clave1!", e.encode("Clave1!")));
    }
}
