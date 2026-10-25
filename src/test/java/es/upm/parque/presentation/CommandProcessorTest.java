package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.controller.UserController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserController manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        TestContext ctx = TestContext.start(dir);
        manager = ctx.users();
        processor = ctx.processor();
    }

    @Test
    void createDeleteAndUpdateThroughCommands() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void reportsWrongParameterCountAndUnknownCommands() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("USER UPDATE;a;b;NAME").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("FOO BAR;1").startsWith("ERROR Unknown command"));
        assertTrue(processor.process("USER").startsWith("ERROR Unknown command"));
        assertTrue(manager.getUsers().isEmpty());
        assertFalse(processor.isFinished());
    }

    @Test
    void rejectsDniAndUnknownFieldsOnUpdate() {
        processor.process(CREATE_ANA);
        assertEquals("ERROR The DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR Unknown field"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        for (String command : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Precio", "GROUP CREATE;Id;Nombre",
                "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo", "GROUP DELETE;Id", "TICKET CREATE;Id;Correo;yyyy-MM-dd",
                "TICKET DELETE;Id", "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;Id",
                "ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION DELETE;Id", "ATTRACTION ENTER;IdAtraccion;IdEntrada",
                "ATTRACTION EXIT;IdAtraccion;IdEntrada", "ATTRACTION STATUS;IdAtraccion", "HELP", "EXIT", "SPEEDPASS CREATE;IdEntrada;Cantidad",
                "SPEEDPASS DELETE;IdPase", "SPECIALPASS CREATE;IdEntrada", "SPECIALPASS DELETE;IdPase",
                "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo", "ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED",
                "INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo", "HELP EXTENDED"}) {
            assertTrue(help.contains(command), command);
        }
    }

    @Test
    void reportsWrongParameterCountForNewCommands() {
        for (String line : new String[] {"SYSTEM DATE", "SYSTEM PRICE;1;2", "GROUP CREATE;G01", "GROUP ADD;G01",
                "GROUP REMOVE;G01", "GROUP DELETE", "TICKET CREATE;T1;a@b.com", "TICKET DELETE",
                "GROUPTICKET CREATE;TG;G", "GROUPTICKET DELETE", "ATTRACTION CREATE;A;B", "ATTRACTION CREATE;A;B;1;GROUP;X", "ATTRACTION DELETE",
                "ATTRACTION ENTER;A", "ATTRACTION ENTER;A;T;SPEED;X", "ATTRACTION EXIT;A", "ATTRACTION STATUS",
                "SPEEDPASS CREATE;T", "SPEEDPASS DELETE", "SPECIALPASS CREATE", "SPECIALPASS DELETE;A;B",
                "INVOICE CREATE;USER", "HELP;X", "HELP EXTENDED;X", "EXIT;X"}) {
            assertTrue(processor.process(line).startsWith("ERROR Wrong number"), line);
        }
    }

    @Test
    void exitFinishesProcessing() {
        assertTrue(processor.process("EXIT").startsWith("OK"));
        assertTrue(processor.isFinished());
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
