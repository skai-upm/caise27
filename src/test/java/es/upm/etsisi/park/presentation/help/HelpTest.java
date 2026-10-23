package es.upm.etsisi.park.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.ApplicationContext;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandProcessor;
import es.upm.etsisi.park.presentation.CommandRegistry;
import es.upm.etsisi.park.support.TestContext;

class HelpTest {

    static final List<String> ALL_SYNTAXES = List.of(
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>", "USER UPDATE;<email>;<password>;<field>;<value>",
            "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<price>", "GROUP CREATE;<identifier>;<name>",
            "GROUP ADD;<identifier>;<email>", "GROUP REMOVE;<identifier>;<email>", "GROUP DELETE;<identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>", "ATTRACTION STATUS;<attraction>",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP", "HELP EXTENDED", "EXIT");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        TestContext.initialize(dir);
        processor = ApplicationContext.commandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpListsEverySyntax() {
        String text = run("HELP");
        for (String syntax : ALL_SYNTAXES) {
            assertTrue(text.contains("  " + syntax + "\n"), syntax);
        }
        assertTrue(text.contains("<field> is one of: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        assertTrue(text.contains("<type> is one of: GENERAL, INDIVIDUAL, GROUP, SPECIAL"));
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String text = run("help extended");
        for (String syntax : ALL_SYNTAXES) {
            assertTrue(text.contains("  " + syntax + "\n"), syntax);
        }
        assertTrue(text.contains("Buys 1 or 10 SpeedPasses"));
        assertTrue(text.contains("Prints an invoice"));
        assertTrue(text.length() > run("HELP").length());
    }

    @Test
    void rendererIsInjected() {
        CommandRegistry registry = new CommandRegistry();
        registry.register(CommandDefinition.of("PING", "Answers pong.", (args, out) -> out.println("PONG"), "PING"));
        HelpGenerator generator = new HelpGenerator(registry,
                Map.of("COUNT", commands -> commands.size() + " command(s)"));
        assertEquals("1 command(s)", generator.render("COUNT"));
        assertThrows(IllegalArgumentException.class, () -> generator.render(HelpGenerator.BASIC));
    }

    @Test
    void helpRejectsArguments() {
        assertTrue(run("HELP;x").startsWith("ERROR: Wrong number of arguments"));
    }
}
