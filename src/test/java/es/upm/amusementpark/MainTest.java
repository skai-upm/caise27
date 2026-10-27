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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path data;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String input, String... args) {
        return Main.run(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8), data.resolve("users.txt"));
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void interactiveModeProcessesStandardInput() {
        assertEquals(0, run(CREATE_ANA + "\n" + CREATE_ANA + "\n"));
        assertEquals("USER CREATED\nERROR: DNI already registered\n", output());
        assertEquals("", err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void fileModeProcessesGivenFileLikeInteractiveMode(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, CREATE_ANA + "\n" + CREATE_ANA + "\n");
        assertEquals(0, run("", input.toString()));
        assertEquals("USER CREATED\nERROR: DNI already registered\n", output());
    }

    @Test
    void missingFileIsReportedWithNonZeroStatus(@TempDir Path dir) {
        assertEquals(1, run("", dir.resolve("missing.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
    }

    @Test
    void usersArePreservedBetweenRuns() throws IOException {
        assertEquals(0, run(CREATE_ANA + "\n"));
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                Files.readString(data.resolve("users.txt")).trim());
        out.reset();
        assertEquals(0, run(CREATE_ANA + "\n"));
        assertEquals("ERROR: DNI already registered\n", output());
    }
}
