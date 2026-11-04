package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;
import es.upm.etsisi.amusementpark.persistence.AttractionFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.TicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private static final LocalDate START = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private Path file;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        processor = newProcessor();
    }

    private CommandProcessor newProcessor() throws Exception {
        UserManager users = new UserManager(new UserFileRepository(file));
        GroupManager groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        return new CommandProcessor(new ParkController(users, groups, new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), users, groups),
                new AttractionManager(new AttractionFileRepository(dir.resolve("attractions.txt"))), START));
    }

    private static void assertOk(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    private static void assertError(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    @Test
    void appendixExampleSequence() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666"));
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana.nueva@correo.com;Nueva2#;612345678;9999888877776666"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        CommandProcessor restarted = newProcessor();
        assertError(restarted.process("USER DELETE;ana.nueva@correo.com;Clave1!"));
        assertOk(restarted.process("USER DELETE;ana.nueva@correo.com;Nueva2#"));
        assertTrue(Files.readAllLines(file, StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    void duplicateIdentitiesRejected() {
        assertOk(processor.process(CREATE_ANA));
        assertError(processor.process("USER CREATE;Otra;X;12345678Z;otro@correo.com;Clave1!;1;1234567890123456"));
        assertError(processor.process("USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;1;1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER RENAME;ana@correo.com",
        "HELLO",
        "USER",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER DELETE;ana@correo.com",
        "USER DELETE;ana@correo.com;Clave1!;extra",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "USER UPDATE;ana@correo.com;Clave1!;AGE;30",
        ";;;"
    })
    void invalidCommandsAreRejectedWithoutChanges(String command) throws Exception {
        assertOk(processor.process(CREATE_ANA));
        List<String> before = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertError(processor.process(command));
        assertEquals(before, Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void dniUpdateKeepsDni() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertTrue(Files.readString(file).contains("12345678Z"));
    }

    @Test
    void fieldNameIsCaseInsensitiveAndOperationWhitespaceTolerated() {
        assertOk(processor.process(CREATE_ANA.replace("USER CREATE", "user  create")));
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;phone;699999999"));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertOk(help);
        for (String syntax : List.of("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT")) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertError(processor.process("HELP;extra"));
    }

    @Test
    void exitSetsFlagAndRejectsParameters() {
        assertError(processor.process("EXIT;now"));
        assertFalse(processor.isExitRequested());
        assertOk(processor.process("exit"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void startupDateIsTheInjectedDateOrToday() throws Exception {
        UserManager m = new UserManager(new UserFileRepository(dir.resolve("other.txt")));
        GroupManager g = new GroupManager(new GroupFileRepository(dir.resolve("other-groups.txt")), m);
        TicketManager t = new TicketManager(new TicketFileRepository(dir.resolve("other-tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("other-grouptickets.txt")), m, g);
        AttractionManager a = new AttractionManager(new AttractionFileRepository(dir.resolve("other-attractions.txt")));
        assertEquals(LocalDate.of(2026, 1, 1), new ParkController(m, g, t, a, LocalDate.of(2026, 1, 1)).getCurrentDate());
        LocalDate before = LocalDate.now();
        LocalDate actual = new ParkController(m, g, t, a).getCurrentDate();
        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(LocalDate.now()));
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        assertOk(processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getController().getCurrentDate());
        assertOk(processor.process("SYSTEM DATE;2025-01-01"));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getController().getCurrentDate());
        assertOk(processor.process("system date;2027-12-31"));
        assertEquals(LocalDate.of(2027, 12, 31), processor.getController().getCurrentDate());
    }

    @Test
    void leapDayBoundary() {
        assertOk(processor.process("SYSTEM DATE;2028-02-29"));
        assertError(processor.process("SYSTEM DATE;2026-02-29"));
        assertEquals(LocalDate.of(2028, 2, 29), processor.getController().getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026/09/15", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;2026-13-01",
        "SYSTEM DATE;2026-02-30", "SYSTEM DATE;hoy", "SYSTEM DATE;", "SYSTEM DATE",
        "SYSTEM DATE;2026-09-15;2026-09-16", "SYSTEM DATE;+2026-09-15", "SYSTEM DATE;12026-09-15"
    })
    void invalidDatesLeaveDateUnchanged(String command) {
        assertOk(processor.process("SYSTEM DATE;2026-09-15"));
        assertError(processor.process(command));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getController().getCurrentDate());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void groupCommandsFromTheSource() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertOk(processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertOk(processor.process("GROUP ADD;G01;ana@correo.com"));
        assertError(processor.process("GROUP ADD;G01;ana@correo.com"));
        assertError(processor.process("GROUP ADD;G01;nadie@correo.com"));
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        assertOk(processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(List.of("G01;Familia Garcia"), Files.readAllLines(dir.resolve("groups.txt")));
        assertOk(processor.process("GROUP DELETE;G01"));
        assertTrue(Files.readAllLines(dir.resolve("groups.txt")).isEmpty());
        assertError(processor.process("GROUP DELETE;G01"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP CREATE;G02", "GROUP CREATE;G02;N;x", "GROUP ADD;G01", "GROUP ADD;G01;a;b",
        "GROUP REMOVE;G01", "GROUP DELETE", "GROUP DELETE;G01;extra", "GROUP CREATE;;Familia", "GROUP CREATE;G02;"})
    void malformedGroupCommandsChangeNothing(String command) throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertOk(processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertOk(processor.process("GROUP ADD;G01;ana@correo.com"));
        List<String> before = Files.readAllLines(dir.resolve("groups.txt"));
        assertError(processor.process(command));
        assertEquals(before, Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void deletingAUserRemovesItFromGroupsAcrossRestart() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertOk(processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertOk(processor.process("GROUP ADD;G01;ana@correo.com"));
        assertOk(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(List.of("G01;Familia Garcia"), Files.readAllLines(dir.resolve("groups.txt")));
        CommandProcessor restarted = newProcessor();
        assertOk(restarted.process(CREATE_ANA));
        assertError(restarted.process("GROUP REMOVE;G01;ana@correo.com"));
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String syntax : List.of("GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo", "GROUP DELETE;Id")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void ticketCreationReportsPrice() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-01"));
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00", "T002;2026-10-01;12345678Z;40.00"),
                Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TICKET CREATE;T009;nadie@correo.com;2026-10-15", "TICKET CREATE;T009;ana@correo.com;2026-09-30",
        "TICKET CREATE;T001;ana@correo.com;2026-10-20", "TICKET CREATE;;ana@correo.com;2026-10-20",
        "TICKET CREATE;T009;ana@correo.com;2026-10-32", "TICKET CREATE;T009;ana@correo.com;15-10-2026",
        "TICKET CREATE;T009;ana@correo.com", "TICKET CREATE;T009;ana@correo.com;2026-10-20;x",
        "TICKET DELETE", "TICKET DELETE;T001;x", "TICKET DELETE;T999"})
    void invalidTicketCommandsChangeNothing(String command) throws Exception {
        assertOk(processor.process(CREATE_ANA));
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        List<String> before = Files.readAllLines(dir.resolve("tickets.txt"));
        assertError(processor.process(command));
        assertEquals(before, Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void systemPriceAppliesToLaterTicketsAndResetsOnRestart() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertOk(processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));
        assertOk(processor.process("SYSTEM PRICE;55"));
        assertEquals(new BigDecimal("55.00"), processor.getController().getTicketPrice());
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00", "T002;2026-10-20;12345678Z;60.00"),
                Files.readAllLines(dir.resolve("tickets.txt")));
        assertEquals(new BigDecimal("40.00"), newProcessor().getController().getTicketPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;0", "SYSTEM PRICE;0.00", "SYSTEM PRICE;-5.00", "SYSTEM PRICE;abc",
        "SYSTEM PRICE;60,00", "SYSTEM PRICE;60.001", "SYSTEM PRICE;.50", "SYSTEM PRICE;60.", "SYSTEM PRICE;1e2",
        "SYSTEM PRICE", "SYSTEM PRICE;60.00;1"})
    void invalidPricesAreRejected(String command) {
        assertOk(processor.process("SYSTEM PRICE;60.00"));
        assertError(processor.process(command));
        assertEquals(new BigDecimal("60.00"), processor.getController().getTicketPrice());
    }

    @Test
    void ticketDeletionOnlyForFutureTickets() {
        assertOk(processor.process(CREATE_ANA));
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-15");
        assertOk(processor.process("TICKET DELETE;T001"));
        assertOk(processor.process("SYSTEM DATE;2026-10-15"));
        assertError(processor.process("TICKET DELETE;T002"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertError(processor.process("TICKET DELETE;T002"));
    }

    @Test
    void sourceExampleUserDeletionWithTickets() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertOk(processor.process("SYSTEM DATE;2026-10-01"));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertError(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertOk(processor.process("SYSTEM DATE;2026-10-15"));
        assertError(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertError(processor.process("USER DELETE;ana@correo.com;Wrong1!"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertOk(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(Files.readAllLines(dir.resolve("users.txt")).isEmpty());
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00"), Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void helpListsTicketCommands() {
        String help = processor.process("HELP");
        for (String syntax : List.of("TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id", "SYSTEM PRICE;Precio")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String CREATE_LUIS =
            "USER CREATE;Luis;Martin Ruiz;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444";

    private void groupOfThree() {
        assertOk(processor.process(CREATE_ANA));
        assertOk(processor.process(CREATE_PEDRO));
        assertOk(processor.process(CREATE_LUIS));
        assertOk(processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertOk(processor.process("GROUP ADD;G01;ana@correo.com"));
        assertOk(processor.process("GROUP ADD;G01;pedro@correo.com"));
        assertOk(processor.process("GROUP ADD;G01;luis@correo.com"));
    }

    @Test
    void groupTicketPurchaseFromTheSource() throws Exception {
        groupOfThree();
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals(List.of("TG01;2026-10-15;G01;40.00;108.00;12345678Z;87654321X;11111111H"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
        assertFalse(Files.exists(dir.resolve("tickets.txt")), "constituent tickets are not individual tickets");
    }

    @Test
    void groupTicketUsesCurrentPrice() {
        groupOfThree();
        assertOk(processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK GROUP TICKET CREATED -> 162.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUPTICKET CREATE;TG02;G99;2026-10-15", "GROUPTICKET CREATE;TG02;G02;2026-10-15",
        "GROUPTICKET CREATE;TG02;G01;2026-09-30", "GROUPTICKET CREATE;TG02;G01;2026-02-30",
        "GROUPTICKET CREATE;TG01;G01;2026-10-20", "GROUPTICKET CREATE;T001;G01;2026-10-20",
        "GROUPTICKET CREATE;;G01;2026-10-20", "GROUPTICKET CREATE;TG02;G01", "GROUPTICKET CREATE;TG02;G01;2026-10-20;x",
        "GROUPTICKET DELETE", "GROUPTICKET DELETE;TG99", "GROUPTICKET DELETE;T001", "TICKET DELETE;TG01",
        "TICKET DELETE;TG01-12345678Z"})
    void invalidGroupTicketCommandsChangeNothing(String command) throws Exception {
        groupOfThree();
        assertOk(processor.process("GROUP CREATE;G02;Vacio"));
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-20");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        List<String> before = Files.readAllLines(dir.resolve("grouptickets.txt"));
        List<String> beforeTickets = Files.readAllLines(dir.resolve("tickets.txt"));
        assertError(processor.process(command));
        assertEquals(before, Files.readAllLines(dir.resolve("grouptickets.txt")));
        assertEquals(beforeTickets, Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void groupIsLockedWhileGroupTicketIsCurrentOrFuture() {
        groupOfThree();
        assertOk(processor.process("USER CREATE;Eva;Sanz;22222222J;eva@correo.com;Eva4%a;645678901;5555666677778888"));
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        for (String date : List.of("2026-10-01", "2026-10-15")) {
            assertOk(processor.process("SYSTEM DATE;" + date));
            assertError(processor.process("GROUP ADD;G01;eva@correo.com"));
            assertError(processor.process("GROUP REMOVE;G01;ana@correo.com"));
            assertError(processor.process("GROUP DELETE;G01"));
        }
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertOk(processor.process("GROUP ADD;G01;eva@correo.com"));
        assertOk(processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertOk(processor.process("GROUP DELETE;G01"));
    }

    @Test
    void groupTicketMembersCannotBeDeletedWhileValid() throws Exception {
        groupOfThree();
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertOk(processor.process("SYSTEM DATE;2026-10-15"));
        assertError(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertOk(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(List.of("TG01;2026-10-15;G01;40.00;108.00;12345678Z;87654321X;11111111H"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void deletingFutureGroupTicketRemovesItsTickets() throws Exception {
        groupOfThree();
        processor.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        assertError(processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        assertOk(processor.process("GROUPTICKET DELETE;TG001"));
        assertTrue(Files.readAllLines(dir.resolve("grouptickets.txt")).isEmpty());
        assertOk(processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        assertOk(processor.process("GROUP REMOVE;G01;ana@correo.com"));
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd"));
        assertTrue(help.contains("GROUPTICKET DELETE;Id"));
    }

    private void sourceAttractionSetup() {
        groupOfThree();
        assertOk(processor.process("ATTRACTION CREATE;A01;Tornado;20"));
        assertOk(processor.process("SYSTEM DATE;2026-10-15"));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertOk(processor.process("USER CREATE;Eva;Sanz;22222222J;eva@correo.com;Eva4%a;645678901;5555666677778888"));
        assertOk(processor.process("GROUP CREATE;G02;Amigos"));
        assertOk(processor.process("GROUP ADD;G02;pedro@correo.com"));
        assertOk(processor.process("GROUP ADD;G02;luis@correo.com"));
        assertOk(processor.process("GROUP ADD;G02;eva@correo.com"));
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G02;2026-10-15"));
    }

    @Test
    void attractionSourceExample() {
        sourceAttractionSetup();
        assertOk(processor.process("ATTRACTION ENTER;A01;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A01;TG01"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.startsWith("OK: Attraction A01 (Tornado) capacity=20 present=4 available=16"), status);
        for (String email : List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com", "eva@correo.com")) {
            assertTrue(status.contains(email), email);
        }
        assertOk(processor.process("ATTRACTION EXIT;A01;T001"));
        status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("present=3 available=17"), status);
        assertFalse(status.contains("ana@correo.com"));
    }

    @Test
    void attractionEntryRules() {
        sourceAttractionSetup();
        assertOk(processor.process("ATTRACTION CREATE;A02;Mini;3"));
        assertOk(processor.process("ATTRACTION ENTER;A02;T001"));
        assertError(processor.process("ATTRACTION ENTER;A02;TG01"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("present=1 available=2"));
        assertError(processor.process("ATTRACTION ENTER;A02;T001"));
        assertOk(processor.process("ATTRACTION EXIT;A02;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A02;TG01"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("present=3 available=0"));
        assertOk(processor.process("ATTRACTION ENTER;A01;TG01-87654321X"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertError(processor.process("ATTRACTION ENTER;A01;T001"));
        assertOk(processor.process("ATTRACTION EXIT;A02;TG01"));
        assertOk(processor.process("SYSTEM DATE;2026-10-14"));
        assertError(processor.process("ATTRACTION ENTER;A01;T001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;A09;X;0", "ATTRACTION CREATE;A09;X;dos",
        "ATTRACTION CREATE;A09;X", "ATTRACTION ENTER;A99;T001", "ATTRACTION ENTER;A01;T999", "ATTRACTION ENTER;A01",
        "ATTRACTION EXIT;A01;T001", "ATTRACTION EXIT;A01;T999", "ATTRACTION EXIT;A99;T001", "ATTRACTION STATUS;A99",
        "ATTRACTION STATUS", "ATTRACTION OPEN;A01"})
    void invalidAttractionCommands(String command) {
        sourceAttractionSetup();
        assertError(processor.process(command));
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("present=0 available=20"));
    }

    @Test
    void deletedUserLeavesAttractions() {
        sourceAttractionSetup();
        assertOk(processor.process("ATTRACTION ENTER;A01;T001"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertOk(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("present=0 available=20"), status);
    }

    @Test
    void helpListsAttractionCommands() {
        String help = processor.process("HELP");
        for (String syntax : List.of("ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION ENTER;IdAtraccion;IdTicket",
                "ATTRACTION EXIT;IdAtraccion;IdTicket", "ATTRACTION STATUS;IdAtraccion")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
