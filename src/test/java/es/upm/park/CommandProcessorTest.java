package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private UserManager manager;
    private GroupManager groups;
    private TicketManager tickets;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserFileStore(dir.resolve("users.txt")));
        groups = new GroupManager(new GroupFileStore(dir.resolve("groups.txt")), manager);
        tickets = new TicketManager(new TicketFileStore(dir.resolve("tickets.txt")), new GroupTicketFileStore(dir.resolve("grouptickets.txt")), manager, groups);
        processor = new CommandProcessor(manager, groups, tickets);
    }

    @Test
    void createsUserFromExampleCommand() {
        Result r = processor.process(CREATE_ANA);
        assertTrue(r.success(), r.message());
        assertTrue(r.toString().startsWith("OK: "));
        assertEquals("Garcia Lopez", manager.getUsers().get(0).getSurnames());
    }

    @Test
    void runsSourceUpdateExamplesInOrder() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").success());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").success());
        User u = manager.getUsers().get(0);
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("9999888877776666", u.getCardNumber());
    }

    @Test
    void deleteExample() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void dniIsNotAnEditableField() {
        processor.process(CREATE_ANA);
        Result r = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertFalse(r.success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "FOO",
        "USER",
        "USER LIST",
        "USER CREATE;Ana",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;a@b.com",
        "USER DELETE;ana@correo.com;",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        ";;;",
        "   "
    })
    void malformedCommandsAreRejectedSafely(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertTrue(r.toString().startsWith("ERROR: "));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void nullCommandIsRejected() {
        assertFalse(processor.process(null).success());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertTrue(processor.process("  USER  CREATE ; Ana ;Garcia Lopez; 12345678Z ;ana@correo.com;Clave1!;612345678;1234567890123456 ").success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @Test
    void helpListsAllCommandsAndChangesNothing() {
        processor.process(CREATE_ANA);
        Result r = processor.process("HELP");
        assertTrue(r.success());
        assertFalse(r.terminate());
        for (String syntax : new String[] {
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT"}) {
            assertTrue(r.message().contains(syntax), syntax);
        }
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void exitTerminates() {
        Result r = processor.process("EXIT");
        assertTrue(r.success());
        assertTrue(r.terminate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"HELP;extra", "EXIT;now", "HELP ME", "help", "exit"})
    void generalCommandsRejectExtras(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertFalse(r.terminate());
    }

    @Test
    void userCommandsDoNotTerminate() {
        assertFalse(processor.process(CREATE_ANA).terminate());
        assertFalse(processor.process("FOO").terminate());
    }

    // ---- system date ----

    private static final LocalDate START = LocalDate.of(2026, 9, 15);

    private CommandProcessor processorAt(LocalDate date) {
        return new CommandProcessor(manager, groups, tickets, date);
    }

    @Test
    void defaultControllerStartsWithLocalDate() {
        LocalDate before = LocalDate.now();
        LocalDate date = processor.getCurrentDate();
        LocalDate after = LocalDate.now();
        assertTrue(!date.isBefore(before) && !date.isAfter(after));
    }

    @Test
    void injectedInitialDateIsReported() {
        assertEquals(START, processorAt(START).getCurrentDate());
    }

    @Test
    void systemDateSetsCurrentDate() {
        CommandProcessor p = processorAt(LocalDate.of(2025, 1, 1));
        Result r = p.process("SYSTEM DATE;2026-09-15");
        assertTrue(r.success(), r.message());
        assertEquals(START, p.getCurrentDate());
    }

    @Test
    void dateCanMoveBackwardAndForward() {
        CommandProcessor p = processorAt(START);
        assertTrue(p.process("SYSTEM DATE;2020-01-01").success());
        assertEquals(LocalDate.of(2020, 1, 1), p.getCurrentDate());
        assertTrue(p.process("SYSTEM DATE;2030-12-31").success());
        assertEquals(LocalDate.of(2030, 12, 31), p.getCurrentDate());
    }

    @Test
    void leapDayAccepted() {
        CommandProcessor p = processorAt(START);
        assertTrue(p.process("SYSTEM DATE;2028-02-29").success());
        assertEquals(LocalDate.of(2028, 2, 29), p.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026/09/15", "SYSTEM DATE;2026-9-15",
        "SYSTEM DATE;2026-02-30", "SYSTEM DATE;2027-02-29", "SYSTEM DATE;hoy",
        "SYSTEM DATE;2026-13-01", "SYSTEM DATE;+2026-09-15", "SYSTEM DATE;20260915",
        "SYSTEM DATE", "SYSTEM DATE;", "SYSTEM DATE;2026-09-15;x", "SYSTEM;2026-09-15",
        "SYSTEM TIME;2026-09-15"})
    void invalidDatesLeaveDateUnchanged(String command) {
        CommandProcessor p = processorAt(START);
        Result r = p.process(command);
        assertFalse(r.success());
        assertFalse(r.terminate());
        assertEquals(START, p.getCurrentDate());
    }

    @Test
    void helpDoesNotChangeDate() {
        CommandProcessor p = processorAt(START);
        p.process("HELP");
        assertEquals(START, p.getCurrentDate());
    }

    // ---- groups ----

    @Test
    void sourceGroupCommands() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").success());
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").success());
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").success());
        assertTrue(processor.process("GROUP DELETE;G01").success());
        assertTrue(groups.getGroups().isEmpty());
        assertEquals(1, manager.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP CREATE;G01", "GROUP CREATE;;Familia", "GROUP CREATE;G01;Familia;x",
        "GROUP ADD;G01", "GROUP REMOVE;G01;", "GROUP DELETE", "GROUP DELETE;G01;x", "GROUP LIST", "GROUP"})
    void malformedGroupCommandsRejected(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertTrue(groups.getGroups().isEmpty());
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void failedUserDeleteKeepsMembership() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertFalse(processor.process("USER DELETE;ana@correo.com;Mala1!").success());
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
    }

    // ---- tickets and price ----

    @Test
    void sourceTicketExample() {
        CommandProcessor p = processorAt(START);
        p.process(CREATE_ANA);
        assertTrue(p.process("SYSTEM DATE;2026-10-01").success());
        assertEquals("OK TICKET CREATED -> 40.00", p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15").toString());
        assertFalse(p.process("USER DELETE;ana@correo.com;Clave1!").success());
        p.process("SYSTEM DATE;2026-10-15");
        assertFalse(p.process("USER DELETE;ana@correo.com;Clave1!").success(), "ticket for today still blocks");
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(tickets.find("T001").isPresent(), "history retained");
    }

    @Test
    void wrongPasswordReportedBeforeTicketRule() {
        CommandProcessor p = processorAt(LocalDate.of(2026, 10, 1));
        p.process(CREATE_ANA);
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        Result r = p.process("USER DELETE;ana@correo.com;Mala1!");
        assertEquals("ERROR: Unknown email or wrong password.", r.toString());
    }

    @Test
    void priceStartsAt40AndAppliesToLaterTicketsOnly() {
        CommandProcessor p = processorAt(LocalDate.of(2026, 10, 1));
        p.process(CREATE_ANA);
        assertEquals(new BigDecimal("40.00"), p.getCurrentPrice());
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(p.process("SYSTEM PRICE;60.00").success());
        assertEquals("OK TICKET CREATED -> 60.00", p.process("TICKET CREATE;T002;ana@correo.com;2026-10-20").toString());
        assertEquals(new BigDecimal("40.00"), tickets.find("T001").orElseThrow().getPrice());
        assertTrue(p.process("SYSTEM PRICE;55").success());
        assertEquals("OK TICKET CREATED -> 55.00", p.process("TICKET CREATE;T003;ana@correo.com;2026-10-20").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;-5", "SYSTEM PRICE;abc", "SYSTEM PRICE;60,00", "SYSTEM PRICE;1.234",
        "SYSTEM PRICE;1e3", "SYSTEM PRICE", "SYSTEM PRICE;", "SYSTEM PRICE;.5", "SYSTEM PRICE;5.", "SYSTEM PRICE;1;2"})
    void invalidPricesRejected(String command) {
        CommandProcessor p = processorAt(START);
        assertFalse(p.process(command).success());
        assertEquals(new BigDecimal("40.00"), p.getCurrentPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TICKET CREATE;T005;ana@correo.com", "TICKET CREATE;T005;ana@correo.com;2026-02-30",
        "TICKET CREATE;T005;ana@correo.com;15-10-2026", "TICKET CREATE;;ana@correo.com;2026-10-15",
        "TICKET DELETE", "TICKET DELETE;T001;x", "TICKET LIST", "TICKET CREATE;T005;nadie@correo.com;2026-10-15",
        "TICKET CREATE;T005;ana@correo.com;2026-09-14"})
    void malformedTicketCommandsRejected(String command) {
        CommandProcessor p = processorAt(START);
        p.process(CREATE_ANA);
        assertFalse(p.process(command).success());
        assertTrue(tickets.getTickets().isEmpty());
    }

    @Test
    void ticketDeleteUsesSystemDate() {
        CommandProcessor p = processorAt(LocalDate.of(2026, 10, 1));
        p.process(CREATE_ANA);
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("SYSTEM DATE;2026-10-15");
        assertFalse(p.process("TICKET DELETE;T001").success());
        p.process("SYSTEM DATE;2026-10-14");
        assertTrue(p.process("TICKET DELETE;T001").success());
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").success());
    }

    // ---- group tickets ----

    private CommandProcessor groupOf(int members) {
        CommandProcessor p = processorAt(LocalDate.of(2026, 10, 1));
        p.process("GROUP CREATE;G01;Familia Garcia");
        for (int i = 0; i < members; i++) {
            String dni = String.format("%08d", i);
            dni += "TRWAGMYFPDXBNJZSQVHLCKE".charAt(i % 23);
            String email = "u" + i + "@correo.com";
            assertTrue(p.process("USER CREATE;U" + i + ";S;" + dni + ";" + email + ";Clave1!;6;1234567890123456").success());
            assertTrue(p.process("GROUP ADD;G01;" + email).success());
        }
        return p;
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void groupTicketPricesByBracket(int members, String total) {
        CommandProcessor p = groupOf(members);
        assertEquals("OK GROUP TICKET CREATED -> " + total, p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15").toString());
        assertEquals(members, tickets.findGroupTicket("TG01").orElseThrow().getTickets().size());
    }

    @Test
    void lockedGroupAndMembersUntilDatePasses() {
        CommandProcessor p = groupOf(2);
        p.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;6;1111222233334444");
        assertTrue(p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15").success());
        assertFalse(p.process("GROUP ADD;G01;luis@correo.com").success());
        assertFalse(p.process("GROUP REMOVE;G01;u0@correo.com").success());
        assertFalse(p.process("GROUP DELETE;G01").success());
        assertFalse(p.process("USER DELETE;u0@correo.com;Clave1!").success());
        p.process("SYSTEM DATE;2026-10-15");
        assertFalse(p.process("GROUP ADD;G01;luis@correo.com").success(), "ticket for today still locks");
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("GROUP ADD;G01;luis@correo.com").success());
        assertEquals(2, tickets.findGroupTicket("TG01").orElseThrow().getTickets().size(), "composition frozen");
        assertTrue(p.process("USER DELETE;u0@correo.com;Clave1!").success());
        assertTrue(p.process("GROUP DELETE;G01").success());
    }

    @Test
    void groupTicketDeleteRemovesConstituents() {
        CommandProcessor p = groupOf(2);
        p.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        assertFalse(p.process("TICKET DELETE;TG001-00000000T").success());
        assertTrue(p.process("GROUPTICKET DELETE;TG001").success());
        assertTrue(p.process("GROUP ADD;G01;u0@correo.com").message().contains("already"), "group unlocked");
        assertTrue(p.process("USER DELETE;u0@correo.com;Clave1!").success());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUPTICKET CREATE;TG01;G99;2026-10-15", "GROUPTICKET CREATE;TG01;G01",
        "GROUPTICKET CREATE;TG01;G01;2026-02-30", "GROUPTICKET CREATE;TG01;G01;2026-09-30",
        "GROUPTICKET DELETE", "GROUPTICKET DELETE;TG99", "GROUPTICKET LIST"})
    void malformedGroupTicketCommands(String command) {
        CommandProcessor p = groupOf(1);
        assertFalse(p.process(command).success());
        assertTrue(tickets.getGroupTickets().isEmpty());
    }
}
