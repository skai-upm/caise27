package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mindrot.jbcrypt.BCrypt;

class PasswordProtectionTest {

    @TempDir
    Path dir;

    private String storedPassword() throws IOException {
        return Files.readAllLines(dir.resolve("users.txt")).get(0).split(";")[4];
    }

    @Test
    void plaintextIsNeverStored() throws IOException {
        CommandProcessor p = App.createProcessor(dir);
        assertTrue(p.process(TestData.CREATE_ANA).startsWith("OK:"));
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertTrue(BCrypt.checkpw("Clave1!", storedPassword()));
    }

    @Test
    void deleteOnlyWithTheCorrectPassword() {
        CommandProcessor p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        assertEquals("ERROR: Incorrect password", p.process("USER DELETE;ana@correo.com;Mala1!"));
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK:"));
    }

    @Test
    void updateWithWrongPasswordChangesNothing() throws IOException {
        CommandProcessor p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        List<String> before = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals("ERROR: Incorrect password", p.process("USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999"));
        assertEquals("ERROR: Incorrect password", p.process("USER UPDATE;ana@correo.com;Mala1!;PASSWORD;Nueva2#"));
        assertEquals(before, Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void passwordChangePersistsANewHashThatWorksAfterRestart() throws IOException {
        CommandProcessor p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        String oldHash = storedPassword();
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK:"));
        String newHash = storedPassword();
        assertNotEquals(oldHash, newHash);
        assertTrue(BCrypt.checkpw("Nueva2#", newHash));
        CommandProcessor restarted = App.createProcessor(dir);
        assertEquals("ERROR: Incorrect password", restarted.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;6"));
        assertTrue(restarted.process("USER UPDATE;ana@correo.com;Nueva2#;PHONE;6").startsWith("OK:"));
    }

    @Test
    void e1RulesApplyBeforeHashing() throws IOException {
        CommandProcessor p = App.createProcessor(dir);
        assertTrue(p.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave12;612345678;1234567890123456")
                .startsWith("ERROR: Invalid password"));
        assertTrue(Files.notExists(dir.resolve("users.txt")));
        p.process(TestData.CREATE_ANA);
        String hash = storedPassword();
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Clave12").startsWith("ERROR: Invalid password"));
        assertEquals(hash, storedPassword());
    }

    @Test
    void legacyPlaintextRecordNeverAuthenticates() throws IOException {
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        CommandProcessor p = App.createProcessor(dir);
        assertEquals("ERROR: Incorrect password", p.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(p.process(TestData.CREATE_ANA).startsWith("ERROR:")); // still registered (DNI/email unique)
    }
}
