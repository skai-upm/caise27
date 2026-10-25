package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final String NL = System.lineSeparator();

    private final CommandRegistry registry = new CommandRegistry()
            .register("HELP", "Lists the syntax of every command.")
            .register("EXIT", "Ends the application.");

    @Test
    void syntaxFormatListsOneSyntaxPerLine() {
        assertEquals("HELP" + NL + "EXIT", new HelpGenerator(registry, new SyntaxHelpFormat()).generate());
    }

    @Test
    void extendedFormatAddsIndentedDescriptions() {
        assertEquals(String.join(NL, "HELP", "    Lists the syntax of every command.", "EXIT",
                "    Ends the application."), new HelpGenerator(registry, new ExtendedHelpFormat()).generate());
    }

    @Test
    void generatorUsesTheInjectedFormat() {
        HelpFormat counting = commands -> "commands: " + commands.size();
        assertEquals("commands: 2", new HelpGenerator(registry, counting).generate());
    }

    @Test
    void standardRegistryContainsEveryCommandOnce() {
        List<CommandInfo> commands = CommandRegistry.standard().getCommands();
        assertEquals(27, commands.size());
        assertEquals(commands.size(), commands.stream().map(CommandInfo::getSyntax).distinct().count());
        assertTrue(commands.stream().allMatch(c -> !c.getDescription().isBlank()));
        CommandInfo extra = new CommandInfo("X", "x");
        assertThrows(UnsupportedOperationException.class, () -> commands.add(extra));
    }
}
