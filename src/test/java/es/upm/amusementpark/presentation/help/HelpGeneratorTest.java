package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final CommandRegistry registry = CommandRegistry.standard();

    @Test
    void conciseHelpShowsEverySyntaxWithoutDescriptions() {
        String help = new HelpGenerator(registry, new SyntaxHelpFormatter()).generate();
        for (CommandInfo command : registry.getCommands()) {
            assertTrue(help.contains(command.syntax()), command.syntax());
            assertFalse(help.contains(command.description()), command.description());
        }
    }

    @Test
    void extendedHelpShowsSyntaxAndDescription() {
        String help = new HelpGenerator(registry, new ExtendedHelpFormatter()).generate();
        for (CommandInfo command : registry.getCommands()) {
            assertTrue(help.contains(command.syntax()), command.syntax());
            assertTrue(help.contains(command.description()), command.description());
        }
    }

    @Test
    void usesTheInjectedFormatter() {
        HelpFormatter csv = commands -> String.join(",", commands.stream().map(CommandInfo::syntax).toList());
        CommandRegistry small = new CommandRegistry(List.of(new CommandInfo("HELP", "h"), new CommandInfo("EXIT", "e")));
        assertEquals("HELP,EXIT", new HelpGenerator(small, csv).generate());
    }

    @Test
    void everyCommandHasADescription() {
        for (CommandInfo command : registry.getCommands()) {
            assertFalse(command.description().isBlank(), command.syntax());
        }
    }
}
