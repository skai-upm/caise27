package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager manager;
    private GroupManager groups;
    private TicketManager tickets;
    private AttractionManager attractions;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserStorage(dir.resolve("users.txt")));
        groups = new GroupManager(manager, new GroupStorage(dir.resolve("groups.txt")));
        tickets = new TicketManager(manager, groups,
            new TicketStorage(dir.resolve("tickets.txt"), dir.resolve("group_tickets.txt")));
        attractions = new AttractionManager(new AttractionStorage(dir.resolve("attractions.txt")));
        processor = new CommandProcessor(manager, groups, tickets, attractions);
    }

    @Test
    void createDeleteAndUpdateCommands() {
        assertTrue(processor.execute(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.getUsers().iterator().next().getName());
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com")
            .startsWith("OK"));
        assertTrue(processor.execute("USER DELETE;ana.nueva@correo.com;Clave1!").startsWith("OK"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void duplicateCreationIsReported() {
        processor.execute(CREATE_ANA);
        String response = processor.execute(
            "USER CREATE;Otra;X;12345678Z;otro@correo.com;Clave1!;600000000;1234567890123456");
        assertTrue(response.startsWith("ERROR") && response.contains("DNI"), response);
    }

    @Test
    void unknownCommandsAreReported() {
        assertTrue(processor.execute("USER LIST").startsWith("ERROR: Unknown command"));
        assertTrue(processor.execute("HELLO").startsWith("ERROR: Unknown command"));
        assertTrue(processor.execute("USER;CREATE").startsWith("ERROR"));
    }

    @Test
    void wrongParameterCountIsReported() {
        assertTrue(processor.execute("USER CREATE;Ana;Garcia").startsWith("ERROR: Expected 7"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com").startsWith("ERROR: Expected 2"));
        assertTrue(processor.execute(CREATE_ANA + ";extra").startsWith("ERROR: Expected 7"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void dniIsNotEditable() {
        processor.execute(CREATE_ANA);
        String response = processor.execute("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(response.startsWith("ERROR") && response.contains("cannot be updated"), response);
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.execute("   "));
        assertNull(processor.execute(""));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.execute("HELP");
        for (String usage : new String[] {"USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo", "GROUP DELETE;Id", "TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;Id", "ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION DELETE;Id",
            "ATTRACTION ENTER;Id;IdTicket", "ATTRACTION EXIT;Id;IdTicket", "ATTRACTION STATUS;Id", "SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Importe", "HELP", "EXIT"}) {
            assertTrue(help.contains(usage), usage);
        }
        assertFalse(processor.isExitRequested());
    }

    @Test
    void generalCommandsRejectParameters() {
        assertTrue(processor.execute("HELP;extra").startsWith("ERROR: Expected 0"));
        assertTrue(processor.execute("EXIT;now").startsWith("ERROR: Expected 0"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void exitSetsTheExitFlag() {
        assertTrue(processor.execute("EXIT").startsWith("OK"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void runStopsAtExitAndIgnoresBlankLines() throws IOException {
        String input = CREATE_ANA + "\n\n   \nHELP\nEXIT\n"
            + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains("ERROR"), output);
        assertTrue(output.contains("Available commands"));
        assertTrue(output.trim().endsWith("OK: Bye"), output);
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void systemDateStartsWithOperatingSystemDate() {
        LocalDate before = LocalDate.now();
        LocalDate date = new CommandProcessor(manager, groups, tickets, attractions).getCurrentDate();
        assertTrue(!date.isBefore(before) && !date.isAfter(LocalDate.now()));
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        assertEquals("OK: System date set to 2026-09-15", processor.execute("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
        assertTrue(processor.execute("SYSTEM DATE;2025-01-01").startsWith("OK"));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getCurrentDate());
    }

    @Test
    void invalidSystemDatesAreRejected() {
        CommandProcessor fixed = new CommandProcessor(manager, groups, tickets, attractions, LocalDate.of(2026, 1, 1));
        for (String value : new String[] {"15/09/2026", "2026-9-15", "2026-02-30", "hoy", ""}) {
            assertTrue(fixed.execute("SYSTEM DATE;" + value).startsWith("ERROR: Invalid date"), value);
        }
        assertTrue(fixed.execute("SYSTEM DATE").startsWith("ERROR: Expected 1"));
        assertTrue(fixed.execute("SYSTEM TIME;10:00").startsWith("ERROR: Unknown command"));
        assertEquals(LocalDate.of(2026, 1, 1), fixed.getCurrentDate());
    }

    @Test
    void groupCommands() {
        processor.execute(CREATE_ANA);
        assertTrue(processor.execute("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertTrue(processor.execute("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.execute("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.execute("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.execute("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.execute("GROUP DELETE;G01").startsWith("OK"));
        assertTrue(processor.execute("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.execute("GROUP CREATE;G02").startsWith("ERROR: Expected 2"));
        assertTrue(processor.execute("GROUP LIST").startsWith("ERROR: Unknown command"));
    }

    @Test
    void deletingAUserRemovesItFromGroups() {
        processor.execute(CREATE_ANA);
        processor.execute("GROUP CREATE;G01;Familia Garcia");
        processor.execute("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(groups.find("G01").getMembers().isEmpty());
    }

    @Test
    void ticketLifecycleFollowsTheSystemDate() {
        processor.execute(CREATE_ANA);
        processor.execute("SYSTEM DATE;2026-10-01");
        assertEquals("OK TICKET CREATED -> 40.00", processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.execute("SYSTEM DATE;2026-10-15");
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.execute("TICKET DELETE;T001").startsWith("ERROR"));
        processor.execute("SYSTEM DATE;2026-10-16");
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertEquals("12345678Z", ((IndividualTicket) tickets.find("T001")).getOwnerDni());
    }

    @Test
    void futureTicketCanBeDeleted() {
        processor.execute(CREATE_ANA);
        processor.execute("SYSTEM DATE;2026-10-01");
        processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK: Ticket T001 deleted", processor.execute("TICKET DELETE;T001"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void ticketCreationErrors() {
        processor.execute(CREATE_ANA);
        processor.execute("SYSTEM DATE;2026-10-01");
        assertTrue(processor.execute("TICKET CREATE;T001;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.execute("TICKET CREATE;T001;ana@correo.com;2026-13-01").startsWith("ERROR: Invalid date"));
        assertTrue(processor.execute("TICKET CREATE;T001;ana@correo.com;2026-09-30").startsWith("ERROR"));
        assertTrue(processor.execute("TICKET CREATE;T001;ana@correo.com").startsWith("ERROR: Expected 3"));
        assertTrue(processor.execute("TICKET UPDATE;T001").startsWith("ERROR: Unknown command"));
        assertTrue(tickets.getTickets().isEmpty());
    }

    @Test
    void systemPriceChangesNewTicketsOnly() {
        processor.execute(CREATE_ANA);
        processor.execute("SYSTEM DATE;2026-10-01");
        processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK: Ticket price set to 60.00", processor.execute("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.execute("TICKET CREATE;T002;ana@correo.com;2026-10-20"));
        assertEquals("40.00", tickets.find("T001").getPrice().toPlainString());
        assertEquals("OK: Ticket price set to 55.50", processor.execute("SYSTEM PRICE;55.5"));
    }

    @Test
    void invalidPricesAreRejected() {
        for (String value : new String[] {"-5", "0", "abc", "10.555", ""}) {
            assertTrue(processor.execute("SYSTEM PRICE;" + value).startsWith("ERROR: Invalid price"), value);
        }
        assertEquals(CommandProcessor.INITIAL_PRICE, processor.getCurrentPrice());
    }

    private void createFamilyG01() {
        processor.execute(CREATE_ANA);
        processor.execute("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.execute("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;600000000;1111222233334444");
        processor.execute("GROUP CREATE;G01;Familia Garcia");
        processor.execute("GROUP ADD;G01;ana@correo.com");
        processor.execute("GROUP ADD;G01;pedro@correo.com");
        processor.execute("SYSTEM DATE;2026-10-01");
    }

    @Test
    void groupTicketLocksGroupAndMembersUntilItsDate() {
        createFamilyG01();
        assertEquals("OK GROUP TICKET CREATED -> 80.00", processor.execute("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(processor.execute("GROUP ADD;G01;luis@correo.com").contains("cannot be modified"));
        assertTrue(processor.execute("GROUP REMOVE;G01;ana@correo.com").contains("cannot be modified"));
        assertTrue(processor.execute("GROUP DELETE;G01").contains("cannot be modified"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").contains("cannot be deleted"));
        assertTrue(processor.execute("TICKET DELETE;TG01-1").contains("GROUPTICKET DELETE"));

        processor.execute("SYSTEM DATE;2026-10-15");
        assertTrue(processor.execute("GROUP DELETE;G01").contains("cannot be modified"));
        assertTrue(processor.execute("GROUPTICKET DELETE;TG01").startsWith("ERROR"));

        processor.execute("SYSTEM DATE;2026-10-16");
        assertTrue(processor.execute("GROUP ADD;G01;luis@correo.com").startsWith("OK"));
        assertEquals(2, ((GroupTicket) tickets.find("TG01")).getComponents().size());
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(processor.execute("GROUP DELETE;G01").startsWith("OK"));
        assertEquals("G01", ((GroupTicket) tickets.find("TG01")).getGroupId());
    }

    @Test
    void futureGroupTicketCanBeDeleted() {
        createFamilyG01();
        processor.execute("SYSTEM PRICE;60.00");
        assertEquals("OK GROUP TICKET CREATED -> 120.00", processor.execute("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals("OK: Group ticket TG01 deleted", processor.execute("GROUPTICKET DELETE;TG01"));
        assertTrue(processor.execute("GROUP DELETE;G01").startsWith("OK"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void groupTicketErrors() {
        createFamilyG01();
        assertTrue(processor.execute("GROUPTICKET CREATE;TG01;G99;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.execute("GROUPTICKET CREATE;TG01;G01;2026-09-30").startsWith("ERROR"));
        assertTrue(processor.execute("GROUPTICKET CREATE;TG01;G01").startsWith("ERROR: Expected 3"));
        assertTrue(processor.execute("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(processor.execute("GROUPTICKET LIST").startsWith("ERROR: Unknown command"));
        assertTrue(tickets.getTickets().isEmpty());
    }

    @Test
    void attractionEntryExitAndStatus() {
        createFamilyG01();
        processor.execute("GROUP CREATE;G02;Amigos");
        processor.execute("GROUP ADD;G02;pedro@correo.com");
        processor.execute("GROUP ADD;G02;luis@correo.com");
        processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.execute("GROUPTICKET CREATE;TG01;G02;2026-10-15");
        assertTrue(processor.execute("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));

        assertTrue(processor.execute("ATTRACTION ENTER;A01;T001").startsWith("ERROR: The ticket is not valid"));
        processor.execute("SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered attraction A01", processor.execute("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK: 2 user(s) entered attraction A01", processor.execute("ATTRACTION ENTER;A01;TG01"));
        String status = processor.execute("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01") && status.contains("Tornado"), status);
        assertTrue(status.contains("Capacity: 20 | Present: 3 | Available: 17"), status);
        assertTrue(status.contains("ana@correo.com") && status.contains("pedro@correo.com")
            && status.contains("luis@correo.com"), status);

        assertEquals("OK: 1 user(s) left attraction A01", processor.execute("ATTRACTION EXIT;A01;T001"));
        status = processor.execute("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 2 | Available: 18") && !status.contains("ana@correo.com"), status);
        assertTrue(processor.execute("ATTRACTION EXIT;A01;T001").startsWith("ERROR"));
        assertTrue(processor.execute("ATTRACTION DELETE;A01").startsWith("ERROR"));
        processor.execute("ATTRACTION EXIT;A01;TG01");
        assertTrue(processor.execute("ATTRACTION STATUS;A01").contains("Users present: (none)"));
        assertTrue(processor.execute("ATTRACTION DELETE;A01").startsWith("OK"));
    }

    @Test
    void groupEntryRejectedWhenCapacityIsShort() {
        createFamilyG01();
        processor.execute("GROUP ADD;G01;luis@correo.com");
        processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        processor.execute("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        processor.execute("ATTRACTION CREATE;A01;Tornado;3");
        assertTrue(processor.execute("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(processor.execute("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(processor.execute("ATTRACTION ENTER;A01;TG01-2").startsWith("OK"));
        assertTrue(processor.execute("ATTRACTION STATUS;A01").contains("Present: 2 | Available: 1"));
    }

    @Test
    void attractionCommandErrors() {
        assertTrue(processor.execute("ATTRACTION CREATE;A01;Tornado;0").startsWith("ERROR: Invalid capacity"));
        assertTrue(processor.execute("ATTRACTION CREATE;A01;Tornado").startsWith("ERROR: Expected 3"));
        processor.execute("ATTRACTION CREATE;A01;Tornado;5");
        assertTrue(processor.execute("ATTRACTION ENTER;A01;T999").startsWith("ERROR: No ticket"));
        assertTrue(processor.execute("ATTRACTION STATUS;A99").startsWith("ERROR: No attraction"));
        assertTrue(processor.execute("ATTRACTION OPEN;A01").startsWith("ERROR: Unknown command"));
    }

    @Test
    void deletedUserLeavesAttractions() {
        processor.execute(CREATE_ANA);
        processor.execute("SYSTEM DATE;2026-10-15");
        processor.execute("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.execute("ATTRACTION CREATE;A01;Tornado;5");
        processor.execute("ATTRACTION ENTER;A01;T001");
        processor.execute("SYSTEM DATE;2026-10-16");
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(attractions.find("A01").getPresent().isEmpty());
    }
}
