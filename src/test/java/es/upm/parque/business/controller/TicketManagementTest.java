package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TicketManagementTest {

    @TempDir
    Path dir;

    private TestContext ctx;
    private User ana;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ana = ctx.user(1);
    }

    @Test
    void buysTicketForExistingUserAtCurrentPrice() {
        assertEquals("OK TICKET CREATED -> 40.00", ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        IndividualTicket ticket = (IndividualTicket) ctx.tickets().find("T001");
        assertSame(ana, ticket.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        ok(ctx.park().setTicketPrice("60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", ctx.tickets().createTicket("T002", email(1), "2026-10-15"));
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }

    @Test
    void rejectsInvalidPurchases() {
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        error(ctx.tickets().createTicket("T002", "nadie@correo.com", "2026-10-15"));
        error(ctx.tickets().createTicket("T001", email(1), "2026-10-16"));
        error(ctx.tickets().createTicket("T003", email(1), "2026-09-30"));
        error(ctx.tickets().createTicket("T004", email(1), "15/10/2026"));
        error(ctx.tickets().createTicket("", email(1), "2026-10-15"));
        assertEquals(1, ctx.tickets().getIndividualTickets().size());
    }

    @Test
    void deletesOnlyFutureTickets() {
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        ok(ctx.tickets().createTicket("T002", email(1), "2026-10-15"));
        ok(ctx.tickets().deleteTicket("T001"));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.tickets().deleteTicket("T002"));
        error(ctx.tickets().deleteTicket("T999"));
        assertNotNull(ctx.tickets().find("T002"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        error(ctx.users().deleteUser(email(1), "Clave1!"));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.users().deleteUser(email(1), "Clave1!"));
        ok(ctx.park().setSystemDate("2026-10-16"));
        ok(ctx.users().deleteUser(email(1), "Clave1!"));
        assertNull(ctx.users().findByEmail(email(1)));
        assertNull(ctx.tickets().find("T001"));
    }

    @Test
    void deletedUserLeavesItsGroups() {
        ctx.group("G01", 2);
        ok(ctx.users().deleteUser(email(1), "Clave1!"));
        assertFalse(ctx.groups().find("G01").hasMember(ana));
        assertEquals(1, ctx.groups().find("G01").getMembers().size());
    }

    @Test
    void ticketsPersistIncludingPastOnes() throws IOException {
        ok(ctx.park().setTicketPrice("60.00"));
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-02"));
        ok(ctx.park().setSystemDate("2026-10-20"));

        TestContext restarted = ctx.restart();
        IndividualTicket ticket = (IndividualTicket) restarted.tickets().find("T001");
        assertEquals(new BigDecimal("60.00"), ticket.getPrice());
        assertEquals(LocalDate.of(2026, 10, 2), ticket.getDate());
        assertSame(restarted.users().findByEmail(email(1)), ticket.getUser());
        assertEquals(new BigDecimal("40.00"), restarted.park().getTicketPrice());
        assertTrue(restarted.tickets().hasCurrentOrFutureTicket(ticket.getUser(), TestContext.START));
    }
}
