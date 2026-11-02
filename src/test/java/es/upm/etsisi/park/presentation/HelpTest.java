package es.upm.etsisi.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.ParkApplication;
import es.upm.etsisi.park.business.ParkTestSupport;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelpTest extends ParkTestSupport {

    private static final List<String> REQUIRED = List.of(
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

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = ParkApplication.commandProcessor(services);
    }

    @Test
    void helpShowsEverySyntaxLine() {
        List<String> lines = processor.execute("HELP").lines().toList();
        for (String syntax : REQUIRED) {
            assertTrue(lines.contains(syntax), "HELP must list " + syntax);
        }
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String extended = processor.execute("HELP EXTENDED");
        for (CommandDefinition c : processor.getCommands()) {
            assertFalse(c.description().isBlank(), c.keyword());
            assertTrue(extended.contains(c.syntax().get(c.syntax().size() - 1) + System.lineSeparator() + "    " + c.description()), c.keyword());
            assertTrue(extended.contains(c.description()), c.keyword());
        }
        assertFalse(processor.execute("HELP").contains("Registers a new user"));
    }

    @Test
    void generatorUsesInjectedStrategy() {
        HelpGenerator custom = new HelpGenerator(commands -> "count=" + commands.size());
        assertEquals("count=" + processor.getCommands().size(), custom.generate(processor.getCommands()));
    }

    @Test
    void parameterCountsComeFromSyntax() {
        CommandDefinition enter = processor.getCommands().stream()
                .filter(c -> c.keyword().equals("ATTRACTION ENTER")).findFirst().orElseThrow();
        assertEquals(java.util.Set.of(2, 3), enter.parameterCounts());
        assertTrue(processor.execute("ATTRACTION ENTER;A01;T1;FAST").startsWith("ERROR:"));
        assertTrue(processor.execute("INVOICE CREATE;OTHER;x").startsWith("ERROR:"));
        assertTrue(processor.execute("HELP EXTENDED;x").startsWith("ERROR:"));
    }
}
