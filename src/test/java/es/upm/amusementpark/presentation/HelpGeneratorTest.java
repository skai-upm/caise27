package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    @Test
    void basicHelpListsEverySyntax() {
        String help = new HelpGenerator(CommandCatalog.all(), new BasicHelpFormat()).generate();
        DeliveryTwoIntegrationTest.HELP_SYNTAX.forEach(s -> assertTrue(help.contains(s), s));
        assertTrue(help.contains("HELP EXTENDED"));
        assertTrue(help.contains("INVOICE CREATE;USER;<email>"));
    }

    @Test
    void extendedHelpPairsSyntaxWithDescription() {
        String[] lines = new HelpGenerator(CommandCatalog.all(), new ExtendedHelpFormat()).generate()
                .split(System.lineSeparator());
        assertEquals(1 + 2 * CommandCatalog.all().size(), lines.length);
        for (int i = 0; i < CommandCatalog.all().size(); i++) {
            CommandDescriptor c = CommandCatalog.all().get(i);
            assertEquals(c.syntax(), lines[1 + 2 * i]);
            assertEquals("    " + c.description(), lines[2 + 2 * i]);
        }
    }

    @Test
    void injectedFormatIsUsed() {
        HelpGenerator custom = new HelpGenerator(List.of(new CommandDescriptor("A", "a"), new CommandDescriptor("B", "b")),
                commands -> commands.size() + " commands");
        assertEquals("2 commands", custom.generate());
    }
}
