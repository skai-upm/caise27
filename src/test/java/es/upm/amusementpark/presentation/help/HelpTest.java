package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.StorageLocation;
import es.upm.amusementpark.presentation.ApplicationBootstrap;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.command.CommandDefinition;
import es.upm.amusementpark.presentation.command.CommandRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private String run(CommandProcessor processor, String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private CommandProcessor start(HelpFormatter concise, HelpFormatter extended) throws IOException {
        return ApplicationBootstrap.start(new StorageLocation(dir), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                concise, extended);
    }

    @Test
    void conciseAndExtendedListEveryRegisteredCommand() throws IOException {
        List<CommandDefinition> commands = new ArrayList<>();
        CommandProcessor processor = start(list -> {
            commands.clear();
            commands.addAll(list);
            return new ConciseHelpFormatter().format(list);
        }, new ExtendedHelpFormatter());
        String concise = run(processor, "HELP");
        String extended = run(processor, "HELP EXTENDED");
        assertTrue(commands.size() >= 20);
        for (CommandDefinition command : commands) {
            for (String syntax : command.syntaxes()) {
                assertTrue(concise.contains(syntax), syntax);
                assertTrue(extended.contains(syntax), syntax);
            }
            assertTrue(extended.contains(command.description()), command.name());
            assertFalse(concise.contains(command.description()), command.name());
        }
    }

    @Test
    void injectedFormattersAreUsed() throws IOException {
        CommandProcessor processor = start(list -> "CUSTOM " + list.size(), list -> "CUSTOM-EXT");
        assertTrue(run(processor, "HELP").startsWith("CUSTOM "));
        assertEquals("CUSTOM-EXT", run(processor, "HELP EXTENDED").trim());
    }

    @Test
    void generatorUsesTheRegistry() {
        CommandRegistry registry = new CommandRegistry()
                .register(CommandDefinition.of("PING;Texto", "Answers pong.", p -> "pong"));
        HelpGenerator generator = new HelpGenerator(registry, new ConciseHelpFormatter(), new ExtendedHelpFormatter());
        assertTrue(generator.concise().contains("PING;Texto"));
        assertTrue(generator.extended().contains("Answers pong."));
    }
}
