package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketTest extends ParkTestSupport {

    @BeforeEach
    void users() {
        run(ANA);
        run(PEDRO);
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) park.getTickets().find(id);
    }

    @Test
    void createsTicketWithExactOutput() {
        assertEquals("OK TICKET CREATED -> 40.00", run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        IndividualTicket t = ticket("T001");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertSame(park.getUsers().findByEmail("ana@correo.com"), t.getUser());
    }

    @Test
    void ticketForTodayIsAllowed() {
        assertTrue(ok(run("TICKET CREATE;T001;ana@correo.com;2026-10-01")));
    }

    @Test
    void rejectsUnknownUserDuplicateIdPastOrInvalidDate() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(error(run("TICKET CREATE;T002;nadie@correo.com;2026-10-15")));
        assertTrue(error(run("TICKET CREATE;T001;pedro@correo.com;2026-10-15")));
        assertTrue(error(run("TICKET CREATE;T003;ana@correo.com;2026-09-30")));
        assertTrue(error(run("TICKET CREATE;T004;ana@correo.com;2026-13-01")));
        assertEquals(1, park.getTickets().getIndividualTickets().size());
    }

    @Test
    void priceChangeIsNotRetroactive() throws IOException {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SYSTEM PRICE;60.00");
        assertEquals("OK TICKET CREATED -> 60.00", run("TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertEquals(new BigDecimal("40.00"), ticket("T001").getPrice());
        restart();
        assertEquals(new BigDecimal("40.00"), ticket("T001").getPrice());
        assertEquals(new BigDecimal("60.00"), ticket("T002").getPrice());
    }

    @Test
    void onlyStrictlyFutureTicketsCanBeDeleted() {
        run("TICKET CREATE;PAST;ana@correo.com;2026-10-14");
        run("TICKET CREATE;TODAY;ana@correo.com;2026-10-15");
        run("TICKET CREATE;FUTURE;ana@correo.com;2026-10-16");
        run("SYSTEM DATE;2026-10-15");

        assertTrue(error(run("TICKET DELETE;PAST")));
        assertTrue(error(run("TICKET DELETE;TODAY")));
        assertTrue(ok(run("TICKET DELETE;FUTURE")));
        assertTrue(error(run("TICKET DELETE;NOPE")));
        assertNotNull(ticket("PAST"));
        assertNotNull(ticket("TODAY"));
        assertNull(ticket("FUTURE"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(error(run("USER DELETE;ana@correo.com;Clave1!")));
        run("SYSTEM DATE;2026-10-15");
        assertTrue(error(run("USER DELETE;ana@correo.com;Clave1!")));
        assertNotNull(park.getUsers().findByEmail("ana@correo.com"));
        assertNotNull(ticket("T001"));
    }

    @Test
    void historicalTicketDoesNotBlockDeletionAndSurvivesRestart() throws IOException {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SYSTEM DATE;2026-10-16");
        assertTrue(ok(run("USER DELETE;ana@correo.com;Clave1!")));
        assertNotNull(ticket("T001"));

        restart();

        assertNull(park.getUsers().findByEmail("ana@correo.com"));
        User holder = ticket("T001").getUser();
        assertEquals("12345678Z", holder.getDni());
        assertEquals("ana@correo.com", holder.getEmail());
        assertEquals("Ana", holder.getName());
        assertEquals("Garcia Lopez", holder.getSurname());
    }

    @Test
    void ticketsAreRestoredWithTheirUser() throws IOException {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        restart();
        IndividualTicket t = ticket("T001");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertSame(park.getUsers().findByEmail("ana@correo.com"), t.getUser());
        assertTrue(error(run("USER DELETE;ana@correo.com;Clave1!")));
    }
}
