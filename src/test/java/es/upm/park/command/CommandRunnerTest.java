package es.upm.park.command;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRunnerTest {

    private String run(String input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new CommandRunner(new CommandProcessor(), out).run(new BufferedReader(new StringReader(input)));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static int occurrences(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            count++;
        }
        return count;
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws IOException {
        String out = run("HELP\nEXIT\nHELP\n");
        assertEquals(1, occurrences(out, "Available commands:"));
        assertTrue(out.contains(CommandProcessor.EXIT_TEXT));
    }

    @Test
    void linesAfterExitAreNotRead() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("EXIT\nFOO\n"));
        new CommandRunner(new CommandProcessor(), new PrintStream(new ByteArrayOutputStream())).run(reader);
        assertEquals("FOO", reader.readLine());
    }

    @Test
    void endOfInputWithoutExitTerminatesNormally() throws IOException {
        String out = run("HELP\n");
        assertEquals(1, occurrences(out, "Available commands:"));
    }

    @Test
    void unknownCommandDoesNotStopProcessing() throws IOException {
        String out = run("FOO\nEXIT\n");
        assertTrue(out.contains("ERROR: Unknown command: FOO"));
        assertTrue(out.contains(CommandProcessor.EXIT_TEXT));
    }
}
