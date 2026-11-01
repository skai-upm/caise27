package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("FOO;X", "Does foo."), new CommandInfo("BAR", "Does bar."));

    @Test
    void usesInjectedFormatter() {
        HelpGenerator generator = new HelpGenerator(COMMANDS,
                commands -> String.join("|", commands.stream().map(CommandInfo::syntax).toList()));
        assertEquals("FOO;X|BAR", generator.generate());
    }

    @Test
    void conciseFormatListsSyntaxOnly() {
        String text = new HelpGenerator(COMMANDS, new ConciseHelpFormatter()).generate();
        assertTrue(text.contains("FOO;X"));
        assertTrue(!text.contains("Does foo."));
    }

    @Test
    void extendedFormatAddsDescriptions() {
        String text = new HelpGenerator(COMMANDS, new ExtendedHelpFormatter()).generate();
        assertTrue(text.contains("FOO;X"));
        assertTrue(text.contains("Does foo."));
        assertTrue(text.contains("Does bar."));
    }

    @Test
    void everyRegisteredCommandHasSyntaxAndDescription() {
        for (CommandInfo command : CommandCatalog.COMMANDS) {
            assertTrue(!command.syntax().isBlank() && !command.description().isBlank(), command.toString());
        }
    }
}
