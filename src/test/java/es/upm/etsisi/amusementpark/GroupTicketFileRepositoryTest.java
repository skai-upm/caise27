package es.upm.etsisi.amusementpark;

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

class GroupTicketFileRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
    private final User luis = new User("Luis", "M", "11111111H", "luis@correo.com", "Luis3$", "3", "1111222233334444");
    private final Map<String, User> byDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro, luis.getDni(), luis);

    @Test
    void missingFileMeansNone() throws Exception {
        assertTrue(new GroupTicketFileRepository(dir.resolve("g.txt")).load(id -> null, byDni::get).isEmpty());
    }

    @Test
    void savesExactLineAndRestoresSameObjects() throws Exception {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        g.addMember(luis);
        Path file = dir.resolve("grouptickets.txt");
        new GroupTicketFileRepository(file).save(List.of(
                new GroupTicket("TG01", LocalDate.of(2026, 10, 15), g, new BigDecimal("40.00"))));

        assertEquals(List.of("TG01;2026-10-15;G01;40.00;108.00;12345678Z;87654321X;11111111H"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        List<GroupTicket> loaded = new GroupTicketFileRepository(file).load(Map.of("G01", g)::get, byDni::get);
        GroupTicket t = loaded.get(0);
        assertSame(g, t.getGroup().orElseThrow());
        assertEquals(new BigDecimal("108.00"), t.getPrice());
        assertEquals(3, t.getParts().size());
        assertSame(pedro, t.getParts().get(1).getUser().orElseThrow());
        assertEquals("TG01-87654321X", t.getParts().get(1).getId());
    }

    @Test
    void keepsOrphansAndSkipsMalformedLines() throws Exception {
        Path file = dir.resolve("grouptickets.txt");
        Files.write(file, List.of("TG01;2026-10-15;G99;40.00;80.00;12345678Z;00000000T", "TG02;bad;G01;40.00;80.00",
                "TG03;2026-10-15;G01;40.00", ""), StandardCharsets.UTF_8);
        List<GroupTicket> loaded = new GroupTicketFileRepository(file).load(id -> null, byDni::get);
        assertEquals(1, loaded.size());
        GroupTicket t = loaded.get(0);
        assertTrue(t.getGroup().isEmpty());
        assertEquals("G99", t.getGroupId());
        assertSame(ana, t.getParts().get(0).getUser().orElseThrow());
        assertTrue(t.getParts().get(1).getUser().isEmpty());
    }
}
