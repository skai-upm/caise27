package es.upm.amusementpark.persistence.file;

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

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.SpecialAttraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

class ParkFileStorageTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void groupsRoundTrip() throws Exception {
        GroupFileStorage storage = new GroupFileStorage(dir.resolve("sub").resolve("groups.txt"));
        Group g = new Group("G01", "Familia, Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        storage.save(List.of(g, new Group("G02", "Vacio")));
        Map<String, User> users = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);
        List<Group> loaded = storage.load(dni -> Optional.ofNullable(users.get(dni)), new ArrayList<>());
        assertEquals("Familia, Garcia", loaded.get(0).getName());
        assertEquals(List.of(ana, pedro), loaded.get(0).getMembers());
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void attractionsRoundTripWithoutOccupants() throws Exception {
        AttractionFileStorage storage = new AttractionFileStorage(dir.resolve("attractions.txt"));
        Attraction<?> a = new GeneralAttraction("A01", "Dragon Rojo", 20);
        a.addOccupant(ana);
        storage.save(List.of(a, new SpecialAttraction("A04", "Pasaje del Terror", 15)));
        Attraction<?> loaded = storage.load(new ArrayList<>()).get(0);
        assertEquals(AttractionType.SPECIAL, storage.load(new ArrayList<>()).get(1).getType());
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

    @Test
    void attractionsWithoutTypeAreGeneral() throws Exception {
        Path file = dir.resolve("old-attractions.txt");
        java.nio.file.Files.writeString(file, "A01;Dragon Rojo;20\n");
        Attraction<?> loaded = new AttractionFileStorage(file).load(new ArrayList<>()).get(0);
        assertEquals(AttractionType.GENERAL, loaded.getType());
        assertEquals(20, loaded.getCapacity());
    }

    @Test
    void passesRoundTripWithPriceAndState() throws Exception {
        LocalDate day = LocalDate.of(2027, 4, 15);
        IndividualTicket t1 = new IndividualTicket("T001", day, ana, new BigDecimal("40.00"));
        GroupTicket tg = new GroupTicket("TG01", day, new Group("G01", "g"), List.of(ana, pedro, ana2()),
                new BigDecimal("40.00"));
        IndividualTicket composition = tg.individualTickets().get(1);
        SpeedPass consumed = new SpeedPass("SP01:1", t1);
        consumed.consume();
        PassFileStorage storage = new PassFileStorage(dir.resolve("passes.txt"));
        storage.save(List.of(consumed, new SpeedPass("SP02:1", composition), new SpecialPass("PE01", t1)));
        Map<String, IndividualTicket> tickets = Map.of("T001", t1, composition.getId(), composition);
        List<String> warnings = new ArrayList<>();
        List<Pass> loaded = storage.load(id -> Optional.ofNullable(tickets.get(id)), warnings);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertEquals(3, loaded.size());
        assertTrue(loaded.get(0) instanceof SpeedPass && loaded.get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), loaded.get(0).getPrice());
        assertSame(composition, loaded.get(1).getTicket());
        assertEquals(new BigDecimal("7.20"), loaded.get(1).getPrice());
        assertTrue(loaded.get(2) instanceof SpecialPass && !loaded.get(2).isConsumed());
        assertEquals(new BigDecimal("40.00"), loaded.get(2).getPrice());
    }

    private User ana2() {
        return new User("Luis", "P", "11111111H", "luis@correo.com", "x", "6", "1111222233334444");
    }
}
