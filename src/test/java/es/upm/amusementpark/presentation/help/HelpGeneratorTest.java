package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * HELP / HELP EXTENDED generation with injected formatting strategies (Delivery 3, F3-C04, F3-C10).
 */
class HelpGeneratorTest {

    private static final List<String> REQUIRED_SYNTAX = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>",
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP",
            "HELP EXTENDED",
            "EXIT");

    private final CommandCatalog catalog = new CommandCatalog();

    @Test
    void basicHelpListsEverySyntax() {
        String help = new HelpGenerator(catalog, new SyntaxHelpFormatter()).generate();
        for (String syntax : REQUIRED_SYNTAX) {
            assertTrue(help.lines().anyMatch(line -> line.equals(syntax)), syntax);
        }
        assertFalse(help.contains("Registers a new user"));
    }

    @Test
    void extendedHelpAddsDescriptions() {
        List<String> lines = new HelpGenerator(catalog, new ExtendedHelpFormatter()).generate().lines().toList();
        for (String syntax : REQUIRED_SYNTAX) {
            int index = lines.indexOf(syntax);
            assertTrue(index >= 0, syntax);
            assertTrue(lines.get(index + 1).startsWith("    ") && !lines.get(index + 1).isBlank(), syntax);
        }
    }

    @Test
    void catalogEntriesAllHaveDescriptions() {
        assertEquals(REQUIRED_SYNTAX.size(), catalog.commands().size());
        for (CommandInfo command : catalog.commands()) {
            assertFalse(command.description().isBlank(), command.syntax());
        }
    }

    @Test
    void injectedStrategyControlsOutput() {
        HelpFormatter countOnly = commands -> commands.size() + " commands";
        assertEquals(REQUIRED_SYNTAX.size() + " commands", new HelpGenerator(catalog, countOnly).generate());
    }
}
