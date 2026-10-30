package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String runWithFile(Path users, List<String> commands) throws IOException {
        Path input = dir.resolve("input_" + System.nanoTime() + ".txt");
        Files.write(input, commands, StandardCharsets.UTF_8);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {input.toString()}, users, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String runWithStdin(Path users, List<String> commands) {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream((String.join("\n", commands) + "\n").getBytes(StandardCharsets.UTF_8)));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            App.run(new String[0], users, new PrintStream(buffer, true, StandardCharsets.UTF_8));
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(original);
        }
    }

    @Test
    void fileModeProcessesInOrderAndIgnoresBlankLines() throws IOException {
        Path users = dir.resolve("users.txt");
        String out = runWithFile(users, List.of(CREATE_ANA, "", "   ", CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "HELP", "EXIT"));
        assertFalse(out.contains("ERROR"));
        List<String> lines = Files.readAllLines(users);
        assertEquals(2, lines.size());
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456", lines.get(0));
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws IOException {
        Path users = dir.resolve("users.txt");
        runWithFile(users, List.of(CREATE_ANA, "EXIT", CREATE_PEDRO));
        List<String> lines = Files.readAllLines(users);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("Ana;"));
    }

    @Test
    void nonexistentFileIsReportedInControlledManner() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Path missing = dir.resolve("does_not_exist.txt");
        App.run(new String[] {missing.toString()}, dir.resolve("users.txt"),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("ERROR: cannot open command file"));
        assertTrue(out.contains("does_not_exist.txt"));
        assertFalse(out.contains("Exception"));
    }

    @Test
    void directoryAsFileIsReportedInControlledManner() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {dir.toString()}, dir.resolve("users.txt"),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("ERROR"));
    }

    @Test
    void interactiveModeStopsAtExitOrEndOfInput() throws IOException {
        Path users = dir.resolve("users.txt");
        String out = runWithStdin(users, List.of(CREATE_ANA, "FOO", "HELP", "EXIT", CREATE_PEDRO));
        assertTrue(out.contains("ERROR: unknown command"));
        assertTrue(out.contains("Available commands"));
        assertEquals(1, Files.readAllLines(users).size());

        Path users2 = dir.resolve("users2.txt");
        runWithStdin(users2, List.of(CREATE_ANA));
        assertEquals(1, Files.readAllLines(users2).size());
    }

    @Test
    void sameResultInBothModes() throws IOException {
        List<String> commands = List.of(CREATE_ANA, CREATE_PEDRO, "USER CREATE;Bad", "USER DELETE;pedro@correo.com;Otra2#",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com", "HELP", "EXIT");
        Path usersFile = dir.resolve("usersFile.txt");
        Path usersStdin = dir.resolve("usersStdin.txt");
        String fileOut = runWithFile(usersFile, commands);
        String stdinOut = runWithStdin(usersStdin, commands);
        assertEquals(fileOut, stdinOut);
        assertEquals(Files.readAllLines(usersFile), Files.readAllLines(usersStdin));
    }

    @Test
    void restartRestoresUsers() throws IOException {
        Path users = dir.resolve("users.txt");
        runWithFile(users, List.of(CREATE_ANA));
        String out = runWithFile(users, List.of(CREATE_ANA));
        assertTrue(out.contains("ERROR: a user with DNI"));
    }
}
