package es.upm.park;

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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketFileStoreTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final Group g01 = new Group("G01", "Familia Garcia");
    private final Map<String, User> users = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);

    private Optional<Group> group(String id) {
        return g01.getId().equals(id) ? Optional.of(g01) : Optional.empty();
    }

    private Optional<User> user(String dni) {
        return Optional.ofNullable(users.get(dni));
    }

    @Test
    void writesLineAndRestoresLinkedComposition() throws IOException {
        g01.addMember(ana);
        g01.addMember(pedro);
        Path file = dir.resolve("grouptickets.txt");
        GroupTicketFileStore store = new GroupTicketFileStore(file);
        store.save(List.of(GroupTicket.purchase("TG01", LocalDate.of(2026, 10, 15), g01, new BigDecimal("40.00"))), List.of());
        assertEquals(List.of("TG01;2026-10-15;80.00;G01;40.00;12345678Z;87654321X"), Files.readAllLines(file));

        GroupTicketFileStore.Contents c = store.load(this::group, this::user);
        GroupTicket t = c.groupTickets().get(0);
        assertSame(g01, t.getGroup());
        assertSame(pedro, t.getTickets().get(1).getUser());
        assertEquals("TG01-87654321X", t.getTickets().get(1).getId());
        assertEquals(new BigDecimal("80.00"), t.getPrice());
        assertTrue(c.historicalLines().isEmpty());
    }

    @Test
    void unresolvableLinesBecomeHistory() throws IOException {
        Path file = dir.resolve("grouptickets.txt");
        Files.write(file, List.of("TG01;2026-10-15;80.00;G99;40.00;12345678Z;87654321X",
                "TG02;2026-10-15;40.00;G01;40.00;99999999R", "TG03;bad;40.00;G01;40.00;12345678Z", ""));
        GroupTicketFileStore.Contents c = new GroupTicketFileStore(file).load(this::group, this::user);
        assertTrue(c.groupTickets().isEmpty());
        assertEquals(3, c.historicalLines().size());
        assertEquals(List.of("TG01", "TG01-12345678Z", "TG01-87654321X"),
                GroupTicketFileStore.idsOf(c.historicalLines().get(0)));
    }

    @Test
    void missingFile() throws IOException {
        assertTrue(new GroupTicketFileStore(dir.resolve("x.txt")).load(this::group, this::user).groupTickets().isEmpty());
    }
}
