package es.upm.park;

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
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoTickets() {
        assertTrue(new TicketFileStorage(dir.resolve("none.txt")).load(dni -> null).isEmpty());
    }

    @Test
    void roundTripAndOrphans() throws IOException {
        User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        Path file = dir.resolve("tickets.txt");
        TicketFileStorage storage = new TicketFileStorage(file);
        storage.save(List.of(new Ticket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40.00"))));
        Files.writeString(file, "T002;87654321X;2026-01-01;60.00\nbad;line\nT003;1;notadate;1.00\nT004;1;2026-01-01;x\n",
                java.nio.file.StandardOpenOption.APPEND);
        List<Ticket> loaded = storage.load(Map.of("12345678Z", ana)::get);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getOwner());
        assertNull(loaded.get(1).getOwner());
        assertEquals("87654321X", loaded.get(1).getOwnerDni());
        assertEquals(new BigDecimal("60.00"), loaded.get(1).getPrice());
    }
}
