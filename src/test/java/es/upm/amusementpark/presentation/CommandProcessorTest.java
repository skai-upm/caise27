package es.upm.amusementpark.presentation;

import es.upm.amusementpark.presentation.help.HelpCommands;
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
        HelpCommands.registerDefault(processor, new PrintStream(buffer, true, StandardCharsets.UTF_8));
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
        for (String syntax : new String[] {
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT"}) {
            assertTrue(out.contains("  " + syntax + System.lineSeparator()), "missing: " + syntax);
        }
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
    void invalidCommandExceptionIsHandledCentrally() throws IOException {
        processor.register("THROWS", line -> {
            throw new InvalidCommandException("Invalid syntax. Usage: THROWS;<x>");
        });
        String out = run("THROWS", "HELP");
        assertTrue(out.contains("ERROR: Invalid syntax. Usage: THROWS;<x>"));
        assertTrue(out.contains("Available commands:"));
    }

    @Test
    void parkExceptionIsHandledCentrally() throws IOException {
        processor.register("REFUSED", line -> {
            throw new es.upm.amusementpark.business.ParkException("Group G01 already exists");
        });
        String out = run("REFUSED", "HELP");
        assertTrue(out.contains("ERROR: Group G01 already exists"));
        assertTrue(out.contains("Available commands:"));
    }

    @Test
    void endOfInputWithoutExitTerminatesNormally() throws IOException {
        assertTrue(run("HELP").contains("Available commands:"));
    }
}
