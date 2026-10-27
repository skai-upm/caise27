package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final CommandCatalog catalog = new CommandCatalog();

    @Test
    void syntaxHelpListsEveryCommandWithoutPurpose() {
        String help = new HelpGenerator(catalog, new SyntaxHelpFormatter()).generate();
        assertEquals(catalog.commands().size() + 1, help.split("\n").length);
        for (CommandInfo command : catalog.commands()) {
            assertTrue(help.contains(command.syntax()));
            assertFalse(help.contains(command.purpose()));
        }
    }

    @Test
    void extendedHelpAddsPurposes() {
        String help = new HelpGenerator(catalog, new ExtendedHelpFormatter()).generate();
        for (CommandInfo command : catalog.commands()) {
            assertTrue(help.contains(command.syntax() + "\n    " + command.purpose()), command.syntax());
        }
    }

    @Test
    void injectedFormatterDefinesTheOutput() {
        HelpFormatter count = commands -> commands.size() + " commands";
        assertEquals(catalog.commands().size() + " commands", new HelpGenerator(catalog, count).generate());
    }
}
