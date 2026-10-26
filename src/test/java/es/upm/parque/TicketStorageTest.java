package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketStorageTest {

    @TempDir
    Path dir;

    private final User ana =
        new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");

    @Test
    void missingFileLoadsNoTickets() {
        assertTrue(new TicketStorage(dir.resolve("tickets.txt")).load(dni -> null).isEmpty());
    }

    @Test
    void roundTrip() throws IOException {
        Path file = dir.resolve("tickets.txt");
        TicketStorage storage = new TicketStorage(file);
        storage.save(List.of(new Ticket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), ana)));

        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z"), Files.readAllLines(file));

        Ticket loaded = storage.load(dni -> dni.equals("12345678Z") ? ana : null).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(LocalDate.of(2026, 10, 15), loaded.getDate());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
        assertSame(ana, loaded.getOwner());
    }

    @Test
    void unresolvedOwnerIsKeptAsHistoryAndMalformedLinesSkipped() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T001;2026-10-15;40.00;87654321X", "T002;notadate;40.00;12345678Z",
            "T003;2026-10-15;cheap;12345678Z", "short;line"));
        List<Ticket> loaded = new TicketStorage(file).load(dni -> null);
        assertEquals(1, loaded.size());
        assertNull(loaded.get(0).getOwner());
        assertEquals("87654321X", loaded.get(0).getOwnerDni());
    }
}
