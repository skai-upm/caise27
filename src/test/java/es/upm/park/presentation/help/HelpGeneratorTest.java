package es.upm.park.presentation.help;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpGeneratorTest {

    private static final List<CommandDescription> COMMANDS = List.of(
            new CommandDescription("HELP", "Shows the syntax of every command."),
            new CommandDescription("EXIT", "Ends the application."));

    @Test
    void syntaxFormatListsOnlySyntax() {
        String nl = System.lineSeparator();
        assertEquals("Available commands:" + nl + "HELP" + nl + "EXIT",
                new HelpGenerator(COMMANDS, new SyntaxHelpFormatter()).generate());
    }

    @Test
    void extendedFormatAddsPurpose() {
        String text = new HelpGenerator(COMMANDS, new ExtendedHelpFormatter()).generate();
        assertTrue(text.contains("HELP" + System.lineSeparator() + "    Shows the syntax of every command."));
        assertTrue(text.contains("    Ends the application."));
    }

    @Test
    void injectedBehaviorDecidesTheOutput() {
        HelpFormatter compact = commands -> String.join(" | ",
                commands.stream().map(CommandDescription::keyword).toList());
        assertEquals("HELP | EXIT", new HelpGenerator(COMMANDS, compact).generate());
    }

    @Test
    void standardCatalogHasPurposeForEveryCommand() {
        List<CommandDescription> catalog = CommandCatalog.standard();
        assertTrue(catalog.stream().noneMatch(command -> command.purpose().isBlank()));
        assertTrue(catalog.stream().anyMatch(command -> command.syntax().equals("HELP EXTENDED")));
        assertEquals("INVOICE CREATE", new CommandDescription("INVOICE CREATE;USER;<email>", "x").keyword());
        assertFalse(catalog.isEmpty());
    }
}
