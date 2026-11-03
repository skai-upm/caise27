package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketControllerTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;
    Path file;
    UserController users;
    TicketController tickets;
    User ana;

    @BeforeEach
    void setUp() {
        users = new UserController(new UserFileStorage(dir.resolve("users.txt")));
        ana = users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        file = dir.resolve("tickets.txt");
        tickets = new TicketController(new TicketFileStorage(file), new GroupTicketFileStorage(dir.resolve("grouptickets.txt")),
                users, new GroupController(new GroupFileStorage(dir.resolve("groups.txt")), users));
    }

    @Test
    void createsTicketForRegisteredUserWithCapturedPrice() throws IOException {
        Ticket t = tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15), TODAY, PRICE);
        assertSame(ana, t.getUser().orElseThrow());
        assertEquals(PRICE, t.getPrice());
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"), Files.readAllLines(file));
        tickets.create("T002", "ana@correo.com", TODAY, TODAY, new BigDecimal("60.00"));
        assertEquals(PRICE, tickets.find("T001").orElseThrow().getPrice());
    }

    @Test
    void rejectsInvalidPurchases() {
        tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15), TODAY, PRICE);
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 16), TODAY, PRICE));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T002", "nadie@correo.com", LocalDate.of(2026, 10, 16), TODAY, PRICE));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T003", "ana@correo.com", TODAY.minusDays(1), TODAY, PRICE));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create(" ", "ana@correo.com", TODAY, TODAY, PRICE));
        assertEquals(1, tickets.getTickets().size());
    }

    @Test
    void deletesOnlyTicketsDatedAfterToday() {
        tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15), TODAY, PRICE);
        LocalDate ticketDay = LocalDate.of(2026, 10, 15);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", ticketDay));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", ticketDay.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T999", TODAY));
        tickets.delete("T001", ticketDay.minusDays(1));
        assertTrue(tickets.getTickets().isEmpty());
    }

    @Test
    void activeTicketsAreThoseOnOrAfterToday() {
        LocalDate ticketDay = LocalDate.of(2026, 10, 15);
        tickets.create("T001", "ana@correo.com", ticketDay, TODAY, PRICE);
        assertTrue(tickets.hasActiveTickets(ana, TODAY));
        assertTrue(tickets.hasActiveTickets(ana, ticketDay));
        assertFalse(tickets.hasActiveTickets(ana, ticketDay.plusDays(1)));
    }

    @Test
    void failedSaveRevertsChanges() throws IOException {
        tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15), TODAY, PRICE);
        Files.delete(file);
        Files.createDirectory(file);
        assertThrows(RuntimeException.class,
                () -> tickets.create("T002", "ana@correo.com", TODAY, TODAY, PRICE));
        assertThrows(RuntimeException.class, () -> tickets.delete("T001", TODAY));
        assertEquals(List.of("T001"), tickets.getTickets().stream().map(Ticket::getId).toList());
    }
}
