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
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final LocalDate date = LocalDate.of(2026, 10, 15);

    private TicketRepository repository() {
        return new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt"));
    }

    @Test
    void roundTripResolvesRegisteredUsers() throws IOException {
        repository().saveAll(List.of(new IndividualTicket("T001", date, ana, new BigDecimal("40.00"))));

        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez;"),
                Files.readAllLines(dir.resolve("tickets.txt")));

        IndividualTicket loaded = (IndividualTicket) repository().loadAll(Map.of(ana.getDni(), ana)::get, id -> null).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(date, loaded.getDate());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
        assertSame(ana, loaded.getUser());
    }

    @Test
    void deletedHolderIsRebuiltAndLegacyLinesLoad() throws IOException {
        Files.writeString(dir.resolve("tickets.txt"),
                "T001;2026-09-01;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez\nbad;line\nT002;notadate;40;1;2;3;4\n");
        List<Ticket> loaded = repository().loadAll(dni -> null, id -> null);
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", ((IndividualTicket) loaded.get(0)).getUser().getDni());
        assertEquals("Ana", ((IndividualTicket) loaded.get(0)).getUser().getName());
    }

    @Test
    void groupTicketRoundTrip() throws IOException {
        Group group = new Group("G01", "Familia Garcia");
        IndividualTicket t1 = new IndividualTicket("TG01-1", date, ana, new BigDecimal("40.00"));
        IndividualTicket t2 = new IndividualTicket("TG01-2", date, pedro, new BigDecimal("40.00"));
        GroupTicket groupTicket = new GroupTicket("TG01", date, group, List.of(t1, t2));
        repository().saveAll(List.of(t1, t2, groupTicket));

        assertEquals(List.of("TG01;2026-10-15;G01;Familia Garcia"), Files.readAllLines(dir.resolve("grouptickets.txt")));

        Map<String, User> users = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);
        List<Ticket> loaded = repository().loadAll(users::get, Map.of("G01", group)::get);
        assertEquals(3, loaded.size());
        GroupTicket restored = (GroupTicket) loaded.get(2);
        assertSame(group, restored.getGroup());
        assertSame(loaded.get(0), restored.getTickets().get(0));
        assertSame(pedro, restored.getTickets().get(1).getUser());
        assertEquals(new BigDecimal("80.00"), restored.getPrice());

        GroupTicket detached = (GroupTicket) repository().loadAll(users::get, id -> null).get(2);
        assertEquals("Familia Garcia", detached.getGroup().getName());
    }
}
