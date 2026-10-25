package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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

    private final User ana = UserManagerTest.ana();
    private final Map<String, User> users = Map.of(ana.getDni(), ana);

    @Test
    void missingFileMeansNoTickets() throws IOException {
        assertTrue(new TicketRepository(dir.resolve("tickets.txt")).load(users::get).isEmpty());
    }

    @Test
    void savesAndLoadsTickets() throws IOException {
        Path file = dir.resolve("data").resolve("tickets.txt");
        TicketRepository repository = new TicketRepository(file);
        repository.save(List.of(new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"))));
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00"), Files.readAllLines(file));
        IndividualTicket loaded = repository.load(users::get).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(LocalDate.of(2026, 10, 15), loaded.getDate());
        assertSame(ana, loaded.getHolder());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
    }

    @Test
    void keepsTicketsOfDeletedUsersAsHistory() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T009;2025-01-01;87654321X;35.50"));
        IndividualTicket loaded = new TicketRepository(file).load(users::get).get(0);
        assertEquals("87654321X", loaded.getHolder().getDni());
        assertEquals(new BigDecimal("35.50"), loaded.getPrice());
    }

    @Test
    void ignoresMalformedLines() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("roto", ";2026-10-15;12345678Z;40.00", "T1;mal;12345678Z;40.00",
                "T2;2026-10-15;12345678Z;gratis", "T3;2026-10-15;12345678Z;40.00"));
        assertEquals(1, new TicketRepository(file).load(users::get).size());
    }
}
