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

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private UserManager users;
    private TicketManager tickets;
    private User ana;

    @BeforeEach
    void setUp() {
        users = new UserManager(new UserStorage(dir.resolve("users.txt")));
        ana = users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
        tickets = new TicketManager(users, new TicketStorage(dir.resolve("tickets.txt")));
    }

    @Test
    void createStoresTicketWithUserReference() {
        Ticket t = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertSame(ana, t.getOwner());
        assertEquals(PRICE, t.getPrice());
        assertEquals(OCT_15, t.getDate());
    }

    @Test
    void createRejectsInvalidRequests() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T002", "nadie@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T003", "ana@correo.com", OCT_1.minusDays(1), OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class, () -> tickets.create(" ", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertEquals(1, tickets.getTickets().size());
        assertNotNull(tickets.create("T004", "ana@correo.com", OCT_1, OCT_1, PRICE));
    }

    @Test
    void deleteOnlyFutureTickets() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", OCT_15.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T999", OCT_1));
        tickets.delete("T001", OCT_15.minusDays(1));
        assertNull(tickets.find("T001"));
    }

    @Test
    void currentOrFutureTicketsAreDetected() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_1));
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(ana, OCT_15.plusDays(1)));
    }

    @Test
    void restartRestoresTicketsAndKeepsHistory() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        UserManager reloadedUsers = new UserManager(new UserStorage(dir.resolve("users.txt")));
        TicketManager reloaded = new TicketManager(reloadedUsers, new TicketStorage(dir.resolve("tickets.txt")));
        assertSame(reloadedUsers.findByDni("12345678Z"), reloaded.find("T001").getOwner());

        User deleted = reloadedUsers.delete("ana@correo.com", "Clave1!");
        reloaded.detachOwner(deleted);
        assertNull(reloaded.find("T001").getOwner());
        TicketManager again = new TicketManager(reloadedUsers, new TicketStorage(dir.resolve("tickets.txt")));
        assertEquals("12345678Z", again.find("T001").getOwnerDni());
        assertNull(again.find("T001").getOwner());
    }
}
