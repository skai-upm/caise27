package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketRepositoryTest {
    @TempDir
    Path dir;
    final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    final Map<String, User> byDni = Map.of(ana.getDni(), ana);

    @Test
    void missingFileMeansNoTickets() throws Exception {
        assertTrue(new TicketRepository(dir.resolve("tickets.txt")).load(byDni::get).isEmpty());
    }

    @Test
    void roundTrip() throws Exception {
        Path file = dir.resolve("tickets.txt");
        TicketRepository repo = new TicketRepository(file);
        repo.save(List.of(new Ticket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40.00")),
                new Ticket("T002", ana, LocalDate.of(2026, 11, 1), new BigDecimal("60"))));
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00", "T002;12345678Z;2026-11-01;60.00"),
                Files.readAllLines(file));
        List<Ticket> loaded = repo.load(byDni::get);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getUser());
        assertEquals(new BigDecimal("60.00"), loaded.get(1).getPrice());
    }

    @Test
    void unknownDniKeptAsHistoryAndMalformedSkipped() throws Exception {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T001;87654321X;2026-01-01;40.00", "T002;12345678Z;bad-date;40.00", "x;y",
                "T003;12345678Z;2026-01-02;abc", ""));
        List<Ticket> loaded = new TicketRepository(file).load(byDni::get);
        assertEquals(1, loaded.size());
        assertNull(loaded.get(0).getUser());
        assertEquals("87654321X", loaded.get(0).getHolderDni());
    }
}
