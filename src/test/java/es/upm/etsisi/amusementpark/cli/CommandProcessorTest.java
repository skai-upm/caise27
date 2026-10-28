package es.upm.etsisi.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static String run(BufferedReader in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(in, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String run(String input) throws IOException {
        return run(new BufferedReader(new StringReader(input)));
    }

    private static int count(String text, String fragment) {
        return text.split(java.util.regex.Pattern.quote(fragment), -1).length - 1;
    }

    @Test
    void helpListsMandatorySyntax() throws IOException {
        String out = run("HELP\n");
        for (String line : HelpCommand.SYNTAX) {
            assertTrue(out.contains(line + System.lineSeparator()), "missing: " + line);
        }
    }

    @Test
    void helpDoesNotEndSession() throws IOException {
        assertEquals(2, count(run("HELP\nHELP\n"), "Available commands:"));
    }

    @Test
    void exitStopsSubsequentLines() throws IOException {
        String out = run("HELP\nEXIT\nHELP\n");
        assertEquals(1, count(out, "Available commands:"));
        assertTrue(out.contains("Exiting application."));
    }

    @Test
    void unknownCommandIsReportedAndProcessingContinues() throws IOException {
        String out = run("FOO\nHELP\n");
        assertTrue(out.contains("ERROR: Unknown command: FOO"));
        assertTrue(out.indexOf("ERROR") < out.indexOf("Available commands:"));
    }

    @Test
    void blankLinesAreIgnored() throws IOException {
        assertEquals("", run("\n   \n"));
    }

    @Test
    void fileAndStreamSourcesBehaveTheSame(@TempDir Path dir) throws IOException {
        String commands = "HELP\nFOO\nEXIT\nHELP\n";
        Path file = dir.resolve("commands.txt");
        Files.writeString(file, commands);
        try (BufferedReader fromFile = Files.newBufferedReader(file)) {
            assertEquals(run(commands), run(fromFile));
        }
    }
}
