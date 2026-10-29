package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.invoice.InvoiceDirector;
import es.upm.amusementpark.presentation.CommandCatalog;
import es.upm.amusementpark.presentation.CommandInfo;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.HelpGenerator;
import es.upm.amusementpark.presentation.SyntaxHelpFormatter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelpGenerationTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        ParkController.getInstance().reset();
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void everyCommandHasAPurpose() {
        List<CommandInfo> commands = CommandCatalog.standard().commands();
        assertEquals(23, commands.size());
        for (CommandInfo command : commands) {
            assertFalse(command.description().isBlank(), command.syntax());
        }
    }

    @Test
    void syntaxHelpKeepsPreviousLayout() {
        String text = new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter()).generate();
        List<String> lines = text.lines().toList();
        assertEquals("Available commands:", lines.get(0));
        assertEquals("  USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", lines.get(1));
        assertTrue(lines.contains("  HELP EXTENDED"));
        assertEquals("  EXIT", lines.get(lines.size() - 1));
    }

    @Test
    void extendedHelpShowsSyntaxAndPurpose() throws IOException {
        String out = run("HELP EXTENDED\nHELP\n");
        assertTrue(out.contains("Available commands (extended):"));
        for (CommandInfo command : CommandCatalog.standard().commands()) {
            assertTrue(out.contains("  " + command.syntax() + "\n      " + command.description()), command.syntax());
        }
        assertTrue(out.contains("Available commands:"));
    }

    @Test
    void extendedHelpRejectsArguments() throws IOException {
        String out = run("HELP EXTENDED;x\nHELP EXTENDED\n");
        assertTrue(out.contains("Error: invalid arguments for HELP EXTENDED"));
        assertTrue(out.contains("Available commands (extended):"));
    }

    @Test
    void injectedFormatterIsUsed() throws IOException {
        HelpGenerator custom = new HelpGenerator(CommandCatalog.standard(),
                commands -> "COUNT=" + commands.size());
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(ParkController.getInstance(), new InvoiceDirector());
        CommandProcessor customProcessor = new CommandProcessor(
                new PrintStream(buffer, true, StandardCharsets.UTF_8), ParkController.getInstance(), invoices,
                custom, new HelpGenerator(CommandCatalog.standard(), new ExtendedHelpFormatter()));
        customProcessor.run(new BufferedReader(new StringReader("HELP\nSYSTEM PRICE;45\n")));
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("COUNT=23"));
        assertTrue(out.contains("SYSTEM PRICE UPDATED"));
    }
}
