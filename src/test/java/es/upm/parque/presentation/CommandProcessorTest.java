package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.TextInvoiceBuilder;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.help.CommandCatalog;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final HelpGenerator HELP = new HelpGenerator(CommandCatalog.standard(),
            Map.of("", new SyntaxHelpFormatter(), "EXTENDED", new ExtendedHelpFormatter()));

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private final SystemDate systemDate = new SystemDate(LocalDate.of(2026, 9, 1));
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        processor = newProcessor();
    }

    private CommandProcessor newProcessor() throws IOException {
        UserController users = UserController.getInstance().configure(new UserRepository(dir.resolve("users.txt")));
        GroupController groups = GroupController.getInstance().configure(new GroupRepository(dir.resolve("groups.txt")), users);
        TicketController tickets = TicketController.getInstance().configure(
                new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")), users, groups, systemDate);
        AttractionController attractions = AttractionController.getInstance().configure(
                new AttractionRepository(dir.resolve("attractions.txt")), tickets, systemDate);
        return new CommandProcessor(users, groups, tickets, attractions,
                InvoiceController.getInstance().configure(users, groups, tickets, new TextInvoiceBuilder()), systemDate, HELP);
    }

    @Test
    void createDeleteUpdateFlow() {
        assertEquals("OK USER CREATED", processor.process(CREATE_ANA));
        assertTrue(processor.process(CREATE_ANA).startsWith("ERROR"));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana.nueva@correo.com;Clave1!"));
    }

    @Test
    void reportsInvalidData() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456")
                .startsWith("ERROR Invalid DNI"));
    }

    @Test
    void reportsWrongParameterCount() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;a;b;c").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void reportsUnknownCommand() {
        assertEquals("ERROR Unknown command: FOO BAR", processor.process("FOO BAR;1"));
        assertEquals("ERROR Unknown command: USER", processor.process("USER"));
    }

    @Test
    void helpListsEveryCommand() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void extendedHelpAndUnknownMode() {
        String extended = processor.process("HELP EXTENDED");
        assertTrue(extended.startsWith("Available commands (extended):"));
        assertTrue(extended.contains("USER DELETE;Correo;Contraseña" + System.lineSeparator() + "    Deletes the user"));
        assertTrue(processor.process("HELP").contains("HELP EXTENDED"));
        assertEquals("ERROR Unknown help mode: VERBOSE (available: HELP, HELP EXTENDED)", processor.process("HELP VERBOSE"));
        assertTrue(processor.process("HELP;x").startsWith("ERROR Wrong number of parameters. Usage: HELP"));
    }

    @Test
    void exitSetsExitFlag() {
        assertFalse(processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        assertEquals("OK SYSTEM DATE 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), systemDate.get());
        assertEquals("OK SYSTEM DATE 2026-01-01", processor.process("SYSTEM DATE;2026-01-01"));
        assertEquals(LocalDate.of(2026, 1, 1), systemDate.get());
    }

    @Test
    void invalidSystemDateIsRejected() {
        assertTrue(processor.process("SYSTEM DATE;15/09/2026").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-02-30").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE").startsWith("ERROR Wrong number of parameters"));
        assertEquals(LocalDate.of(2026, 9, 1), systemDate.get());
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void groupCommands() throws IOException {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;pedro@correo.com"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;pedro@correo.com"));
        assertTrue(processor.process("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP CREATE;G02").startsWith("ERROR Wrong number of parameters"));

        Group restored = newProcessorGroups().find("G01");
        assertEquals("Familia Garcia", restored.getName());
        assertEquals(1, restored.getMembers().size());
        assertEquals("12345678Z", restored.getMembers().get(0).getDni());

        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("HELP").contains("GROUP ADD;IdGrupo;Correo"));
    }

    @Test
    void deletedUserLeavesGroups() throws IOException {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(newProcessorGroups().find("G01").getMembers().isEmpty());
    }

    private GroupController newProcessorGroups() throws IOException {
        UserController users = UserController.getInstance().configure(new UserRepository(dir.resolve("users.txt")));
        return GroupController.getInstance().configure(new GroupRepository(dir.resolve("groups.txt")), users);
    }

    @Test
    void ticketCommandsAndDeletionRules() throws IOException {
        processor.process(CREATE_ANA);
        assertEquals("OK SYSTEM DATE 2026-10-01", processor.process("SYSTEM DATE;2026-10-01"));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-20").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T002;nadie@correo.com;2026-10-20").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T002;ana@correo.com;2026-09-30").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T002;ana@correo.com;2026/10/20").startsWith("ERROR"));
        assertEquals("OK SYSTEM PRICE 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertTrue(processor.process("SYSTEM PRICE;-5").startsWith("ERROR"));
        assertTrue(processor.process("SYSTEM PRICE;abc").startsWith("ERROR"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));

        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals("OK TICKET DELETED", processor.process("TICKET DELETE;T002"));
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("ERROR"));

        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));

        CommandProcessor restarted = newProcessor();
        assertEquals("OK USER CREATED", restarted.process(CREATE_ANA));
        assertEquals("OK TICKET CREATED -> 40.00", restarted.process("TICKET CREATE;T003;ana@correo.com;2026-10-20"));
        assertTrue(restarted.process("TICKET CREATE;T001;ana@correo.com;2026-10-20").startsWith("ERROR"),
                "historical ticket T001 must survive the restart");
        assertTrue(processor.process("HELP").contains("TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd"));
    }

    @Test
    void groupTicketCommandsAndLocking() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;00000000T;luis@correo.com;Clave1!;600000000;1111222233334444");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");

        assertEquals("OK GROUP TICKET CREATED -> 80.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-16").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;TG01-1").startsWith("ERROR"));

        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;luis@correo.com"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));

        processor.process("SYSTEM DATE;2026-10-17");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG02;G01;2026-10-20"));
        assertTrue(processor.process("GROUP REMOVE;G01;luis@correo.com").startsWith("ERROR"));
        assertEquals("OK GROUP TICKET DELETED", processor.process("GROUPTICKET DELETE;TG02"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;luis@correo.com"));
        assertTrue(processor.process("HELP").contains("GROUPTICKET CREATE;IdEntradaGrupo;IdGrupo;yyyy-MM-dd"));
    }

    @Test
    void attractionCommandsAndStatus() throws IOException {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;00000000T;luis@correo.com;Clave1!;600000000;1111222233334444");
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-16");

        assertEquals("OK ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Tornado;20"));
        assertTrue(processor.process("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Noria;0").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Noria;muchos").startsWith("ERROR"));
        assertEquals("OK ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A02;Mini;2"));

        assertEquals("OK ATTRACTION ENTERED -> 1 users", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK ATTRACTION ENTERED -> 2 users", processor.process("ATTRACTION ENTER;A01;TG01"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;T002").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;T999").startsWith("ERROR"));
        assertEquals("OK ATTRACTION ENTERED -> 1 users", processor.process("ATTRACTION ENTER;A02;T001"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("Present: 1"));

        assertEquals("OK ATTRACTION EXITED -> 1 users", processor.process("ATTRACTION EXIT;A01;T001"));
        assertEquals(String.join(System.lineSeparator(),
                "OK ATTRACTION STATUS A01", "Name: Tornado", "Capacity: 20", "Present: 2", "Available: 18", "Users:",
                "- Pedro Lopez Martin (87654321X)", "- Luis Ruiz (00000000T)"),
                processor.process("ATTRACTION STATUS;A01"));
        assertTrue(processor.process("ATTRACTION DELETE;A01").startsWith("ERROR"));
        assertEquals("OK ATTRACTION EXITED -> 2 users", processor.process("ATTRACTION EXIT;A01;TG01"));
        assertTrue(processor.process("ATTRACTION EXIT;A01;TG01").startsWith("ERROR"));

        CommandProcessor restarted = newProcessor();
        assertTrue(restarted.process("ATTRACTION STATUS;A02").contains("Present: 0"));
        assertEquals("OK ATTRACTION DELETED", restarted.process("ATTRACTION DELETE;A01"));
        assertTrue(restarted.process("ATTRACTION STATUS;A01").startsWith("ERROR"));
        assertTrue(processor.process("HELP").contains("ATTRACTION STATUS;IdAtraccion"));
    }

    @Test
    void invoiceCommands() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-16");

        assertEquals(String.join(System.lineSeparator(), "OK INVOICE", "Invoice: USER",
                "Recipient: Ana Garcia Lopez (ana@correo.com)", "Individual tickets:", "  - T001 2026-10-15: 40.00",
                "TOTAL: 40.00"), processor.process("INVOICE CREATE;USER;ana@correo.com"));
        assertEquals(String.join(System.lineSeparator(), "OK INVOICE", "Invoice: GROUP", "Recipient: Familia (G01)",
                "Group tickets:", "  - TG01 2026-10-16 2 members -0%: 80.00", "TOTAL: 80.00"),
                processor.process("INVOICE CREATE;GROUP;G01"));
        assertTrue(processor.process("INVOICE CREATE;USER;pedro@correo.com").contains("No billable items"));
        assertTrue(processor.process("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;PARK;X").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;USER").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("HELP").contains("INVOICE CREATE;USER|GROUP;Correo|IdGrupo"));
    }

    @Test
    void rejectsDniUpdate() {
        processor.process(CREATE_ANA);
        assertEquals("ERROR Invalid field: DNI", processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }
}
