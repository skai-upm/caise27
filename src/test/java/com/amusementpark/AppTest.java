package com.amusementpark;

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

class AppTest {

    private static final List<String> SCRIPT = List.of(
            CommandProcessorTest.CREATE_ANA,
            "",
            CommandProcessorTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER CREATE;Ana;Garcia Lopez",
            "HELP",
            "EXIT",
            "USER DELETE;pedro@correo.com;Otra2#");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path usersFile) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), usersFile);
    }

    @Test
    void interactiveAndFileModesProduceEquivalentResults() throws IOException {
        Path interactiveUsers = dir.resolve("users-interactive.txt");
        assertEquals(0, run(new String[0], String.join("\n", SCRIPT), interactiveUsers));
        String interactiveOut = out.toString(StandardCharsets.UTF_8);
        out.reset();

        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, SCRIPT);
        Path fileUsers = dir.resolve("users-file.txt");
        assertEquals(0, run(new String[] {commands.toString()}, "", fileUsers));

        assertEquals(interactiveOut, out.toString(StandardCharsets.UTF_8));
        assertEquals(Files.readAllLines(interactiveUsers), Files.readAllLines(fileUsers));
        // Pedro survives because the DELETE after EXIT is never executed.
        assertEquals(2, Files.readAllLines(fileUsers).size());
        assertTrue(Files.readAllLines(fileUsers).get(0).endsWith(";699999999;1234567890123456"));
    }

    @Test
    void endOfInputTerminatesInteractiveMode() {
        assertEquals(0, run(new String[0], CommandProcessorTest.CREATE_ANA, dir.resolve("users.txt")));
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("OK:"));
    }

    @Test
    void usersAreReloadedOnRestart() {
        Path users = dir.resolve("users.txt");
        run(new String[0], CommandProcessorTest.CREATE_ANA + "\nEXIT", users);
        out.reset();
        run(new String[0], "USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;6;1234567890123456\n"
                + "USER DELETE;ana@correo.com;Clave1!\nEXIT", users);
        List<String> lines = out.toString(StandardCharsets.UTF_8).lines().toList();
        assertTrue(lines.get(0).startsWith("ERROR:"), "duplicate email of reloaded user must be rejected");
        assertTrue(lines.get(1).startsWith("OK:"), "reloaded user must be deletable");
    }

    @Test
    void nonexistentCommandFileIsReportedWithoutStackTrace() {
        int code = run(new String[] {dir.resolve("missing.txt").toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, code);
        String error = err.toString(StandardCharsets.UTF_8);
        assertTrue(error.startsWith("ERROR: Command file not found"));
        assertFalse(error.contains("Exception"));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        int code = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, code);
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }
}
