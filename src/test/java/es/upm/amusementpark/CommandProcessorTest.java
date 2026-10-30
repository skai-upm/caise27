package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        buffer = new ByteArrayOutputStream();
        Park park = new Park(new StorageLocation(dir));
        service = park.getUserService();
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createDeleteUpdateCommands() {
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", service.getUsers().get(0).getName());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(service.getUsers().isEmpty());
        assertEquals(3, output().split("OK:", -1).length - 1);
    }

    @Test
    void insufficientParametersReportError() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez"));
        assertTrue(processor.process("USER DELETE;ana@correo.com"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertTrue(service.getUsers().isEmpty());
        assertEquals(3, output().split("ERROR:", -1).length - 1);
    }

    @Test
    void dniFieldCannotBeUpdated() {
        processor.process(CREATE_ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertEquals("12345678Z", service.getUsers().get(0).getDni());
        assertTrue(output().contains("ERROR:"));
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String help = output();
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        for (String command : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Precio",
            "GROUP CREATE;IdGrupo;Nombre", "GROUP ADD;IdGrupo;Correo", "GROUP REMOVE;IdGrupo;Correo",
            "GROUP DELETE;IdGrupo", "TICKET CREATE;IdTicket;Correo;Fecha", "TICKET DELETE;IdTicket",
            "GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha", "GROUPTICKET DELETE;IdTicket",
            "ATTRACTION CREATE;IdAtraccion;Nombre;Aforo", "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION EXIT;IdAtraccion;IdTicket", "ATTRACTION STATUS;IdAtraccion"}) {
            assertTrue(help.contains(command), command);
        }
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertEquals(false, processor.process("EXIT"));
        assertEquals(false, processor.process("  EXIT  "));
    }

    @Test
    void unknownCommandDoesNotStopProcessing() {
        assertTrue(processor.process("FOO BAR"));
        assertTrue(processor.process(""));
        assertTrue(output().contains("ERROR: unknown command"));
    }
}
