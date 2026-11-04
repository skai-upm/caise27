package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.ParkServices;
import es.upm.etsisi.amusementpark.business.service.PassManager;
import es.upm.etsisi.amusementpark.persistence.PassFileRepository;
import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
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

import org.junit.jupiter.api.AfterEach;
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
        UserManager users = new UserManager(new UserFileRepository(file), TestSupport.HASHER);
        GroupManager groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        ParkController.shutdown();
        TicketManager tickets = new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), users, groups);
        ParkServices services = new ParkServices(users, groups, tickets,
                new AttractionManager(new AttractionFileRepository(dir.resolve("attractions.txt"))),
                new PassManager(new PassFileRepository(dir.resolve("passes.txt")), tickets));
        return new CommandProcessor(ParkController.initialize(services, new InvoiceDirector(), START),
                new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter(), new ExtendedHelpFormatter()));
    }

    @AfterEach
    void tearDown() {
        ParkController.shutdown();
    }

    private static void assertOk(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    private static void assertError(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    /** The users file holds exactly this user, with a BCrypt hash of the given password instead of the plaintext. */
    private void assertStoredUser(String expectedWithPlainPassword) throws Exception {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        String[] expected = expectedWithPlainPassword.split(";", -1);
        String[] actual = lines.get(0).split(";", -1);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            if (i == 4) {
                assertTrue(actual[i].startsWith("$2a$"), actual[i]);
                assertTrue(TestSupport.HASHER.matches(expected[i], actual[i]));
                assertFalse(lines.get(0).contains(expected[i]), "plaintext password stored");
            } else {
                assertEquals(expected[i], actual[i]);
            }
        }
    }

    @Test
    void appendixExampleSequence() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertStoredUser("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");

        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666"));
        assertStoredUser("Ana Maria;Garcia Lopez;12345678Z;ana.nueva@correo.com;Nueva2#;612345678;9999888877776666");

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
        UserManager m = new UserManager(new UserFileRepository(dir.resolve("other.txt")), TestSupport.HASHER);
        GroupManager g = new GroupManager(new GroupFileRepository(dir.resolve("other-groups.txt")), m);
        TicketManager t = new TicketManager(new TicketFileRepository(dir.resolve("other-tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("other-grouptickets.txt")), m, g);
        AttractionManager a = new AttractionManager(new AttractionFileRepository(dir.resolve("other-attractions.txt")));
        ParkController.shutdown();
        assertEquals(LocalDate.of(2026, 1, 1),
                ParkController.initialize(new ParkServices(m, g, t, a, new PassManager(
                        new PassFileRepository(dir.resolve("other-passes.txt")), t)), new InvoiceDirector(),
                        LocalDate.of(2026, 1, 1)).getCurrentDate());
        ParkController.shutdown();
        LocalDate before = LocalDate.now();
        LocalDate actual = ParkController.initialize(new ParkServices(m, g, t, a, new PassManager(
                new PassFileRepository(dir.resolve("other-passes.txt")), t)), new InvoiceDirector()).getCurrentDate();
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
    void userInvoiceDoesNotCountGroupTicketParts() throws Exception {
        groupOfThree();
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertOk(processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-16"));
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        String invoice = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertEquals(String.join(System.lineSeparator(),
                "OK: Invoice for user Ana Garcia Lopez <ana@correo.com> (DNI 12345678Z)",
                "  Individual tickets:",
                "    T001 2026-10-15: 40.00",
                "    T002 2026-10-16: 60.00",
                "  TOTAL: 100.00"), invoice);
    }

    @Test
    void groupInvoiceListsOnlyGroupTickets() {
        groupOfThree();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        String invoice = processor.process("invoice create;group;G01");
        assertEquals(String.join(System.lineSeparator(),
                "OK: Invoice for group G01 (Familia Garcia)",
                "  Group tickets:",
                "    TG01 2026-10-15 (3 members, 10% discount): 108.00",
                "  TOTAL: 108.00"), invoice);
    }

    @Test
    void invoiceWithoutPurchasesHasNoSections() throws Exception {
        groupOfThree();
        assertEquals(String.join(System.lineSeparator(),
                "OK: Invoice for user Pedro Lopez Martin <pedro@correo.com> (DNI 87654321X)",
                "  TOTAL: 0.00"), processor.process("INVOICE CREATE;USER;pedro@correo.com"));
        assertEquals(String.join(System.lineSeparator(), "OK: Invoice for group G01 (Familia Garcia)", "  TOTAL: 0.00"),
                processor.process("INVOICE CREATE;GROUP;G01"));
        assertFalse(Files.exists(dir.resolve("tickets.txt")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99",
        "INVOICE CREATE;COMPANY;X", "INVOICE CREATE;USER", "INVOICE CREATE;USER;ana@correo.com;extra", "INVOICE CREATE"})
    void invalidInvoiceCommands(String command) throws Exception {
        groupOfThree();
        List<String> before = Files.readAllLines(dir.resolve("users.txt"));
        String result = processor.process(command);
        assertError(result);
        assertFalse(result.contains("TOTAL"));
        assertEquals(before, Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void helpListsInvoiceCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("INVOICE CREATE;USER;Correo"));
        assertTrue(help.contains("INVOICE CREATE;GROUP;IdGrupo"));
    }

    @Test
    void helpExtendedShowsDescriptions() {
        String extended = processor.process("HELP EXTENDED");
        assertTrue(extended.startsWith("OK: Available commands (extended):"));
        assertTrue(extended.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(extended.contains("Registers a user"));
        assertTrue(processor.process("help extended").startsWith("OK"));
        assertError(processor.process("HELP EXTENDED;all"));
        assertTrue(processor.process("HELP").contains("  HELP EXTENDED"));
    }

    @Test
    void passwordsAreStoredAsHashesAndVerified() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertFalse(Files.readString(file).contains("Clave1!"));
        String before = Files.readString(file);
        assertError(processor.process("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999"));
        assertError(processor.process("USER DELETE;ana@correo.com;Wrong1!"));
        assertEquals(before, Files.readString(file));
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva"));
        assertEquals(before, Files.readString(file));
        assertError(processor.process(CREATE_ANA.replace("Clave1!", "clave1!").replace("12345678Z", "87654321X")
                .replace("ana@", "otra@")));
    }

    @Test
    void passwordRotationSurvivesRestartWithANewHash() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        String oldHash = Files.readString(file).split(";")[4];
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        String newHash = Files.readString(file).split(";")[4];
        assertFalse(oldHash.equals(newHash));
        CommandProcessor restarted = newProcessor();
        assertError(restarted.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertOk(restarted.process("USER UPDATE;ana@correo.com;Nueva2#;PHONE;699999999"));
    }

    @Test
    void legacyPlaintextEntryDoesNotAuthenticate() throws Exception {
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                StandardCharsets.UTF_8);
        CommandProcessor legacy = newProcessor();
        assertError(legacy.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(1, Files.readAllLines(file).size());
    }

    @Test
    void speedPassPurchasesAndRefunds() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00", processor.process("SPEEDPASS CREATE;SP1;T001;10"));
        assertEquals("OK SPEEDPASS CREATED -> 8.00", processor.process("SPEEDPASS CREATE;SP2;T001;1"));
        assertEquals(11, Files.readAllLines(dir.resolve("passes.txt")).size());
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).contains("SPEED;SP2-1;T001;8.00;false"));
        for (String bad : List.of("SPEEDPASS CREATE;SP3;T001;5", "SPEEDPASS CREATE;SP1;T001;10", "SPEEDPASS CREATE;SP3;T999;1",
                "SPEEDPASS CREATE;SP3;T001", "SPEEDPASS DELETE;SP9-1", "SPECIALPASS DELETE;SP1-1", "SPEEDPASS DELETE")) {
            List<String> before = Files.readAllLines(dir.resolve("passes.txt"));
            assertError(processor.process(bad));
            assertEquals(before, Files.readAllLines(dir.resolve("passes.txt")), bad);
        }
        assertOk(processor.process("SPEEDPASS DELETE;SP1-1"));
        assertOk(processor.process("SYSTEM DATE;2026-10-15"));
        assertError(processor.process("SPEEDPASS DELETE;SP1-2"));
        assertEquals("OK SPEEDPASS CREATED -> 8.00", processor.process("SPEEDPASS CREATE;SP4;T001;1"));
        assertOk(processor.process("SYSTEM DATE;2026-10-16"));
        assertError(processor.process("SPEEDPASS CREATE;SP5;T001;1"));
    }

    @Test
    void specialPassIsUniquePerTicketAndUsesStoredGroupPrice() {
        groupOfThree();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK SPECIALPASS CREATED -> 40.00", processor.process("SPECIALPASS CREATE;VIP1;T001"));
        assertError(processor.process("SPECIALPASS CREATE;VIP2;T001"));
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals("OK SPEEDPASS CREATED -> 7.20", processor.process("SPEEDPASS CREATE;SP4;TG01-12345678Z;1"));
        assertEquals("OK SPECIALPASS CREATED -> 36.00", processor.process("SPECIALPASS CREATE;VIP3;TG01-12345678Z"));
        assertError(processor.process("SPECIALPASS CREATE;VIP4;TG01"));
        assertOk(processor.process("SPECIALPASS DELETE;VIP1"));
        assertError(processor.process("SPEEDPASS DELETE;VIP3"));
    }

    @Test
    void ticketsWithPassesCannotBeDeletedAlone() {
        groupOfThree();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("SPEEDPASS CREATE;SP1;T001;1");
        processor.process("SPEEDPASS CREATE;SP2;TG01-87654321X;1");
        assertError(processor.process("TICKET DELETE;T001"));
        assertError(processor.process("GROUPTICKET DELETE;TG01"));
        assertOk(processor.process("SPEEDPASS DELETE;SP1-1"));
        assertOk(processor.process("SPEEDPASS DELETE;SP2-1"));
        assertOk(processor.process("TICKET DELETE;T001"));
        assertOk(processor.process("GROUPTICKET DELETE;TG01"));
    }

    @Test
    void invoicesIncludePersonalPassesOnlyForUsers() {
        groupOfThree();
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("SPEEDPASS CREATE;SP1;TG01-12345678Z;1");
        assertEquals(String.join(System.lineSeparator(),
                "OK: Invoice for user Ana Garcia Lopez <ana@correo.com> (DNI 12345678Z)",
                "  Individual tickets:",
                "    T001 2026-10-15: 40.00",
                "  Personal passes:",
                "    SP1-1 SpeedPass for TG01-12345678Z: 7.20",
                "  TOTAL: 47.20"), processor.process("INVOICE CREATE;USER;ana@correo.com"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 108.00"));
    }

    @Test
    void passesSurviveRestart() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("SPECIALPASS CREATE;VIP1;T001");
        CommandProcessor restarted = newProcessor();
        assertError(restarted.process("SPECIALPASS CREATE;VIP2;T001"));
        assertOk(restarted.process("SPECIALPASS DELETE;VIP1"));
    }

    @Test
    void typedAttractionsAdmitOnlyTheirTicketKinds() throws Exception {
        sourceAttractionSetup();
        assertOk(processor.process("ATTRACTION CREATE;A02;Coches;10;GROUP"));
        assertOk(processor.process("ATTRACTION CREATE;A03;Noria;10;individual"));
        assertOk(processor.process("ATTRACTION CREATE;A04;VIP;10;SPECIAL"));
        assertError(processor.process("ATTRACTION ENTER;A02;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A02;TG01"));
        assertError(processor.process("ATTRACTION ENTER;A03;TG01"));
        assertOk(processor.process("ATTRACTION ENTER;A03;T001"));
        assertError(processor.process("ATTRACTION ENTER;A04;TG01"));
        assertOk(processor.process("SPECIALPASS CREATE;VIP1;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A04;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A01;T001"));
        assertOk(processor.process("ATTRACTION ENTER;A01;TG01"));
        assertTrue(processor.process("ATTRACTION STATUS;A02")
                .startsWith("OK: Attraction A02 (Coches) capacity=10 present=3 available=7 type=GROUP"));
        assertTrue(Files.readAllLines(dir.resolve("attractions.txt")).contains("A02;Coches;10;GROUP"));
        assertTrue(Files.readAllLines(dir.resolve("attractions.txt")).contains("A01;Tornado;20;GENERAL"));
        assertError(processor.process("ATTRACTION CREATE;A05;X;5;VIP"));
        assertError(processor.process("ATTRACTION CREATE;A05;X;5;GROUP;extra"));
        assertFalse(processor.process("ATTRACTION STATUS;A05").startsWith("OK"));
    }

    @Test
    void legacyAttractionLinesLoadAsGeneral() throws Exception {
        Files.write(dir.resolve("attractions.txt"), List.of("A09;Noria;5"), StandardCharsets.UTF_8);
        CommandProcessor legacy = newProcessor();
        assertTrue(legacy.process("ATTRACTION STATUS;A09").contains("capacity=5 present=0 available=5 type=GENERAL"));
    }

    private void passAccessSetup() {
        sourceAttractionSetup();
        assertOk(processor.process("ATTRACTION CREATE;A02;Coches;10;GROUP"));
        assertOk(processor.process("ATTRACTION CREATE;A04;VIP;10;SPECIAL"));
        assertOk(processor.process("ATTRACTION CREATE;A05;Mini;2"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00", processor.process("SPEEDPASS CREATE;SP1;T001;10"));
    }

    private List<String> passesFile() throws Exception {
        return Files.readAllLines(dir.resolve("passes.txt"));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() throws Exception {
        passAccessSetup();
        assertEquals("OK: 1 visitor(s) entered A01 with ticket T001; consumed SpeedPass SP1-1",
                processor.process("ATTRACTION ENTER;A01;T001;speed"));
        assertEquals(1, passesFile().stream().filter(l -> l.endsWith(";true")).count());
        assertTrue(passesFile().contains("SPEED;SP1-1;T001;8.00;true"));
        assertOk(processor.process("ATTRACTION EXIT;A01;T001"));
        assertEquals("OK: 1 visitor(s) entered A01 with ticket T001", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals(1, passesFile().stream().filter(l -> l.endsWith(";true")).count());
        assertError(processor.process("ATTRACTION ENTER;A01;T001;FAST"));
        CommandProcessor restarted = newProcessor();
        restarted.process("SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 visitor(s) entered A01 with ticket T001; consumed SpeedPass SP1-2",
                restarted.process("ATTRACTION ENTER;A01;T001;SPEED"));
    }

    @Test
    void groupSpeedEntryIsAllOrNothing() throws Exception {
        passAccessSetup();
        processor.process("SPEEDPASS CREATE;P1;TG01-87654321X;1");
        processor.process("SPEEDPASS CREATE;P2;TG01-11111111H;1");
        List<String> before = passesFile();
        assertError(processor.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertEquals(before, passesFile());
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("present=0"));
        processor.process("SPEEDPASS CREATE;P3;TG01-22222222J;1");
        before = passesFile();
        assertError(processor.process("ATTRACTION ENTER;A05;TG01;SPEED"));
        assertEquals(before, passesFile());
        assertEquals("OK: 3 visitor(s) entered A02 with ticket TG01; consumed SpeedPass P1-1, P2-1, P3-1",
                processor.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertEquals(3, passesFile().stream().filter(l -> l.endsWith(";true")).count());
    }

    @Test
    void specialAttractionConsumesTheSpecialPass() throws Exception {
        passAccessSetup();
        assertError(processor.process("ATTRACTION ENTER;A04;T001"));
        assertEquals("OK SPECIALPASS CREATED -> 40.00", processor.process("SPECIALPASS CREATE;VIP1;T001"));
        List<String> before = passesFile();
        assertError(processor.process("ATTRACTION ENTER;A04;T001;SPEED"));
        assertEquals(before, passesFile());
        assertEquals("OK: 1 visitor(s) entered A04 with ticket T001; consumed Special Pass VIP1",
                processor.process("ATTRACTION ENTER;A04;T001"));
        assertTrue(passesFile().contains("SPECIAL;VIP1;T001;40.00;true"));
        assertOk(processor.process("ATTRACTION EXIT;A04;T001"));
        assertError(processor.process("ATTRACTION ENTER;A04;T001"));
        assertError(processor.process("ATTRACTION ENTER;A04;TG01"));
    }

    @Test
    void failedSpeedEntryOnAnotherDayConsumesNothing() throws Exception {
        passAccessSetup();
        assertOk(processor.process("SYSTEM DATE;2026-10-14"));
        List<String> before = passesFile();
        assertError(processor.process("ATTRACTION ENTER;A01;T001;SPEED"));
        assertEquals(before, passesFile());
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
