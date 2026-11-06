package es.upm.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.model.Attraction;
import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;

class ParkFileStoresTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
            "9876543210987654");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void groupsRoundTrip() {
        Group g = new Group("G01", "Familia, Garcia");
        g.addMember("12345678Z");
        g.addMember("87654321X");
        GroupFileStore store = new GroupFileStore(dir.resolve("groups.txt"));
        store.save(List.of(g, new Group("G02", "Vacio")));
        List<Group> loaded = store.load();
        assertEquals("Familia, Garcia", loaded.get(0).getName());
        assertEquals(List.of("12345678Z", "87654321X"), loaded.get(0).getMemberDnis());
        assertTrue(loaded.get(1).getMemberDnis().isEmpty());
    }

    @Test
    void attractionsRoundTripWithoutOccupants() throws Exception {
        Attraction a = new Attraction("A01", "Dragon Rojo", 20);
        a.enter(ana);
        Path file = dir.resolve("attractions.txt");
        AttractionFileStore store = new AttractionFileStore(file);
        store.save(List.of(a));
        assertEquals(List.of("A01;Dragon Rojo;20"), Files.readAllLines(file));
        Files.write(file, List.of("A01;Dragon Rojo;20", "bad;line", "A02;X;0", "A03;Y;abc"));
        List<Attraction> loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals(20, loaded.get(0).getCapacity());
        assertTrue(loaded.get(0).getOccupants().isEmpty());
    }

    @Test
    void ticketsRoundTripWithLiveAndDeletedUsersAndGroups() {
        Group g = new Group("G01", "F");
        g.addMember(ana.getDni());
        g.addMember(pedro.getDni());
        IndividualTicket t1 = new IndividualTicket("T001", day, new BigDecimal("45.50"), ana);
        GroupTicket tg1 = new GroupTicket("TG01", day, new BigDecimal("40.00"), g, List.of(ana, pedro));
        GroupTicket tg2 = new GroupTicket("TG02", day, new BigDecimal("40.00"), g, List.of(ana));
        tg2.detachGroup();
        TicketFileStore store = new TicketFileStore(dir.resolve("tickets.txt"));
        store.save(List.of(t1, tg1, tg2));
        Group newG01 = new Group("G01", "Nuevo");
        List<Ticket> loaded = store.load(
                dni -> dni.equals(ana.getDni()) ? Optional.of(ana) : Optional.empty(),
                id -> id.equals("G01") ? Optional.of(newG01) : Optional.empty());
        assertEquals(3, loaded.size());
        IndividualTicket l1 = (IndividualTicket) loaded.get(0);
        assertSame(ana, l1.getUser());
        assertEquals(new BigDecimal("45.50"), l1.getPrice());
        GroupTicket l2 = (GroupTicket) loaded.get(1);
        assertSame(newG01, l2.getGroup());
        assertEquals(tg1.ids(), l2.ids());
        assertEquals(new BigDecimal("80.00"), l2.getPrice());
        User deletedPedro = l2.getComposition().get(1).getUser();
        assertEquals("87654321X", deletedPedro.getDni());
        assertEquals("Pedro", deletedPedro.getFirstName());
        assertEquals("pedro@correo.com", deletedPedro.getEmail());
        GroupTicket l3 = (GroupTicket) loaded.get(2);
        assertNull(l3.getGroup());
        assertEquals("G01", l3.getGroupId());
    }

    @Test
    void malformedTicketLinesAreSkipped() throws Exception {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("INDIVIDUAL;T1;nodate;1;0;1;a;b;c", "GROUP;TG;2027-01-01;x;0;;G", "MEMBER;1;a;b;c",
                "GROUP;TG2;2027-01-01;1;0;;G", "OTHER", "MEMBER;orphan;a;b;c"));
        assertTrue(new TicketFileStore(file).load(d -> Optional.empty(), g -> Optional.empty()).isEmpty());
    }

    @Test
    void temporaryDirectoryIsCreatedAndDeleted() throws Exception {
        Path base = dir.resolve("tmp");
        DataDirectory first = DataDirectory.temporary(base, java.time.LocalDateTime.of(2026, 8, 26, 19, 3, 15));
        DataDirectory second = DataDirectory.temporary(base, java.time.LocalDateTime.of(2026, 8, 26, 19, 3, 15));
        assertEquals(base.resolve("20260826_190315"), first.path());
        assertEquals(base.resolve("20260826_190315_1"), second.path());
        assertTrue(first.isTemporary());
        Files.createDirectories(first.path().resolve("sub"));
        Files.writeString(first.path().resolve("sub/users.txt"), "x");
        first.close();
        second.close();
        first.close();
        assertTrue(Files.notExists(first.path()));
        assertTrue(Files.notExists(second.path()));
        DataDirectory permanent = DataDirectory.permanent(dir);
        permanent.close();
        assertTrue(Files.exists(dir));
        Files.writeString(dir.resolve("file"), "x");
        assertThrows(PersistenceException.class, () -> DataDirectory.temporary(dir.resolve("file"), java.time.LocalDateTime.now()));
    }
}
