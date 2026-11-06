package es.upm.park;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private static final String COMMANDS = "HELP\nEXIT\nHELP\n";

    private String[] runApp(String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        App.run(args, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
        return new String[] {out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8)};
    }

    @Test
    void fileAndStandardInputProduceIdenticalOutput(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, COMMANDS, StandardCharsets.UTF_8);
        String fromStdin = runApp(new String[0], COMMANDS)[0];
        String fromFile = runApp(new String[] {file.toString()}, "")[0];
        assertEquals(fromStdin, fromFile);
        assertTrue(fromFile.contains("Available commands:"));
    }

    @Test
    void unreadableFileIsReportedInControlledManner(@TempDir Path dir) {
        String[] result = runApp(new String[] {dir.resolve("missing.txt").toString()}, "");
        assertEquals("", result[0]);
        assertTrue(result[1].contains("Cannot read command file"));
    }
}
