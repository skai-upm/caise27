package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
    }

    private static int occurrences(String text, String fragment) {
        int count = 0;
        for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + 1)) {
            count++;
        }
        return count;
    }

    @Test
    void helpListsMinimumCommandSet() {
        assertTrue(processor.execute("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(out.contains("USER DELETE;<email>;<password>"));
        assertTrue(out.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void helpDoesNotStopProcessing() throws IOException {
        run("HELP\nHELP\n");
        assertEquals(2, occurrences(output(), "Available commands:"));
    }

    @Test
    void exitStopsSubsequentLines() throws IOException {
        run("HELP\nEXIT\nHELP\n");
        assertEquals(1, occurrences(output(), "Available commands:"));
    }

    @Test
    void exitReturnsFalse() {
        assertFalse(processor.execute("EXIT"));
    }

    @Test
    void unknownCommandIsRejectedAndProcessingContinues() throws IOException {
        run("FOO\nHELP\n");
        String out = output();
        assertTrue(out.contains("Error: unknown command: FOO"));
        assertEquals(1, occurrences(out, "Available commands:"));
    }

    @Test
    void generalCommandsRejectExtraFields() throws IOException {
        run("HELP;extra\nEXIT;x\nHELP;\nHELP\n");
        String out = output();
        assertTrue(out.contains("Error: invalid arguments for HELP"));
        assertTrue(out.contains("Error: invalid arguments for EXIT"));
        assertEquals(1, occurrences(out, "Available commands:"));
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertTrue(processor.execute("  HELP  "));
        assertTrue(output().contains("Available commands:"));
        assertFalse(processor.execute(" EXIT "));
    }

    @Test
    void unexpectedErrorOnOneLineDoesNotStopProcessing() throws IOException {
        CommandProcessor failing = new CommandProcessor(
                new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            @Override
            public boolean execute(String line) {
                if (line.equals("BOOM")) {
                    throw new IllegalStateException("boom");
                }
                return super.execute(line);
            }
        };
        failing.run(new BufferedReader(new StringReader("BOOM\nHELP\n")));
        String out = output();
        assertTrue(out.contains("Error: could not process command: BOOM"));
        assertEquals(1, occurrences(out, "Available commands:"));
    }

    @Test
    void blankLinesAreIgnored() throws IOException {
        run("\n   \nHELP\n");
        assertFalse(output().contains("Error"));
        assertEquals(1, occurrences(output(), "Available commands:"));
    }
}
