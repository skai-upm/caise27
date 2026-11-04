package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketFileStoreTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
            "Clave1!", "612345678", "1234567890123456");

    private Optional<User> lookup(String dni) {
        return ana.getDni().equals(dni) ? Optional.of(ana) : Optional.empty();
    }

    @Test
    void writesOneLinePerTicketThenHistory() throws IOException {
        Path file = dir.resolve("tickets.txt");
        IndividualTicket t = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), ana);
        new TicketFileStore(file).save(List.of(t), List.of("T000;2025-01-01;40.00;87654321X"));
        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z", "T000;2025-01-01;40.00;87654321X"),
                Files.readAllLines(file));
    }

    @Test
    void loadLinksUsersAndSeparatesHistory() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T001;2026-10-15;40.00;12345678Z", "", "T000;2025-01-01;60.00;87654321X",
                "broken", "T009;not-a-date;40.00;12345678Z"));
        TicketFileStore.Contents c = new TicketFileStore(file).load(this::lookup);
        assertEquals(1, c.tickets().size());
        IndividualTicket t = c.tickets().get(0);
        assertSame(ana, t.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(List.of("T000;2025-01-01;60.00;87654321X"), c.historicalLines());
    }

    @Test
    void missingFileMeansNoTickets() throws IOException {
        TicketFileStore.Contents c = new TicketFileStore(dir.resolve("tickets.txt")).load(this::lookup);
        assertTrue(c.tickets().isEmpty());
        assertTrue(c.historicalLines().isEmpty());
    }
}
