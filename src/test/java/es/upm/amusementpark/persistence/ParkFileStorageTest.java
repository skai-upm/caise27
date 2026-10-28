package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;

class ParkFileStorageTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void groupsRoundTrip() throws Exception {
        GroupFileStorage storage = new GroupFileStorage(dir.resolve("sub").resolve("groups.txt"));
        Group g = new Group("G01", "Familia, Garcia");
        g.addMember("12345678Z");
        g.addMember("87654321X");
        storage.save(List.of(g, new Group("G02", "Vacio")));
        List<Group> loaded = storage.load(new ArrayList<>());
        assertEquals("Familia, Garcia", loaded.get(0).getName());
        assertEquals(List.of("12345678Z", "87654321X"), loaded.get(0).getMemberDnis());
        assertTrue(loaded.get(1).getMemberDnis().isEmpty());
    }

    @Test
    void attractionsRoundTripWithoutOccupants() throws Exception {
        AttractionFileStorage storage = new AttractionFileStorage(dir.resolve("attractions.txt"));
        Attraction a = new Attraction("A01", "Dragon Rojo", 20);
        a.addOccupant(ana);
        storage.save(List.of(a));
        Attraction loaded = storage.load(new ArrayList<>()).get(0);
        assertEquals("Dragon Rojo", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.getOccupants().isEmpty());
    }

    @Test
    void ticketsRoundTripWithRelationsAndHistoricalData() throws Exception {
        LocalDate day = LocalDate.of(2027, 4, 15);
        Group g = new Group("G01", "Familia");
        GroupTicket active = new GroupTicket("TG01", day, g, List.of(ana, pedro), new BigDecimal("40.00"));
        GroupTicket detached = new GroupTicket("TG00", day.minusDays(3), g, List.of(ana), new BigDecimal("45.50"));
        detached.detachGroup();
        IndividualTicket single = new IndividualTicket("T001", day, ana, new BigDecimal("45.50"));
        TicketFileStorage storage = new TicketFileStorage(dir.resolve("tickets.txt"));
        storage.save(List.of(single, active, detached));

        Map<String, User> users = Map.of("87654321X", pedro);
        List<String> warnings = new ArrayList<>();
        List<Ticket> loaded = storage.load(dni -> Optional.ofNullable(users.get(dni)),
                id -> id.equals("G01") ? Optional.of(g) : Optional.empty(), warnings);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertEquals(3, loaded.size());
        IndividualTicket t1 = (IndividualTicket) loaded.get(0);
        assertEquals(new BigDecimal("45.50"), t1.getPrice());
        assertEquals("ana@correo.com", t1.getUser().getEmail());
        assertEquals("12345678Z", t1.getUser().getDni());
        GroupTicket tg1 = (GroupTicket) loaded.get(1);
        assertSame(g, tg1.getGroup());
        assertEquals(new BigDecimal("80.00"), tg1.getPrice());
        assertEquals("TG01:87654321X", tg1.individualTickets().get(1).getId());
        assertSame(pedro, tg1.individualTickets().get(1).getUser());
        GroupTicket tg0 = (GroupTicket) loaded.get(2);
        assertNull(tg0.getGroup());
        assertEquals(new BigDecimal("45.50"), tg0.getPrice());
        assertEquals(day.minusDays(3), tg0.getDate());
    }
}
