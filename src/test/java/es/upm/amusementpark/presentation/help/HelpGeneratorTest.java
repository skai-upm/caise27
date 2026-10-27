package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final List<String> SYNTAX_HELP = List.of(
            "Available commands:",
            "  USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "  USER DELETE;<Email>;<Password>",
            "  USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "  SYSTEM DATE;<yyyy-MM-dd>",
            "  SYSTEM PRICE;<Price>",
            "  GROUP CREATE;<Identifier>;<Name>",
            "  GROUP ADD;<Identifier>;<Email>",
            "  GROUP REMOVE;<Identifier>;<Email>",
            "  GROUP DELETE;<Identifier>",
            "  TICKET CREATE;<identifier>;<email>;<date>",
            "  TICKET DELETE;<identifier>",
            "  GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "  GROUPTICKET DELETE;<identifier>",
            "  ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "  ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "  ATTRACTION ENTER;<attraction>;<ticket>",
            "  ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "  ATTRACTION EXIT;<attraction>;<email>",
            "  ATTRACTION LIST;<attraction>",
            "  ATTRACTION STATUS;<attractionIdentifier>",
            "  SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "  SPEEDPASS DELETE;<speedPassIdentifier>",
            "  SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "  SPECIALPASS DELETE;<specialPassIdentifier>",
            "  INVOICE CREATE;USER;<email>",
            "  INVOICE CREATE;GROUP;<groupIdentifier>",
            "  HELP",
            "  HELP EXTENDED",
            "  EXIT");

    @Test
    void syntaxHelpListsEveryCommand() {
        assertEquals(String.join(System.lineSeparator(), SYNTAX_HELP),
                new HelpGenerator(new SyntaxHelpFormatter()).generate());
    }

    @Test
    void extendedHelpExplainsEveryCommand() {
        String extended = new HelpGenerator(new ExtendedHelpFormatter()).generate();
        for (CommandInfo command : CommandCatalog.commands()) {
            assertFalse(command.description().isBlank(), command.syntax());
            assertTrue(extended.contains("  " + command.syntax() + System.lineSeparator() + "      "
                    + command.description()), command.syntax());
        }
    }

    @Test
    void injectedFormatterDecidesTheOutput() {
        HelpFormatter namesOnly = commands -> commands.stream().map(c -> c.syntax().split(";")[0]).toList();
        HelpGenerator generator = new HelpGenerator(namesOnly, List.of(new CommandInfo("HELP;x", "d"),
                new CommandInfo("EXIT", "e")));
        assertEquals("HELP" + System.lineSeparator() + "EXIT", generator.generate());
    }
}
