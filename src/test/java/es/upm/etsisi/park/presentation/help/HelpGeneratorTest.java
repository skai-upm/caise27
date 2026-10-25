package es.upm.etsisi.park.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final CommandCatalog catalog = new CommandCatalog();

    @Test
    void usesInjectedFormatter() {
        HelpGenerator generator = new HelpGenerator(catalog, commands -> "count=" + commands.size());
        assertEquals("count=" + catalog.commands().size(), generator.generate());
    }

    @Test
    void syntaxHelpListsDelivery3Commands() {
        String help = new HelpGenerator(catalog, new SyntaxHelpFormatter()).generate();
        for (String syntax : new String[] {
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>",
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP EXTENDED",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>"}) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertFalse(help.contains(CommandCatalog.SPEEDPASS_CREATE.description()));
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String help = new HelpGenerator(catalog, new ExtendedHelpFormatter()).generate();
        for (CommandInfo command : catalog.commands()) {
            assertTrue(help.contains(command.syntax()), command.syntax());
            assertTrue(help.contains(command.description()), command.description());
        }
    }
}
