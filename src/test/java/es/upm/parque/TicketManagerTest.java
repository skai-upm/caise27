package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    @TempDir
    Path dir;

    private TestPark park;
    private User ana;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        ana = park.users.findByEmail("ana@correo.com");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 1));
    }

    @Test
    void createChargesCurrentPriceAndRefersToUser() {
        Ticket t = park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertSame(ana, t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());

        park.controller.setTicketPrice(new BigDecimal("60.00"));
        assertEquals(new BigDecimal("60.00"), park.tickets.create("T002", "ana@correo.com", "2026-10-01").getPrice());
        assertEquals(new BigDecimal("40.00"), park.tickets.findById("T001").getPrice());
    }

    @Test
    void createRejections() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertThrows(ParkException.class, () -> park.tickets.create("T001", "ana@correo.com", "2026-10-20"));
        assertThrows(ParkException.class, () -> park.tickets.create("T002", "nadie@correo.com", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.create(" ", "ana@correo.com", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.create("T003", "ana@correo.com", "2026-09-30"));
        assertThrows(ParkException.class, () -> park.tickets.create("T004", "ana@correo.com", "15/10/2026"));
        assertEquals(1, park.tickets.getTickets().size());
    }

    @Test
    void deleteOnlyStrictlyFutureTickets() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
        assertNotNull(park.tickets.findById("T001"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 14));
        park.tickets.delete("T001");
        assertNull(park.tickets.findById("T001"));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
    }

    @Test
    void currentOrFutureTicketDetection() {
        assertFalse(park.tickets.hasCurrentOrFutureTicket(ana));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertTrue(park.tickets.hasCurrentOrFutureTicket(ana));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertTrue(park.tickets.hasCurrentOrFutureTicket(ana));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertFalse(park.tickets.hasCurrentOrFutureTicket(ana));
        assertNotNull(park.tickets.findById("T001"));
    }

    @Test
    void removeTicketsOfUser() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.removeTicketsOf(ana);
        park.tickets.removeTicketsOf(ana);
        assertTrue(new TestPark(dir).tickets.getTickets().isEmpty());
    }

    @Test
    void ticketsSurviveRestartWithTheirPrice() {
        park.controller.setTicketPrice(new BigDecimal("60.00"));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        TestPark restarted = new TestPark(dir);
        Ticket t = restarted.tickets.findById("T001");
        assertEquals(new BigDecimal("60.00"), t.getPrice());
        assertSame(restarted.users.findByEmail("ana@correo.com"), t.getUser());
        assertEquals(MainController.DEFAULT_TICKET_PRICE, restarted.controller.getTicketPrice());
    }

    @Test
    void saveFailureRestoresPersistedState() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        Path file = dir.resolve("tickets.txt");
        assertTrue(file.toFile().setWritable(false));
        try {
            assertThrows(ParkException.class, () -> park.tickets.create("T002", "ana@correo.com", "2026-10-15"));
            assertNull(park.tickets.findById("T002"));
        } finally {
            assertTrue(file.toFile().setWritable(true));
        }
    }
}
