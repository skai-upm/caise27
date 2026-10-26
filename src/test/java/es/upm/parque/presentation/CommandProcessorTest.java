package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private UserController manager;
    private GroupController groups;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserController(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupController(new GroupRepository(dir.resolve("groups.txt")), manager);
        SystemDate date = new SystemDate();
        TicketController tickets = new TicketController(
                new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")), manager, groups, date);
        processor = new CommandProcessor(manager, groups, tickets,
                new AttractionController(new AttractionRepository(dir.resolve("attractions.txt")), tickets, date), date);
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
                "SYSTEM DATE;yyyy-MM-dd", "GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo",
                "GROUP DELETE;Id", "TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id", "SYSTEM PRICE;Importe",
                "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;Id",
                "ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION DELETE;Id", "ATTRACTION ENTER;IdAtraccion;IdEntrada",
                "ATTRACTION EXIT;IdAtraccion;IdEntrada", "ATTRACTION STATUS;IdAtraccion"}) {
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

    @Test
    void groupCommands() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP JOIN;G01").startsWith("ERROR"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertEquals(0, groups.findById("G01").getMembers().size());
        GroupController reloaded = new GroupController(new GroupRepository(dir.resolve("groups.txt")),
                new UserController(new UserRepository(dir.resolve("users.txt"))));
        assertEquals(0, reloaded.findById("G01").getMembers().size());
    }

    @Test
    void ticketCommandsFollowTheSystemDateAndPrice() {
        processor.process(CREATE_ANA);
        processor.process("SYSTEM DATE;2026-10-01");
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;ana@correo.com;2026-13-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK SYSTEM PRICE -> 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertTrue(processor.process("SYSTEM PRICE;abc").startsWith("ERROR"));
        assertTrue(processor.process("SYSTEM PRICE;-5").startsWith("ERROR"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));

        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("OK"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void groupTicketCommands() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        assertEquals("OK GROUP TICKET CREATED -> 108.00",
                processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG02;G99;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG02;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;TG01-1").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("OK"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
    }

    @Test
    void attractionStatusReflectsMixedEntriesAndExits() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Otra;cero").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Otra;0").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION DELETE;A01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION EXIT;A01;T001").startsWith("OK"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01") && status.contains("Tornado") && status.contains("Capacity: 20")
                && status.contains("Present: 2") && status.contains("Available: 18") && status.contains("Pedro")
                && status.contains("Luis") && !status.contains("Ana"), status);
        assertTrue(processor.process("ATTRACTION STATUS").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T999").startsWith("ERROR"));
    }
}
