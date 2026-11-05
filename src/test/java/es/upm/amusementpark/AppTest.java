package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String UPDATE = "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path users) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8), users);
    }

    @Test
    void interactiveModeEndsNormallyAtEndOfInput() {
        assertEquals(0, run(new String[0], "", dir.resolve("users.txt")));
    }

    @Test
    void fileModeProcessesInOrderSkippingBlankLines() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of("FOO", ANA, "", UPDATE), StandardCharsets.UTF_8);
        assertEquals(0, run(new String[] {input.toString()}, "", dir.resolve("users.txt")));
        String[] lines = out.toString(StandardCharsets.UTF_8).lines().toArray(String[]::new);
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("ERROR"));
        assertEquals("USER CREATED", lines[1]);
        assertEquals("USER UPDATED", lines[2]);
    }

    @Test
    void sameBehaviourForBothSources() throws IOException {
        Path a = Files.createDirectory(dir.resolve("a"));
        Path b = Files.createDirectory(dir.resolve("b"));
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(ANA, UPDATE), StandardCharsets.UTF_8);
        run(new String[] {input.toString()}, "", a.resolve("users.txt"));
        String fileOut = out.toString(StandardCharsets.UTF_8);
        out.reset();
        run(new String[0], ANA + "\n" + UPDATE + "\n", b.resolve("users.txt"));
        assertEquals(fileOut, out.toString(StandardCharsets.UTF_8));
        assertEquals(Files.readAllLines(a.resolve("users.txt")), Files.readAllLines(b.resolve("users.txt")));
    }

    @Test
    void missingOrUnreadableFileIsControlled() {
        assertEquals(1, run(new String[] {dir.resolve("missing.txt").toString()}, "", dir.resolve("users.txt")));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
        assertEquals(1, run(new String[] {dir.toString()}, "", dir.resolve("users.txt")));
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }
}
