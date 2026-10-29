package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpTest {

    private static final List<String> REQUIRED = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "SPECIALPASS CREATE;<identifier>;<individualTicket>", "SPECIALPASS DELETE;<specialPassIdentifier>",
            "INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP", "HELP EXTENDED", "EXIT");

    @Test
    void helpListsEverySyntax() {
        String help = new HelpGenerator(CommandCatalog.COMMANDS, new SyntaxHelpFormatter()).generate();
        for (String syntax : REQUIRED) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertFalse(help.contains(CommandCatalog.COMMANDS.get(0).description()));
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String help = new HelpGenerator(CommandCatalog.COMMANDS, new ExtendedHelpFormatter()).generate();
        for (CommandInfo command : CommandCatalog.COMMANDS) {
            assertTrue(help.contains(command.syntax()));
            assertTrue(help.contains(command.description()));
            assertFalse(command.description().isBlank());
        }
    }

    @Test
    void formatterIsInjected() {
        HelpFormatter custom = commands -> "custom:" + commands.size();
        assertEquals("custom:" + CommandCatalog.COMMANDS.size(),
                new HelpGenerator(CommandCatalog.COMMANDS, custom).generate());
    }
}
