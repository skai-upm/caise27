package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    UserService service;
    GroupService groups;
    TicketService tickets;
    CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        service = new UserService(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), service);
        tickets = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), service, groups);
        processor = new CommandProcessor(service, groups, tickets);
    }

    @Test
    void createDeleteUpdateRespondOk() {
        assertEquals("OK: User created", processor.process(CREATE_ANA));
        assertEquals("OK: User updated", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", service.findByEmail("ana@correo.com").getName());
        assertEquals("OK: User deleted", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, service.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"HELLO", "USER RENAME;ana@correo.com", "USER", "USER CREATE", "", "   ", ";;;",
            "USER CREATE;Ana;Garcia Lopez;12345678Z",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
            "USER DELETE;ana@correo.com", "USER UPDATE;ana@correo.com;Clave1!;NAME",
            "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"})
    void invalidCommandsAreRejectedSafely(String command) {
        String response = processor.process(command);
        assertTrue(response.startsWith("ERROR: "), response);
        assertEquals(0, service.getUsers().size());
    }

    @Test
    void dniFieldCannotBeUpdated() {
        processor.process(CREATE_ANA);
        String response = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(response.startsWith("ERROR: "), response);
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void validationErrorsReported() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456")
                .startsWith("ERROR: Invalid DNI"));
        processor.process(CREATE_ANA);
        assertTrue(processor.process(
                "USER CREATE;Otro;X;12345678Z;otro@correo.com;Clave1!;600000000;1234567890123456")
                .startsWith("ERROR: "));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!").startsWith("ERROR: "));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void helpListsDeliveryOneCommands() {
        String response = processor.process("HELP");
        java.util.List<String> lines = response.lines().map(String::trim).toList();
        assertEquals("OK: Available commands:", lines.get(0));
        for (String syntax : java.util.List.of(
                "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña",
                "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT")) {
            assertTrue(lines.contains(syntax), "HELP must list " + syntax);
        }
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void exitSetsFlag() {
        assertEquals(false, processor.isExitRequested());
        assertEquals("OK: Exiting", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @ParameterizedTest
    @ValueSource(strings = {"HELP;extra", "EXIT;now", "HELP;"})
    void generalCommandsRejectParameters(String command) {
        assertTrue(processor.process(command).startsWith("ERROR: "));
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void groupCommandsAndCascadeOnUserDelete() {
        processor.process(CREATE_ANA);
        assertEquals("OK: Group created", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("OK: User added to group", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR: "));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR: "));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR: "));
        assertEquals(1, groups.find("G01").getMembers().size());
        assertEquals("OK: User deleted", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, groups.find("G01").getMembers().size());
        assertEquals("OK: Group deleted", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR: "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP CREATE;G01", "GROUP CREATE;G01;A;B", "GROUP ADD;G01", "GROUP REMOVE;G01",
            "GROUP DELETE", "GROUP DELETE;G01;x", "GROUP LIST"})
    void malformedGroupCommandsRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR: "), command);
        assertEquals(0, groups.getGroups().size());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String line : java.util.List.of("GROUP CREATE;Identificador;Nombre", "GROUP ADD;Identificador;Correo",
                "GROUP REMOVE;Identificador;Correo", "GROUP DELETE;Identificador")) {
            assertTrue(help.contains(line), line);
        }
    }
}
