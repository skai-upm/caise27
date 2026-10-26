package es.upm.parque.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final CommandCatalog catalog = CommandCatalog.standard();
    private final HelpGenerator generator = new HelpGenerator(catalog,
            Map.of("", new SyntaxHelpFormatter(), "EXTENDED", new ExtendedHelpFormatter()));

    @Test
    void conciseHelpListsSyntaxOnly() {
        List<String> lines = generator.generate("").lines().toList();
        assertEquals("Available commands:", lines.get(0));
        assertEquals(catalog.getCommands().size() + 1, lines.size());
        assertTrue(lines.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(lines.contains("HELP EXTENDED"));
        assertTrue(lines.contains("EXIT"));
    }

    @Test
    void extendedHelpDescribesEveryCommand() {
        String help = generator.generate("EXTENDED");
        for (CommandInfo command : catalog.getCommands()) {
            assertTrue(help.contains(command.syntax() + System.lineSeparator() + "    " + command.description()),
                    command.operation());
            assertTrue(!command.description().isBlank(), command.operation());
        }
    }

    @Test
    void unknownModeAndCustomFormatter() {
        assertNull(generator.generate("VERBOSE"));
        HelpGenerator custom = new HelpGenerator(catalog, Map.of("COUNT", commands -> "Commands: " + commands.size()));
        assertEquals("Commands: " + catalog.getCommands().size(), custom.generate("COUNT"));
    }

    @Test
    void syntaxLookup() {
        assertEquals("USER DELETE;Correo;Contraseña", catalog.syntaxOf("USER DELETE"));
        assertEquals("NOPE", catalog.syntaxOf("NOPE"));
    }
}
