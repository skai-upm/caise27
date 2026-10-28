package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGenerationTest {

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void injectedStrategyDecidesTheOutput() {
        HelpGenerator generator = new HelpGenerator(entries -> "count=" + entries.size(),
                List.of(new CommandHelp("A", "a"), new CommandHelp("B", "b")));
        assertEquals("count=2", generator.generate());
    }

    @Test
    void builtInStrategies() {
        List<CommandHelp> entries = List.of(new CommandHelp("FOO;<x>", "Does foo."));
        String nl = System.lineSeparator();
        assertEquals("Available commands:" + nl + "FOO;<x>", new SyntaxHelpStrategy().render(entries));
        assertEquals("Available commands (extended):" + nl + "FOO;<x>" + nl + "    Does foo.",
                new ExtendedHelpStrategy().render(entries));
    }

    @Test
    void helpExtendedExplainsEveryCommand() throws IOException {
        String nl = System.lineSeparator();
        String out = run("HELP EXTENDED\n");
        for (CommandHelp entry : CommandCatalog.ENTRIES) {
            assertFalse(entry.description().isBlank());
            assertTrue(out.contains(entry.syntax() + nl + "    " + entry.description() + nl), entry.syntax());
        }
    }

    @Test
    void plainHelpListsSyntaxOnlyIncludingHelpExtended() throws IOException {
        String out = run("HELP EXTENDED\nHELP\n");
        String plain = out.substring(out.indexOf("Available commands:"));
        assertTrue(plain.contains("HELP EXTENDED" + System.lineSeparator()));
        for (CommandHelp entry : CommandCatalog.ENTRIES) {
            assertTrue(plain.contains(entry.syntax()));
            assertFalse(plain.contains(entry.description()));
        }
    }
}
