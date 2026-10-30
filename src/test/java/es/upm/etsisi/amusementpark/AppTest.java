package es.upm.etsisi.amusementpark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private static final List<String> EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    private final InputStream noInput = new ByteArrayInputStream(new byte[0]);

    @Test
    void fileModeRunsExampleAndPersists() throws IOException {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, EXAMPLE);
        Path users = dir.resolve("users.txt");

        assertEquals(0, App.run(new String[]{commands.toString()}, users, noInput, out));

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(2, output.split("USER CREATED", -1).length - 1);
        assertEquals(2, output.split("USER UPDATED", -1).length - 1);
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("USER DELETE;<email>;<password>"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(users));
    }

    @Test
    void interactiveModeReadsStandardInputWithSameProcessing() throws IOException {
        Path users = dir.resolve("users.txt");
        InputStream in = new ByteArrayInputStream(String.join("\n", EXAMPLE).getBytes(StandardCharsets.UTF_8));

        assertEquals(0, App.run(new String[0], users, in, out));

        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(users));
    }

    @Test
    void dataSurvivesRestartAndUniquenessApplies() throws IOException {
        Path users = dir.resolve("users.txt");
        Path first = dir.resolve("first.txt");
        Files.write(first, List.of(EXAMPLE.get(0)));
        App.run(new String[]{first.toString()}, users, noInput, out);

        Path second = dir.resolve("second.txt");
        Files.write(second, List.of(
                "USER CREATE;Otra;Persona;11111111H;ana@correo.com;Clave1!;600000000;1111222233334444"));
        App.run(new String[]{second.toString()}, users, noInput, out);

        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("ERROR: Email already registered"));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void missingCommandFileIsHandled() {
        int code = App.run(new String[]{dir.resolve("missing.txt").toString()}, dir.resolve("users.txt"), noInput, out);
        assertEquals(1, code);
        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith("ERROR: Command file not found"));
    }
}
