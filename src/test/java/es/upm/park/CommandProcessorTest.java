package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    UserManager manager;
    GroupManager groups;
    TicketManager tickets;
    CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserFileStorage(dir.resolve("users.txt")));
        groups = new GroupManager(new GroupFileStorage(dir.resolve("groups.txt")), manager);
        tickets = new TicketManager(new TicketFileStorage(dir.resolve("tickets.txt")), manager);
        processor = new CommandProcessor(manager, groups, tickets);
    }

    @Test
    void createDeleteAndUpdateExamples() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByEmail("ana.nueva@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Nueva2#").startsWith("OK"));
        assertEquals(0, manager.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "hello", "USER RENAME;ana@correo.com", "USER CREATE;Ana;Garcia Lopez;12345678Z",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER DELETE;ana@correo.com", "USER DELETE;ana@correo.com;Clave1!;x", "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER", ";;;"})
    void invalidCommandsProduceErrors(String line) {
        assertTrue(processor.process(line).startsWith("ERROR"), line);
        assertEquals(0, manager.getUsers().size());
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(CREATE_ANA);
        String result = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(result.startsWith("ERROR"), result);
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR"));
    }

    @Test
    void invalidCreationNamesTheField() {
        String result = processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(result.startsWith("ERROR") && result.contains("DNI"), result);
    }

    @Test
    void lowercaseFieldNamesAreAccepted() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;phone;600000000").startsWith("OK"));
    }

    @Test
    void helpListsAllCommandsAndFields() {
        String help = processor.process("HELP");
        assertTrue(help.startsWith("OK"));
        for (String usage : new String[] {
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo", "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Precio", "HELP", "EXIT",
            "NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"}) {
            assertTrue(help.contains(usage), usage);
        }
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void helpAndExitRejectParameters() {
        assertTrue(processor.process("HELP;extra").startsWith("ERROR"));
        assertTrue(processor.process("EXIT;now").startsWith("ERROR"));
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void exitSetsFlag() {
        assertTrue(processor.process("EXIT").startsWith("OK"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void startsWithLocalDate() {
        LocalDate before = LocalDate.now();
        LocalDate date = new CommandProcessor(manager, groups, tickets).getCurrentDate();
        LocalDate after = LocalDate.now();
        assertTrue(!date.isBefore(before) && !date.isAfter(after));
    }

    @Test
    void systemDateCanMoveBackwardAndForward() {
        assertEquals("OK: system date set to 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2020-01-01").startsWith("OK"));
        assertEquals(LocalDate.of(2020, 1, 1), processor.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2031-12-31").startsWith("OK"));
        assertEquals(LocalDate.of(2031, 12, 31), processor.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2028-02-29").startsWith("OK"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;2026-02-30",
        "SYSTEM DATE;2026-13-01", "SYSTEM DATE;hoy", "SYSTEM DATE;", "SYSTEM DATE", "SYSTEM DATE;2026-09-15;x",
        "SYSTEM DATE;2027-02-29", "SYSTEM DATE; 2026-09-16"})
    void invalidDatesAreRejectedWithoutChange(String command) {
        processor.process("SYSTEM DATE;2026-09-15");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
    }

    @Test
    void injectedInitialDateIsUsed() {
        assertEquals(LocalDate.of(2026, 1, 1), new CommandProcessor(manager, groups, tickets, LocalDate.of(2026, 1, 1)).getCurrentDate());
    }

    @Test
    void groupCommands() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertEquals(0, groups.getGroups().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP CREATE;G01", "GROUP CREATE;G01;a;b", "GROUP ADD;G01", "GROUP REMOVE;G01",
        "GROUP DELETE", "GROUP DELETE;G01;x", "GROUP CREATE;;Familia", "GROUP CREATE;G02;"})
    void malformedGroupCommandsAreRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(0, groups.getGroups().size());
    }

    @Test
    void deletedUserLeavesItsGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertNotNull(groups.find("G01"));
        assertEquals(0, groups.find("G01").getMembers().size());
        GroupManager reloaded = new GroupManager(new GroupFileStorage(dir.resolve("groups.txt")), manager);
        assertEquals(0, reloaded.find("G01").getMembers().size());
    }

    @Test
    void groupMemberReflectsUserUpdate() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", groups.find("G01").getMembers().get(0).getName());
    }

    private void ticketSetup() {
        processor.process(CREATE_ANA);
        processor.process("SYSTEM DATE;2026-10-01");
    }

    @Test
    void ticketCreationReportsPriceAndReferencesUser() {
        ticketSetup();
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        Ticket t = tickets.find("T001");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertTrue(t.getOwner() == manager.findByEmail("ana@correo.com"));
        assertTrue(processor.process("TICKET CREATE;T002;ANA@correo.com;2026-10-01").startsWith("OK"), "today allowed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TICKET CREATE;T002;nadie@correo.com;2026-10-15", "TICKET CREATE;T002;ana@correo.com;2026-09-30",
        "TICKET CREATE;;ana@correo.com;2026-10-15", "TICKET CREATE;T002;ana@correo.com;15-10-2026",
        "TICKET CREATE;T002;ana@correo.com;2026-02-30", "TICKET CREATE;T002;ana@correo.com",
        "TICKET CREATE;T002;ana@correo.com;2026-10-15;x", "TICKET CREATE;T001;ana@correo.com;2026-10-20"})
    void invalidTicketCreationIsRejected(String command) {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(1, tickets.getTickets().size());
        assertEquals(LocalDate.of(2026, 10, 15), tickets.find("T001").getDate());
    }

    @Test
    void ticketDeletionOnlyForFutureDates() {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"), "today does not count");
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"), "past ticket");
        processor.process("SYSTEM DATE;2026-10-14");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("OK"));
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"), "unknown");
        assertTrue(processor.process("TICKET DELETE").startsWith("ERROR"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertNotNull(tickets.find("T001"), "historical ticket retained");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!").startsWith("ERROR"));
    }

    @Test
    void wrongPasswordStillReportedWithTickets() {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        String result = processor.process("USER DELETE;ana@correo.com;Mala1!");
        assertTrue(result.contains("invalid email or password"), result);
    }

    @Test
    void priceChangeAppliesToLaterTicketsOnly() {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process("SYSTEM PRICE;60.00").startsWith("OK"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertEquals(new BigDecimal("40.00"), tickets.find("T001").getPrice());
        processor.process("SYSTEM PRICE;55");
        assertEquals(new BigDecimal("55.00"), processor.getCurrentPrice());
        assertEquals(new BigDecimal("40.00"),
                new CommandProcessor(manager, groups, tickets).getCurrentPrice(), "new execution starts at 40.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;abc", "SYSTEM PRICE;-5", "SYSTEM PRICE;0", "SYSTEM PRICE;0.00",
        "SYSTEM PRICE;10.999", "SYSTEM PRICE;", "SYSTEM PRICE", "SYSTEM PRICE;1.", "SYSTEM PRICE;.5",
        "SYSTEM PRICE;6E1", "SYSTEM PRICE;60,00", "SYSTEM PRICE;60.00;x"})
    void invalidPriceIsRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(new BigDecimal("40.00"), processor.getCurrentPrice());
    }
}
