package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    @TempDir
    Path dir;

    private CommandProcessor cp;
    private ParkController controller;

    private String run(String command) {
        return cp.process(command);
    }

    private void createUser(int n) {
        // Valid DNIs: number n with its check letter
        String number = String.format("%08d", n);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        assertEquals("OK USER CREATED", run("USER CREATE;U" + n + ";S;" + number + letter + ";u" + n
                + "@correo.com;Clave1!;600000000;1234567890123456"));
    }

    private void restart() throws Exception {
        controller = ParkController.load(dir);
        cp = new CommandProcessor(controller);
    }

    @BeforeEach
    void setUp() throws Exception {
        restart();
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        run("SYSTEM DATE;2026-10-01");
    }

    // ---- SYSTEM ----

    @Test
    void systemDateStartsWithOsDateAndCanMoveBothWays() throws Exception {
        assertEquals(LocalDate.now(), ParkController.load(dir.resolve("other")).getCurrentDate());
        assertEquals("OK SYSTEM DATE -> 2026-09-15", run("SYSTEM DATE;2026-09-15"));
        assertEquals("OK SYSTEM DATE -> 2026-01-01", run("SYSTEM DATE;2026-01-01"));
        assertEquals(LocalDate.of(2026, 1, 1), controller.getCurrentDate());
    }

    @Test
    void systemDateRejectsInvalid() {
        assertTrue(run("SYSTEM DATE;15/09/2026").startsWith("ERROR"));
        assertTrue(run("SYSTEM DATE;2026-02-30").startsWith("ERROR"));
        assertTrue(run("SYSTEM DATE;2026-9-5").startsWith("ERROR"));
        assertEquals(LocalDate.of(2026, 10, 1), controller.getCurrentDate());
    }

    @Test
    void systemPrice() {
        assertEquals("OK SYSTEM PRICE -> 60.00", run("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(run("SYSTEM PRICE;abc").startsWith("ERROR"));
        assertTrue(run("SYSTEM PRICE;-5").startsWith("ERROR"));
        assertEquals(new BigDecimal("60.00"), controller.getTicketPrice());
    }

    @Test
    void priceResetsTo40OnRestart() throws Exception {
        run("SYSTEM PRICE;60.00");
        restart();
        assertEquals(new BigDecimal("40.00"), controller.getTicketPrice());
    }

    // ---- GROUPS ----

    @Test
    void groupLifecycle() {
        assertEquals("OK GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(run("GROUP CREATE;G01;Otro").startsWith("ERROR"));
        assertEquals("OK USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        assertTrue(run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        Group g = controller.getGroupManager().find("G01");
        assertSame(controller.getUserManager().findByEmail("ana@correo.com"), g.getUsers().get(0));
        run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", g.getUsers().get(0).getName());
        assertTrue(run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertEquals("OK USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals("OK GROUP DELETED", run("GROUP DELETE;G01"));
        assertEquals(null, controller.getGroupManager().find("G01"));
        assertEquals(2, controller.getUserManager().getUsers().size());
    }

    @Test
    void groupsPersist() throws Exception {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        restart();
        Group g = controller.getGroupManager().find("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(2, g.getUsers().size());
        assertSame(controller.getUserManager().findByEmail("pedro@correo.com"), g.getUsers().get(1));
    }

    @Test
    void groupLockedByCurrentOrFutureGroupTicket() {
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;ana@correo.com");
        assertEquals("OK GROUP TICKET CREATED -> 40.00", run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertTrue(run("GROUP ADD;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP DELETE;G01").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-15");
        assertTrue(run("GROUP ADD;G01;pedro@correo.com").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER ADDED TO GROUP", run("GROUP ADD;G01;pedro@correo.com"));
        assertEquals("OK GROUP DELETED", run("GROUP DELETE;G01"));
    }

    // ---- TICKETS ----

    @Test
    void createIndividualTicket() {
        assertEquals("OK TICKET CREATED -> 40.00", run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        IndividualTicket t = (IndividualTicket) controller.getTicketManager().find("T001");
        assertSame(controller.getUserManager().findByEmail("ana@correo.com"), t.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertTrue(run("TICKET CREATE;T002;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T001;ana@correo.com;2026-10-20").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T003;ana@correo.com;2026-13-01").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T003;ana@correo.com;2026-09-30").startsWith("ERROR"));
    }

    @Test
    void deleteTicketOnlyInFuture() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SYSTEM DATE;2026-10-15");
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-01");
        assertEquals("OK TICKET DELETED", run("TICKET DELETE;T001"));
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
    }

    @Test
    void groupTicketDiscountBrackets() {
        int[] sizes = {2, 3, 5, 6, 10, 11};
        String[] totals = {"80.00", "108.00", "180.00", "192.00", "320.00", "220.00"};
        for (int n = 1; n <= 11; n++) {
            createUser(n);
        }
        for (int i = 0; i < sizes.length; i++) {
            String gid = "G" + sizes[i];
            run("GROUP CREATE;" + gid + ";Grupo");
            for (int n = 1; n <= sizes[i]; n++) {
                run("GROUP ADD;" + gid + ";u" + n + "@correo.com");
            }
            assertEquals("OK GROUP TICKET CREATED -> " + totals[i],
                    run("GROUPTICKET CREATE;TG" + sizes[i] + ";" + gid + ";2026-10-15"));
        }
    }

    @Test
    void groupTicketCompositionFrozenAndDeletion() {
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(run("GROUPTICKET CREATE;TG01;G99;2026-10-15").startsWith("ERROR"));
        run("GROUP CREATE;G02;Empty");
        assertTrue(run("GROUPTICKET CREATE;TG01;G02;2026-10-15").startsWith("ERROR"));
        run("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        run("SYSTEM DATE;2026-10-02");
        run("GROUP ADD;G01;pedro@correo.com");
        GroupTicket tg = (GroupTicket) controller.getTicketManager().find("TG01");
        assertEquals(1, tg.getTickets().size());
        assertTrue(run("TICKET DELETE;TG01-1").startsWith("ERROR"));
        assertTrue(run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertEquals("OK GROUP TICKET CREATED -> 80.00", run("GROUPTICKET CREATE;TG02;G01;2026-10-20"));
        assertEquals("OK GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG02"));
        assertEquals(null, controller.getTicketManager().find("TG02-1"));
    }

    @Test
    void ticketIdsAreGloballyUnique() {
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;ana@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(run("GROUPTICKET CREATE;T001;G01;2026-10-15").startsWith("ERROR"));
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(run("TICKET CREATE;TG01-1;pedro@correo.com;2026-10-15").startsWith("ERROR"));
    }

    @Test
    void userDeletionBlockedByActiveTickets() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-15");
        assertTrue(run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", run("USER DELETE;ana@correo.com;Clave1!"));
        assertNotNull(controller.getTicketManager().find("T001"));
    }

    @Test
    void userDeletionBlockedByGroupTicketAndRemovesMembership() {
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(run("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", run("USER DELETE;pedro@correo.com;Otra2#"));
        assertEquals(1, controller.getGroupManager().find("G01").getUsers().size());
    }

    @Test
    void ticketsPersistIncludingHistoryOfDeletedUsersAndGroups() throws Exception {
        run("SYSTEM PRICE;50");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SYSTEM DATE;2026-10-16");
        run("USER DELETE;pedro@correo.com;Otra2#");
        run("GROUP DELETE;G01");
        restart();
        TicketManager tm = controller.getTicketManager();
        IndividualTicket t1 = (IndividualTicket) tm.find("T001");
        assertEquals(new BigDecimal("50.00"), t1.getPrice());
        assertSame(controller.getUserManager().findByEmail("ana@correo.com"), t1.getUser());
        GroupTicket tg = (GroupTicket) tm.find("TG01");
        assertEquals(new BigDecimal("100.00"), tg.getPrice());
        assertEquals("G01", tg.getGroup().getId());
        assertEquals(2, tg.getTickets().size());
        assertEquals("87654321X", tg.getTickets().get(1).getUser().getDni());
        assertSame(tg, ((IndividualTicket) tm.find("TG01-2")).getGroupTicket());
    }

    // ---- ATTRACTIONS ----

    @Test
    void attractionCreateAndDelete() throws Exception {
        assertEquals("OK ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Tornado;20"));
        assertTrue(run("ATTRACTION CREATE;A01;Tornado;20").startsWith("ERROR"));
        assertTrue(run("ATTRACTION CREATE;A02;Noria;0").startsWith("ERROR"));
        assertTrue(run("ATTRACTION CREATE;A02;Noria;x").startsWith("ERROR"));
        restart();
        assertEquals(20, controller.getAttractionManager().find("A01").getCapacity());
        assertEquals("OK ATTRACTION DELETED", run("ATTRACTION DELETE;A01"));
        assertTrue(run("ATTRACTION DELETE;A01").startsWith("ERROR"));
    }

    @Test
    void enterExitAndStatus() throws Exception {
        run("ATTRACTION CREATE;A01;Tornado;20");
        createUser(1);
        createUser(2);
        run("GROUP CREATE;G01;F");
        run("GROUP ADD;G01;u1@correo.com");
        run("GROUP ADD;G01;u2@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        run("SYSTEM DATE;2026-10-15");
        assertTrue(run("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 3"), status);
        assertTrue(status.contains("Available: 17"), status);
        assertTrue(status.contains("ana@correo.com") && status.contains("u2@correo.com"), status);
        run("SYSTEM DATE;2026-10-20");
        assertTrue(run("ATTRACTION EXIT;A01;T001").startsWith("OK"));
        assertTrue(run("ATTRACTION EXIT;A01;T001").startsWith("ERROR"));
        status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Name: Tornado"), status);
        assertTrue(status.contains("Capacity: 20"), status);
        assertTrue(status.contains("Present: 2"), status);
        assertTrue(status.contains("Available: 18"), status);
        assertFalse(status.contains("ana@correo.com"), status);
        assertTrue(run("ATTRACTION DELETE;A01").startsWith("ERROR"));
        restart();
        assertTrue(run("ATTRACTION STATUS;A01").contains("Present: 0"));
    }

    @Test
    void groupEntryIsAtomic() {
        run("ATTRACTION CREATE;A01;Tornado;4");
        for (int n = 1; n <= 4; n++) {
            createUser(n);
        }
        run("GROUP CREATE;G01;F");
        for (int n = 1; n <= 4; n++) {
            run("GROUP ADD;G01;u" + n + "@correo.com");
        }
        run("SYSTEM DATE;2026-10-15");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("ATTRACTION ENTER;A01;T001");
        assertTrue(run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertEquals(1, controller.getAttractionManager().find("A01").getUsers().size());
        assertEquals(3, controller.getAttractionManager().find("A01").getAvailable());
    }
}
