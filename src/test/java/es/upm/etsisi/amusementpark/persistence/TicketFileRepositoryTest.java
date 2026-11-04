package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketFileRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    private final Map<String, User> byDni = Map.of(ana.getDni(), ana);

    @Test
    void missingFileMeansNoTickets() throws Exception {
        assertTrue(new TicketFileRepository(dir.resolve("tickets.txt")).load(byDni::get).isEmpty());
    }

    @Test
    void savesExactLineAndRestoresSameUser() throws Exception {
        Path file = dir.resolve("tickets.txt");
        new TicketFileRepository(file).save(List.of(
                new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00")),
                new IndividualTicket("T002", LocalDate.of(2026, 10, 20), ana, new BigDecimal("60.00"))));
        assertEquals(List.of("T001;2026-10-15;12345678Z;40.00", "T002;2026-10-20;12345678Z;60.00"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        List<IndividualTicket> loaded = new TicketFileRepository(file).load(byDni::get);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getUser().orElseThrow());
        assertEquals(new BigDecimal("60.00"), loaded.get(1).getPrice());
    }

    @Test
    void keepsOrphanTicketsAndSkipsMalformedLines() throws Exception {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("T001;2026-10-15;00000000T;40.00", "T002;not-a-date;12345678Z;40.00",
                "T003;2026-10-15;12345678Z", "T004;2026-10-15;12345678Z;abc", ""), StandardCharsets.UTF_8);
        List<IndividualTicket> loaded = new TicketFileRepository(file).load(byDni::get);
        assertEquals(1, loaded.size());
        assertTrue(loaded.get(0).getUser().isEmpty());
        assertEquals("00000000T", loaded.get(0).getOwnerDni());
    }
}
