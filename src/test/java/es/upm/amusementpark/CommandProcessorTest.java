package es.upm.amusementpark;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void resetPark() {
        park.reset();
    }

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
        String delivery2Minimum = "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card> "
                + "USER DELETE;<Email>;<Password> USER UPDATE;<Email>;<Password>;<Field>;<NewValue> "
                + "SYSTEM DATE;<yyyy-MM-dd> SYSTEM PRICE;<Price> GROUP CREATE;<Identifier>;<Name> "
                + "GROUP ADD;<Identifier>;<Email> GROUP REMOVE;<Identifier>;<Email> GROUP DELETE;<Identifier> "
                + "TICKET CREATE;<identifier>;<email>;<date> TICKET DELETE;<identifier> "
                + "GROUPTICKET CREATE;<identifier>;<group>;<date> GROUPTICKET DELETE;<identifier> "
                + "ATTRACTION CREATE;<identifier>;<name>;<capacity> ATTRACTION ENTER;<attraction>;<ticket> "
                + "ATTRACTION EXIT;<attraction>;<email> ATTRACTION LIST;<attraction> "
                + "ATTRACTION STATUS;<attractionIdentifier> HELP EXIT";
        // Each syntax of the Delivery 2 minimum set appears on its own line.
        java.util.List<String> lines = out.lines().map(String::trim).toList();
        String[] tokens = delivery2Minimum.split(" ");
        int found = 0;
        for (int i = 0; i < tokens.length; i++) {
            String syntax = tokens[i];
            if (i + 1 < tokens.length && !syntax.equals("HELP") && !syntax.equals("EXIT")) {
                syntax = syntax + " " + tokens[++i];
            }
            assertTrue(lines.contains(syntax), "missing in HELP: " + syntax);
            found++;
        }
        assertEquals(20, found);
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

    @Test
    void helpListsSystemCommands() {
        processor.execute("HELP");
        assertTrue(output().contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(output().contains("SYSTEM PRICE;<Price>"));
    }

    @Test
    void systemDateSetsCurrentDate() throws IOException {
        run("SYSTEM DATE;2027-04-15\n");
        assertTrue(output().contains("SYSTEM DATE UPDATED"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        run("SYSTEM DATE;2028-01-01\n");
        assertEquals(LocalDate.of(2028, 1, 1), park.getCurrentDate());
    }

    @Test
    void invalidSystemDateIsRejected() throws IOException {
        LocalDate before = park.getCurrentDate();
        run("SYSTEM DATE;2027-02-30\nSYSTEM DATE;15-04-2027\nSYSTEM DATE\nSYSTEM DATE;2027-04-15;x\n");
        assertEquals(4, occurrences(output(), "Error:"));
        assertEquals(before, park.getCurrentDate());
    }

    @Test
    void systemPriceSetsBasePrice() {
        processor.execute("SYSTEM PRICE;45.50");
        assertTrue(output().contains("SYSTEM PRICE UPDATED"));
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
    }

    @Test
    void invalidSystemPriceIsRejected() throws IOException {
        run("SYSTEM PRICE;0\nSYSTEM PRICE;-3\nSYSTEM PRICE;abc\nSYSTEM PRICE;45,50\nSYSTEM PRICE;1.234\nSYSTEM PRICE\n");
        assertEquals(6, occurrences(output(), "Error:"));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }
}
