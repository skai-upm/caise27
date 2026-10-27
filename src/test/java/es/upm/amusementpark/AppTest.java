package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class AppTest {

    @TempDir
    Path tempDir;

    private static String runApp(String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Path writeFile(String content) throws IOException {
        Path file = tempDir.resolve("input.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void stdinAndFileProduceSameOutput() throws IOException {
        String commands = "HELP\nEXIT\n";
        String fromStdin = runApp(new String[0], commands);
        String fromFile = runApp(new String[] {writeFile(commands).toString()}, "");
        assertTrue(fromStdin.contains("USER DELETE;<email>;<password>"));
        assertEquals(fromStdin, fromFile);
    }

    @Test
    void fileLinesAfterExitAreNotProcessed() throws IOException {
        String out = runApp(new String[] {writeFile("EXIT\nHELP\n").toString()}, "");
        assertFalse(out.contains("USER DELETE"));
    }

    @Test
    void stdinLinesAfterExitAreNotProcessed() {
        String out = runApp(new String[0], "EXIT\nHELP\n");
        assertFalse(out.contains("USER DELETE"));
    }

    @Test
    void missingFileIsReportedInControlledManner() {
        String out = runApp(new String[] {tempDir.resolve("missing.txt").toString()}, "");
        assertTrue(out.startsWith("ERROR"));
    }
}
