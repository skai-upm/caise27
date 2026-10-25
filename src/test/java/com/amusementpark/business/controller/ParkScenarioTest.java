package com.amusementpark.business.controller;

import com.amusementpark.TestSupport;
import com.amusementpark.business.model.Group;
import com.amusementpark.presentation.Bootstrap;
import com.amusementpark.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ParkScenarioTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private ParkController park;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private AttractionController attractions;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws Exception {
        start();
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
    }

    private void start() throws Exception {
        buffer = new ByteArrayOutputStream();
        processor = Bootstrap.initialize(dir, START, TestSupport.FAST_HASHER, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        park = ParkController.getInstance();
        users = UserController.getInstance();
        groups = GroupController.getInstance();
        tickets = TicketController.getInstance();
        attractions = AttractionController.getInstance();
    }

    /** Runs one command and returns its output (trimmed). */
    private String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private void assertOk(String command) {
        String out = run(command);
        assertTrue(out.startsWith("OK"), command + " -> " + out);
    }

    private void assertError(String command) {
        String out = run(command);
        assertTrue(out.startsWith("ERROR"), command + " -> " + out);
    }

    /** Registers users u0..u(n-1) if missing and puts them in the given group. */
    private void groupOf(String groupId, int members) {
        assertOk("GROUP CREATE;" + groupId + ";Grupo " + groupId);
        String[] dnis = {"00000000T", "00000001R", "00000002W", "00000003A", "00000004G", "00000005M",
            "00000006Y", "00000007F", "00000008P", "00000009D", "00000010X"};
        for (int i = 0; i < members; i++) {
            String email = "u" + i + "@correo.com";
            run("USER CREATE;U" + i + ";S;" + dnis[i] + ";" + email + ";Clave1!;600;1234567890123456");
            assertOk("GROUP ADD;" + groupId + ";" + email);
        }
    }

    // ---- System settings ----

    @Test
    public void systemDateCanMoveBothWaysAndRejectsBadValues() {
        assertEquals(START, park.getCurrentDate());
        assertOk("SYSTEM DATE;2026-09-15");
        assertEquals(LocalDate.of(2026, 9, 15), park.getCurrentDate());
        assertOk("SYSTEM DATE;2027-01-01");
        assertError("SYSTEM DATE;2026-9-15");
        assertError("SYSTEM DATE;15-09-2026");
        assertError("SYSTEM DATE;2026-02-30");
        assertEquals(LocalDate.of(2027, 1, 1), park.getCurrentDate());
    }

    @Test
    public void systemPriceAppliesToNewTickets() {
        assertEquals("OK TICKET CREATED -> 40.00", run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertOk("SYSTEM PRICE;60.00");
        assertEquals("OK TICKET CREATED -> 60.00", run("TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertError("SYSTEM PRICE;0");
        assertError("SYSTEM PRICE;-5");
        assertError("SYSTEM PRICE;abc");
        assertEquals(new BigDecimal("60.00"), park.getTicketPrice());
    }

    @Test
    public void dateAndPriceAreNotPersisted() throws Exception {
        run("SYSTEM DATE;2030-01-01");
        run("SYSTEM PRICE;55");
        start();
        assertEquals(START, park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getTicketPrice());
    }

    // ---- Groups ----

    @Test
    public void groupMembership() {
        assertOk("GROUP CREATE;G01;Familia Garcia");
        assertError("GROUP CREATE;G01;Otra");
        assertError("GROUP CREATE;G 02;Otra");
        assertError("GROUP CREATE;G02;");
        assertOk("GROUP ADD;G01;ana@correo.com");
        assertError("GROUP ADD;G01;ana@correo.com");
        assertError("GROUP ADD;G01;nadie@correo.com");
        assertError("GROUP ADD;G99;ana@correo.com");
        assertOk("GROUP ADD;G01;pedro@correo.com");
        assertOk("GROUP REMOVE;G01;pedro@correo.com");
        assertError("GROUP REMOVE;G01;pedro@correo.com");
        Group group = groups.getGroups().get(0);
        assertEquals(1, group.getMembers().size());
        assertSame(users.findByEmail("ana@correo.com"), group.getMembers().get(0));
        run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", group.getMembers().get(0).getName());
        assertOk("GROUP DELETE;G01");
        assertError("GROUP DELETE;G01");
    }

    @Test
    public void groupsRestoredAfterRestart() throws Exception {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        start();
        Group group = groups.getGroups().get(0);
        assertEquals("Familia Garcia", group.getName());
        assertSame(users.findByEmail("ana@correo.com"), group.getMembers().get(0));
    }

    // ---- Tickets ----

    @Test
    public void individualTicketRules() {
        assertError("TICKET CREATE;T001;nadie@correo.com;2026-10-15");
        assertError("TICKET CREATE;T001;ana@correo.com;2026-09-30");
        assertError("TICKET CREATE;T001;ana@correo.com;2026-15-01");
        assertOk("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        assertError("TICKET CREATE;T001;pedro@correo.com;2026-10-15");
        assertError("TICKET DELETE;T001");
        assertError("TICKET DELETE;T999");
        assertOk("TICKET CREATE;T002;ana@correo.com;2026-10-02");
        assertOk("TICKET DELETE;T002");
        assertEquals(1, tickets.getTickets().size());
    }

    @Test
    public void userDeletionLockedByCurrentOrFutureTickets() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertError("USER DELETE;ana@correo.com;Clave1!");
        run("SYSTEM DATE;2026-10-15");
        assertError("USER DELETE;ana@correo.com;Clave1!");
        run("SYSTEM DATE;2026-10-16");
        assertOk("USER DELETE;ana@correo.com;Clave1!");
        assertEquals(1, tickets.getTickets().size());
        assertEquals("12345678Z", tickets.getTickets().get(0).getUser().getDni());
    }

    @Test
    public void historicalTicketOfDeletedUserSurvivesRestart() throws Exception {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SYSTEM DATE;2026-10-16");
        assertOk("USER DELETE;ana@correo.com;Clave1!");
        start();
        assertEquals(1, tickets.getTickets().size());
        assertEquals("12345678Z", tickets.getTickets().get(0).getUser().getDni());
        assertOk("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
    }

    @Test
    public void deletedUserLeavesGroups() {
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        assertOk("USER DELETE;ana@correo.com;Clave1!");
        assertTrue(groups.getGroups().get(0).getMembers().isEmpty());
    }

    @Test
    public void groupTicketPricesAtBracketBoundaries() {
        int[] sizes = {2, 3, 5, 6, 10, 11};
        String[] prices = {"80.00", "108.00", "180.00", "192.00", "320.00", "220.00"};
        for (int i = 0; i < sizes.length; i++) {
            String groupId = "G" + sizes[i];
            groupOf(groupId, sizes[i]);
            assertEquals("OK GROUP TICKET CREATED -> " + prices[i],
                    run("GROUPTICKET CREATE;TG" + sizes[i] + ";" + groupId + ";2026-10-15"));
        }
    }

    @Test
    public void groupTicketRulesAndLocks() {
        assertOk("GROUP CREATE;G00;Vacio");
        assertError("GROUPTICKET CREATE;TG00;G00;2026-10-15");
        assertError("GROUPTICKET CREATE;TG01;G99;2026-10-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        assertError("GROUPTICKET CREATE;TG01;G01;2026-09-30");
        assertEquals("OK GROUP TICKET CREATED -> 80.00", run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertError("TICKET CREATE;TG01-1;ana@correo.com;2026-10-15");
        assertError("TICKET DELETE;TG01-1");

        assertError("GROUP REMOVE;G01;pedro@correo.com");
        assertError("GROUP DELETE;G01");
        assertError("USER DELETE;pedro@correo.com;Otra2#");
        run("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600;1234567890123456");
        assertError("GROUP ADD;G01;luis@correo.com");

        run("SYSTEM DATE;2026-10-16");
        assertOk("GROUP ADD;G01;luis@correo.com");
        assertEquals(2, tickets.getGroupTickets().get(0).getTickets().size());
        assertOk("GROUP REMOVE;G01;pedro@correo.com");
        assertOk("USER DELETE;pedro@correo.com;Otra2#");
        assertOk("GROUP DELETE;G01");
        assertEquals(1, tickets.getGroupTickets().size());
    }

    @Test
    public void groupTicketDeletion() throws Exception {
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("GROUPTICKET CREATE;TG02;G01;2026-10-01");
        assertError("GROUPTICKET DELETE;TG02");
        assertError("GROUPTICKET DELETE;TG99");
        assertOk("GROUPTICKET DELETE;TG01");
        assertEquals(null, tickets.findTicket("TG01-1"));
        start();
        assertEquals(1, tickets.getGroupTickets().size());
        assertEquals("TG02", tickets.getGroupTickets().get(0).getId());
    }

    @Test
    public void ticketsRestoredWithLocks() throws Exception {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        start();
        assertSame(users.findByEmail("ana@correo.com"), tickets.getTickets().get(0).getUser());
        assertSame(groups.getGroups().get(0), tickets.getGroupTickets().get(0).getGroup());
        assertError("USER DELETE;ana@correo.com;Clave1!");
        assertError("USER DELETE;pedro@correo.com;Otra2#");
        assertError("GROUP DELETE;G01");
    }

    // ---- Attractions ----

    @Test
    public void attractionLifecycle() throws Exception {
        assertOk("ATTRACTION CREATE;A01;Tornado;20");
        assertError("ATTRACTION CREATE;A01;Otra;5");
        assertError("ATTRACTION CREATE;A02;Noria;0");
        assertError("ATTRACTION CREATE;A02;Noria;diez");
        run("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600;1234567890123456");
        run("TICKET CREATE;T001;luis@correo.com;2026-10-01");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        run("TICKET CREATE;T002;ana@correo.com;2026-10-02");

        assertError("ATTRACTION ENTER;A01;T002");
        assertError("ATTRACTION ENTER;A99;T001");
        assertError("ATTRACTION ENTER;A01;T999");
        assertOk("ATTRACTION ENTER;A01;T001");
        assertError("ATTRACTION ENTER;A01;T001");
        assertOk("ATTRACTION ENTER;A01;TG01");
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01") && status.contains("Tornado"), status);
        assertTrue(status.contains("Capacity: 20") && status.contains("Present: 3")
                && status.contains("Available: 17"), status);
        assertTrue(status.contains("Luis") && status.contains("Ana") && status.contains("Pedro"), status);

        assertError("ATTRACTION DELETE;A01");
        assertOk("ATTRACTION EXIT;A01;T001");
        assertError("ATTRACTION EXIT;A01;T001");
        status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 2") && status.contains("Available: 18") && !status.contains("Luis"),
                status);

        run("SYSTEM DATE;2026-10-02");
        assertOk("ATTRACTION EXIT;A01;TG01");
        start();
        assertTrue(run("ATTRACTION STATUS;A01").contains("Present: 0"));
        assertOk("ATTRACTION DELETE;A01");
        assertError("ATTRACTION STATUS;A01");
    }

    @Test
    public void groupEntryRejectedWhenCapacityInsufficient() {
        run("ATTRACTION CREATE;A01;Tornado;4");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        run("ATTRACTION ENTER;A01;T001");
        groupOf("G4", 4);
        run("GROUPTICKET CREATE;TG01;G4;2026-10-01");
        assertError("ATTRACTION ENTER;A01;TG01");
        assertTrue(run("ATTRACTION STATUS;A01").contains("Present: 1"));
    }
}
