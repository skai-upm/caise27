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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true), new PrintStream(err, true), dir.resolve("users.txt"));
    }

    @Test
    void fileModeProcessesAssignmentExample() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT"));

        assertEquals(0, run(new String[] {input.toString()}, ""));

        String output = out.toString();
        assertTrue(output.startsWith(String.join(System.lineSeparator(),
                "USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED")));
        assertTrue(output.contains("USER DELETE;<email>;<password>"));
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
}
