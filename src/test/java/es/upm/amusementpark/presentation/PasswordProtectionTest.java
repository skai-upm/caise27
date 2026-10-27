package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordProtectionTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path data;

    private String run(String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Main.run(new String[0], new ByteArrayInputStream((String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()), data);
        return out.toString(StandardCharsets.UTF_8);
    }

    private String usersFile() throws IOException {
        return Files.readString(data.resolve("users.txt"));
    }

    @Test
    void fileStoresOnlyBcryptValue() throws IOException {
        assertEquals("USER CREATED\n", run(ANA));
        assertFalse(usersFile().contains("Clave1!"));
        assertTrue(usersFile().split(";")[4].startsWith("$2a$"));
    }

    @Test
    void invalidPasswordRejectedBeforeStoring() {
        assertEquals("ERROR: Invalid password\n",
                run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456"));
    }

    @Test
    void verificationWorksAfterRestart() {
        run(ANA);
        assertEquals("ERROR: Incorrect password\nUSER UPDATED\n",
                run("USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999", "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
    }

    @Test
    void passwordChangeStoresNewValue() throws IOException {
        run(ANA);
        String before = usersFile();
        assertEquals("USER UPDATED\n", run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertFalse(usersFile().contains("Nueva2#"));
        assertTrue(!before.equals(usersFile()));
        assertEquals("ERROR: Incorrect password\nUSER DELETED\n",
                run("USER DELETE;ana@correo.com;Clave1!", "USER DELETE;ana@correo.com;Nueva2#"));
    }
}
