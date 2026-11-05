package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.persistence.UserRepository;

class HelpTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = TestSupport.processor(new UserRepository());
    }

    @Test
    void extendedHelpShowsEverySyntaxWithItsPurposeInOrder() {
        String help = processor.process("HELP EXTENDED");
        assertTrue(help.startsWith("Available commands (extended):"), help);
        int from = 0;
        for (CommandDescriptor command : CommandCatalog.COMMANDS) {
            int at = help.indexOf(command.syntax(), from);
            assertTrue(at >= from, "missing or out of order: " + command.syntax());
            int purpose = help.indexOf(command.purpose(), at);
            assertTrue(purpose > at, "missing purpose: " + command.syntax());
            from = purpose + command.purpose().length();
        }
        assertTrue(help.contains("INVOICE CREATE;USER;<email>" + System.lineSeparator()
                + "      Generates the invoice of a user"), help);
    }

    @Test
    void plainHelpShowsOnlySyntax() {
        String help = processor.process("HELP");
        assertTrue(help.startsWith("Available commands:"), help);
        assertTrue(help.contains("HELP EXTENDED"), help);
        for (CommandDescriptor command : CommandCatalog.COMMANDS) {
            assertFalse(help.contains(command.purpose()), command.purpose());
        }
    }

    @Test
    void extraFieldsAreRejected() {
        assertEquals("ERROR: HELP EXTENDED takes no fields", processor.process("HELP EXTENDED;x"));
        assertEquals("ERROR: HELP takes no fields", processor.process("HELP;x"));
    }

    @Test
    void everyCatalogEntryHasAPurpose() {
        for (CommandDescriptor command : CommandCatalog.COMMANDS) {
            assertFalse(command.purpose().isBlank(), command.syntax());
        }
    }

    @Test
    void everyDispatchedCommandIsInTheCatalog() throws IOException {
        Path sources = Path.of("src", "main", "java", "es", "upm", "parque", "presentation");
        Pattern constant = Pattern.compile("static final String \\w+ = \"([A-Z]+(?: [A-Z]+)?)\";");
        List<String> syntaxes = CommandCatalog.syntaxes();
        for (String file : List.of("UserCommands.java", "GroupCommands.java", "TicketCommands.java",
                "AttractionCommands.java", "InvoiceCommands.java", "CommandProcessor.java")) {
            Matcher m = constant.matcher(Files.readString(sources.resolve(file), StandardCharsets.UTF_8));
            while (m.find()) {
                String name = m.group(1);
                if (name.contains(" ") || name.equals("EXIT")) {
                    assertTrue(syntaxes.stream().anyMatch(s -> s.equals(name) || s.startsWith(name + ";")),
                            "not in the help catalog: " + name);
                }
            }
        }
    }

    @Test
    void injectedHelpBehaviourIsUsedWithoutChangingTheProcessor() {
        HelpFormatter countOnly = commands -> commands.size() + " commands";
        HelpCommands help = HelpConfiguration.standard()
                .register("HELP COUNT", new HelpGenerator(CommandCatalog.COMMANDS, countOnly));
        CommandProcessor custom = new CommandProcessor(help);
        assertEquals(CommandCatalog.COMMANDS.size() + " commands", custom.process("HELP COUNT"));
        assertTrue(custom.process("HELP").startsWith("Available commands:"));
    }

    @Test
    void generatorReturnsExactlyTheInjectedFormatterOutput() {
        List<CommandDescriptor> catalog = List.of(new CommandDescriptor("A;<x>", "does a"));
        HelpGenerator generator = new HelpGenerator(catalog, commands -> "<" + commands.get(0).syntax() + ">");
        assertEquals("<A;<x>>", generator.generate());
    }
}
