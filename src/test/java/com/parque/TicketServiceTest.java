package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketServiceTest {
    static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    static final BigDecimal P40 = new BigDecimal("40.00");

    @TempDir
    Path dir;
    UserService users;
    GroupService groups;
    TicketService tickets;
    User ana;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        ana = users.findByEmail("ana@correo.com");
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
    }

    private List<String> lines() throws Exception {
        Path f = dir.resolve("tickets.txt");
        return Files.exists(f) ? Files.readAllLines(f) : List.of();
    }

    @Test
    void createForRegisteredUserFromTodayOn() throws Exception {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertSame(ana, t.getUser());
        tickets.create("T002", "ana@correo.com", OCT_1, OCT_1, P40);
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00", "T002;12345678Z;2026-10-01;40.00"), lines());
    }

    @Test
    void rejectedCreationsChangeNothing() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        List<String> before = lines();
        assertThrows(IllegalArgumentException.class, () -> tickets.create("T002", "nadie@correo.com", OCT_15, OCT_1, P40));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T002", "ana@correo.com", LocalDate.of(2026, 9, 30), OCT_1, P40));
        assertThrows(IllegalArgumentException.class, () -> tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40));
        assertThrows(IllegalArgumentException.class, () -> tickets.create(" ", "ana@correo.com", OCT_15, OCT_1, P40));
        assertEquals(before, lines());
        assertEquals(1, tickets.getTickets().size());
    }

    @Test
    void deleteOnlyStrictlyFutureTickets() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", LocalDate.of(2026, 10, 16)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T999", OCT_1));
        assertNotNull(tickets.find("T001"));
        tickets.delete("T001", LocalDate.of(2026, 10, 14));
        assertNull(tickets.find("T001"));
        assertEquals(List.of(), lines());
    }

    @Test
    void blockingRuleUsesGreaterOrEqual() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertTrue(tickets.hasCurrentOrFutureTicket(ana, OCT_1));
        assertTrue(tickets.hasCurrentOrFutureTicket(ana, OCT_15));
        assertFalse(tickets.hasCurrentOrFutureTicket(ana, LocalDate.of(2026, 10, 16)));
    }

    @Test
    void capturedPriceIsKeptAndRestored() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        tickets.create("T002", "ana@correo.com", OCT_15, OCT_1, new BigDecimal("60.00"));
        TicketService restarted = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
        assertEquals("40.00", restarted.find("T001").getPriceText());
        assertEquals("60.00", restarted.find("T002").getPriceText());
        assertSame(ana, restarted.find("T001").getUser());
        assertTrue(restarted.hasCurrentOrFutureTicket(ana, OCT_15));
    }
}
