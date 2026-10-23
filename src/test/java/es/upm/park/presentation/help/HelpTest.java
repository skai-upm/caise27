package es.upm.park.presentation.help;

import es.upm.park.App;
import es.upm.park.business.controller.ParkFixture;
import es.upm.park.presentation.CommandProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpTest {

    private static final String[] EXPECTED_SYNTAX = {
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
            "EXIT"};

    @TempDir
    Path dir;

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = App.createCommandProcessor(ParkFixture.initialize(dir));
    }

    @Test
    void helpListsEverySyntax() {
        String help = processor.process("HELP").output();
        for (String syntax : EXPECTED_SYNTAX) {
            assertTrue(help.contains(syntax), "missing " + syntax);
        }
    }

    @Test
    void extendedHelpDescribesEveryCommand() {
        String extended = processor.process("help extended").output();
        List<CommandHelp> entries = processor.helpEntries();
        assertEquals(EXPECTED_SYNTAX.length, entries.size());
        for (CommandHelp entry : entries) {
            assertTrue(extended.contains(entry.syntax()));
            assertTrue(entry.description().length() > 10, "no description for " + entry.syntax());
            assertTrue(extended.contains(entry.description()));
        }
    }

    @Test
    void formatterIsInjected() {
        HelpGenerator custom = new HelpGenerator(commands -> "count=" + commands.size());
        assertEquals("count=2", custom.generate(List.of(new CommandHelp("A", "a"), new CommandHelp("B", "b"))));
        String syntaxOnly = new SyntaxHelpFormatter().format(List.of(new CommandHelp("X;<y>", "does x")));
        assertTrue(syntaxOnly.contains("X;<y>"));
        assertTrue(!syntaxOnly.contains("does x"));
    }
}
