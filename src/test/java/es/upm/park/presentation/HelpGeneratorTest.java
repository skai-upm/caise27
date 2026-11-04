package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    static final String NL = System.lineSeparator();

    @Test
    void conciseHelpKeepsThePreviousFormat() {
        String help = new HelpGenerator(CommandCatalog.commands(), new SyntaxHelpFormatter()).generate();
        String[] lines = help.split("\\R");
        assertEquals("OK: available commands:", lines[0]);
        assertEquals("  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta", lines[1]);
        assertEquals("  HELP EXTENDED", lines[lines.length - 3]);
        assertEquals("  EXIT", lines[lines.length - 2]);
        assertEquals("  Campo (USER UPDATE): NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD", lines[lines.length - 1]);
        assertEquals(CommandCatalog.commands().size() + 2, lines.length);
    }

    @Test
    void extendedHelpHasSyntaxAndDescriptionForEveryCommand() {
        String help = new HelpGenerator(CommandCatalog.commands(), new ExtendedHelpFormatter()).generate();
        for (CommandInfo c : CommandCatalog.commands()) {
            assertTrue(help.contains("  " + c.syntax() + NL + "      " + c.description()), c.syntax());
            assertTrue(!c.description().isBlank());
        }
        assertTrue(help.contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
    }

    @Test
    void usesTheInjectedFormatter() {
        HelpFormatter countOnly = commands -> "OK: " + commands.size() + " commands";
        List<CommandInfo> two = List.of(new CommandInfo("A", "a"), new CommandInfo("B", "b"));
        assertEquals("OK: 2 commands", new HelpGenerator(two, countOnly).generate());
    }
}
