package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mindrot.jbcrypt.BCrypt;

import es.upm.parque.business.BCryptPasswordProtector;
import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.PasswordProtector;
import es.upm.parque.persistence.ParkFileStore;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.persistence.UserRepository;

class PasswordProtectionTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"
            + "1234567890123456";

    @TempDir
    Path dir;

    private CommandProcessor start(PasswordProtector passwords) {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkConfiguration.initialize(ParkRepositories.fileBacked(users, new ParkFileStore(dir)), passwords);
        return new CommandProcessor();
    }

    private CommandProcessor start() {
        return start(TestSupport.FAST_PASSWORDS);
    }

    private String storedPassword() throws IOException {
        List<String> lines = Files.readAllLines(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        return lines.get(0).split(";", -1)[4];
    }

    @Test
    void usersFileHoldsOnlyTheBCryptValue() throws IOException {
        assertTrue(start().process(ANA).startsWith("OK"));
        String content = Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        assertFalse(content.contains("Clave1!"), content);
        String stored = storedPassword();
        assertTrue(stored.startsWith("$2a$"), stored);
        assertTrue(BCrypt.checkpw("Clave1!", stored));
    }

    @Test
    void invalidPasswordIsRejectedBeforeProtection() {
        CommandProcessor p = start();
        String r = p.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456");
        assertTrue(r.startsWith("ERROR"), r);
        assertFalse(readQuietly().contains("12345678Z"), "no user is stored");
    }

    private String readQuietly() {
        try {
            return Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    @Test
    void passwordsAreVerifiedAgainstTheStoredValue() throws IOException {
        CommandProcessor p = start();
        p.process(ANA);
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        String wrong = p.process("USER DELETE;ana@correo.com;Clave2!");
        assertEquals("ERROR: incorrect password", wrong);
        assertTrue(p.process("USER DELETE;ana@correo.com;" + storedPassword()).startsWith("ERROR"),
                "the protected value itself is not a valid password");
    }

    @Test
    void verificationWorksAfterRestart() {
        start().process(ANA);
        assertTrue(start().process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void changedPasswordIsProtectedAgain() throws IOException {
        CommandProcessor p = start();
        p.process(ANA);
        String before = storedPassword();
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        String after = storedPassword();
        assertNotEquals(before, after);
        assertTrue(BCrypt.checkpw("Nueva2#", after));
        assertFalse(BCrypt.checkpw("Clave1!", after));
        CommandProcessor restarted = start();
        assertTrue(restarted.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(restarted.process("USER DELETE;ana@correo.com;Nueva2#").startsWith("OK"));
    }

    @Test
    void invalidNewPasswordKeepsTheStoredValue() throws IOException {
        CommandProcessor p = start();
        p.process(ANA);
        String before = storedPassword();
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;sinmayus1!").startsWith("ERROR"));
        assertEquals(before, storedPassword());
    }

    @Test
    void plainTextUsersFromEarlierVersionsAreNotLoaded() throws IOException {
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                StandardCharsets.UTF_8);
        CommandProcessor p = start();
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR: no user"));
    }

    @Test
    void malformedStoredValuesNeverMatch() {
        PasswordProtector protector = new BCryptPasswordProtector(4);
        assertFalse(protector.verify("Clave1!", "Clave1!"));
        assertFalse(protector.verify("Clave1!", "$2a$04$tooShort"));
        assertFalse(protector.verify("Clave1!", null));
        assertFalse(protector.verify(null, protector.protect("Clave1!")));
        assertTrue(protector.verify("Clave1!", protector.protect("Clave1!")));
        assertTrue(new BCryptPasswordProtector().protect("Clave1!").startsWith("$2a$10$"), "default cost is 10");
    }

    @Test
    void userManagementUsesTheInjectedProtection() {
        PasswordProtector reversed = new PasswordProtector() {
            @Override
            public String protect(String plainPassword) {
                return new StringBuilder(plainPassword).reverse().toString();
            }

            @Override
            public boolean verify(String plainPassword, String protectedValue) {
                return protect(plainPassword).equals(protectedValue);
            }
        };
        CommandProcessor p = start(reversed);
        assertTrue(p.process(ANA).startsWith("OK"));
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertTrue(p.process("USER UPDATE;ana@correo.com;Clave2!;NAME;X").startsWith("ERROR"));
    }
}
