package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the terminal loop end to end with in-memory input/output.
 */
class AppTest {

    @TempDir
    Path dir;

    private List<String> run(String input, Path file) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), file);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void oneResponsePerCommandAndStateSurvivesRestart() throws Exception {
        Path file = dir.resolve("users.txt");
        List<String> first = run("""
                USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456
                USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654
                NOT A COMMAND

                USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com
                USER DELETE;pedro@correo.com;Otra2#
                """, file);
        assertEquals(List.of("OK: User created", "OK: User created", "ERROR: Unknown command: NOT A COMMAND",
                "OK: User updated", "OK: User deleted"), first);
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));

        List<String> second = run("USER DELETE;ana.nueva@correo.com;Clave1!\n", file);
        assertEquals(List.of("OK: User deleted"), second);
    }
}
