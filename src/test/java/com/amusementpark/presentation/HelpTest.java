package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.controller.ParkController;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
    }

    private List<CommandDefinition> registry() {
        return App.createProcessor(f.park, new PrintStream(new ByteArrayOutputStream())).getCommands();
    }

    @Test
    void helpShowsSyntaxOfEveryRegisteredCommandWithoutDescriptions() {
        String help = f.run("HELP");
        for (CommandDefinition c : registry()) {
            assertTrue(help.contains(c.syntax()), c.name());
            assertFalse(help.contains(c.description()), c.name());
        }
    }

    @Test
    void extendedHelpShowsSyntaxAndDescriptionOfEveryCommand() {
        String help = f.run("HELP EXTENDED");
        List<CommandDefinition> commands = registry();
        assertEquals(25, commands.size());
        for (CommandDefinition c : commands) {
            assertTrue(help.contains(c.syntax()), c.name());
            assertTrue(help.contains(c.description()), c.name());
        }
        assertTrue(help.contains("HELP EXTENDED"));
    }

    @Test
    void helpFormatIsInjected() {
        HelpFormatter counting = entries -> "commands=" + entries.size();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(ParkController.getInstance(), new HelpGenerator(counting),
                new HelpGenerator(new SyntaxHelpFormatter()), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        processor.process("HELP");
        assertEquals("commands=" + processor.getCommands().size(), buffer.toString(StandardCharsets.UTF_8).trim());
    }

    @Test
    void helpRejectsParameters() {
        assertTrue(f.run("HELP EXTENDED;x").startsWith("ERROR:"));
    }
}
