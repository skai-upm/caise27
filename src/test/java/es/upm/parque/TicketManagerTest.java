package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    @TempDir
    Path dir;

    private UserManager users;
    private SystemDate today;
    private TicketManager tickets;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        users.create(UserManagerTest.ana());
        today = new SystemDate(LocalDate.of(2026, 10, 1));
        tickets = new TicketManager(new TicketRepository(dir.resolve("tickets.txt")), users, today);
    }

    @Test
    void createsTicketAtCurrentPrice() {
        assertEquals("OK TICKET CREATED -> 40.00", tickets.create("T001", "ana@correo.com", "2026-10-15"));
        Ticket t001 = tickets.find("T001");
        assertSame(users.findByEmail("ana@correo.com"), t001.getHolder());
        assertEquals(LocalDate.of(2026, 10, 15), t001.getDate());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        assertEquals("OK TICKET CREATED -> 40.00", tickets.create("T002", "ana@correo.com", "2026-10-01"));
    }

    @Test
    void rejectsInvalidPurchases() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(UserManager.EMPTY_FIELD, tickets.create(" ", "ana@correo.com", "2026-10-15"));
        assertEquals(TicketManager.DUPLICATE_TICKET, tickets.create("T001", "ana@correo.com", "2026-10-16"));
        assertEquals(GroupManager.USER_NOT_FOUND, tickets.create("T002", "nadie@correo.com", "2026-10-15"));
        assertEquals(TicketManager.INVALID_DATE, tickets.create("T002", "ana@correo.com", "15/10/2026"));
        assertEquals(TicketManager.PAST_DATE, tickets.create("T002", "ana@correo.com", "2026-09-30"));
        assertNull(tickets.find("T002"));
    }

    @Test
    void priceChangesOnlyAffectNewTickets() {
        assertEquals(TicketManager.INITIAL_PRICE, tickets.getPrice());
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals("OK SYSTEM PRICE -> 60.00", tickets.setPrice("60"));
        assertEquals("OK TICKET CREATED -> 60.00", tickets.create("T002", "ana@correo.com", "2026-10-15"));
        assertEquals(new BigDecimal("40.00"), tickets.find("T001").getPrice());
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("gratis"));
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("0"));
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("-5"));
        assertEquals(new BigDecimal("60.00"), tickets.getPrice());
    }

    @Test
    void refundsOnlyFutureTickets() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(TicketManager.TICKET_NOT_FOUND, tickets.delete("T999"));
        today.set(LocalDate.of(2026, 10, 15));
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.delete("T001"));
        today.set(LocalDate.of(2026, 10, 16));
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.delete("T001"));
        assertNotNull(tickets.find("T001"));
        today.set(LocalDate.of(2026, 10, 14));
        assertEquals(TicketManager.TICKET_DELETED, tickets.delete("T001"));
        assertNull(tickets.find("T001"));
    }

    @Test
    void activeTicketsDependOnSystemDate() {
        User ana = users.findByEmail("ana@correo.com");
        assertFalse(tickets.hasActiveTickets(ana));
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertTrue(tickets.hasActiveTickets(ana));
        today.set(LocalDate.of(2026, 10, 15));
        assertTrue(tickets.hasActiveTickets(ana));
        today.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.hasActiveTickets(ana));
    }

    @Test
    void ticketsSurviveRestart() throws IOException {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        UserManager restartedUsers = new UserManager(new UserRepository(dir.resolve("users.txt")));
        TicketManager restarted = new TicketManager(new TicketRepository(dir.resolve("tickets.txt")), restartedUsers,
                new SystemDate(LocalDate.of(2026, 11, 1)));
        Ticket t001 = restarted.find("T001");
        assertSame(restartedUsers.findByEmail("ana@correo.com"), t001.getHolder());
        assertEquals(TicketManager.INITIAL_PRICE, restarted.getPrice());
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = Files.createFile(dir.resolve("blocked"));
        TicketManager broken = new TicketManager(new TicketRepository(blocked.resolve("t.txt")), users, today);
        assertEquals(UserManager.STORAGE_FAILURE, broken.create("T001", "ana@correo.com", "2026-10-15"));
    }
}
