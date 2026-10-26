package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void missingFileMeansNoTickets() {
        assertTrue(new TicketRepository(dir.resolve("none.txt")).loadAll(Map.of()).isEmpty());
    }

    @Test
    void roundTrip() throws IOException {
        Path file = dir.resolve("data").resolve("tickets.txt");
        TicketRepository repo = new TicketRepository(file);
        repo.saveAll(List.of(new Ticket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"))));
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"), Files.readAllLines(file));

        Ticket loaded = repo.loadAll(Map.of("12345678Z", ana)).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(LocalDate.of(2026, 10, 15), loaded.getDate());
        assertSame(ana, loaded.getUser());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
    }

    @Test
    void invalidLinesAreSkipped() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T1;00000000T;2026-10-15;40.00", "T2;12345678Z;2026-13-01;40.00",
                "T3;12345678Z;2026-10-15;cuarenta", "bad", "T4;12345678Z;2026-10-15;60.00"));
        List<Ticket> loaded = new TicketRepository(file).loadAll(Map.of("12345678Z", ana));
        assertEquals(1, loaded.size());
        assertEquals("T4", loaded.get(0).getId());
    }

    @Test
    void ioErrorsAreUnchecked() {
        TicketRepository repo = new TicketRepository(dir);
        Map<String, User> none = Map.of();
        List<Ticket> empty = List.of();
        assertThrows(UncheckedIOException.class, () -> repo.loadAll(none));
        assertThrows(UncheckedIOException.class, () -> repo.saveAll(empty));
    }
}
