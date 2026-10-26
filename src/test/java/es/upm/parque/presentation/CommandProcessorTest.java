package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private UserController manager;
    private GroupController groups;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = App.initialize(dir);
        manager = UserController.getInstance();
        groups = GroupController.getInstance();
    }

    @Test
    void createUpdateDelete() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByDni("12345678Z").getName());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com")
                .startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666")
                .startsWith("OK"));
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Nueva2#").startsWith("OK"));
        assertEquals(0, manager.getUsers().size());
    }

    @Test
    void invalidDniIsReported() {
        String out = processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(out.startsWith("ERROR") && out.contains("DNI"), out);
    }

    @Test
    void unknownCommandsAndWrongParameterCounts() {
        assertTrue(processor.process("USER FLY;x").startsWith("ERROR"));
        assertTrue(processor.process("HELLO").startsWith("ERROR"));
        assertTrue(processor.process("").startsWith("ERROR"));
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;3").startsWith("ERROR"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        for (String syntax : new String[] {"USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT",
                "SYSTEM DATE;yyyy-MM-dd", "GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo",
                "GROUP DELETE;Id", "TICKET CREATE;Id;Correo;yyyy-MM-dd", "TICKET DELETE;Id", "SYSTEM PRICE;Importe",
                "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;Id",
                "ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION DELETE;Id", "ATTRACTION ENTER;IdAtraccion;IdEntrada",
                "ATTRACTION EXIT;IdAtraccion;IdEntrada", "ATTRACTION STATUS;IdAtraccion", "INVOICE CREATE;USER;Correo",
                "INVOICE CREATE;GROUP;IdGrupo"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void exitSetsFlagAndRejectsParameters() {
        assertTrue(processor.process("HELP;x").startsWith("ERROR"));
        assertTrue(processor.process("EXIT;now").startsWith("ERROR"));
        assertTrue(!processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateCanBeSetBackwardAndForward() {
        assertEquals("OK SYSTEM DATE -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(java.time.LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
        assertTrue(processor.process("SYSTEM DATE;2025-01-01").startsWith("OK"));
        assertEquals(java.time.LocalDate.of(2025, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void invalidSystemDatesAreRejected() {
        processor.process("SYSTEM DATE;2026-09-15");
        for (String bad : new String[] {"SYSTEM DATE;15/09/2026", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;2026-02-30",
                "SYSTEM DATE;", "SYSTEM DATE", "SYSTEM DATE;2026-09-15;x"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(java.time.LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
    }

    @Test
    void groupCommands() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP JOIN;G01").startsWith("ERROR"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertEquals(0, groups.findById("G01").getMembers().size());
        GroupController reloaded = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")),
                UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")), new BCryptPasswordEncoder(4)));
        assertEquals(0, reloaded.findById("G01").getMembers().size());
    }

    @Test
    void ticketCommandsFollowTheSystemDateAndPrice() {
        processor.process(CREATE_ANA);
        processor.process("SYSTEM DATE;2026-10-01");
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;ana@correo.com;2026-13-15").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T009;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK SYSTEM PRICE -> 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertTrue(processor.process("SYSTEM PRICE;abc").startsWith("ERROR"));
        assertTrue(processor.process("SYSTEM PRICE;-5").startsWith("ERROR"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));

        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("OK"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void groupTicketCommands() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        assertEquals("OK GROUP TICKET CREATED -> 108.00",
                processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG02;G99;2026-10-15").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG02;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;TG01-1").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("OK"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
    }

    @Test
    void attractionStatusReflectsMixedEntriesAndExits() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Otra;cero").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Otra;0").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION DELETE;A01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION EXIT;A01;T001").startsWith("OK"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01") && status.contains("Tornado") && status.contains("Capacity: 20")
                && status.contains("Present: 2") && status.contains("Available: 18") && status.contains("Pedro")
                && status.contains("Luis") && !status.contains("Ana"), status);
        assertTrue(processor.process("ATTRACTION STATUS").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T999").startsWith("ERROR"));
    }

    @Test
    void invoiceCommands() throws Exception {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        String userInvoice = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(userInvoice.startsWith("OK INVOICE CREATED") && userInvoice.contains("T001")
                && !userInvoice.contains("TG01") && userInvoice.contains("TOTAL: 40.00"), userInvoice);
        String groupInvoice = processor.process("INVOICE CREATE;GROUP;G01");
        assertTrue(groupInvoice.contains("TG01") && !groupInvoice.contains("TG01-1")
                && groupInvoice.contains("TOTAL: 80.00"), groupInvoice);
        String empty = processor.process("INVOICE CREATE;USER;pedro@correo.com");
        assertTrue(empty.contains("TOTAL: 0.00") && !empty.contains("tickets:"), empty);
        assertTrue(processor.process("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;SHOP;x").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;USER").startsWith("ERROR"));
        try (var files = java.nio.file.Files.list(dir)) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().contains("invoice")));
        }
    }

    @Test
    void extendedHelpAddsDescriptionsAndCoversEveryCommandFamily() {
        String help = processor.process("HELP");
        String extended = processor.process("HELP EXTENDED");
        assertTrue(help.contains("HELP EXTENDED") && !help.contains("after checking the password"), help);
        assertTrue(extended.contains("USER DELETE;Correo;Contraseña") && extended.contains("after checking the password"),
                extended);
        for (String family : new String[] {"USER ", "GROUP ", "TICKET ", "GROUPTICKET ", "ATTRACTION ", "INVOICE ",
                "SYSTEM DATE", "SYSTEM PRICE", "HELP", "EXIT"}) {
            assertTrue(help.contains(family) && extended.contains(family), family);
        }
        assertTrue(processor.process("HELP MARKDOWN").startsWith("ERROR"));
        assertTrue(processor.process("HELP EXTENDED;x").startsWith("ERROR"));
    }

    @Test
    void passCommands() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2026-10-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 10 x 8.00 = 80.00 (T001-SP1 .. T001-SP10)",
                processor.process("SPEEDPASS CREATE;T001;10"));
        assertEquals("OK SPEEDPASS CREATED -> 1 x 8.00 = 8.00 (T001-SP11)", processor.process("SPEEDPASS CREATE;T001;1"));
        assertTrue(processor.process("SPEEDPASS CREATE;T001;5").startsWith("ERROR"));
        assertTrue(processor.process("SPEEDPASS CREATE;TG01;1").startsWith("ERROR"));
        assertTrue(processor.process("SPEEDPASS CREATE;T999;1").startsWith("ERROR"));
        assertEquals("OK SPEEDPASS CREATED -> 1 x 8.00 = 8.00 (TG01-1-SP1)",
                processor.process("SPEEDPASS CREATE;TG01-1;1"));
        assertEquals("OK SPECIALPASS CREATED -> T001-SPECIAL -> 40.00", processor.process("SPECIALPASS CREATE;T001"));
        assertTrue(processor.process("SPECIALPASS CREATE;T001").startsWith("ERROR"));
        assertEquals("OK SPEEDPASS REFUNDED -> T001-SP3 -> 8.00", processor.process("SPEEDPASS DELETE;T001-SP3"));
        assertTrue(processor.process("SPEEDPASS DELETE;T001-SP3").startsWith("ERROR"));
        assertTrue(processor.process("SPECIALPASS DELETE;T001-SP4").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        String invoice = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("TG01-1-SP1") && invoice.contains("T001-SPECIAL") && !invoice.contains("TG01-1 ")
                && invoice.contains("TOTAL: 168.00"), invoice);
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("SPECIALPASS DELETE;T001-SPECIAL").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2026-10-16");
        assertTrue(processor.process("SPEEDPASS CREATE;T001;1").startsWith("ERROR"));
    }

    @Test
    void typedAttractionCommands() {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("GROUP CREATE;G01;Amigos");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2026-10-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20").contains("GENERAL"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Familiar;30;group").contains("GROUP"));
        assertTrue(processor.process("ATTRACTION CREATE;A03;Solo;30;INDIVIDUAL").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A04;Magia;30;SPECIAL").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION CREATE;A05;X;30;VIP").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A03;TG01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A03;T001").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("Type: GROUP"));
    }
}
