package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private TestPark park;
    private UserController manager;
    private MainController controller;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        manager = park.users;
        controller = park.controller;
        processor = park.processor;
    }

    @Test
    void createDeleteUpdate() {
        assertEquals("OK USER CREATED", processor.process(ANA));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void operationIsCaseInsensitiveAndTrimmed() {
        assertEquals("OK USER CREATED",
                processor.process(" user create ; Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456 "));
    }

    @Test
    void unknownCommand() {
        String out = processor.process("FOO BAR;1");
        assertTrue(out.startsWith("ERROR Unknown command"), out);
    }

    @Test
    void wrongParameterCount() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER UPDATE;a;b;c").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR Wrong number of parameters"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void trailingEmptyValueIsBlankNotMissing() {
        String out = processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;");
        assertEquals("ERROR The card number is required", out);
    }

    @Test
    void businessErrorsAreReported() {
        processor.process(ANA);
        assertTrue(processor.process(ANA).startsWith("ERROR"));
        assertEquals("ERROR Incorrect password", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("ERROR The DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }

    @Test
    void helpListsEveryCommand() {
        String out = processor.process("HELP");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("OK Available commands:", lines[0]);
        assertEquals(CommandProcessor.COMMAND_SYNTAX, List.of(lines).subList(1, lines.length));
        assertTrue(out.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(processor.process("HELP;x").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void exitSetsFlag() {
        assertTrue(processor.process("EXIT;x").startsWith("ERROR Wrong number of parameters"));
        assertFalse(processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("exit"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateMovesForwardAndBackward() {
        assertEquals(LocalDate.of(2026, 9, 1), controller.getCurrentDate());
        assertEquals("OK SYSTEM DATE SET -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), controller.getCurrentDate());
        assertEquals("OK SYSTEM DATE SET -> 2025-01-01", processor.process("system date; 2025-01-01 "));
        assertEquals(LocalDate.of(2025, 1, 1), controller.getCurrentDate());
    }

    @Test
    void invalidSystemDateLeavesDateUnchanged() {
        assertTrue(processor.process("SYSTEM DATE;15/09/2026").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-9-15").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-02-30").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE").startsWith("ERROR Wrong number of parameters"));
        assertEquals(LocalDate.of(2026, 9, 1), controller.getCurrentDate());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void groupCommands() {
        processor.process(ANA);
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP CREATE;G01").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(new TestPark(dir).groups.findById("G01").getMembers().isEmpty());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String syntax : List.of("GROUP CREATE;IdentificadorGrupo;Nombre", "GROUP ADD;IdentificadorGrupo;Correo",
                "GROUP REMOVE;IdentificadorGrupo;Correo", "GROUP DELETE;IdentificadorGrupo")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void ticketCommandsAndPrice() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2026-10-01");
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15").startsWith("ERROR"));
        assertEquals("OK SYSTEM PRICE SET -> 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));
        assertTrue(processor.process("SYSTEM PRICE;-5").startsWith("ERROR"));
        assertEquals("OK TICKET DELETED", processor.process("TICKET DELETE;T002"));
        assertTrue(processor.process("TICKET DELETE;T999").startsWith("ERROR"));
        String help = processor.process("HELP");
        assertTrue(help.contains("TICKET CREATE;IdentificadorEntrada;Correo;yyyy-MM-dd"));
        assertTrue(help.contains("TICKET DELETE;IdentificadorEntrada"));
        assertTrue(help.contains("SYSTEM PRICE;Precio"));
    }

    @Test
    void ticketsBlockUserDeletionUntilPast() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("ERROR Incorrect password", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(new TestPark(dir).tickets.getTickets().isEmpty());
    }

    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444";

    @Test
    void groupTicketLifecycleWithLocks() {
        processor.process(ANA);
        processor.process(PEDRO);
        processor.process(LUIS);
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        assertEquals("OK GROUP TICKET CREATED -> 80.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").contains("locked"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").contains("locked"));
        assertTrue(processor.process("GROUP DELETE;G01").contains("locked"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;TG01-1").startsWith("ERROR"));

        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").contains("locked"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));

        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;luis@correo.com"));
        assertEquals(List.of("Ana", "Pedro"),
                ((GroupTicket) park.tickets.findById("TG01")).getUsers().stream().map(User::getName).toList());
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG02;G01;2026-10-20"));
        assertEquals("OK GROUP TICKET DELETED", processor.process("GROUPTICKET DELETE;TG02"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertTrue(park.tickets.getTickets().isEmpty());
        String help = processor.process("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;IdentificadorEntradaGrupo;IdentificadorGrupo;yyyy-MM-dd"));
        assertTrue(help.contains("GROUPTICKET DELETE;IdentificadorEntradaGrupo"));
    }

    @Test
    void attractionStatusReflectsEntriesAndExits() {
        processor.process(ANA);
        processor.process(PEDRO);
        processor.process(LUIS);
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertEquals("OK ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Tornado;20"));
        assertEquals("OK ENTERED A01 -> 1 user", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK ENTERED A01 -> 2 users", processor.process("ATTRACTION ENTER;A01;TG01"));
        String nl = System.lineSeparator();
        assertEquals(String.join(nl, "OK ATTRACTION STATUS A01", "Name: Tornado", "Capacity: 20", "Present: 3",
                "Available: 17", "Users present:", "- Ana Garcia Lopez (ana@correo.com)",
                "- Pedro Lopez Martin (pedro@correo.com)", "- Luis Perez (luis@correo.com)"),
                processor.process("ATTRACTION STATUS;A01"));
        assertEquals("OK EXITED A01 -> 1 user", processor.process("ATTRACTION EXIT;A01;T001"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 2" + nl + "Available: 18"));
        processor.process("ATTRACTION EXIT;A01;TG01");
        assertTrue(processor.process("ATTRACTION STATUS;A01").endsWith("Users present:" + nl + "- none"));
        assertTrue(processor.process("ATTRACTION STATUS;A99").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Mini;2").startsWith("OK"));
        processor.process("ATTRACTION ENTER;A02;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR Not enough places"));
        assertTrue(processor.process("HELP").contains("ATTRACTION STATUS;IdentificadorAtraccion"));
    }

    @Test
    void invoiceCommands() throws IOException {
        processor.process(ANA);
        processor.process(PEDRO);
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUP CREATE;G01;Pareja");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        List<String> stored = Files.readAllLines(dir.resolve("tickets.txt"));
        String nl = System.lineSeparator();
        assertEquals(String.join(nl, "OK INVOICE USER ana@correo.com", "Recipient: Ana Garcia Lopez (ana@correo.com)",
                "Individual tickets:", "- Ticket T001 2026-10-15 40.00", "Total: 40.00"),
                processor.process("INVOICE CREATE;USER;ana@correo.com"));
        assertEquals(String.join(nl, "OK INVOICE GROUP G01", "Recipient: Group G01 - Pareja", "Group tickets:",
                "- Group ticket TG01 2026-10-15 (2 members) 80.00", "Total: 80.00"),
                processor.process("INVOICE CREATE;group;G01"));
        assertEquals(String.join(nl, "OK INVOICE USER pedro@correo.com",
                "Recipient: Pedro Lopez Martin (pedro@correo.com)", "Total: 0.00"),
                processor.process("INVOICE CREATE;USER;pedro@correo.com"));
        assertTrue(processor.process("INVOICE CREATE;COMPANY;X").contains("USER or GROUP"));
        assertTrue(processor.process("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertEquals(stored, Files.readAllLines(dir.resolve("tickets.txt")));
        assertTrue(processor.process("HELP").contains("INVOICE CREATE;USER|GROUP;Correo|IdentificadorGrupo"));
    }

    @Test
    void unexpectedErrorsDoNotPropagate() {
        CommandProcessor broken = new CommandProcessor(null, null, null, null, null, new MainController(TestPark.CLOCK));
        assertTrue(broken.process(ANA).startsWith("ERROR Unexpected error"));
    }
}
