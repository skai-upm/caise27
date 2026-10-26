package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void roundTripResolvesRegisteredUsers() throws IOException {
        Path file = dir.resolve("tickets.txt");
        TicketRepository repository = new TicketRepository(file);
        repository.saveAll(List.of(new Ticket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"))));

        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez"), Files.readAllLines(file));

        Ticket loaded = repository.loadAll(Map.of(ana.getDni(), ana)::get).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(LocalDate.of(2026, 10, 15), loaded.getDate());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
        assertSame(ana, loaded.getUser());
    }

    @Test
    void deletedHolderIsRebuiltFromStoredIdentity() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.writeString(file, "T001;2026-09-01;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez\nbad;line\nT002;notadate;40;1;2;3;4\n");
        List<Ticket> loaded = new TicketRepository(file).loadAll(dni -> null);
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getUser().getDni());
        assertEquals("Ana", loaded.get(0).getUser().getName());
    }
}
