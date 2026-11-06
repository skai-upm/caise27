package es.upm.park.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final String NL = System.lineSeparator();

    @Test
    void syntaxHelpKeepsThePreviousLayout() {
        String help = new HelpGenerator(new SyntaxHelpFormatter(), CommandCatalog.commands()).generate();
        assertTrue(help.startsWith("Available commands:" + NL
                + "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>" + NL));
        assertTrue(help.contains("  USER UPDATE;<email>;<password>;<field>;<value>" + NL
                + "    <field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD" + NL));
        assertTrue(help.endsWith("  HELP" + NL + "  HELP EXTENDED" + NL + "  EXIT"));
        assertFalse(help.contains(" - "));
    }

    @Test
    void extendedHelpExplainsEveryCommand() {
        List<CommandInfo> catalog = CommandCatalog.commands();
        String help = new HelpGenerator(new ExtendedHelpFormatter(), catalog).generate();
        for (CommandInfo c : catalog) {
            assertFalse(c.purpose().isBlank(), c.syntax());
            assertTrue(help.contains("  " + c.syntax() + NL + "      - " + c.purpose()), c.syntax());
        }
        assertEquals(27, catalog.size());
    }

    @Test
    void anyFormatterCanBeInjected() {
        HelpFormatter names = commands -> String.join(",", commands.stream().map(CommandInfo::syntax).toList());
        HelpGenerator generator = new HelpGenerator(names, List.of(CommandInfo.of("A", "a"), CommandInfo.of("B", "b")));
        assertEquals("A,B", generator.generate());
    }
}
