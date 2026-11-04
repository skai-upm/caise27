package com.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.TestPark;
import com.parque.business.controller.ParkController;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() throws Exception {
        ParkController c = TestPark.start(dir);
        CommandCatalog catalog = CommandCatalog.standard();
        p = new CommandProcessor(c, new HelpGenerator(catalog, new SyntaxHelpFormatter()),
                new HelpGenerator(catalog, new ExtendedHelpFormatter()));
    }

    @Test
    void helpKeepsSyntaxListAndAddsHelpExtended() {
        List<String> lines = p.process("HELP").lines().toList();
        assertEquals("OK: Available commands:", lines.get(0));
        assertTrue(lines.contains("  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(lines.contains("  HELP EXTENDED"));
        assertEquals("  EXIT", lines.get(lines.size() - 1));
    }

    @Test
    void extendedHelpHasSameCommandsWithDescriptions() {
        List<String> basic = p.process("HELP").lines().skip(1).map(String::trim).toList();
        List<String> ext = p.process("HELP EXTENDED").lines().toList();
        assertEquals("OK: Available commands (extended):", ext.get(0));
        List<String> syntaxes = new ArrayList<>();
        for (int i = 1; i < ext.size(); i += 2) {
            assertTrue(ext.get(i).startsWith("  ") && !ext.get(i).startsWith("   "), ext.get(i));
            syntaxes.add(ext.get(i).trim());
            assertTrue(ext.get(i + 1).startsWith("      ") && !ext.get(i + 1).isBlank());
        }
        assertEquals(basic, syntaxes);
    }

    @Test
    void parametersRejected() {
        assertTrue(p.process("HELP EXTENDED;x").startsWith("ERROR: "));
        assertTrue(p.process("HELP;x").startsWith("ERROR: "));
    }

    @Test
    void everyRegisteredCommandIsRecognised() {
        for (CommandInfo c : CommandCatalog.standard().commands()) {
            String operation = c.syntax().split(";")[0];
            if (operation.equals("EXIT")) {
                continue;
            }
            assertFalse(p.process(operation).startsWith("ERROR: Unknown command"), operation);
        }
    }
}
