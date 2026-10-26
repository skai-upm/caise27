package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketStorageTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private final User ana =
        new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    private final User pedro =
        new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    private final Map<String, User> usersByDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);
    private Path ticketsFile;
    private Path groupTicketsFile;
    private TicketStorage storage;

    @BeforeEach
    void setUp() {
        ticketsFile = dir.resolve("tickets.txt");
        groupTicketsFile = dir.resolve("group_tickets.txt");
        storage = new TicketStorage(ticketsFile, groupTicketsFile);
    }

    @Test
    void missingFilesLoadNoTickets() {
        assertTrue(storage.load(dni -> null, id -> null).isEmpty());
    }

    @Test
    void individualRoundTrip() throws IOException {
        storage.save(List.of(new IndividualTicket("T001", DATE, new BigDecimal("40.00"), ana)));

        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z"), Files.readAllLines(ticketsFile));

        IndividualTicket loaded = (IndividualTicket) storage.load(usersByDni::get, id -> null).get(0);
        assertEquals("T001", loaded.getId());
        assertEquals(DATE, loaded.getDate());
        assertEquals(new BigDecimal("40.00"), loaded.getPrice());
        assertSame(ana, loaded.getOwner());
    }

    @Test
    void groupRoundTripRestoresGroupAndMembers() throws IOException {
        Group group = new Group("G01", "Familia");
        group.addMember(ana);
        group.addMember(pedro);
        storage.save(List.of(new GroupTicket("TG01", DATE, group, new BigDecimal("40.00"))));

        assertEquals(List.of("TG01;2026-10-15;G01;40.00;0;TG01-1:12345678Z,TG01-2:87654321X"),
            Files.readAllLines(groupTicketsFile));

        GroupTicket loaded = (GroupTicket) storage.load(usersByDni::get, Map.of("G01", group)::get).get(0);
        assertSame(group, loaded.getGroup());
        assertEquals(new BigDecimal("80.00"), loaded.getPrice());
        assertSame(ana, loaded.getComponents().get(0).getOwner());
        assertSame(pedro, loaded.getComponents().get(1).getOwner());
        assertEquals("TG01", loaded.getComponents().get(1).getGroupTicketId());
    }

    @Test
    void historyIsKeptWhenOwnersOrGroupsAreGone() throws IOException {
        Files.write(ticketsFile, List.of("T001;2026-10-15;40.00;87654321X", "T002;notadate;40.00;12345678Z",
            "T003;2026-10-15;cheap;12345678Z", "short;line"));
        Files.write(groupTicketsFile, List.of("TG01;2026-10-15;G09;40.00;10;TG01-1:1,TG01-2:2,TG01-3:3",
            "TG02;2026-10-15;G01;40.00;0;broken", "TG03;bad"));
        List<Ticket> loaded = storage.load(dni -> null, id -> null);
        assertEquals(2, loaded.size());
        IndividualTicket individual = (IndividualTicket) loaded.get(0);
        assertNull(individual.getOwner());
        assertEquals("87654321X", individual.getOwnerDni());
        GroupTicket group = (GroupTicket) loaded.get(1);
        assertNull(group.getGroup());
        assertEquals("G09", group.getGroupId());
        assertEquals(new BigDecimal("108.00"), group.getPrice());
    }
}
