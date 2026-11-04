package es.upm.park.presentation;

import es.upm.park.business.service.BCryptPasswordHasher;
import es.upm.park.business.service.PasswordHasher;
import es.upm.park.business.service.ParkSession;
import es.upm.park.business.service.InvoiceDirector;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.service.AttractionManager;
import es.upm.park.business.service.GroupManager;
import es.upm.park.business.service.ParkController;
import es.upm.park.business.service.PassManager;
import es.upm.park.business.service.TicketManager;
import es.upm.park.business.service.UserManager;
import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.PassFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    UserManager manager;
    GroupManager groups;
    TicketManager tickets;
    AttractionManager attractions;
    PassManager passes;
    ParkController park;
    CommandProcessor processor;

    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @BeforeEach
    void setUp() {
        ParkSession.resetControllers();
        manager = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")), FAST_HASHER);
        groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), manager);
        tickets = TicketManager.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), manager, groups);
        attractions = AttractionManager.initialize(new AttractionFileStorage(dir.resolve("attractions.txt")));
        passes = PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
        park = ParkController.initialize(manager, groups, tickets, attractions, passes, new InvoiceDirector());
        processor = App.newCommandProcessor(park);
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
            "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;IdTicket",
            "ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
            "ATTRACTION ENTER;IdAtraccion;IdTicket", "ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED",
            "ATTRACTION EXIT;IdAtraccion;IdTicket", "ATTRACTION STATUS;IdAtraccion",
            "INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo", "HELP EXTENDED",
            "SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad", "SPEEDPASS DELETE;IdPase",
            "SPECIALPASS CREATE;IdPase;IdTicket", "SPECIALPASS DELETE;IdPase",
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
        ParkController.reset();
        LocalDate date = ParkController.initialize(manager, groups, tickets, attractions, passes, new InvoiceDirector()).getCurrentDate();
        LocalDate after = LocalDate.now();
        assertTrue(!date.isBefore(before) && !date.isAfter(after));
    }

    @Test
    void systemDateCanMoveBackwardAndForward() {
        assertEquals("OK: system date set to 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), park.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2020-01-01").startsWith("OK"));
        assertEquals(LocalDate.of(2020, 1, 1), park.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2031-12-31").startsWith("OK"));
        assertEquals(LocalDate.of(2031, 12, 31), park.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2028-02-29").startsWith("OK"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;2026-02-30",
        "SYSTEM DATE;2026-13-01", "SYSTEM DATE;hoy", "SYSTEM DATE;", "SYSTEM DATE", "SYSTEM DATE;2026-09-15;x",
        "SYSTEM DATE;2027-02-29", "SYSTEM DATE; 2026-09-16"})
    void invalidDatesAreRejectedWithoutChange(String command) {
        processor.process("SYSTEM DATE;2026-09-15");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(LocalDate.of(2026, 9, 15), park.getCurrentDate());
    }

    @Test
    void injectedInitialDateIsUsed() {
        ParkController.reset();
        assertEquals(LocalDate.of(2026, 1, 1), ParkController.initialize(manager, groups, tickets, attractions, passes, new InvoiceDirector(), LocalDate.of(2026, 1, 1)).getCurrentDate());
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
        GroupManager.reset();
        GroupManager reloaded = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), manager);
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
        IndividualTicket t = tickets.find("T001");
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
        assertEquals(new BigDecimal("55.00"), park.getCurrentPrice());
        ParkController.reset();
        assertEquals(new BigDecimal("40.00"),
                ParkController.initialize(manager, groups, tickets, attractions, passes, new InvoiceDirector()).getCurrentPrice(), "new execution starts at 40.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;abc", "SYSTEM PRICE;-5", "SYSTEM PRICE;0", "SYSTEM PRICE;0.00",
        "SYSTEM PRICE;10.999", "SYSTEM PRICE;", "SYSTEM PRICE", "SYSTEM PRICE;1.", "SYSTEM PRICE;.5",
        "SYSTEM PRICE;6E1", "SYSTEM PRICE;60,00", "SYSTEM PRICE;60.00;x"})
    void invalidPriceIsRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    private void groupOf(int members) {
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        for (int i = 0; i < members; i++) {
            int number = 10000000 + i;
            String dni = number + String.valueOf("TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23));
            String email = "u" + i + "@correo.com";
            processor.process("USER CREATE;U" + i + ";S;" + dni + ";" + email + ";Clave1!;600;1234567890123456");
            processor.process("GROUP ADD;G01;" + email);
        }
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void groupTicketPurchaseAppliesBrackets(int members, String total) {
        groupOf(members);
        assertEquals("OK GROUP TICKET CREATED -> " + total, processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        GroupTicket t = tickets.findGroupTicket("TG01");
        assertEquals(members, t.getConstituents().size());
        assertTrue(t.getConstituents().get(0).getOwner() == manager.findByEmail("u0@correo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUPTICKET CREATE;TG02;G99;2026-10-15", "GROUPTICKET CREATE;TG02;G01;2026-09-30",
        "GROUPTICKET CREATE;;G01;2026-10-15", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
        "GROUPTICKET CREATE;T001;G01;2026-10-15", "GROUPTICKET CREATE;TG02;G01;2026-13-01",
        "GROUPTICKET CREATE;TG02;G01", "GROUPTICKET CREATE;TG02;G01;2026-10-15;x"})
    void invalidGroupTicketPurchaseRejected(String command) {
        groupOf(2);
        processor.process("TICKET CREATE;T001;u0@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(1, tickets.getGroupTickets().size());
        assertEquals(1, tickets.getTickets().size());
    }

    @Test
    void emptyGroupCannotBuy() {
        groupOf(0);
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15").startsWith("ERROR"));
    }

    @Test
    void constituentIdsShareTheTicketIdSpace() {
        groupOf(2);
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("TICKET CREATE;TG01-1;u0@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;TG01;u0@correo.com;2026-10-15").startsWith("ERROR"));
        String r = processor.process("TICKET DELETE;TG01-1");
        assertTrue(r.startsWith("ERROR") && r.contains("group ticket"), r);
        assertEquals(2, tickets.findGroupTicket("TG01").getConstituents().size());
    }

    @Test
    void groupLockedWhileGroupTicketIsCurrentOrFuture() {
        groupOf(2);
        processor.process("USER CREATE;Luis;M;11111111H;luis@correo.com;Clave1!;600;1234567890123456");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;u0@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"), "today still locked");
        assertEquals(2, groups.find("G01").getMembers().size());

        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").startsWith("OK"));
        assertEquals(2, tickets.findGroupTicket("TG01").getConstituents().size(), "composition unchanged");
        assertTrue(processor.process("GROUP REMOVE;G01;u0@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        assertNotNull(tickets.findGroupTicket("TG01"), "history kept");
    }

    @Test
    void otherGroupsAreNotLocked() {
        groupOf(2);
        processor.process("GROUP CREATE;G02;Amigos");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("GROUP ADD;G02;u0@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP DELETE;G02").startsWith("OK"));
    }

    @Test
    void constituentTicketsProtectMembers() {
        groupOf(2);
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("USER DELETE;u0@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("USER DELETE;u0@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void groupTicketDeletionOnlyForFutureAndRemovesConstituents() {
        groupOf(3);
        processor.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("GROUPTICKET DELETE;TG001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("GROUPTICKET DELETE;TG001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-01");
        assertTrue(processor.process("GROUPTICKET DELETE;TG001").startsWith("OK"));
        assertNull(tickets.findGroupTicket("TG001"));
        assertTrue(processor.process("TICKET CREATE;TG001-1;u0@correo.com;2026-10-15").startsWith("OK"),
                "constituent ids released");
        assertTrue(processor.process("GROUPTICKET DELETE;TG001").startsWith("ERROR"), "unknown");
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"), "unlocked");
        assertTrue(processor.process("GROUPTICKET DELETE").startsWith("ERROR"));
    }

    @Test
    void groupTicketUsesCurrentPrice() {
        groupOf(3);
        processor.process("SYSTEM PRICE;60.00");
        assertEquals("OK GROUP TICKET CREATED -> 162.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals(new BigDecimal("60.00"), tickets.findGroupTicket("TG01").getConstituents().get(2).getPrice());
    }

    @Test
    void attractionSourceExample() {
        processor.process(CREATE_ANA);
        processor.process(CommandRunnerTest.CREATE_PEDRO);
        processor.process("USER CREATE;Luis;Martin;11111111H;luis@correo.com;Clave1!;600;1234567890123456");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        String status = processor.process("ATTRACTION STATUS;A01");
        for (String expected : new String[] {"A01", "Tornado", "capacity: 20", "present: 3", "available: 17",
            "ana@correo.com", "pedro@correo.com", "luis@correo.com"}) {
            assertTrue(status.contains(expected), expected + " in " + status);
        }
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("ATTRACTION EXIT;A01;T001").startsWith("OK"), "exit has no date check");
        status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("present: 2") && status.contains("available: 18") && !status.contains("ana@"), status);
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"), "not valid on 2026-10-16");
        assertTrue(processor.process("ATTRACTION EXIT;A01;T001").startsWith("ERROR"), "not present");
        assertTrue(processor.process("ATTRACTION EXIT;A01;TG01").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("users: (none)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A01;Tornado", "ATTRACTION CREATE;A02;X;0", "ATTRACTION ENTER;A99;T001",
        "ATTRACTION ENTER;A01;T999", "ATTRACTION ENTER;A01", "ATTRACTION EXIT;A99;T001", "ATTRACTION EXIT;A01;T999",
        "ATTRACTION STATUS;A99", "ATTRACTION STATUS", "ATTRACTION ENTER;A01;TG01-1"})
    void invalidAttractionCommandsRejected(String command) {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        processor.process("ATTRACTION CREATE;A01;Tornado;20");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertEquals(0, attractions.find("A01").getPresentCount());
    }

    @Test
    void groupDeniedWhenCapacityShort() {
        groupOf(4);
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("ATTRACTION CREATE;A01;Tornado;3");
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("present: 0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A02;X;veinte", "ATTRACTION CREATE;A02;X;2.5", "ATTRACTION CREATE;A02;X;",
        "ATTRACTION CREATE;A02;X;99999999999", "ATTRACTION CREATE;A02;X;-3", "ATTRACTION CREATE;;X;5",
        "ATTRACTION CREATE;A02;;5"})
    void invalidCapacityTextRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertNull(attractions.find("A02"));
    }

    @Test
    void userAndGroupInvoicesDoNotDoubleCount() throws java.io.IOException {
        groupOf(3);
        processor.process("TICKET CREATE;T001;u0@correo.com;2026-10-15");
        processor.process("SYSTEM PRICE;60.00");
        processor.process("TICKET CREATE;T002;u0@correo.com;2026-10-15");
        processor.process("SYSTEM PRICE;40.00");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG02;G01;2026-10-16");
        java.util.List<String> before = java.nio.file.Files.readAllLines(dir.resolve("tickets.txt"));

        String user = processor.process("INVOICE CREATE;USER;u0@correo.com");
        assertTrue(user.startsWith("OK: invoice for user U0"), user);
        assertTrue(user.contains("T001 2026-10-15: 40.00") && user.contains("T002 2026-10-15: 60.00"), user);
        assertTrue(!user.contains("TG01") && user.contains("TOTAL: 100.00"), user);

        String group = processor.process("INVOICE CREATE;GROUP;G01");
        assertTrue(group.startsWith("OK: invoice for group G01"), group);
        assertTrue(group.contains("TG01 2026-10-15 (3 members, 10% discount): 108.00"), group);
        assertTrue(group.contains("TOTAL: 216.00") && !group.contains("T001") && !group.contains("Individual"), group);
        assertEquals(before, java.nio.file.Files.readAllLines(dir.resolve("tickets.txt")), "invoices are not persisted");
    }

    @Test
    void invoiceWithoutPurchasesShowsZeroTotal() {
        processor.process(CREATE_ANA);
        String invoice = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertEquals(2, invoice.split("\\R").length, invoice);
        assertTrue(invoice.endsWith("TOTAL: 0.00") && !invoice.contains("tickets:"), invoice);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99", "INVOICE CREATE;COMPANY;X",
        "INVOICE CREATE;USER", "INVOICE CREATE;USER;a;b", "INVOICE CREATE", "INVOICE CREATE;user;ana@correo.com"})
    void invalidInvoiceRequestsRejected(String command) {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(command).startsWith("ERROR"), command);
    }

    @Test
    void helpExtendedExplainsCommands() {
        String help = processor.process("HELP EXTENDED");
        assertTrue(help.startsWith("OK: available commands (extended):"), help);
        assertTrue(help.contains("TICKET DELETE;Id") && help.contains("Deletes an individual ticket"), help);
        assertTrue(processor.process("HELP EXTENDED;x").startsWith("ERROR"));
        assertTrue(processor.process("HELP").contains("  HELP EXTENDED"));
    }

    @Test
    void passCommandsSourceScenarios() {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 8.00", processor.process("SPEEDPASS CREATE;SP1;T001;1"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00", processor.process("SPEEDPASS CREATE;PK;T001;10"));
        assertEquals("OK SPECIALPASS CREATED -> 40.00", processor.process("SPECIALPASS CREATE;SX1;T001"));
        assertTrue(processor.process("SPECIALPASS CREATE;SX2;T001").startsWith("ERROR"));
        assertEquals(12, passes.getPasses().size());
        assertTrue(processor.process("SPEEDPASS DELETE;PK-4").startsWith("OK"));
        assertTrue(processor.process("SPECIALPASS DELETE;SP1").startsWith("ERROR"), "wrong kind");
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("SPEEDPASS DELETE;SP1").startsWith("ERROR"), "ticket is today");
        assertTrue(processor.process("SPECIALPASS DELETE;SX1").startsWith("ERROR"));
        assertEquals(11, passes.getPasses().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SPEEDPASS CREATE;SP1;T001;0", "SPEEDPASS CREATE;SP1;T001;2", "SPEEDPASS CREATE;SP1;T001;diez",
        "SPEEDPASS CREATE;SP1;T001;", "SPEEDPASS CREATE;SP1;T999;1", "SPEEDPASS CREATE;SP1;T001",
        "SPECIALPASS CREATE;SX1;T999", "SPECIALPASS CREATE;SX1", "SPEEDPASS DELETE;NOPE", "SPECIALPASS DELETE",
        "SPEEDPASS CREATE;;T001;1", "SPEEDPASS CREATE;SP1;T001;99999999999"})
    void invalidPassCommandsRejected(String command) {
        ticketSetup();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process(command).startsWith("ERROR"), command);
        assertTrue(passes.getPasses().isEmpty());
    }

    @Test
    void passesOnConstituentTicketsAndInvoices() {
        groupOf(3);
        processor.process("SYSTEM PRICE;60.00");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("TICKET CREATE;T001;u0@correo.com;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 12.00", processor.process("SPEEDPASS CREATE;SP1;TG01-1;1"));
        assertEquals("OK SPECIALPASS CREATED -> 60.00", processor.process("SPECIALPASS CREATE;SX1;T001"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP2;TG01;1").startsWith("ERROR"), "group ticket is not individual");

        String user = processor.process("INVOICE CREATE;USER;u0@correo.com");
        assertTrue(user.contains("Personal passes:"), user);
        assertTrue(user.contains("SPEED pass SP1 (ticket TG01-1): 12.00"), user);
        assertTrue(user.contains("SPECIAL pass SX1 (ticket T001): 60.00"), user);
        assertTrue(user.contains("TOTAL: 132.00") && !user.contains("TG01-1 2026"), user);

        String group = processor.process("INVOICE CREATE;GROUP;G01");
        assertTrue(!group.contains("pass") && group.contains("TOTAL: 162.00"), group);
        assertTrue(processor.process("INVOICE CREATE;USER;u1@correo.com").endsWith("TOTAL: 0.00"));
    }

    @Test
    void attractionTypesRestrictTicketKinds() {
        groupOf(2);
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;u0@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Saltos;20;GROUP").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A03;Solo;20;individual").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A04;Especial;20;SPECIAL").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A05;X;20;VIP").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A05;X;20;GROUP;extra").startsWith("ERROR"));

        String r = processor.process("ATTRACTION ENTER;A02;T001");
        assertTrue(r.startsWith("ERROR") && r.contains("does not accept individual tickets"), r);
        r = processor.process("ATTRACTION ENTER;A03;TG01");
        assertTrue(r.startsWith("ERROR") && r.contains("does not accept group tickets"), r);
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A03;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;T001").startsWith("ERROR"), "no Special Pass yet");
        processor.process("SPECIALPASS CREATE;SX1;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A04;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("  type: GENERAL"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("  type: GROUP"));
        assertTrue(processor.process("ATTRACTION EXIT;A03;TG01").startsWith("ERROR"));
    }

    private void passEntrySetup() {
        groupOf(2);
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;u0@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("ATTRACTION CREATE;A01;Tornado;20");
        processor.process("ATTRACTION CREATE;A02;Saltos;20;GROUP");
        processor.process("ATTRACTION CREATE;A03;Solo;20;INDIVIDUAL");
        processor.process("ATTRACTION CREATE;A04;Especial;20;SPECIAL");
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        passEntrySetup();
        processor.process("SPEEDPASS CREATE;SP1;T001;1");
        processor.process("SPEEDPASS CREATE;SP2;T001;1");
        assertEquals("OK: 1 user(s) entered attraction A01 (19 places available); SpeedPass consumed: SP1",
                processor.process("ATTRACTION ENTER;A01;T001;SPEED"));
        assertTrue(passes.find("SP1").isConsumed() && !passes.find("SP2").isConsumed());
        processor.process("ATTRACTION EXIT;A01;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("OK"), "normal entry keeps passes");
        assertTrue(!passes.find("SP2").isConsumed());
        assertTrue(processor.process("ATTRACTION ENTER;A03;T001;speed").contains("SpeedPass consumed: SP2"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR"));
        processor.process("ATTRACTION EXIT;A03;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A03;T001;SPEED").startsWith("ERROR"), "no unused SpeedPass left");
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        passEntrySetup();
        processor.process("SPEEDPASS CREATE;PA;TG01-1;1");
        String r = processor.process("ATTRACTION ENTER;A02;TG01;SPEED");
        assertTrue(r.startsWith("ERROR") && r.contains("TG01-2"), r);
        assertTrue(!passes.find("PA").isConsumed());
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("present: 0"));
        processor.process("SPEEDPASS CREATE;PB;TG01-2;10");
        assertEquals("OK: 2 user(s) entered attraction A02 (18 places available); SpeedPasses consumed: PA, PB-1",
                processor.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertTrue(passes.find("PA").isConsumed() && passes.find("PB-1").isConsumed() && !passes.find("PB-2").isConsumed());
    }

    @Test
    void failedEntryConsumesNothing() {
        passEntrySetup();
        processor.process("SPEEDPASS CREATE;PA;TG01-1;1");
        processor.process("SPEEDPASS CREATE;PB;TG01-2;1");
        processor.process("ATTRACTION CREATE;A05;Mini;1;GROUP");
        assertTrue(processor.process("ATTRACTION ENTER;A05;TG01;SPEED").startsWith("ERROR"), "capacity");
        assertTrue(processor.process("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ERROR"), "kind");
        processor.process("TICKET CREATE;T002;u1@correo.com;2026-10-16");
        processor.process("SPECIALPASS CREATE;SX2;T002");
        assertTrue(processor.process("ATTRACTION ENTER;A04;T002").startsWith("ERROR"), "date");
        assertTrue(!passes.find("PA").isConsumed() && !passes.find("PB").isConsumed() && !passes.find("SX2").isConsumed());
    }

    @Test
    void specialAttractionConsumesSpecialPassAutomatically() {
        passEntrySetup();
        processor.process("SPECIALPASS CREATE;SX1;T001");
        processor.process("SPEEDPASS CREATE;SP1;T001;1");
        String r = processor.process("ATTRACTION ENTER;A04;T001;SPEED");
        assertTrue(r.startsWith("ERROR") && r.contains("SPEED option is not accepted"), r);
        assertTrue(!passes.find("SX1").isConsumed() && !passes.find("SP1").isConsumed());
        assertEquals("OK: 1 user(s) entered attraction A04 (19 places available); Special Pass consumed: SX1",
                processor.process("ATTRACTION ENTER;A04;T001"));
        processor.process("ATTRACTION EXIT;A04;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A04;T001").contains("no unused Special Pass"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"), "group ticket at SPECIAL");
        assertTrue(processor.process("SPECIALPASS CREATE;SX9;T001").startsWith("ERROR"), "still one per lifetime");
    }
}
