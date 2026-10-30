package es.upm.etsisi.amusementpark.presentation.help;

import es.upm.etsisi.amusementpark.presentation.command.CommandDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HelpGeneratorTest {

    private final List<CommandDescriptor> commands = List.of(
            new CommandDescriptor("A", "A;<x>", "Does A.", p -> ""),
            new CommandDescriptor("B", "B", "Does B.", p -> ""));

    @Test
    void usesInjectedFormatterPerMode() {
        HelpGenerator generator = new HelpGenerator(Map.of(
                HelpMode.BASIC, new SyntaxHelpFormatter(),
                HelpMode.EXTENDED, new ExtendedHelpFormatter()));

        String basic = generator.generate(HelpMode.BASIC, commands);
        assertTrue(basic.contains("A;<x>"));
        assertFalse(basic.contains("Does A."));

        String extended = generator.generate(HelpMode.EXTENDED, commands);
        assertTrue(extended.contains("A;<x>"));
        assertTrue(extended.contains("Does A."));
        assertTrue(extended.contains("Does B."));
    }

    @Test
    void alternativeFormatterCanBeInjected() {
        HelpGenerator generator = new HelpGenerator(Map.of(HelpMode.BASIC, c -> "custom:" + c.size()));
        assertEquals("custom:2", generator.generate(HelpMode.BASIC, commands));
        assertThrows(IllegalStateException.class, () -> generator.generate(HelpMode.EXTENDED, commands));
    }
}
