package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the execution modes of the application.
 */
public class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String runWith(String[] args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, dir.resolve("users.txt"), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileModeProcessesInOrderIgnoringBlankLinesAndStopsAtExit() throws Exception {
        Path commands = dir.resolve("input_basico.txt");
        Files.writeString(commands, CREATE_ANA + "\n\n   \nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n"
                + CREATE_PEDRO + "\n");
        String out = runWith(new String[] {commands.toString()});
        assertFalse(out.contains("ERROR"), out);
        String stored = Files.readString(dir.resolve("users.txt"));
        assertTrue(stored.contains("12345678Z;ana@correo.com;Clave1!;699999999"));
        assertFalse(stored.contains("Pedro"));
    }

    @Test
    void usersAreRestoredOnRestart() throws Exception {
        Path first = dir.resolve("first.txt");
        Files.writeString(first, CREATE_ANA + "\nEXIT\n");
        runWith(new String[] {first.toString()});
        Path second = dir.resolve("second.txt");
        Files.writeString(second, "USER CREATE;Otra;X;12345678Z;otra@correo.com;Clave1!;6;1234567890123456\n"
                + "USER DELETE;ana@correo.com;Clave1!\n");
        String out = runWith(new String[] {second.toString()});
        assertTrue(out.contains("ERROR a user with that DNI already exists"), out);
        assertTrue(out.contains("OK USER DELETED"), out);
    }

    @Test
    void nonexistentFileIsReported() {
        String out = runWith(new String[] {dir.resolve("missing.txt").toString()});
        assertTrue(out.startsWith("ERROR cannot open command file"), out);
    }

    @Test
    void interactiveModeReadsStandardInputUntilExit() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream((CREATE_ANA + "\nHELP\nEXIT\n" + CREATE_PEDRO + "\n")
                    .getBytes(StandardCharsets.UTF_8)));
            String out = runWith(new String[0]);
            String[] lines = out.split(System.lineSeparator());
            assertEquals("OK USER CREATED", lines[0]);
            assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
            assertEquals("OK BYE", lines[lines.length - 1]);
        } finally {
            System.setIn(original);
        }
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream("HELP\n".getBytes(StandardCharsets.UTF_8)));
            String out = runWith(new String[0]);
            assertTrue(out.contains("Available commands"));
        } finally {
            System.setIn(original);
        }
    }
}
