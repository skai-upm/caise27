package es.upm.parque;

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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the interactive and file execution modes.
 */
public class AppTest {

    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT",
            "USER DELETE;pedro@correo.com;Otra2#");

    @TempDir
    Path dir;

    private String runApp(String[] args, String stdin, Path usersFile, int expectedCode) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        int code = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), out, usersFile);
        assertEquals(expectedCode, code);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileModeRunsCommandsInOrderAndStopsAtExit() throws IOException {
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, COMMANDS);
        Path users = dir.resolve("users.txt");

        String output = runApp(new String[] {input.toString()}, "", users, 0);

        assertTrue(output.contains("OK USER CREATED ana@correo.com"));
        assertTrue(output.contains("OK USER CREATED pedro@correo.com"));
        assertTrue(output.contains("OK USER UPDATED PHONE"));
        assertTrue(output.contains("USER DELETE;Correo;Contraseña"));
        assertFalse(output.contains("USER DELETED"));
        List<String> lines = Files.readAllLines(users);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("699999999"));
    }

    @Test
    void interactiveModeProducesSameResultsAsFileMode() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, COMMANDS);
        String fileOutput = runApp(new String[] {input.toString()}, "", dir.resolve("a.txt"), 0);
        String interactiveOutput = runApp(new String[0], COMMANDS, dir.resolve("b.txt"), 0);

        assertTrue(interactiveOutput.endsWith(fileOutput));
        assertEquals(Files.readAllLines(dir.resolve("a.txt")), Files.readAllLines(dir.resolve("b.txt")));
    }

    @Test
    void interactiveModeEndsWhenInputEnds() {
        String output = runApp(new String[0], "HELP\n", dir.resolve("users.txt"), 0);
        assertTrue(output.contains("Available commands"));
    }

    @Test
    void missingFileIsReportedInAControlledWay() {
        String output = runApp(new String[] {dir.resolve("missing.txt").toString()}, "", dir.resolve("u.txt"), 1);
        assertTrue(output.startsWith("ERROR Cannot read command file"));
    }

    @Test
    void usersPersistAcrossRuns() throws IOException {
        Path users = dir.resolve("users.txt");
        runApp(new String[0], "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"
                + "1234567890123456\nEXIT\n", users, 0);
        String output = runApp(new String[0], "USER DELETE;ana@correo.com;Clave1!\n", users, 0);
        assertTrue(output.contains("OK USER DELETED ana@correo.com"));
    }
}
