package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /** Reference sequence from the assignment (source section 16). */
    static final String REFERENCE = String.join("\n",
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT") + "\n";

    @TempDir
    Path dir;

    private record Result(int status, String out, String err) {
    }

    private Result run(String[] args, String stdin, Path users) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8), users);
        return new Result(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveAndFileModesAreEquivalent() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, REFERENCE);
        Path usersFile = dir.resolve("file-users.txt");
        Path usersInteractive = dir.resolve("stdin-users.txt");

        Result fromFile = run(new String[] {input.toString()}, "", usersFile);
        Result fromStdin = run(new String[0], REFERENCE, usersInteractive);

        assertEquals(0, fromFile.status());
        assertEquals(0, fromStdin.status());
        assertEquals(fromStdin.out(), fromFile.out());
        assertEquals(Files.readString(usersInteractive), Files.readString(usersFile));
    }

    @Test
    void referenceExecutionProducesExpectedOutputAndState() throws IOException {
        Path users = dir.resolve("users.txt");
        Result r = run(new String[0], REFERENCE, users);
        String expected = String.join(System.lineSeparator(),
                "USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED",
                CommandProcessor.HELP_TEXT, "BYE", "");
        assertEquals(expected, r.out());
        assertEquals(java.util.List.of(
                "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(users));
    }

    @Test
    void mixedValidAndInvalidCommandsOnlyApplyValidOnes() throws IOException {
        Path users = dir.resolve("users.txt");
        String input = String.join("\n",
                CommandProcessorTest.CREATE_ANA,
                "USER CREATE;Bad;Dni;12345678A;bad@correo.com;Clave1!;600;1111222233334444",
                "USER CREATE;Dup;Mail;87654321X;ana@correo.com;Clave1!;600;1111222233334444",
                "USER DELETE;ana@correo.com;WrongPass1!",
                "USER UPDATE;ana@correo.com;Clave1!;CARD;123",
                "DANCE",
                "",
                CommandProcessorTest.CREATE_PEDRO,
                "USER UPDATE;pedro@correo.com;Otra2#;EMAIL;ana@correo.com");
        Result r = run(new String[0], input, users);
        assertEquals(0, r.status());
        assertEquals(6, r.out().split("ERROR:", -1).length - 1);
        assertEquals(java.util.List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(users));
    }

    @Test
    void stateIsReloadedAcrossRuns() throws IOException {
        Path users = dir.resolve("users.txt");
        run(new String[0], CommandProcessorTest.CREATE_ANA + "\nEXIT\n", users);
        Result second = run(new String[0], CommandProcessorTest.CREATE_ANA + "\nUSER DELETE;ana@correo.com;Clave1!\n", users);
        assertTrue(second.out().startsWith("ERROR: Email already registered") || second.out().startsWith("ERROR: DNI already registered"));
        assertTrue(second.out().contains("USER DELETED"));
        assertEquals("", Files.readString(users));
    }

    @Test
    void missingCommandFileIsControlled() {
        Result r = run(new String[] {dir.resolve("nope.txt").toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.err().contains("Cannot read command file"));
        assertEquals("", r.out());
    }

    @Test
    void directoryAsCommandFileIsControlled() {
        Result r = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.err().contains("Cannot read command file"));
    }

    @Test
    void endOfInputWithoutExitEndsNormally() {
        Result r = run(new String[0], "HELP\n", dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertTrue(r.out().contains("USER CREATE;"));
    }
}
