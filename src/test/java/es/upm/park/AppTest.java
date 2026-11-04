package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

class AppTest {

    private static final List<String> SESSION = List.of(
            CommandRunnerTest.CREATE_ANA,
            CommandRunnerTest.CREATE_PEDRO,
            "",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!");

    @TempDir
    Path dir;

    /** Captured outcome of one application run. */
    private record Run(int status, String out, String err) {
    }

    private Run launch(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                usersFile);
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveSessionStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Run r = launch(new String[0], String.join("\n", SESSION), users);
        assertEquals(0, r.status());
        assertTrue(r.out().contains("Available commands:"));
        assertTrue(r.out().trim().endsWith("OK: Bye."));
        assertEquals(2, Files.readAllLines(users).size(), "delete after EXIT must not run");
    }

    @Test
    void interactiveEndOfInputWithoutExit() {
        Run r = launch(new String[0], CommandRunnerTest.CREATE_ANA, dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertEquals("OK: User ana@correo.com created.", r.out().trim());
    }

    @Test
    void fileModeMatchesInteractiveMode() throws IOException {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, SESSION);
        Path fileUsers = dir.resolve("file-users.txt");
        Path stdinUsers = dir.resolve("stdin-users.txt");

        Run fromFile = launch(new String[] {commands.toString()}, "", fileUsers);
        Run fromStdin = launch(new String[0], String.join("\n", SESSION), stdinUsers);

        assertEquals(0, fromFile.status());
        assertEquals(fromStdin.out(), fromFile.out());
        assertEquals(Files.readAllLines(stdinUsers), Files.readAllLines(fileUsers));
        assertTrue(Files.readString(fileUsers).contains("699999999"));
    }

    @Test
    void fileModeIgnoresStandardInput() throws IOException {
        Path commands = dir.resolve("cmds.txt");
        Files.write(commands, List.of("HELP"));
        Run r = launch(new String[] {commands.toString()}, CommandRunnerTest.CREATE_ANA, dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void missingFileIsReportedWithoutStackTrace() {
        String missing = dir.resolve("nope.txt").toString();
        Run r = launch(new String[] {missing}, "", dir.resolve("users.txt"));
        assertNotEquals(0, r.status());
        assertTrue(r.err().contains("ERROR: Cannot read command file '" + missing + "'"));
        assertFalse(r.err().contains("\tat "), "no stack trace");
        assertTrue(r.out().isEmpty());
    }

    @Test
    void directoryPathIsReportedWithoutStackTrace() {
        Run r = launch(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertNotEquals(0, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"));
        assertFalse(r.err().contains("\tat "));
    }

    @Test
    void invalidPathIsReported() {
        Run r = launch(new String[] {"bad\u0000name"}, "", dir.resolve("users.txt"));
        assertNotEquals(0, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"));
    }
}
