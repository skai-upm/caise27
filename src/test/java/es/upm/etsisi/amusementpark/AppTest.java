package es.upm.etsisi.amusementpark;

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

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private int run(String stdin, String... args) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), out,
                dir.resolve("users.txt"));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(CREATE_ANA + "\nEXIT\n"));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("USER CREATED"));
    }

    @Test
    void fileModeReadsFirstArgument() throws IOException {
        Path input = Files.write(dir.resolve("input.txt"), List.of(CREATE_ANA, "EXIT"), StandardCharsets.UTF_8);
        assertEquals(0, run("", input.toString()));
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
    }

    @Test
    void usersPersistBetweenRuns() {
        run(CREATE_ANA + "\n");
        bytes.reset();
        run(CREATE_ANA + "\n");
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }

    @Test
    void missingCommandFileFailsInControlledWay() {
        assertEquals(1, run("", dir.resolve("missing.txt").toString()));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("ERROR: cannot read command file"));
    }
}
