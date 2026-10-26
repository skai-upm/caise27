package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager, new SystemDate());
    }

    @Test
    void createUpdateDelete() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByDni("12345678Z").getName());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com")
                .startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666")
                .startsWith("OK"));
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Nueva2#").startsWith("OK"));
        assertEquals(0, manager.getUsers().size());
    }

    @Test
    void invalidDniIsReported() {
        String out = processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(out.startsWith("ERROR") && out.contains("DNI"), out);
    }

    @Test
    void unknownCommandsAndWrongParameterCounts() {
        assertTrue(processor.process("USER FLY;x").startsWith("ERROR"));
        assertTrue(processor.process("HELLO").startsWith("ERROR"));
        assertTrue(processor.process("").startsWith("ERROR"));
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;3").startsWith("ERROR"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        for (String syntax : new String[] {"USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT",
                "SYSTEM DATE;yyyy-MM-dd"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void exitSetsFlagAndRejectsParameters() {
        assertTrue(processor.process("HELP;x").startsWith("ERROR"));
        assertTrue(processor.process("EXIT;now").startsWith("ERROR"));
        assertTrue(!processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateCanBeSetBackwardAndForward() {
        assertEquals("OK SYSTEM DATE -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(java.time.LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
        assertTrue(processor.process("SYSTEM DATE;2025-01-01").startsWith("OK"));
        assertEquals(java.time.LocalDate.of(2025, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void invalidSystemDatesAreRejected() {
        processor.process("SYSTEM DATE;2026-09-15");
        for (String bad : new String[] {"SYSTEM DATE;15/09/2026", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;2026-02-30",
                "SYSTEM DATE;", "SYSTEM DATE", "SYSTEM DATE;2026-09-15;x"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(java.time.LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
    }
}
