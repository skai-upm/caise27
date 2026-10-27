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

    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String input, String... args) {
        return Main.run(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    @Test
    void interactiveModeReadsStandardInputUntilEnd() {
        assertEquals(0, run("line one\nline two\n"));
        assertEquals("", err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void fileModeReadsGivenFile(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "first\nsecond\n");
        assertEquals(0, run("", input.toString()));
        assertEquals("", err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void missingFileIsReportedWithNonZeroStatus(@TempDir Path dir) {
        assertEquals(1, run("", dir.resolve("missing.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
    }
}
