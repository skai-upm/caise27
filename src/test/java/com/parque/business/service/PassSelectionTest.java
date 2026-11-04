package com.parque.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.business.model.*;
import com.parque.persistence.PassRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassSelectionTest {
    @TempDir
    Path dir;

    @Test
    void individualTicketsBehindAdmissions() {
        User u = new User("A", "B", "d", "e@x.com", "h", "1", "1234567890123456");
        IndividualTicket a = new IndividualTicket("T1", u, LocalDate.of(2026, 1, 1), BigDecimal.TEN);
        IndividualTicket b = new IndividualTicket("G-1", u, LocalDate.of(2026, 1, 1), BigDecimal.TEN);
        GroupTicket g = new GroupTicket("G", null, "G01", LocalDate.of(2026, 1, 1), BigDecimal.TEN, BigDecimal.TEN, List.of(b));
        assertEquals(List.of(a), PassService.individualTicketsOf(a));
        assertEquals(List.of(b), PassService.individualTicketsOf(g));
    }

    @Test
    void consumeRevertsWhenSaveFails() throws Exception {
        Path blocked = dir.resolve("blocked");
        Files.createDirectory(blocked);
        Path passesFile = blocked.resolve("passes.txt");
        UserService users = new UserService(new com.parque.persistence.UserRepository(dir.resolve("users.txt")),
                new BCryptPasswordHasher(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        GroupService groups = new GroupService(new com.parque.persistence.GroupRepository(dir.resolve("groups.txt")), users);
        TicketService tickets = new TicketService(new com.parque.persistence.TicketRepository(dir.resolve("tickets.txt")),
                new com.parque.persistence.GroupTicketRepository(dir.resolve("gt.txt")), users, groups);
        tickets.create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), new BigDecimal("40"));
        PassService passes = new PassService(new PassRepository(passesFile), tickets);
        passes.buySpeedPasses("P", "T001", "10", LocalDate.of(2026, 10, 1));
        List<Pass> selected = passes.selectSpeedPasses(tickets.find("T001"));
        assertEquals("P-1", selected.get(0).getId());
        assertFalse(selected.get(0).isConsumed(), "selection does not change state");
        Files.delete(passesFile);
        Files.createDirectory(passesFile); // saving now fails
        assertThrows(IllegalStateException.class, () -> passes.consume(selected));
        assertFalse(selected.get(0).isConsumed());
        Files.delete(passesFile);
        passes.consume(selected);
        assertTrue(selected.get(0).isConsumed());
        assertEquals("P-2", passes.selectSpeedPasses(tickets.find("T001")).get(0).getId());
    }
}
