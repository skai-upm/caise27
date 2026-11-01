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

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "BAD COMMAND",
            "HELP",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!");

    @TempDir
    Path tempDir;

    private record Outcome(int status, String out, String err) {
    }

    private Outcome run(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        int status = App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), usersFile);
        return new Outcome(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveAndFileModesProduceIdenticalResults() throws IOException {
        Path interactiveUsers = tempDir.resolve("interactive-users.txt");
        Outcome interactive = run(new String[0], String.join("\n", COMMANDS) + "\n", interactiveUsers);

        Path commandFile = tempDir.resolve("input_basico.txt");
        Files.write(commandFile, COMMANDS, StandardCharsets.UTF_8);
        Path fileUsers = tempDir.resolve("file-users.txt");
        Outcome file = run(new String[] {commandFile.toString()}, "", fileUsers);

        assertEquals(0, interactive.status());
        assertEquals(0, file.status());
        assertEquals(interactive.out(), file.out());
        assertEquals(Files.readAllLines(interactiveUsers), Files.readAllLines(fileUsers));
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws IOException {
        Path commandFile = tempDir.resolve("input.txt");
        Files.write(commandFile, COMMANDS, StandardCharsets.UTF_8);
        Path users = tempDir.resolve("users.txt");
        run(new String[] {commandFile.toString()}, "", users);

        List<String> stored = Files.readAllLines(users);
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;"));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        Outcome outcome = run(new String[0], "HELP\n", tempDir.resolve("users.txt"));
        assertEquals(0, outcome.status());
        assertTrue(outcome.out().contains("USER DELETE;Correo;Contraseña"));
    }

    @Test
    void usersAreRestoredOnRestart() {
        Path users = tempDir.resolve("users.txt");
        run(new String[0], COMMANDS.get(0) + "\nEXIT\n", users);
        Outcome second = run(new String[0], "USER DELETE;ana@correo.com;Clave1!\n", users);
        assertTrue(second.out().startsWith("OK:"));
    }

    @Test
    void missingCommandFileIsReportedWithoutStackTrace() {
        Outcome outcome = run(new String[] {tempDir.resolve("nope.txt").toString()}, "",
                tempDir.resolve("users.txt"));
        assertEquals(1, outcome.status());
        assertTrue(outcome.err().startsWith("ERROR: cannot read command file"));
        assertFalse(outcome.err().contains("Exception"));
        assertFalse(outcome.err().contains("\tat "));
    }

    @Test
    void directoryAsCommandFileIsReportedWithoutStackTrace() {
        Outcome outcome = run(new String[] {tempDir.toString()}, "", tempDir.resolve("users.txt"));
        assertEquals(1, outcome.status());
        assertTrue(outcome.err().startsWith("ERROR: cannot read command file"));
        assertFalse(outcome.err().contains("\tat "));
    }
}
