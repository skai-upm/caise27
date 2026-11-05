package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class HelpTest {

    private static String run(String command) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        HelpCommands.registerDefault(processor, out);
        processor.execute(command);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void plainHelpListsSyntaxOnly() {
        String out = run("HELP");
        assertTrue(out.startsWith("Available commands:" + System.lineSeparator()));
        assertTrue(out.contains("  GROUPTICKET CREATE;<identifier>;<group>;<date>" + System.lineSeparator()));
        assertTrue(out.contains("  HELP EXTENDED" + System.lineSeparator()));
        assertFalse(out.contains("Buys a discounted ticket"));
    }

    @Test
    void extendedHelpShowsPurposeOfEveryCommand() {
        String out = run("HELP EXTENDED");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("Available commands (extended):", lines[0]);
        List<CommandDescription> commands = CommandCatalog.standard().commands();
        assertEquals(1 + 2 * commands.size(), lines.length);
        for (int i = 0; i < commands.size(); i++) {
            assertEquals("  " + commands.get(i).syntax(), lines[1 + 2 * i]);
            assertFalse(lines[2 + 2 * i].isBlank());
        }
        assertTrue(out.contains("Buys a discounted ticket for every current member of a group."));
    }

    @Test
    void customFormatterCanBeInjected() {
        HelpGenerator generator = new HelpGenerator(
                commands -> commands.size() + " commands: " + commands.get(0).syntax(),
                new CommandCatalog(List.of(new CommandDescription("PING", "Checks the application."))));
        assertEquals("1 commands: PING", generator.generate());
    }

    @Test
    void processorAloneHasNoHelp() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        processor.execute("HELP");
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("ERROR: Unknown command: HELP"));
    }
}
