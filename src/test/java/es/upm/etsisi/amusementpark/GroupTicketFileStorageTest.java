package es.upm.etsisi.amusementpark;

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

class GroupTicketFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoGroupTickets() {
        assertTrue(new GroupTicketFileStorage(dir.resolve("none.txt")).load(List.of(), List.of()).isEmpty());
    }

    @Test
    void roundTripLinksGroupAndUsersAndKeepsHistory() throws IOException {
        Group g = GroupTicketTest.groupOf(3);
        GroupTicketFileStorage storage = new GroupTicketFileStorage(dir.resolve("gt.txt"));
        storage.save(List.of(new GroupTicket("TG01", g, LocalDate.of(2026, 10, 15), new BigDecimal("40.00"))));
        assertEquals(List.of("TG01;G01;2026-10-15;108.00;40.00;00000000X,00000001X,00000002X"),
                Files.readAllLines(dir.resolve("gt.txt")));
        Files.write(dir.resolve("gt.txt"), List.of("TG01;G01;2026-10-15;108.00;40.00;00000000X,00000001X,00000002X",
                "TG02;G77;2026-01-01;80.00;40.00;00000000X,55555555K", "", "bad", "TG03;G01;2026-01-01;x;40.00;0"));
        List<GroupTicket> loaded = storage.load(g.getMembers(), List.of(g));
        assertEquals(2, loaded.size());
        assertSame(g, loaded.get(0).getGroup().orElseThrow());
        assertSame(g.getMembers().get(2), loaded.get(0).getTickets().get(2).getUser().orElseThrow());
        assertEquals("TG01-3", loaded.get(0).getTickets().get(2).getId());
        assertTrue(loaded.get(1).getGroup().isEmpty());
        assertEquals("G77", loaded.get(1).getGroupId());
        assertTrue(loaded.get(1).getTickets().get(1).getUser().isEmpty());
        storage.save(loaded);
        assertEquals("TG02;G77;2026-01-01;80.00;40.00;00000000X,55555555K", Files.readAllLines(dir.resolve("gt.txt")).get(1));
    }
}
