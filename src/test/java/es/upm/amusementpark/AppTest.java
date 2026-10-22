package es.upm.amusementpark;

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

class AppTest {

    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "BAD COMMAND",
            "");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String stdin, Path usersFile, String... args) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), usersFile);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(COMMANDS, dir.resolve("users.txt")));
        assertTrue(output().startsWith("USER CREATED\nUSER CREATED\nUSER UPDATED\nUSER UPDATED\nUSER DELETED\nERROR"));
    }

    @Test
    void fileModeMatchesInteractiveMode() throws IOException {
        Path interactiveUsers = dir.resolve("interactive-users.txt");
        run(COMMANDS, interactiveUsers);
        String interactiveOutput = output();
        out.reset();

        Path input = dir.resolve("input.txt");
        Files.writeString(input, COMMANDS, StandardCharsets.UTF_8);
        Path fileUsers = dir.resolve("file-users.txt");
        assertEquals(0, run("", fileUsers, input.toString()));

        assertEquals(interactiveOutput, output());
        assertEquals(Files.readAllLines(interactiveUsers), Files.readAllLines(fileUsers));
    }

    @Test
    void commandsAfterExitInFileAreNotExecuted() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                StandardCharsets.UTF_8);
        Path users = dir.resolve("users.txt");
        assertEquals(0, run("", users, input.toString()));
        assertEquals(1, Files.readAllLines(users).size());
        assertFalse(output().contains("USER CREATED\nUSER CREATED"));
    }

    @Test
    void missingCommandFileIsReportedWithoutStackTrace() {
        Path missing = dir.resolve("does-not-exist.txt");
        assertEquals(1, run("", dir.resolve("users.txt"), missing.toString()));
        assertTrue(output().startsWith("ERROR"));
        assertTrue(output().contains("does-not-exist.txt"));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("Exception"));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        assertEquals(1, run("", dir.resolve("users.txt"), dir.toString()));
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void unreadableUserFileIsReported() throws IOException {
        Path usersDir = dir.resolve("users-as-dir");
        Files.createDirectory(usersDir);
        assertEquals(1, run("", usersDir));
        assertTrue(output().startsWith("ERROR"));
    }
}
