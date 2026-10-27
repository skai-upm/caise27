package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.UserRepository;

class ParkFileStoresTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    private UserRepository users() {
        UserRepository users = new UserRepository();
        users.add(ana);
        users.add(pedro);
        return users;
    }

    @Test
    void attractionsRoundTripWithoutOccupants() {
        AttractionFileStore store = new AttractionFileStore(dir.resolve("attractions.txt"));
        Attraction a = new Attraction("A01", "Dragon Rojo", 20);
        a.enter(ana);
        store.save(List.of(a));
        Attraction loaded = store.load().get(0);
        assertEquals("A01", loaded.getId());
        assertEquals("Dragon Rojo", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertEquals(0, loaded.occupancy());
    }

    @Test
    void groupsRoundTripReferencingLoadedUsers() {
        UserRepository users = users();
        GroupFileStore store = new GroupFileStore(dir.resolve("groups.txt"));
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        store.save(List.of(g, new Group("G02", "Vacio")));
        List<Group> loaded = store.load(users);
        assertEquals(2, loaded.size());
        assertEquals(List.of(ana, pedro), loaded.get(0).getMembers());
        assertSame(users.findByDni("12345678Z"), loaded.get(0).getMembers().get(0));
        assertEquals(0, loaded.get(1).getMembers().size());
    }

    @Test
    void ticketsRoundTripWithRelationships() {
        UserRepository users = users();
        GroupRepository groups = new GroupRepository();
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        groups.add(g);
        TicketFileStore store = new TicketFileStore(dir.resolve("tickets.txt"));
        LocalDate day = LocalDate.of(2027, 4, 15);
        store.save(List.of(new IndividualTicket("T001", day, new BigDecimal("45.50"), ana),
                new GroupTicket("TG01", day, new BigDecimal("40.00"), g, List.of(ana, pedro))));
        List<Ticket> loaded = store.load(users, groups);
        IndividualTicket t = (IndividualTicket) loaded.get(0);
        assertSame(users.findByDni("12345678Z"), t.getUser());
        assertEquals(new BigDecimal("45.50"), t.getPrice());
        assertEquals(day, t.getDate());
        GroupTicket gt = (GroupTicket) loaded.get(1);
        assertSame(g, gt.getGroup());
        assertEquals("TG01:87654321X", gt.getComposition().get(1).getId());
        assertEquals(new BigDecimal("80.00"), gt.getPrice());
    }

    @Test
    void historicalTicketsSurviveDeletedUsersAndGroups() {
        TicketFileStore store = new TicketFileStore(dir.resolve("tickets.txt"));
        LocalDate day = LocalDate.of(2027, 4, 15);
        GroupTicket gt = new GroupTicket("TG01", day, new BigDecimal("40.00"), new Group("G01", "x"), List.of(ana, pedro));
        gt.clearGroup();
        store.save(List.of(new IndividualTicket("T001", day, new BigDecimal("40.00"), ana), gt));
        List<Ticket> loaded = store.load(new UserRepository(), new GroupRepository());
        IndividualTicket t = (IndividualTicket) loaded.get(0);
        assertEquals("12345678Z", t.getUser().getDni());
        assertEquals("ana@correo.com", t.getUser().getEmail());
        GroupTicket g = (GroupTicket) loaded.get(1);
        assertNull(g.getGroup());
        assertEquals(2, g.getComposition().size());
        assertEquals(new BigDecimal("80.00"), g.getPrice());
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        java.nio.file.Files.writeString(dir.resolve("attractions.txt"), "A01;X;notanumber\nbad\nA02;Y;3\n");
        assertEquals(1, new AttractionFileStore(dir.resolve("attractions.txt")).load().size());
        java.nio.file.Files.writeString(dir.resolve("tickets.txt"), "I;T1;notadate;40;1;2;3;4\nZ;?\n");
        assertTrue(new TicketFileStore(dir.resolve("tickets.txt")).load(new UserRepository(), new GroupRepository()).isEmpty());
    }
}
