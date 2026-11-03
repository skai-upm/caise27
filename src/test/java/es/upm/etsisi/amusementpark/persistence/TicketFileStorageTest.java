package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class TicketFileStorageTest {

    @TempDir
    Path dir;

    final User ana = TestData.user("12345678Z", "ana@correo.com");

    @Test
    void missingFileMeansNoTickets() {
        assertTrue(new TicketFileStorage(dir.resolve("none.txt")).load(List.of(ana)).isEmpty());
    }

    @Test
    void roundTripLinksOwnersAndKeepsHistory() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T001;12345678Z;2026-10-15;40.00", "T002;87654321X;2026-01-10;60.00", "",
                "bad;line", "T003;12345678Z;2026-13-01;40.00", "T004;12345678Z;2026-10-15;abc"));
        TicketFileStorage storage = new TicketFileStorage(file);
        List<Ticket> tickets = storage.load(List.of(ana));
        assertEquals(2, tickets.size());
        assertSame(ana, tickets.get(0).getUser().orElseThrow());
        assertTrue(tickets.get(1).getUser().isEmpty());
        assertEquals("87654321X", tickets.get(1).getOwnerDni());
        storage.save(tickets);
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00", "T002;87654321X;2026-01-10;60.00"),
                Files.readAllLines(file));
    }

    @Test
    void savesExactFormat() throws IOException {
        Path file = dir.resolve("tickets.txt");
        new TicketFileStorage(file).save(List.of(new Ticket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40"))));
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"), Files.readAllLines(file));
    }
}
