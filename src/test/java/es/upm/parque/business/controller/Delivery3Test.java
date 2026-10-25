package es.upm.parque.business.controller;

import es.upm.parque.App;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delivery 3 features: passes, typed attractions, SPEED/Special entry, invoices, cascades and persistence.
 */
class Delivery3Test {

    @TempDir
    Path dir;

    private CommandProcessor cp;
    private ParkController controller;

    private String run(String command) {
        return cp.process(command);
    }

    private void restart() throws Exception {
        controller = App.initialize(dir, new PasswordHasher(4));
        cp = new CommandProcessor(controller, App.createHelpGenerator());
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) controller.getTicketController().find(id);
    }

    private void ok(String command) {
        String reply = run(command);
        assertTrue(reply.startsWith("OK"), command + " -> " + reply);
    }

    private void error(String command) {
        String reply = run(command);
        assertTrue(reply.startsWith("ERROR"), command + " -> " + reply);
    }

    @BeforeEach
    void setUp() throws Exception {
        restart();
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        ok("USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave1!;600000000;1234567890123456");
        ok("SYSTEM DATE;2026-10-01");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;ana@correo.com");
        ok("GROUP ADD;G01;pedro@correo.com");
        ok("GROUP ADD;G01;luis@correo.com");
    }

    // ---- passes ----

    @Test
    void speedPassQuantitiesAndPrices() {
        assertEquals("OK 1 SPEEDPASS CREATED (T001-SP1) -> 8.00", run("SPEEDPASS CREATE;T001;1"));
        assertEquals("OK 10 SPEEDPASS CREATED (T001-SP2 .. T001-SP11) -> 80.00", run("SPEEDPASS CREATE;T001;10"));
        error("SPEEDPASS CREATE;T001;5");
        error("SPEEDPASS CREATE;T001;x");
        error("SPEEDPASS CREATE;T999;1");
        assertEquals(11, ticket("T001").getSpeedPasses().size());
        for (SpeedPass p : ticket("T001").getSpeedPasses()) {
            assertEquals(new BigDecimal("8.00"), p.getPrice());
            assertFalse(p.isConsumed());
        }
    }

    @Test
    void speedPassPriceFollowsStoredTicketPrice() {
        ok("SYSTEM PRICE;55.00");
        ok("TICKET CREATE;T002;pedro@correo.com;2026-10-15");
        ok("SYSTEM PRICE;10.00");
        assertEquals("OK 1 SPEEDPASS CREATED (T002-SP1) -> 11.00", run("SPEEDPASS CREATE;T002;1"));
        assertEquals("OK SPECIALPASS CREATED (T002-SPECIAL) -> 55.00", run("SPECIALPASS CREATE;T002"));
    }

    @Test
    void passesOnGroupTicketsOnlyForInternalTickets() {
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        error("SPEEDPASS CREATE;TG01;1");
        error("SPECIALPASS CREATE;TG01");
        ok("SPEEDPASS CREATE;TG01-1;1");
    }

    @Test
    void onlyOneSpecialPassEvenIfUsed() {
        ok("SPECIALPASS CREATE;T001");
        error("SPECIALPASS CREATE;T001");
        ticket("T001").getSpecialPass().consume();
        error("SPECIALPASS CREATE;T001");
    }

    @Test
    void refunds() {
        ok("SPEEDPASS CREATE;T001;10");
        ok("SPECIALPASS CREATE;T001");
        assertEquals("OK SPEEDPASS REFUNDED -> 8.00", run("SPEEDPASS DELETE;T001-SP3"));
        assertEquals(9, ticket("T001").getSpeedPasses().size());
        error("SPEEDPASS DELETE;T001-SP3");
        error("SPEEDPASS DELETE;T001-SPECIAL");
        ticket("T001").getSpeedPasses().get(0).consume();
        error("SPEEDPASS DELETE;T001-SP1");
        assertEquals("OK SPECIALPASS REFUNDED -> 40.00", run("SPECIALPASS DELETE;T001-SPECIAL"));
        assertNull(ticket("T001").getSpecialPass());
        ok("SYSTEM DATE;2026-10-15");
        error("SPEEDPASS DELETE;T001-SP2");
        assertEquals(9, ticket("T001").getSpeedPasses().size());
    }

    // ---- attraction types ----

    @Test
    void attractionTypesAdmission() throws Exception {
        assertEquals("OK ATTRACTION CREATED (GENERAL)", run("ATTRACTION CREATE;A01;Tornado;20"));
        ok("ATTRACTION CREATE;A02;Noria;20;INDIVIDUAL");
        ok("ATTRACTION CREATE;A03;Barcas;20;group");
        ok("ATTRACTION CREATE;A04;Castillo;20;SPECIAL");
        error("ATTRACTION CREATE;A05;X;20;VIP");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("TICKET CREATE;T002;pedro@correo.com;2026-10-15");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A03;T001");
        error("ATTRACTION ENTER;A02;TG01");
        error("ATTRACTION ENTER;A04;TG01");
        ok("ATTRACTION ENTER;A02;T001");
        ok("ATTRACTION ENTER;A03;TG01");
        ok("ATTRACTION ENTER;A01;T002");
        assertTrue(run("ATTRACTION STATUS;A03").contains("Type: GROUP"));
        restart();
        assertTrue(run("ATTRACTION STATUS;A02").contains("Type: INDIVIDUAL"));
        assertTrue(run("ATTRACTION STATUS;A04").contains("Type: SPECIAL"));
        assertTrue(run("ATTRACTION STATUS;A01").contains("Type: GENERAL"));
    }

    @Test
    void delivery2AttractionFileLoadsAsGeneral() throws Exception {
        Files.writeString(dir.resolve("attractions.txt"), "A09;Antigua;5\n");
        restart();
        assertTrue(run("ATTRACTION STATUS;A09").contains("Type: GENERAL"));
    }

    // ---- SPEED and SPECIAL entry ----

    @Test
    void individualSpeedEntryConsumesExactlyOne() throws Exception {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("SPEEDPASS CREATE;T001;1");
        ok("SPEEDPASS CREATE;T001;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A01;T001;FAST");
        assertEquals("OK ATTRACTION ENTERED (1 users) - SpeedPass consumed: T001-SP1",
                run("ATTRACTION ENTER;A01;T001;SPEED"));
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
        assertFalse(ticket("T001").getSpeedPasses().get(1).isConsumed());
        restart();
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
        assertEquals(2, ticket("T001").getSpeedPasses().size());
    }

    @Test
    void speedEntryWithoutPassesFails() {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A01;T001;SPEED");
        assertTrue(controller.getAttractionController().find("A01").getUsers().isEmpty());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        ok("ATTRACTION CREATE;A01;Barcas;20;GROUP");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SPEEDPASS CREATE;TG01-2;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A01;TG01;SPEED");
        assertTrue(controller.getAttractionController().find("A01").getUsers().isEmpty());
        assertFalse(ticket("TG01-1").getSpeedPasses().get(0).isConsumed());
        assertFalse(ticket("TG01-2").getSpeedPasses().get(0).isConsumed());
        ok("SYSTEM DATE;2026-10-01");
        ok("SPEEDPASS CREATE;TG01-3;1");
        ok("SYSTEM DATE;2026-10-15");
        assertEquals("OK ATTRACTION ENTERED (3 users) - SpeedPass consumed: TG01-1-SP1, TG01-2-SP1, TG01-3-SP1",
                run("ATTRACTION ENTER;A01;TG01;SPEED"));
    }

    @Test
    void failedEntryForCapacityConsumesNothing() {
        ok("ATTRACTION CREATE;A01;Mini;2");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        for (int i = 1; i <= 3; i++) {
            ok("SPEEDPASS CREATE;TG01-" + i + ";1");
        }
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A01;TG01;SPEED");
        for (int i = 1; i <= 3; i++) {
            assertFalse(ticket("TG01-" + i).getSpeedPasses().get(0).isConsumed());
        }
    }

    @Test
    void specialAttractionConsumesSpecialPass() throws Exception {
        ok("ATTRACTION CREATE;A04;Castillo;20;SPECIAL");
        ok("TICKET CREATE;T002;pedro@correo.com;2026-10-15");
        ok("SPECIALPASS CREATE;T001");
        ok("SPEEDPASS CREATE;T001;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A04;T002");
        error("ATTRACTION ENTER;A04;T001;SPEED");
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertFalse(ticket("T001").getSpeedPasses().get(0).isConsumed());
        assertEquals("OK ATTRACTION ENTERED (1 users) - Special Pass consumed: T001-SPECIAL",
                run("ATTRACTION ENTER;A04;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        ok("ATTRACTION EXIT;A04;T001");
        error("ATTRACTION ENTER;A04;T001");
        restart();
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        assertNotNull(controller.getTicketController().findPass("T001-SPECIAL"));
    }

    // ---- cascades ----

    @Test
    void ticketDeletionCascadesToPasses() throws Exception {
        ok("SPEEDPASS CREATE;T001;10");
        ok("SPECIALPASS CREATE;T001");
        ok("TICKET DELETE;T001");
        assertNull(controller.getTicketController().findPass("T001-SP1"));
        restart();
        assertNull(controller.getTicketController().find("T001"));
        assertNull(controller.getTicketController().findPass("T001-SPECIAL"));
        assertEquals("", Files.readString(dir.resolve("passes.txt")));
    }

    @Test
    void groupTicketDeletionCascadesAndFailedDeletionKeepsEverything() throws Exception {
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;10");
        ok("SPECIALPASS CREATE;TG01-2");
        ok("SYSTEM DATE;2026-10-15");
        error("GROUPTICKET DELETE;TG01");
        restart();
        assertNotNull(controller.getTicketController().find("TG01"));
        assertEquals(10, ticket("TG01-1").getSpeedPasses().size());
        assertNotNull(ticket("TG01-2").getSpecialPass());
        ok("SYSTEM DATE;2026-10-01");
        ok("GROUPTICKET DELETE;TG01");
        restart();
        assertNull(controller.getTicketController().find("TG01"));
        assertNull(controller.getTicketController().find("TG01-1"));
        assertNull(controller.getTicketController().findPass("TG01-2-SPECIAL"));
        assertNotNull(controller.getTicketController().find("T001"));
    }

    @Test
    void passesPersistWithPricesAndStatus() throws Exception {
        ok("SPEEDPASS CREATE;T001;1");
        ok("SPECIALPASS CREATE;T001");
        ticket("T001").getSpeedPasses().get(0).consume();
        controller.getTicketController().savePasses();
        restart();
        IndividualTicket t = ticket("T001");
        assertEquals(1, t.getSpeedPasses().size());
        assertEquals(new BigDecimal("8.00"), t.getSpeedPasses().get(0).getPrice());
        assertTrue(t.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("40.00"), t.getSpecialPass().getPrice());
        assertFalse(t.getSpecialPass().isConsumed());
    }

    // ---- invoices ----

    @Test
    void userInvoiceDoesNotCountInternalTicketsTwice() {
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Individual tickets:"), invoice);
        assertTrue(invoice.contains("Ticket T001 (2026-10-15): 40.00"), invoice);
        assertTrue(invoice.contains("Personal passes:"), invoice);
        assertTrue(invoice.contains("SpeedPass TG01-1-SP1 (ticket TG01-1): 8.00"), invoice);
        assertFalse(invoice.contains("Ticket TG01-1 ("), invoice);
        assertFalse(invoice.contains("Group ticket"), invoice);
        assertTrue(invoice.contains("TOTAL: 48.00"), invoice);
    }

    @Test
    void userInvoiceOmitsEmptySections() {
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Individual tickets:"));
        assertFalse(invoice.contains("Personal passes"));
        invoice = run("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("Individual tickets"));
        assertTrue(invoice.contains("TOTAL: 0.00"), invoice);
    }

    @Test
    void groupInvoice() {
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.contains("Group tickets:"), invoice);
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members): 108.00"), invoice);
        assertFalse(invoice.contains("SpeedPass"), invoice);
        assertFalse(invoice.contains("TG01-1"), invoice);
        assertTrue(invoice.contains("TOTAL: 108.00"), invoice);
    }

    @Test
    void invoiceErrors() {
        error("INVOICE CREATE;USER;nadie@correo.com");
        error("INVOICE CREATE;GROUP;G99");
        error("INVOICE CREATE;PARK;X");
    }

    @Test
    void invoicesAreNotPersisted() throws Exception {
        run("INVOICE CREATE;USER;ana@correo.com");
        try (var files = Files.list(dir)) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().toLowerCase().contains("invoice")));
        }
    }

    // ---- passwords through commands ----

    @Test
    void passwordChangeThroughCommands() throws Exception {
        ok("USER UPDATE;pedro@correo.com;Otra2#;PASSWORD;Nueva2#");
        restart();
        error("USER DELETE;pedro@correo.com;Otra2#");
        ok("USER DELETE;pedro@correo.com;Nueva2#");
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Clave1!"));
    }

    @Test
    void passOwnerLookup() {
        ok("SPEEDPASS CREATE;T001;1");
        Pass p = controller.getTicketController().findPass("T001-SP1");
        assertEquals("T001", controller.getTicketController().ownerOf(p).getId());
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket tg = (GroupTicket) controller.getTicketController().find("TG01");
        assertEquals(3, tg.getIndividualTickets().size());
    }
}
