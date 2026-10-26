package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager manager;
    private MainController controller;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        TestPark park = new TestPark(dir);
        manager = park.users;
        controller = park.controller;
        processor = park.processor;
    }

    @Test
    void createDeleteUpdate() {
        assertEquals("OK USER CREATED", processor.process(ANA));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void operationIsCaseInsensitiveAndTrimmed() {
        assertEquals("OK USER CREATED",
                processor.process(" user create ; Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456 "));
    }

    @Test
    void unknownCommand() {
        String out = processor.process("FOO BAR;1");
        assertTrue(out.startsWith("ERROR Unknown command"), out);
    }

    @Test
    void wrongParameterCount() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER UPDATE;a;b;c").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR Wrong number of parameters"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void trailingEmptyValueIsBlankNotMissing() {
        String out = processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;");
        assertEquals("ERROR The card number is required", out);
    }

    @Test
    void businessErrorsAreReported() {
        processor.process(ANA);
        assertTrue(processor.process(ANA).startsWith("ERROR"));
        assertEquals("ERROR Incorrect password", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("ERROR The DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }

    @Test
    void helpListsEveryCommand() {
        String out = processor.process("HELP");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("OK Available commands:", lines[0]);
        assertEquals(CommandProcessor.COMMAND_SYNTAX, List.of(lines).subList(1, lines.length));
        assertTrue(out.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(processor.process("HELP;x").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void exitSetsFlag() {
        assertTrue(processor.process("EXIT;x").startsWith("ERROR Wrong number of parameters"));
        assertFalse(processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("exit"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateMovesForwardAndBackward() {
        assertEquals(LocalDate.of(2026, 9, 1), controller.getCurrentDate());
        assertEquals("OK SYSTEM DATE SET -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), controller.getCurrentDate());
        assertEquals("OK SYSTEM DATE SET -> 2025-01-01", processor.process("system date; 2025-01-01 "));
        assertEquals(LocalDate.of(2025, 1, 1), controller.getCurrentDate());
    }

    @Test
    void invalidSystemDateLeavesDateUnchanged() {
        assertTrue(processor.process("SYSTEM DATE;15/09/2026").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-9-15").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-02-30").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE").startsWith("ERROR Wrong number of parameters"));
        assertEquals(LocalDate.of(2026, 9, 1), controller.getCurrentDate());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void groupCommands() {
        processor.process(ANA);
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP CREATE;G01").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(new TestPark(dir).groups.findById("G01").getMembers().isEmpty());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String syntax : List.of("GROUP CREATE;IdentificadorGrupo;Nombre", "GROUP ADD;IdentificadorGrupo;Correo",
                "GROUP REMOVE;IdentificadorGrupo;Correo", "GROUP DELETE;IdentificadorGrupo")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void unexpectedErrorsDoNotPropagate() {
        CommandProcessor broken = new CommandProcessor(null, null, new MainController(TestPark.CLOCK));
        assertTrue(broken.process(ANA).startsWith("ERROR Unexpected error"));
    }
}
