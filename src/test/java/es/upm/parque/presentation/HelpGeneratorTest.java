package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final List<CommandInfo> COMMANDS =
        List.of(new CommandInfo("A;x", "Does A."), new CommandInfo("B", "Does B."));

    @Test
    void catalogHasUniqueSyntaxesAndDescriptions() {
        Set<String> syntaxes = new HashSet<>();
        for (CommandInfo command : CommandCatalog.all()) {
            assertTrue(syntaxes.add(command.syntax()), command.syntax());
            assertFalse(command.description().isBlank(), command.syntax());
        }
        assertEquals(23, syntaxes.size());
    }

    @Test
    void syntaxFormatterListsSyntaxOnly() {
        String nl = System.lineSeparator();
        assertEquals("Available commands:" + nl + "  A;x" + nl + "  B",
            new HelpGenerator(COMMANDS, new SyntaxHelpFormatter()).generate());
    }

    @Test
    void extendedFormatterAddsDescriptions() {
        String nl = System.lineSeparator();
        assertEquals("Available commands:" + nl + "  A;x" + nl + "      Does A." + nl + "  B" + nl + "      Does B.",
            new HelpGenerator(COMMANDS, new ExtendedHelpFormatter()).generate());
    }

    @Test
    void anyInjectedFormatterIsUsed() {
        HelpFormatter csv = commands -> String.join(",", commands.stream().map(CommandInfo::syntax).toList());
        assertEquals("A;x,B", new HelpGenerator(COMMANDS, csv).generate());
    }
}
