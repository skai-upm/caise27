package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.Park;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.business.model.SystemDate;

class CommandProcessorTest {

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        Park park = new Park(dir, LocalDate.of(2026, 1, 1));
        users = park.users;
        groups = park.groups;
        tickets = park.tickets;
        processor = park.processor;
    }

    @Test
    void createsUser() {
        assertEquals("OK USER CREATED", processor.process(Fixtures.CREATE_ANA));
        assertEquals("ERROR DUPLICATE DNI", processor.process(Fixtures.CREATE_ANA));
    }

    @Test
    void operationIgnoresCaseAndParametersAreTrimmed() {
        assertEquals("OK USER CREATED", processor.process(
                " user create ; Ana ; Garcia Lopez ; 12345678Z ; ana@correo.com ; Clave1! ; 612345678 ; 1234567890123456"));
        assertEquals("Ana", users.findByEmail("ana@correo.com").getName());
    }

    @Test
    void updatesAndDeletesUser() {
        processor.process(Fixtures.CREATE_ANA);
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
        assertEquals("ERROR INVALID CREDENTIALS", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void rejectsUnknownCommands() {
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process("USER FLY;x"));
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process("HOLA"));
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process(";;;"));
    }

    @Test
    void rejectsWrongParameterCounts() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER CREATE;Ana;Garcia Lopez"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process(Fixtures.CREATE_ANA + ";extra"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER DELETE;ana@correo.com"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertEquals(0, users.count());
    }

    @Test
    void unexpectedFailuresBecomeErrors() {
        CommandProcessor broken = new CommandProcessor(null, null, null, null, null, new SystemDate(LocalDate.of(2026, 1, 1)));
        assertEquals(CommandProcessor.UNEXPECTED_ERROR, broken.process(Fixtures.CREATE_ANA));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.lines().anyMatch("HELP"::equals));
        assertTrue(help.lines().anyMatch("EXIT"::equals));
        assertFalse(processor.isFinished());
    }

    @Test
    void everyDocumentedCommandIsRecognized() {
        for (String syntax : CommandProcessor.COMMAND_SYNTAX) {
            assertNotEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process(syntax));
        }
    }

    @Test
    void exitFinishesProcessing() {
        assertFalse(processor.isFinished());
        assertEquals("OK EXIT", processor.process(" exit "));
        assertTrue(processor.isFinished());
    }

    @Test
    void helpAndExitTakeNoParameters() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("HELP;x"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("EXIT;x"));
        assertFalse(processor.isFinished());
    }

    @Test
    void setsSystemDateBackwardsAndForwards() {
        assertEquals("OK SYSTEM DATE -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
        assertEquals("OK SYSTEM DATE -> 2025-01-01", processor.process("system date; 2025-01-01 "));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void rejectsInvalidSystemDate() {
        assertEquals(CommandProcessor.INVALID_DATE, processor.process("SYSTEM DATE;15/09/2026"));
        assertEquals(CommandProcessor.INVALID_DATE, processor.process("SYSTEM DATE;2026-02-30"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("SYSTEM DATE"));
        assertEquals(LocalDate.of(2026, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").lines().anyMatch("SYSTEM DATE;yyyy-MM-dd"::equals));
    }

    @Test
    void runsGroupCommands() {
        processor.process(Fixtures.CREATE_ANA);
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("ERROR USER ALREADY IN GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("ERROR USER NOT FOUND", processor.process("GROUP ADD;G01;nadie@correo.com"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("group remove;G01;ana@correo.com"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertEquals("ERROR GROUP NOT FOUND", processor.process("GROUP DELETE;G01"));
    }

    @Test
    void groupCommandsCheckParameterCounts() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP CREATE;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP ADD;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP REMOVE;G01;a;b"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP DELETE"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(Fixtures.CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("ERROR INVALID CREDENTIALS", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals(1, groups.find("G01").getMembers().size());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(groups.find("G01").getMembers().isEmpty());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String command : List.of("GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo",
                "GROUP DELETE;Id")) {
            assertTrue(help.lines().anyMatch(command::equals), command);
        }
    }

    @Test
    void runsTicketAndPriceCommands() {
        processor.process(Fixtures.CREATE_ANA);
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertEquals("OK SYSTEM PRICE -> 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("ticket create;T002;ana@correo.com;2026-10-16"));
        assertEquals("OK TICKET DELETED", processor.process("TICKET DELETE;T002"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("TICKET CREATE;T003;ana@correo.com"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("TICKET DELETE"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("SYSTEM PRICE"));
        assertTrue(tickets.hasActiveTickets(users.findByEmail("ana@correo.com")));
    }

    @Test
    void currentTicketsBlockUserDeletionUntilTheyArePast() {
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process(Fixtures.CREATE_ANA);
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("ERROR INVALID CREDENTIALS", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals(CommandProcessor.ACTIVE_TICKETS, processor.process("USER DELETE;ana@correo.com;Clave1!"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertEquals(CommandProcessor.ACTIVE_TICKETS, processor.process("USER DELETE;ana@correo.com;Clave1!"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("12345678Z", tickets.findIndividual("T001").getHolder().getDni());
    }

    @Test
    void helpListsTicketCommands() {
        String help = processor.process("HELP");
        for (String command : List.of("TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id", "SYSTEM PRICE;Precio")) {
            assertTrue(help.lines().anyMatch(command::equals), command);
        }
    }

    private void familyWithTicketFor(String date) {
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process(Fixtures.CREATE_ANA);
        processor.process(Fixtures.CREATE_PEDRO);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        assertEquals("OK GROUP TICKET CREATED -> 80.00", processor.process("GROUPTICKET CREATE;TG01;G01;" + date));
    }

    @Test
    void groupIsLockedWhileItsTicketIsCurrent() {
        familyWithTicketFor("2026-10-15");
        users.create(Fixtures.numbered(1));
        assertEquals(CommandProcessor.GROUP_LOCKED, processor.process("GROUP ADD;G01;u1@correo.com"));
        assertEquals(CommandProcessor.GROUP_LOCKED, processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(CommandProcessor.GROUP_LOCKED, processor.process("GROUP DELETE;G01"));
        assertEquals("ERROR GROUP NOT FOUND", processor.process("GROUP DELETE;G99"));
        assertEquals(CommandProcessor.ACTIVE_TICKETS, processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertEquals(CommandProcessor.GROUP_LOCKED, processor.process("GROUP ADD;G01;u1@correo.com"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;u1@correo.com"));
        assertEquals(2, tickets.findGroupTicket("TG01").getTickets().size());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#"));
    }

    @Test
    void groupTicketRefundUnlocksGroup() {
        familyWithTicketFor("2026-10-15");
        assertEquals("ERROR TICKET NOT FOUND", processor.process("TICKET DELETE;TG01-1"));
        assertEquals("OK GROUP TICKET DELETED", processor.process("GROUPTICKET DELETE;TG01"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUPTICKET CREATE;TG02;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUPTICKET DELETE"));
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.lines().anyMatch("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd"::equals));
        assertTrue(help.lines().anyMatch("GROUPTICKET DELETE;Id"::equals));
    }

    @Test
    void runsAttractionCommands() {
        familyWithTicketFor("2026-10-15");
        processor.process("SYSTEM DATE;2026-10-15");
        assertEquals("OK ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Tornado;20"));
        assertEquals("OK ENTERED 2 USERS", processor.process("ATTRACTION ENTER;A01;TG01"));
        assertTrue(processor.process("attraction status;A01").contains("Capacity: 20 | Present: 2 | Available: 18"));
        assertEquals("OK EXITED 2 USERS", processor.process("ATTRACTION EXIT;A01;TG01"));
        assertEquals("OK ATTRACTION DELETED", processor.process("ATTRACTION DELETE;A01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("ATTRACTION CREATE;A01;Tornado"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("ATTRACTION DELETE"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("ATTRACTION ENTER;A01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("ATTRACTION EXIT;A01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("ATTRACTION STATUS"));
    }

    @Test
    void helpListsAttractionCommands() {
        String help = processor.process("HELP");
        for (String command : List.of("ATTRACTION CREATE;Id;Nombre;Aforo", "ATTRACTION DELETE;Id",
                "ATTRACTION ENTER;IdAtraccion;IdEntrada", "ATTRACTION EXIT;IdAtraccion;IdEntrada",
                "ATTRACTION STATUS;IdAtraccion")) {
            assertTrue(help.lines().anyMatch(command::equals), command);
        }
    }

    @Test
    void runsInvoiceCommands() {
        familyWithTicketFor("2026-10-15");
        assertTrue(processor.process("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 80.00"));
        assertTrue(processor.process("invoice create;user;ana@correo.com").contains("No purchases"));
        assertEquals("ERROR INVALID INVOICE TYPE", processor.process("INVOICE CREATE;FAMILY;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("INVOICE CREATE;USER"));
    }

    @Test
    void helpListsInvoiceCommands() {
        String help = processor.process("HELP");
        assertTrue(help.lines().anyMatch("INVOICE CREATE;USER;Correo"::equals));
        assertTrue(help.lines().anyMatch("INVOICE CREATE;GROUP;IdGrupo"::equals));
    }
}
