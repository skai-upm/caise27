package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

import es.upm.parque.command.CommandProcessor;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.repository.UserRepository;

/**
 * Runs the basic execution example of the Delivery 1 assignment (section 16) end to end.
 */
class Delivery1IntegrationTest {

    private static final List<String> EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    private static String run(Path dir, String[] args, String stdin) throws Exception {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.start(args, new CommandProcessor(users),
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void exampleSequenceInFileAndInteractiveModes(@TempDir Path fileDir, @TempDir Path stdinDir) throws Exception {
        Path input = fileDir.resolve("input.txt");
        Files.write(input, EXAMPLE, StandardCharsets.UTF_8);

        String fileOutput = run(fileDir, new String[] {input.toString()}, "");
        String stdinOutput = run(stdinDir, new String[0], String.join("\n", EXAMPLE));

        assertEquals(stdinOutput, fileOutput);
        assertFalse(fileOutput.contains("ERROR"), fileOutput);
        assertTrue(fileOutput.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(fileOutput.trim().endsWith("OK: exiting application"));

        List<String> expected = List.of(
                "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456");
        assertEquals(expected, Files.readAllLines(fileDir.resolve("users.txt")));
        assertArrayEquals(expected.toArray(), Files.readAllLines(stdinDir.resolve("users.txt")).toArray());
    }

    @Test
    void stateSurvivesRestartAndRulesStillApply(@TempDir Path dir) throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, EXAMPLE, StandardCharsets.UTF_8);
        run(dir, new String[] {input.toString()}, "");

        String second = run(dir, new String[0], String.join("\n",
                "USER CREATE;Otra;Persona;12345678Z;otra@correo.com;Clave1!;600000000;1234567890123456",
                "USER DELETE;ana.nueva@correo.com;Mala1!",
                "USER UPDATE;ana.nueva@correo.com;Clave1!;CARD;123",
                "NOT A COMMAND"));
        String[] lines = second.trim().split("\\R");
        assertEquals(4, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("ERROR"), line);
        }
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
    }
}
