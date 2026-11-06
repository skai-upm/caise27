package es.upm.park.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private final CommandProcessor processor = new CommandProcessor();

    private static final String[] DELIVERY_2_SYNTAX = {
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
        "ATTRACTION ENTER;<attraction>;<ticket>",
        "ATTRACTION EXIT;<attraction>;<email>",
        "ATTRACTION LIST;<attraction>",
        "ATTRACTION STATUS;<attractionIdentifier>",
        "HELP",
        "EXIT"
    };

    @Test
    void helpListsEveryMandatedSyntaxLineVerbatim() {
        CommandResult result = processor.process("HELP");
        assertFalse(result.terminate());
        java.util.List<String> lines = java.util.List.of(result.output().split(System.lineSeparator()));
        for (String syntax : DELIVERY_2_SYNTAX) {
            assertTrue(lines.contains(syntax), syntax);
        }
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").terminate());
    }

    @Test
    void unknownCommandReportsErrorAndContinues() {
        CommandResult result = processor.process("FOO");
        assertFalse(result.terminate());
        assertTrue(result.output().startsWith("ERROR"));
        assertTrue(result.output().contains("FOO"));
    }
}
