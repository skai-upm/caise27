package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialAttraction;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkStorageTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private UserRepository users;
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        users.save(ana);
        users.save(pedro);
    }

    @Test
    void groupsRoundTripWithMembers() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        group.addMember(pedro);
        GroupFileStorage storage = new GroupFileStorage(dir.resolve("groups.txt"));
        storage.save(List.of(group, new Group("G02", "Vacio")));
        List<Group> loaded = storage.load(users);
        assertEquals(2, loaded.size());
        assertEquals("Familia Garcia", loaded.get(0).getName());
        assertEquals(List.of(ana, pedro), loaded.get(0).getMembers());
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void attractionsRoundTripEmpty() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        attraction.enter(List.of(ana));
        AttractionFileStorage storage = new AttractionFileStorage(dir.resolve("attractions.txt"));
        storage.save(List.of(attraction));
        Attraction<?> loaded = storage.load().get(0);
        assertEquals("Dragon Rojo", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.getOccupants().isEmpty());
    }

    @Test
    void ticketsRoundTripWithRelationships() {
        GroupRepository groups = new GroupRepository();
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        group.addMember(pedro);
        groups.save(group);
        IndividualTicket t001 = new IndividualTicket("T001", DAY, ana, PRICE);
        List<IndividualTicket> composition = List.of(
                new IndividualTicket("TG01:12345678Z", DAY, ana, PRICE),
                new IndividualTicket("TG01:87654321X", DAY, pedro, PRICE));
        GroupTicket tg01 = new GroupTicket("TG01", DAY, group, composition);
        TicketFileStorage storage = new TicketFileStorage(dir.resolve("tickets.txt"));
        storage.save(List.of(t001, composition.get(0), composition.get(1), tg01));

        List<Ticket> loaded = storage.load(users, groups);
        assertEquals(4, loaded.size());
        IndividualTicket loadedT001 = (IndividualTicket) loaded.get(0);
        assertSame(ana, loadedT001.getUser());
        assertEquals(PRICE, loadedT001.getPrice());
        GroupTicket loadedGroup = (GroupTicket) loaded.get(3);
        assertSame(group, loadedGroup.getGroup());
        assertEquals(2, loadedGroup.getComposition().size());
        assertSame(loadedGroup, loadedGroup.getComposition().get(0).getGroupTicket());
        assertEquals(new BigDecimal("80.00"), loadedGroup.getPrice());
        assertEquals(DAY, loadedGroup.getDate());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupAreRebuilt() {
        IndividualTicket t001 = new IndividualTicket("T001", DAY, ana, PRICE);
        GroupTicket tg01 = new GroupTicket("TG01", DAY, null,
                List.of(new IndividualTicket("TG01:12345678Z", DAY, ana, PRICE)));
        TicketFileStorage storage = new TicketFileStorage(dir.resolve("tickets.txt"));
        storage.save(List.of(t001, tg01.getComposition().get(0), tg01));
        users.delete("ana@correo.com");

        List<Ticket> loaded = storage.load(users, new GroupRepository());
        IndividualTicket historical = (IndividualTicket) loaded.get(0);
        assertEquals("12345678Z", historical.getUser().getDni());
        assertEquals("ana@correo.com", historical.getUser().getEmail());
        assertTrue(users.findByDni("12345678Z").isEmpty());
        GroupTicket loadedGroup = (GroupTicket) loaded.get(2);
        assertNull(loadedGroup.getGroup());
        assertSame(historical.getUser(), loadedGroup.getComposition().get(0).getUser());
    }

    @Test
    void malformedTicketLinesAreSkipped() throws Exception {
        Files.write(dir.resolve("tickets.txt"), List.of("INDIVIDUAL;T001;bad-date;40.00;;12345678Z;A;B;a@b.com",
                "nonsense", "GROUP;TG01;2027-04-15;"));
        TicketFileStorage storage = new TicketFileStorage(dir.resolve("tickets.txt"));
        List<Ticket> loaded = storage.load(users, new GroupRepository());
        assertEquals(1, loaded.size());
        assertEquals(2, storage.getSkippedLines());
    }

    @Test
    void repositoriesReloadFromTheirStorages() {
        GroupRepository groups = new GroupRepository(new GroupFileStorage(dir.resolve("groups.txt")), users);
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        groups.save(group);
        TicketRepository tickets = new TicketRepository(new TicketFileStorage(dir.resolve("tickets.txt")), users,
                groups);
        tickets.save(new IndividualTicket("T001", DAY, ana, PRICE));
        AttractionRepository attractions = new AttractionRepository(
                new AttractionFileStorage(dir.resolve("attractions.txt")));
        attractions.save(new GeneralAttraction("A01", "Dragon Rojo", 20));

        GroupRepository groups2 = new GroupRepository(new GroupFileStorage(dir.resolve("groups.txt")), users);
        assertTrue(groups2.findById("G01").orElseThrow().contains(ana));
        assertTrue(new TicketRepository(new TicketFileStorage(dir.resolve("tickets.txt")), users, groups2)
                .exists("T001"));
        assertTrue(new AttractionRepository(new AttractionFileStorage(dir.resolve("attractions.txt")))
                .findById("A01").isPresent());
    }

    @Test
    void attractionTypeIsStoredAndMissingTypeMeansGeneral() throws Exception {
        AttractionFileStorage storage = new AttractionFileStorage(dir.resolve("attractions.txt"));
        storage.save(List.of(new SpecialAttraction("A04", "Pasaje del Terror", 15)));
        assertEquals(List.of("A04;Pasaje del Terror;15;SPECIAL"), Files.readAllLines(dir.resolve("attractions.txt")));
        assertEquals(AttractionType.SPECIAL, storage.load().get(0).getType());
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A09;Bad;3;VIP"));
        List<Attraction<?>> loaded = storage.load();
        assertEquals(1, loaded.size());
        assertEquals(AttractionType.GENERAL, loaded.get(0).getType());
        assertEquals(20, loaded.get(0).getCapacity());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    void passesRoundTripWithPriceAndConsumedState() throws Exception {
        TicketRepository tickets = new TicketRepository();
        IndividualTicket t001 = new IndividualTicket("T001", DAY, ana, PRICE);
        tickets.save(t001);
        SpeedPass speed = new SpeedPass("SP01:1", t001);
        SpecialPass special = new SpecialPass("PE01", t001);
        special.consume();
        PassFileStorage storage = new PassFileStorage(dir.resolve("passes.txt"));
        storage.save(List.of(speed, special));
        assertEquals(List.of("SPEED;SP01:1;T001;8.00;false", "SPECIAL;PE01;T001;40.00;true"),
                Files.readAllLines(dir.resolve("passes.txt")));

        List<Pass> loaded = storage.load(tickets);
        assertEquals(2, loaded.size());
        assertTrue(loaded.get(0) instanceof SpeedPass);
        assertSame(t001, loaded.get(0).getTicket());
        assertEquals(new BigDecimal("8.00"), loaded.get(0).getPrice());
        assertTrue(loaded.get(1) instanceof SpecialPass);
        assertTrue(loaded.get(1).isConsumed());
    }

    @Test
    void passesOfUnknownTicketsAreSkipped() throws Exception {
        Files.write(dir.resolve("passes.txt"), List.of("SPEED;SP01:1;T404;8.00;false", "OTHER;X;T001;1;false"));
        PassFileStorage storage = new PassFileStorage(dir.resolve("passes.txt"));
        assertTrue(storage.load(new TicketRepository()).isEmpty());
        assertEquals(2, storage.getSkippedLines());
    }
}
