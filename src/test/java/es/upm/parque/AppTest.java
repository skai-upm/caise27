package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserManager manager(Path data) {
        return new TestPark(data).users;
    }

    private String start(Path data, String[] args, String stdin) {
        App app = new TestPark(data).app();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        app.start(args.length == 0 ? null : args[0], new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private Path commandFile(String... lines) throws IOException {
        Path file = dir.resolve("input.txt");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void interactiveProcessesEachNonBlankLineAndSurvivesRestart() {
        Path users = dir.resolve("data");
        String out = start(users, new String[0], ANA + "\n\n   \nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n");
        assertEquals("OK USER CREATED\nOK USER UPDATED\n", out);
        assertEquals("OK USER DELETED\n", start(users, new String[0], "USER DELETE;ana@correo.com;Clave1!\n"));
    }

    @Test
    void interactiveStopsAtExit() {
        Path users = dir.resolve("data");
        String out = start(users, new String[0], "HELP\nEXIT\n" + ANA + "\n");
        assertTrue(out.startsWith("OK Available commands:\n"));
        assertTrue(out.endsWith("OK EXIT\n"));
        assertNull(manager(users).findByEmail("ana@correo.com"));
    }

    @Test
    void fileModeRunsLinesInOrderIgnoringBlankLinesAndStopsAtExit() throws IOException {
        Path users = dir.resolve("data");
        Path file = commandFile(ANA, "", PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "EXIT", "USER DELETE;pedro@correo.com;Otra2#");
        String out = start(users, new String[] {file.toString()}, "USER DELETE;ana@correo.com;Clave1!\n");
        assertEquals("OK USER CREATED\nOK USER CREATED\nOK USER UPDATED\nOK EXIT\n", out);
        UserManager after = manager(users);
        assertEquals("699999999", after.findByEmail("ana@correo.com").getPhone());
        assertNotNull(after.findByEmail("pedro@correo.com"));
    }

    @Test
    void sameResultsFromTerminalAndFile() throws IOException {
        String[] commands = {ANA, "USER CREATE;Ana;X;12345678Z;b@correo.com;Clave1!;1;1234567890123456",
            "HELP", "BOGUS", "USER UPDATE;ana@correo.com;Clave1!;EMAIL;n@correo.com"};
        Path usersA = dir.resolve("a");
        Path usersB = dir.resolve("b");
        String interactive = start(usersA, new String[0], String.join("\n", commands) + "\n");
        String fromFile = start(usersB, new String[] {commandFile(commands).toString()}, "");
        assertEquals(interactive, fromFile);
        assertEquals(Files.readAllLines(usersA.resolve("users.txt")), Files.readAllLines(usersB.resolve("users.txt")));
    }

    @Test
    void missingFileIsReported() {
        Path users = dir.resolve("data");
        String out = start(users, new String[] {dir.resolve("nope.txt").toString()}, ANA + "\n");
        assertTrue(out.startsWith("ERROR Input file not found"), out);
        assertFalse(Files.exists(users));
    }

    @Test
    void directoryIsReportedAsUnreadable() {
        String out = start(dir.resolve("data"), new String[] {dir.toString()}, "");
        assertTrue(out.startsWith("ERROR Cannot read input"), out);
    }

    @Test
    void readFailureIsReported() {
        App app = new TestPark(dir.resolve("u")).app();
        Reader failing = new Reader() {
            @Override
            public int read(char[] buf, int off, int len) throws IOException {
                throw new IOException("boom");
            }

            @Override
            public void close() {
                // nothing to release
            }
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        app.run(new BufferedReader(failing), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("ERROR Cannot read input"));
    }

    @Test
    void runOverReaderStopsAtExit() {
        App app = new TestPark(dir.resolve("u")).app();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        app.run(new BufferedReader(new StringReader("EXIT\nHELP\n")), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        assertEquals("OK EXIT", bytes.toString(StandardCharsets.UTF_8).trim());
    }
}
