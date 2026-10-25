package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private CommandProcessor processor;
    private UserManager manager;

    @BeforeEach
    void setUp() throws Exception {
        ParkController controller = ParkController.load(dir);
        manager = controller.getUserManager();
        processor = new CommandProcessor(controller);
    }

    @Test
    void userCommands() {
        assertEquals("OK USER CREATED", processor.process(CREATE_ANA));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void wrongParameterCounts() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE").startsWith("ERROR"));
        assertTrue(processor.process(CREATE_ANA + ";extra").startsWith("ERROR"));
    }

    @Test
    void unknownCommand() {
        assertTrue(processor.process("FOO BAR").startsWith("ERROR"));
        assertTrue(processor.process("USER").startsWith("ERROR"));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
        for (String cmd : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;", "GROUP CREATE;", "GROUP ADD;",
                "GROUP REMOVE;", "GROUP DELETE;", "TICKET CREATE;", "TICKET DELETE;", "GROUPTICKET CREATE;",
                "GROUPTICKET DELETE;", "ATTRACTION CREATE;", "ATTRACTION DELETE;", "ATTRACTION ENTER;",
                "ATTRACTION EXIT;", "ATTRACTION STATUS;"}) {
            assertTrue(help.contains(cmd), cmd);
        }
    }

    @Test
    void exitSetsFlag() {
        assertFalse(processor.isExitRequested());
        processor.process("EXIT");
        assertTrue(processor.isExitRequested());
    }
}
