package es.upm.parque;

import static es.upm.parque.ParkTestSupport.email;
import static es.upm.parque.ParkTestSupport.error;
import static es.upm.parque.ParkTestSupport.ok;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TicketManagementTest {

    @TempDir
    Path dir;

    private ParkController park;
    private User ana;

    @BeforeEach
    void setUp() throws IOException {
        park = ParkTestSupport.park(dir);
        ana = ParkTestSupport.user(park, 1);
    }

    @Test
    void buysTicketForExistingUserAtCurrentPrice() {
        assertEquals("OK TICKET CREATED -> 40.00", park.createTicket("T001", email(1), "2026-10-15"));
        IndividualTicket ticket = (IndividualTicket) park.getTicketManager().find("T001");
        assertSame(ana, ticket.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        ok(park.setTicketPrice("60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", park.createTicket("T002", email(1), "2026-10-15"));
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }

    @Test
    void rejectsInvalidPurchases() {
        ok(park.createTicket("T001", email(1), "2026-10-15"));
        error(park.createTicket("T002", "nadie@correo.com", "2026-10-15"));
        error(park.createTicket("T001", email(1), "2026-10-16"));
        error(park.createTicket("T003", email(1), "2026-09-30"));
        error(park.createTicket("T004", email(1), "15/10/2026"));
        error(park.createTicket("", email(1), "2026-10-15"));
        assertEquals(1, park.getTicketManager().getIndividualTickets().size());
    }

    @Test
    void deletesOnlyFutureTickets() {
        ok(park.createTicket("T001", email(1), "2026-10-15"));
        ok(park.createTicket("T002", email(1), "2026-10-15"));
        ok(park.deleteTicket("T001"));
        ok(park.setSystemDate("2026-10-15"));
        error(park.deleteTicket("T002"));
        error(park.deleteTicket("T999"));
        assertNotNull(park.getTicketManager().find("T002"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        ok(park.createTicket("T001", email(1), "2026-10-15"));
        error(park.deleteUser(email(1), "Clave1!"));
        ok(park.setSystemDate("2026-10-15"));
        error(park.deleteUser(email(1), "Clave1!"));
        ok(park.setSystemDate("2026-10-16"));
        ok(park.deleteUser(email(1), "Clave1!"));
        assertNull(park.getUserManager().findByEmail(email(1)));
        assertNull(park.getTicketManager().find("T001"));
    }

    @Test
    void deletedUserLeavesItsGroups() {
        ParkTestSupport.group(park, "G01", 2);
        ok(park.deleteUser(email(1), "Clave1!"));
        assertFalse(park.getGroupManager().find("G01").hasMember(ana));
        assertEquals(1, park.getGroupManager().find("G01").getMembers().size());
    }

    @Test
    void ticketsPersistIncludingPastOnes() throws IOException {
        ok(park.setTicketPrice("60.00"));
        ok(park.createTicket("T001", email(1), "2026-10-02"));
        ok(park.setSystemDate("2026-10-20"));

        ParkController restarted = ParkTestSupport.restart(dir);
        IndividualTicket ticket = (IndividualTicket) restarted.getTicketManager().find("T001");
        assertEquals(new BigDecimal("60.00"), ticket.getPrice());
        assertEquals(LocalDate.of(2026, 10, 2), ticket.getDate());
        assertSame(restarted.getUserManager().findByEmail(email(1)), ticket.getUser());
        assertEquals(new BigDecimal("40.00"), restarted.getTicketPrice());
        assertTrue(restarted.getTicketManager().hasCurrentOrFutureTicket(ticket.getUser(), ParkTestSupport.START));
    }
}
