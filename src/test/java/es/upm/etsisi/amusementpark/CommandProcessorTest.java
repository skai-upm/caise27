package es.upm.etsisi.amusementpark;

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
        return new CommandProcessor(users, new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users),
                new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")), users), START);
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
        TicketManager t = new TicketManager(new TicketFileRepository(dir.resolve("other-tickets.txt")), m);
        assertEquals(LocalDate.of(2026, 1, 1), new CommandProcessor(m, g, t, LocalDate.of(2026, 1, 1)).getCurrentDate());
        LocalDate before = LocalDate.now();
        LocalDate actual = new CommandProcessor(m, g, t).getCurrentDate();
        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(LocalDate.now()));
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        assertOk(processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
        assertOk(processor.process("SYSTEM DATE;2025-01-01"));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getCurrentDate());
        assertOk(processor.process("system date;2027-12-31"));
        assertEquals(LocalDate.of(2027, 12, 31), processor.getCurrentDate());
    }

    @Test
    void leapDayBoundary() {
        assertOk(processor.process("SYSTEM DATE;2028-02-29"));
        assertError(processor.process("SYSTEM DATE;2026-02-29"));
        assertEquals(LocalDate.of(2028, 2, 29), processor.getCurrentDate());
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
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
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
        assertEquals(new BigDecimal("55.00"), processor.getTicketPrice());
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00", "T002;2026-10-20;12345678Z;60.00"),
                Files.readAllLines(dir.resolve("tickets.txt")));
        assertEquals(new BigDecimal("40.00"), newProcessor().getTicketPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;0", "SYSTEM PRICE;0.00", "SYSTEM PRICE;-5.00", "SYSTEM PRICE;abc",
        "SYSTEM PRICE;60,00", "SYSTEM PRICE;60.001", "SYSTEM PRICE;.50", "SYSTEM PRICE;60.", "SYSTEM PRICE;1e2",
        "SYSTEM PRICE", "SYSTEM PRICE;60.00;1"})
    void invalidPricesAreRejected(String command) {
        assertOk(processor.process("SYSTEM PRICE;60.00"));
        assertError(processor.process(command));
        assertEquals(new BigDecimal("60.00"), processor.getTicketPrice());
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

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
