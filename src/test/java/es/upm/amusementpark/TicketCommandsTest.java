package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class TicketCommandsTest extends ParkTestSupport {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @Test
    void createsTicketWithExactOutput() {
        ok(ANA);
        expect("OK TICKET CREATED -> 40.00", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
        IndividualTicket ticket = assertInstanceOf(IndividualTicket.class, park.findTicket("T001"));
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        assertSame(park.getUserManager().findByEmail("ana@correo.com"), ticket.getUser());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }

    @Test
    void ticketForTodayIsAllowedButNotForThePast() {
        ok(ANA);
        expect("OK TICKET CREATED -> 40.00", "TICKET CREATE;T001;ana@correo.com;2026-10-01");
        error("TICKET CREATE;T002;ana@correo.com;2026-09-30");
        assertNull(park.findTicket("T002"));
    }

    @Test
    void rejectsUnknownUserDuplicateIdAndBadDate() {
        ok(ANA);
        error("TICKET CREATE;T001;nadie@correo.com;2026-10-15");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        error("TICKET CREATE;T001;ana@correo.com;2026-10-20");
        error("TICKET CREATE;T002;ana@correo.com;15-10-2026");
        error("TICKET CREATE;T002;ana@correo.com");
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void priceChangeIsNotRetroactive() throws IOException {
        ok(ANA);
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("SYSTEM PRICE;60.00");
        expect("OK TICKET CREATED -> 60.00", "TICKET CREATE;T002;ana@correo.com;2026-10-15");
        restart();
        assertEquals(new BigDecimal("40.00"), park.findTicket("T001").getPrice());
        assertEquals(new BigDecimal("60.00"), park.findTicket("T002").getPrice());
    }

    @Test
    void onlyStrictlyFutureTicketsCanBeDeleted() {
        ok(ANA);
        ok("TICKET CREATE;PAST;ana@correo.com;2026-10-14");
        ok("TICKET CREATE;TODAY;ana@correo.com;2026-10-15");
        ok("TICKET CREATE;FUTURE;ana@correo.com;2026-10-16");
        ok("SYSTEM DATE;2026-10-15");
        error("TICKET DELETE;PAST");
        error("TICKET DELETE;TODAY");
        expect("OK: ticket FUTURE deleted", "TICKET DELETE;FUTURE");
        error("TICKET DELETE;UNKNOWN");
        assertNotNull(park.findTicket("PAST"));
        assertNotNull(park.findTicket("TODAY"));
        assertNull(park.findTicket("FUTURE"));
    }

    @Test
    void ticketsSurviveRestart() throws IOException {
        ok(ANA);
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        restart();
        IndividualTicket ticket = assertInstanceOf(IndividualTicket.class, park.findTicket("T001"));
        assertSame(park.getUserManager().findByEmail("ana@correo.com"), ticket.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        assertEquals(List.of("I;T001;2026-10-15;40.00;12345678Z;Ana;Garcia Lopez;ana@correo.com"),
                Files.readAllLines(tempDir.resolve("tickets.txt")));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        ok(ANA);
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        error("USER DELETE;ana@correo.com;Clave1!");
        ok("SYSTEM DATE;2026-10-15");
        error("USER DELETE;ana@correo.com;Clave1!");
        assertNotNull(park.getUserManager().findByEmail("ana@correo.com"));
        assertNotNull(park.findTicket("T001"));
    }

    @Test
    void historicalTicketDoesNotBlockDeletionAndIsKept() throws IOException {
        ok(ANA);
        createUsers(1);
        createGroupWithUsers("G01", 1);
        ok("GROUP ADD;G01;ana@correo.com");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("SYSTEM DATE;2026-10-16");
        expect("OK: user ana@correo.com deleted", "USER DELETE;ana@correo.com;Clave1!");
        assertNull(park.getUserManager().findByEmail("ana@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());

        restart();
        IndividualTicket ticket = assertInstanceOf(IndividualTicket.class, park.findTicket("T001"));
        assertEquals("12345678Z", ticket.getUser().getDni());
        assertEquals("Ana", ticket.getUser().getName());
        assertEquals("ana@correo.com", ticket.getUser().getEmail());
        assertNull(park.getUserManager().findByEmail("ana@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void wrongCredentialsStillRejectDeletion() {
        ok(ANA);
        error("USER DELETE;ana@correo.com;Wrong1!");
        assertTrue(park.getUserManager().findByEmail("ana@correo.com") != null);
    }
}
