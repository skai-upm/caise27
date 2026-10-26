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

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final Group family = new Group("G01", "Familia");

    private TicketRepository repo() {
        return new TicketRepository(dir.resolve("data").resolve("tickets.txt"), dir.resolve("data").resolve("grouptickets.txt"));
    }

    @Test
    void missingFilesMeanNoTickets() {
        TicketRepository.TicketData data = repo().load(Map.of(), Map.of());
        assertTrue(data.individual().isEmpty());
        assertTrue(data.group().isEmpty());
    }

    @Test
    void roundTripIndividualAndGroupTickets() throws IOException {
        IndividualTicket single = new IndividualTicket("T001", DAY, ana, FORTY);
        IndividualTicket a = new IndividualTicket("TG01-1", DAY, ana, FORTY);
        IndividualTicket p = new IndividualTicket("TG01-2", DAY, pedro, FORTY);
        GroupTicket group = new GroupTicket("TG01", DAY, family, new BigDecimal("80.00"), List.of(a, p));
        repo().save(List.of(single, a, p), List.of(group));

        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00;", "TG01-1;12345678Z;2026-10-15;40.00;TG01",
                "TG01-2;87654321X;2026-10-15;40.00;TG01"), Files.readAllLines(dir.resolve("data").resolve("tickets.txt")));
        assertEquals(List.of("TG01;G01;2026-10-15;80.00"), Files.readAllLines(dir.resolve("data").resolve("grouptickets.txt")));

        TicketRepository.TicketData data = repo().load(Map.of("12345678Z", ana, "87654321X", pedro), Map.of("G01", family));
        assertEquals(3, data.individual().size());
        GroupTicket loaded = data.group().get(0);
        assertSame(family, loaded.getGroup());
        assertEquals(new BigDecimal("80.00"), loaded.getPrice());
        assertEquals(List.of(ana, pedro), loaded.getUsers());
        assertSame(data.individual().get(1), loaded.getTickets().get(0));
        assertSame(ana, data.individual().get(0).getUser());
    }

    @Test
    void legacyAndInvalidLinesAreHandled() throws IOException {
        Path individual = dir.resolve("tickets.txt");
        Path group = dir.resolve("grouptickets.txt");
        Files.write(individual, List.of("T1;00000000T;2026-10-15;40.00", "T2;12345678Z;2026-13-01;40.00",
                "T3;12345678Z;2026-10-15;cuarenta", "bad", "T4;12345678Z;2026-10-15;60.00",
                "T5;12345678Z;2026-10-15;40.00;TG99"));
        Files.write(group, List.of("TG98;G99;2026-10-15;80.00", "TG97;G01;fecha;80.00", "x"));
        TicketRepository.TicketData data = new TicketRepository(individual, group).load(Map.of("12345678Z", ana),
                Map.of("G01", family));
        assertEquals(1, data.individual().size());
        assertEquals("T4", data.individual().get(0).getId());
        assertTrue(data.group().isEmpty());
    }

    @Test
    void ioErrorsAreUnchecked() {
        TicketRepository broken = new TicketRepository(dir, dir);
        Map<String, User> users = Map.of();
        Map<String, Group> groups = Map.of();
        List<IndividualTicket> none = List.of();
        List<GroupTicket> noGroups = List.of();
        assertThrows(UncheckedIOException.class, () -> broken.load(users, groups));
        assertThrows(UncheckedIOException.class, () -> broken.save(none, noGroups));
    }
}
