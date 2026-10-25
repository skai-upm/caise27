package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, TODAY);
        t.run(ParkTestSupport.CREATE_ANA);
    }

    @Test
    void datePredicates() {
        Ticket ticket = new IndividualTicket("T1", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), null,
                "12345678Z", null);
        LocalDate day = LocalDate.of(2026, 10, 15);
        assertTrue(ticket.isValidOn(day));
        assertFalse(ticket.isValidOn(day.minusDays(1)));
        assertTrue(ticket.isCurrentOrFuture(day));
        assertTrue(ticket.isCurrentOrFuture(day.minusDays(1)));
        assertFalse(ticket.isCurrentOrFuture(day.plusDays(1)));
        assertTrue(ticket.isRefundable(day.minusDays(1)));
        assertFalse(ticket.isRefundable(day));
        assertTrue(ticket.getUsers().isEmpty());
    }

    @Test
    void createTicketForExistingUser() {
        assertEquals("OK TICKET CREATED -> 40.00", t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        IndividualTicket ticket = assertInstanceOf(IndividualTicket.class, t.park.getTickets().get("T001"));
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        assertSame(t.park.getUsers().findByEmail("ana@correo.com"), ticket.getUser());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }

    @Test
    void priceChangeAppliesToNewTickets() {
        assertEquals("OK TICKET CREATED -> 60.00",
                t.run("SYSTEM PRICE;60.00", "TICKET CREATE;T001;ana@correo.com;2026-10-15"));
    }

    @Test
    void createRejectsInvalidRequests() {
        t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(t.run("TICKET CREATE;T002;nadie@correo.com;2026-10-15").startsWith("ERROR"));
        assertTrue(t.run("TICKET CREATE;T001;ana@correo.com;2026-10-16").startsWith("ERROR"));
        assertTrue(t.run("TICKET CREATE;T003;ana@correo.com;2026-13-01").startsWith("ERROR"));
        assertTrue(t.run("TICKET CREATE;T004;ana@correo.com;2026-09-30").startsWith("ERROR"));
        assertTrue(t.run("TICKET CREATE;T 5;ana@correo.com;2026-10-15").startsWith("ERROR"));
        assertEquals(1, t.park.getTickets().getTickets().size());
        assertTrue(t.run("TICKET CREATE;T006;ana@correo.com;2026-10-01").startsWith("OK"));
    }

    @Test
    void deleteOnlyFutureTickets() {
        t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15", "SYSTEM DATE;2026-10-15");
        assertTrue(t.run("TICKET DELETE;T001").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-16");
        assertTrue(t.run("TICKET DELETE;T001").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-14");
        assertEquals("OK TICKET DELETED", t.run("TICKET DELETE;T001"));
        assertTrue(t.run("TICKET DELETE;T001").startsWith("ERROR"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(t.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-15");
        assertTrue(t.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", t.run("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(1, t.park.getTickets().getTickets().size());
    }

    @Test
    void wrongPasswordIsCheckedBeforeTickets() {
        t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(t.run("USER DELETE;ana@correo.com;Mala1!").contains("Incorrect password"));
    }

    @Test
    void ticketsPersistIncludingHistoryOfDeletedUsers() throws IOException {
        t.run("TICKET CREATE;T001;ana@correo.com;2026-10-15", "SYSTEM PRICE;55.00",
                "TICKET CREATE;T002;ana@correo.com;2026-10-20");
        assertEquals("T001;2026-10-15;12345678Z;40.00;" + System.lineSeparator()
                + "T002;2026-10-20;12345678Z;55.00;" + System.lineSeparator(),
                Files.readString(dir.resolve("tickets.txt")));

        ParkTestSupport restarted = t.restart(LocalDate.of(2026, 10, 21));
        IndividualTicket t1 = assertInstanceOf(IndividualTicket.class, restarted.park.getTickets().get("T001"));
        assertSame(restarted.park.getUsers().findByDni("12345678Z"), t1.getUser());
        assertEquals("OK USER DELETED", restarted.run("USER DELETE;ana@correo.com;Clave1!"));

        IndividualTicket history = assertInstanceOf(IndividualTicket.class,
                restarted.restart(TODAY).park.getTickets().get("T002"));
        assertNull(history.getUser());
        assertEquals("12345678Z", history.getOwnerDni());
        assertEquals(new BigDecimal("55.00"), history.getPrice());
    }

    @Test
    void unknownTicketIsReported() {
        assertThrows(IllegalArgumentException.class, () -> t.park.getTickets().get("NOPE"));
        assertTrue(t.run("TICKET DELETE;NOPE").startsWith("ERROR"));
    }
}
