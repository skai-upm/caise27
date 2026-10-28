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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationIntegrationTest {

    @TempDir
    Path workDir;

    private String run(String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void interactiveModeReadsStandardInput() {
        String out = run(new String[0], "HELP\nEXIT\n");
        assertTrue(out.contains("Available commands:"));
        assertTrue(out.contains("Exiting application."));
    }

    @Test
    void fileModeExecutesCommandsInOrderAndStopsAtExit(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, "FOO\nHELP\nEXIT\nHELP\n");
        String out = run(new String[] {file.toString()}, "");
        int error = out.indexOf("ERROR: Unknown command: FOO");
        int help = out.indexOf("Available commands:");
        assertTrue(error >= 0 && help > error);
        assertEquals(help, out.lastIndexOf("Available commands:"));
    }

    @Test
    void fileModeEndsAtEndOfFileWithoutExit(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, "HELP\n");
        String out = run(new String[] {file.toString()}, "EXIT\n");
        assertTrue(out.contains("Available commands:"));
        assertFalse(out.contains("Exiting application."));
    }

    @Test
    void missingFileIsReportedWithoutStackTrace(@TempDir Path dir) {
        String missing = dir.resolve("missing.txt").toString();
        String out = run(new String[] {missing}, "");
        assertEquals("ERROR: Cannot read command file: " + missing + System.lineSeparator(), out);
    }
}
