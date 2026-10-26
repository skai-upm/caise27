package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private UserManager users;
    private SystemDate date;
    private TicketManager tickets;

    @BeforeEach
    void setUp() {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        date = new SystemDate();
        date.set(OCT_1);
        tickets = newTicketManager(users);
    }

    private TicketManager newTicketManager(UserManager userManager) {
        return new TicketManager(new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                userManager, new GroupManager(new GroupRepository(dir.resolve("groups.txt")), userManager), date);
    }

    @Test
    void createsTicketAtCurrentPrice() {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", OCT_15);
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertSame(users.findByEmail("ana@correo.com"), t.getUser());
        assertEquals(OCT_15, t.getDate());
    }

    @Test
    void rejectsInvalidPurchases() {
        tickets.create("T001", "ana@correo.com", OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.create("T001", "ana@correo.com", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.create("T002", "nadie@correo.com", OCT_15));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T003", "ana@correo.com", OCT_1.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> tickets.create(" ", "ana@correo.com", OCT_15));
        tickets.create("T004", "ana@correo.com", OCT_1);
        assertEquals(2, tickets.getTickets().size());
    }

    @Test
    void priceChangesOnlyAffectNewTickets() {
        Ticket first = tickets.create("T001", "ana@correo.com", OCT_15);
        tickets.setPrice(new BigDecimal("60"));
        assertEquals("60.00", tickets.getPrice().toPlainString());
        assertEquals("60.00", tickets.create("T002", "ana@correo.com", OCT_15).getPrice().toPlainString());
        assertEquals("40.00", first.getPrice().toPlainString());
        assertThrows(IllegalArgumentException.class, () -> tickets.setPrice(new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class, () -> tickets.setPrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> tickets.setPrice(new BigDecimal("1.234")));
        assertEquals("60.00", tickets.getPrice().toPlainString());
        assertEquals("40.00", newTicketManager(users).getPrice().toPlainString());
    }

    @Test
    void onlyStrictlyFutureTicketsCanBeDeleted() {
        tickets.create("T001", "ana@correo.com", OCT_15);
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001"));
        date.set(OCT_15.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001"));
        date.set(OCT_15.minusDays(1));
        tickets.delete("T001");
        assertNull(tickets.findById("T001"));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001"));
    }

    @Test
    void currentOrFutureTicketsBlockUserDeletion() {
        tickets.create("T001", "ana@correo.com", OCT_15);
        assertThrows(IllegalArgumentException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        date.set(LocalDate.of(2026, 10, 16));
        users.delete("ana@correo.com", "Clave1!");
        assertNull(users.findByEmail("ana@correo.com"));
        assertNotNull(tickets.findById("T001"));
    }

    @Test
    void ticketsAreRestoredIncludingHistoryOfDeletedUsers() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15);
        tickets.setPrice(new BigDecimal("60.00"));
        tickets.create("T002", "ana@correo.com", OCT_1);
        assertEquals("T001;2026-10-15;40.00;12345678Z\nT002;2026-10-01;60.00;12345678Z\n",
                Files.readString(dir.resolve("tickets.txt")));
        UserManager restartedUsers = new UserManager(new UserRepository(dir.resolve("users.txt")));
        TicketManager restarted = newTicketManager(restartedUsers);
        assertSame(restartedUsers.findByDni("12345678Z"), restarted.findById("T001").getUsers().get(0));
        assertEquals("60.00", restarted.findById("T002").getPrice().toPlainString());

        date.set(LocalDate.of(2026, 11, 1));
        restartedUsers.delete("ana@correo.com", "Clave1!");
        TicketManager afterDeletion = newTicketManager(new UserManager(new UserRepository(dir.resolve("users.txt"))));
        User holder = afterDeletion.findById("T001").getUsers().get(0);
        assertEquals("12345678Z", holder.getDni());
        assertEquals(TicketRepository.DELETED_USER_NAME, holder.getName());
    }
}
