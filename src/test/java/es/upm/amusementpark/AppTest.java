package es.upm.amusementpark;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final LocalDateTime NOW = LocalDateTime.of(2027, 4, 15, 19, 3, 15);

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true), new PrintStream(err, true), dir, TODAY, NOW);
    }

    private Path writeInput(String name, String... lines) throws Exception {
        Path input = dir.resolve(name);
        Files.write(input, List.of(lines));
        return input;
    }

    @Test
    void fileModeProcessesAssignmentExample() throws Exception {
        Path input = writeInput("input.txt",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT");

        assertEquals(0, run(new String[] {input.toString()}, ""));

        String output = out.toString();
        assertTrue(output.startsWith(String.join(System.lineSeparator(),
                "USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED")));
        assertTrue(output.contains("USER DELETE;<Email>;<Password>"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(new String[0], "HELP\nEXIT\nHELP\n"));
        String output = out.toString();
        assertEquals(output.indexOf("Available commands:"), output.lastIndexOf("Available commands:"));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        assertEquals(0, run(new String[0], "FOO\n"));
        assertTrue(out.toString().startsWith("ERROR:"));
    }

    @Test
    void usersPersistBetweenRuns() {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");
        out.reset();
        run(new String[0], "USER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertEquals("USER DELETED" + System.lineSeparator(), out.toString());
    }

    @Test
    void missingFileIsHandledInControlledWay() {
        int status = run(new String[] {dir.resolve("missing.txt").toString()}, "");
        assertEquals(1, status);
        assertTrue(err.toString().startsWith("ERROR:"));
        assertFalse(err.toString().contains("Exception"));
    }

    @Test
    void directoryInsteadOfFileIsHandled() {
        assertEquals(1, run(new String[] {dir.toString()}, ""));
        assertTrue(err.toString().startsWith("ERROR:"));
    }

    @Test
    void flyWithCommandFileLeavesNoData() throws Exception {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");
        String usersBefore = Files.readString(dir.resolve("users.txt"));
        String groupsBefore = Files.readString(dir.resolve("groups.txt"));
        Path input = writeInput("input_fase2.txt",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;pedro@correo.com",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION ENTER;A01;TG01",
                "USER DELETE;ana@correo.com;Clave1!");
        out.reset();

        assertEquals(0, run(new String[] {"--fly", input.toString()}, ""));

        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "GROUP CREATED", "GROUP USER ADDED",
                "ATTRACTION CREATED", "GROUPTICKET CREATED: total 40.00 EUR", "ATTRACTION ENTERED: 1 user(s)",
                "ERROR: user not found", ""), out.toString());
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertEquals(usersBefore, Files.readString(dir.resolve("users.txt")));
        assertEquals(groupsBefore, Files.readString(dir.resolve("groups.txt")));
        assertEquals("", Files.readString(dir.resolve("tickets.txt")));
    }

    @Test
    void flyInteractiveCleansUpOnExit() throws Exception {
        Files.createDirectories(dir.resolve("tmp").resolve("other"));
        assertEquals(0, run(new String[] {"--fly"},
                "ATTRACTION CREATE;A01;Dragon Rojo;20\nATTRACTION STATUS;A01\nEXIT\n"));
        assertTrue(out.toString().contains("Capacity: 20"));
        assertTrue(Files.exists(dir.resolve("tmp").resolve("other")));
        try (var remaining = Files.list(dir.resolve("tmp"))) {
            assertEquals(List.of(dir.resolve("tmp").resolve("other")), remaining.toList());
        }
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
    }

    @Test
    void flyWithMissingFileStillCleansUp() {
        assertEquals(1, run(new String[] {"--fly", dir.resolve("missing.txt").toString()}, ""));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyStartsFromEmptyState() throws Exception {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");
        out.reset();
        run(new String[] {"--fly"}, "USER DELETE;ana@correo.com;Clave1!\n");
        assertEquals("ERROR: user not found" + System.lineSeparator(), out.toString());
    }
}
