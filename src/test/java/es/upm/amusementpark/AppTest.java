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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String INPUT = "HELP\nFOO\nEXIT\nHELP\n";

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream buffer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void captureOutput() {
        buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private String runWithStdin(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        App.main(new String[0]);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String runWithFile(String input) throws Exception {
        Path file = tempDir.resolve("input.txt");
        Files.writeString(file, input, StandardCharsets.UTF_8);
        App.main(new String[] {file.toString()});
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void stdinAndFileProduceIdenticalOutput() throws Exception {
        String fromStdin = runWithStdin(INPUT);
        buffer.reset();
        String fromFile = runWithFile(INPUT);
        assertEquals(fromStdin, fromFile);
    }

    @Test
    void fileModeStopsAtExit() throws Exception {
        String out = runWithFile(INPUT);
        assertEquals(1, out.split("Available commands:", -1).length - 1);
        assertTrue(out.contains("Error: unknown command: FOO"));
    }

    @Test
    void stdinModeStopsAtExit() {
        String out = runWithStdin("EXIT\nHELP\n");
        assertFalse(out.contains("Available commands:"));
    }

    @Test
    void missingFileIsHandledInAControlledManner() {
        App.main(new String[] {tempDir.resolve("missing.txt").toString()});
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("Error: could not read command file"));
    }
}
