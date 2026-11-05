package es.upm.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String run(String... lines) throws IOException {
        processor.run(new BufferedReader(new StringReader(String.join("\n", lines))));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            n++;
        }
        return n;
    }

    @Test
    void helpListsMandatoryCommands() throws IOException {
        String out = run("HELP");
        assertTrue(out.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(out.contains("USER DELETE;<email>;<password>"));
        assertTrue(out.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void helpDoesNotStopProcessing() throws IOException {
        assertEquals(2, count(run("HELP", "HELP"), "Available commands:"));
    }

    @Test
    void exitPreventsSubsequentCommands() throws IOException {
        String out = run("HELP", "EXIT", "HELP");
        assertEquals(1, count(out, "Available commands:"));
        assertTrue(out.contains("Exiting application."));
    }

    @Test
    void exitReturnsFalseAndOtherCommandsTrue() {
        assertFalse(processor.execute("EXIT"));
        assertTrue(processor.execute("HELP"));
        assertTrue(processor.execute("UNKNOWN"));
    }

    @Test
    void blankLinesAreIgnored() throws IOException {
        assertEquals("", run("", "   ", "\t"));
    }

    @Test
    void unknownCommandIsReportedAndProcessingContinues() throws IOException {
        String out = run("FOO", "HELP");
        assertTrue(out.contains("ERROR: Unknown command: FOO"));
        assertTrue(out.contains("Available commands:"));
    }

    @Test
    void failingCommandIsReportedAndProcessingContinues() throws IOException {
        processor.register("BOOM", line -> {
            throw new IllegalStateException("boom");
        });
        String out = run("BOOM", "HELP");
        assertTrue(out.contains("ERROR: Could not process command: BOOM"));
        assertTrue(out.contains("Available commands:"));
    }

    @Test
    void endOfInputWithoutExitTerminatesNormally() throws IOException {
        assertTrue(run("HELP").contains("Available commands:"));
    }
}
