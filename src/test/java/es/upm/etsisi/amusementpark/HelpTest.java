package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.presentation.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.CommandInfo;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.HelpGenerator;
import java.util.List;
import org.junit.jupiter.api.Test;

class HelpTest extends ParkFixture {

    static final List<String> NEW_SYNTAX = List.of(
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>",
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP", "HELP EXTENDED", "EXIT");

    @Test
    void helpShowsDeliveryThreeSyntax() {
        String help = processor().process("HELP");
        for (String syntax : NEW_SYNTAX) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertTrue(help.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(help.contains("GROUPTICKET DELETE;<identifier>"));
        assertFalse(help.contains(CommandCatalog.COMMANDS.get(0).description()));
    }

    @Test
    void extendedHelpShowsSyntaxAndPurpose() {
        String help = processor().process("HELP EXTENDED");
        for (CommandInfo command : CommandCatalog.COMMANDS) {
            assertTrue(help.contains(command.syntax()), command.syntax());
            assertTrue(help.contains(command.description()), command.description());
            assertFalse(command.description().isBlank());
        }
    }

    @Test
    void helpCommandsRejectArguments() {
        CommandProcessor processor = processor();
        assertTrue(processor.process("HELP EXTENDED;x").startsWith("ERROR:"));
        assertTrue(processor.process("HELP;x").startsWith("ERROR:"));
    }

    @Test
    void generatorUsesInjectedFormat() {
        HelpGenerator generator = new HelpGenerator(commands -> "COUNT=" + commands.size());
        assertEquals("COUNT=" + CommandCatalog.COMMANDS.size(), generator.generate());
    }
}
