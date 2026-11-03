package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> SOURCE_EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true), new PrintStream(err, true), dir.resolve("users.txt"));
    }

    @Test
    void runsSourceExampleFromFile() throws Exception {
        Path input = Files.write(dir.resolve("input.txt"), SOURCE_EXAMPLE);
        assertEquals(0, run(new String[] {input.toString()}, ""));

        String output = out.toString();
        assertEquals(2, output.split("USER CREATED", -1).length - 1);
        assertEquals(2, output.split("USER UPDATED", -1).length - 1);
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("Available commands:"));
        assertFalse(output.contains("ERROR"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void runsInteractivelyFromStandardInputAndPersistsAcrossRuns() throws Exception {
        assertEquals(0, run(new String[0], String.join("\n", SOURCE_EXAMPLE.subList(0, 2)) + "\nEXIT\n"));
        assertEquals(0, run(new String[0], "USER DELETE;ana@correo.com;Clave1!\n"));
        assertTrue(out.toString().contains("USER DELETED"));
        assertEquals(List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void mixedValidAndInvalidInputKeepsStateConsistent() throws Exception {
        Path input = Files.write(dir.resolve("input.txt"), List.of(
                SOURCE_EXAMPLE.get(0),
                "NONSENSE",
                "USER CREATE;Ana;Garcia Lopez;12345678A;x@correo.com;Clave1!;1;1234567890123456",
                "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
                "USER UPDATE;ana@correo.com;Clave1!;CARD;1234",
                "USER DELETE;ana@correo.com;bad",
                "EXIT",
                SOURCE_EXAMPLE.get(1)));
        assertEquals(0, run(new String[] {input.toString()}, ""));
        assertEquals(5, out.toString().split("ERROR:", -1).length - 1);
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void missingCommandFileIsReportedWithoutException() {
        assertEquals(1, run(new String[] {dir.resolve("nope.txt").toString()}, ""));
        assertTrue(err.toString().contains("ERROR: cannot read command file"));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        assertEquals(1, run(new String[] {dir.toString()}, ""));
        assertTrue(err.toString().contains("ERROR: cannot read command file"));
    }

    @Test
    void unreadableUserFileIsReported() throws Exception {
        Files.createDirectory(dir.resolve("users.txt"));
        assertEquals(1, run(new String[0], ""));
        assertTrue(err.toString().contains("ERROR: cannot read user file"));
    }
}
