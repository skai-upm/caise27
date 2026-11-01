package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.persistence.StoragePaths;

public class HelpGeneratorTest {

    @TempDir
    Path dir;

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    @Test
    public void conciseHelpShowsSyntaxOnly() {
        String text = AppBootstrap.help().generate("");
        for (CommandInfo c : CommandRegistry.commands()) {
            assertTrue(text.contains("  " + c.syntax()), c.syntax());
            assertFalse(text.contains(c.description()), c.description());
        }
    }

    @Test
    public void extendedHelpShowsSyntaxAndDescription() {
        String text = AppBootstrap.help().generate("EXTENDED");
        for (CommandInfo c : CommandRegistry.commands()) {
            assertTrue(text.contains("  " + c.syntax() + System.lineSeparator() + "      " + c.description()),
                    c.syntax());
        }
    }

    @Test
    public void injectedFormatterIsUsed() throws IOException {
        HelpGenerator custom = new HelpGenerator(CommandRegistry.commands(),
                Map.of("", commands -> "COUNT=" + commands.size(), "EXTENDED", commands -> "EXT"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(AppBootstrap.start(new StoragePaths(dir)), custom,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        processor.process("HELP");
        processor.process("HELP EXTENDED");
        assertEquals("COUNT=" + CommandRegistry.commands().size() + System.lineSeparator() + "EXT"
                + System.lineSeparator(), buffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void unknownModeIsRejected() {
        HelpGenerator help = AppBootstrap.help();
        assertFalse(help.supports("XML"));
        assertThrows(IllegalArgumentException.class, () -> help.generate("XML"));
    }
}
