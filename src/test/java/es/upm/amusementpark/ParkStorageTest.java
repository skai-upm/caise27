package es.upm.amusementpark;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.TextFileRepositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkStorageTest {

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park.reset();
    }

    private void save(Path directory) {
        park.configure(TextFileRepositories.create(directory));
        park.persist();
    }

    private List<String> load(Path directory) {
        park.reset();
        park.configure(TextFileRepositories.create(directory));
        return park.load();
    }

    private void buildState() {
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.registerUser(new User("pedro@correo.com", "87654321X"));
        park.setCurrentDate("2027-04-01");
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroup("G02", "Temporal");
        park.addUserToGroup("G02", "ana@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createGroupTicket("TG02", "G02", "2027-04-02");
        Attraction<?> a01 = park.createAttraction("A01", "Dragon Rojo", "20");
        a01.addOccupant(park.findUser("ana@correo.com"));
        park.setCurrentDate("2027-04-03");
        park.deleteGroup("G02");
    }

    @Test
    void fullStateRoundTrip() {
        buildState();
        save(dir);
        park.reset();
        List<String> skipped = load(dir);
        assertTrue(skipped.isEmpty());

        User ana = park.findUser("ana@correo.com");
        assertEquals("12345678Z", ana.getDni());
        Group g01 = park.findGroup("G01");
        assertEquals("Familia Garcia", g01.getName());
        assertEquals(2, g01.getMembers().size());
        assertTrue(g01.hasMember(ana));
        assertNull(park.findGroup("G02"));

        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertSame(ana, t001.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t001.getDate());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());

        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertSame(g01, tg01.getGroup());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                tg01.getComposition().stream().map(Ticket::getIdentifier).toList());
        assertSame(ana, tg01.getComposition().get(0).getUser());
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());

        GroupTicket tg02 = (GroupTicket) park.findTicket("TG02");
        assertNull(tg02.getGroup());
        assertEquals(1, tg02.getComposition().size());
        assertEquals(LocalDate.of(2027, 4, 2), tg02.getDate());

        Attraction<?> a01 = park.findAttraction("A01");
        assertEquals("Dragon Rojo", a01.getName());
        assertEquals(20, a01.getCapacity());
        assertTrue(a01.getOccupants().isEmpty());
    }

    @Test
    void repositoriesOnlyTouchTheirDirectory() throws IOException {
        Path other = Files.createDirectory(dir.resolve("other"));
        Path first = Files.createDirectory(dir.resolve("first"));
        park.createAttraction("A01", "Dragon Rojo", "20");
        save(first);
        park.reset();
        load(other);
        assertNull(park.findAttraction("A01"));
        try (var files = Files.list(other)) {
            assertFalse(files.findAny().isPresent());
        }
        assertTrue(Files.exists(first.resolve("attractions.txt")));
    }

    @Test
    void historicalTicketOfUnknownUserStillLoads() throws IOException {
        Files.writeString(dir.resolve("tickets.txt"),
                "INDIVIDUAL;T009;2026-01-01;40.00;gone@correo.com;99999999R\n", StandardCharsets.UTF_8);
        load(dir);
        IndividualTicket ticket = (IndividualTicket) park.findTicket("T009");
        assertEquals("gone@correo.com", ticket.getUser().getEmail());
        assertNull(park.findUser("gone@correo.com"));
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\nbroken\nA02;X;abc\n",
                StandardCharsets.UTF_8);
        List<String> skipped = load(dir);
        assertEquals(List.of("broken", "A02;X;abc"), skipped);
        assertEquals(1, park.getAttractions().size());
    }

    @Test
    void filesAreTextLines() throws IOException {
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.createAttraction("A01", "Dragon Rojo", "20");
        save(dir);
        assertEquals(List.of("ana@correo.com;12345678Z;;;;;"), Files.readAllLines(dir.resolve("users.txt")));
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
    }
}
