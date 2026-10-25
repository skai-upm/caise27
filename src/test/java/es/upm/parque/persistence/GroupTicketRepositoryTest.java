package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.User;

class GroupTicketRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = Fixtures.ana();
    private final Map<String, User> users = Map.of(ana.getDni(), ana);
    private final Map<String, Group> groups = Map.of();

    @Test
    void missingFileMeansNoGroupTickets() throws IOException {
        assertTrue(new GroupTicketRepository(dir.resolve("gt.txt")).load(groups::get, users::get).isEmpty());
    }

    @Test
    void restoresDetachedGroupsAndHoldersAsHistory() throws IOException {
        Path file = dir.resolve("gt.txt");
        Files.write(file, List.of("TG09;2025-01-01;G77;10;12345678Z:40.00,99999999R:40.00,00000000T:40.00"));
        GroupTicket loaded = new GroupTicketRepository(file).load(groups::get, users::get).get(0);
        assertEquals("G77", loaded.getGroup().getId());
        assertEquals(ana, loaded.getTickets().get(0).getHolder());
        assertEquals("99999999R", loaded.getTickets().get(1).getHolder().getDni());
        assertEquals(new BigDecimal("108.00"), loaded.getPrice());
    }

    @Test
    void ignoresMalformedLines() throws IOException {
        Path file = dir.resolve("gt.txt");
        Files.write(file, List.of("roto", ";2026-10-15;G01;0;12345678Z:40.00", "TG1;mal;G01;0;12345678Z:40.00",
                "TG2;2026-10-15;G01;x;12345678Z:40.00", "TG3;2026-10-15;G01;0;12345678Z",
                "TG4;2026-10-15;G01;0;12345678Z:gratis", "TG5;2026-10-15;G01;0;12345678Z:40.00"));
        assertEquals(1, new GroupTicketRepository(file).load(groups::get, users::get).size());
    }
}
