package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketControllerTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, ParkTestSupport.PEDRO, "SYSTEM DATE;2026-10-01");
    }

    private Ticket ticket(String id) {
        return s.park().getTicketController().findById(id);
    }

    @Test
    void createReturnsExactText() {
        assertEquals("OK TICKET CREATED -> 40.00", s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        IndividualTicket t = (IndividualTicket) ticket("T001");
        assertSame(s.park().getUserController().findByEmail("ana@correo.com"), t.getUser());
        assertEquals("2026-10-15", t.getDate().toString());
    }

    @Test
    void ticketForTodayIsAllowed() {
        assertEquals("OK TICKET CREATED -> 40.00", s.run("TICKET CREATE;T001;ana@correo.com;2026-10-01"));
    }

    @Test
    void priceFixedAtPurchase() {
        s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("OK TICKET CREATED -> 60.00", s.run("SYSTEM PRICE;60.00", "TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertEquals(new BigDecimal("40.00"), ticket("T001").getPrice());
    }

    @Test
    void rejectedPurchases() {
        s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(s.run("TICKET CREATE;T001;pedro@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(s.run("TICKET CREATE;T002;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(s.run("TICKET CREATE;T003;ana@correo.com;2026-15-10").startsWith("ERROR"));
        assertTrue(s.run("TICKET CREATE;T004;ana@correo.com;2026-09-30").startsWith("ERROR"));
        assertTrue(s.run("TICKET CREATE;;ana@correo.com;2026-10-15").startsWith("ERROR"));
        assertEquals(1, s.park().getTicketController().getTickets().size());
    }

    @Test
    void onlyFutureTicketsCanBeDeleted() {
        s.run("TICKET CREATE;T1;ana@correo.com;2026-10-09", "TICKET CREATE;T2;ana@correo.com;2026-10-10",
                "TICKET CREATE;T3;ana@correo.com;2026-10-11", "SYSTEM DATE;2026-10-10");
        assertTrue(s.run("TICKET DELETE;T1").startsWith("ERROR"));
        assertTrue(s.run("TICKET DELETE;T2").startsWith("ERROR"));
        assertTrue(s.run("TICKET DELETE;T3").startsWith("OK"));
        assertTrue(s.run("TICKET DELETE;T9").startsWith("ERROR"));
        assertNotNull(ticket("T1"));
        assertNotNull(ticket("T2"));
        assertNull(ticket("T3"));
    }

    @Test
    void ticketsSurviveRestart() throws IOException {
        s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        s.restart();
        IndividualTicket t = (IndividualTicket) ticket("T001");
        assertSame(s.park().getUserController().findByEmail("ana@correo.com"), t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
    }

    @Test
    void activeTicketBlocksUserDeletion() {
        s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(s.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-15");
        assertTrue(s.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertNotNull(s.park().getUserController().findByEmail("ana@correo.com"));
        assertNotNull(ticket("T001"));
    }

    @Test
    void historicalTicketDoesNotBlockDeletionAndIsKept() throws IOException {
        s.run("TICKET CREATE;T001;ana@correo.com;2026-10-15", "SYSTEM DATE;2026-10-16");
        assertTrue(s.run("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertNull(s.park().getUserController().findByEmail("ana@correo.com"));
        assertNotNull(ticket("T001"));
        s.restart();
        IndividualTicket t = (IndividualTicket) ticket("T001");
        assertEquals("12345678Z", t.getUser().getDni());
        assertEquals("ana@correo.com", t.getUser().getEmail());
        assertNull(s.park().getUserController().findByEmail("ana@correo.com"));
    }
}
