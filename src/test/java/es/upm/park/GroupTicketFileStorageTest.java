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

class GroupTicketFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoGroupTickets() {
        assertTrue(new GroupTicketFileStorage(dir.resolve("none.txt")).load(id -> null, dni -> null).isEmpty());
    }

    @Test
    void roundTripWithHistory() throws IOException {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "1", "1234567890123456");
        Group g = new Group("G01", "Familia Garcia");
        g.add(ana);
        g.add(pedro);
        Path file = dir.resolve("group-tickets.txt");
        GroupTicketFileStorage storage = new GroupTicketFileStorage(file);
        storage.save(List.of(GroupTicket.purchase("TG01", g, LocalDate.of(2026, 10, 15), new BigDecimal("40.00"))));
        assertEquals(List.of("TG01;G01;2026-10-15;40.00;80.00;12345678Z;87654321X"), Files.readAllLines(file));
        Files.writeString(file, "bad\nTG02;G01;2026-10-15;40.00;80.00\nTG03;G09;x;1;1;1\n",
                java.nio.file.StandardOpenOption.APPEND);

        List<GroupTicket> loaded = storage.load(Map.of("G01", g)::get, Map.of("12345678Z", ana)::get);
        assertEquals(1, loaded.size());
        GroupTicket t = loaded.get(0);
        assertSame(g, t.getGroup());
        assertSame(ana, t.getConstituents().get(0).getOwner());
        assertNull(t.getConstituents().get(1).getOwner());
        assertEquals("87654321X", t.getConstituents().get(1).getOwnerDni());
        assertEquals(new BigDecimal("80.00"), t.getPrice());

        GroupTicket orphan = storage.load(id -> null, dni -> null).get(0);
        assertNull(orphan.getGroup());
        assertEquals("G01", orphan.getGroupId());
    }
}
