package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.TestSupport;
import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    private ParkController park;
    private ByteArrayOutputStream warnings;

    @BeforeEach
    void setUp() {
        warnings = new ByteArrayOutputStream();
        reload();
        park.setCurrentDate("2027-04-15");
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "634567890",
                "1111222233334444");
    }

    private void reload() {
        park = TestSupport.park(dir, new PrintStream(warnings, true, StandardCharsets.UTF_8));
    }

    @Test
    void reconstructsGroupsTicketsAndAttractions() {
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "laura@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentPrice("45.50");
        park.createTicket("T001", "ana@correo.com", "2027-04-20");
        park.createAttraction("A01", "Dragon Rojo", "20", null);
        park.enterAttraction("A01", "TG01", false);

        reload();

        assertEquals(3, park.findGroup("G01").orElseThrow().getMembers().size());
        GroupTicket group = (GroupTicket) park.findTicket("TG01").orElseThrow();
        assertSame(park.findGroup("G01").orElseThrow(), group.getGroup());
        assertEquals(LocalDate.of(2027, 4, 15), group.getDate());
        assertEquals(new BigDecimal("108.00"), group.getPrice());
        assertEquals(List.of("TG01", "TG01:12345678Z", "TG01:87654321X", "TG01:11111111H"), group.getIdentifiers());
        assertSame(park.users().findByEmail("ana@correo.com").orElseThrow(), group.getTickets().get(0).getUser());
        IndividualTicket single = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertEquals(new BigDecimal("45.50"), single.getPrice());
        Attraction<?> attraction = park.requireAttraction("A01");
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertEquals(0, attraction.getOccupancy());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void historicalTicketsOfDeletedUsersAndGroupsSurviveRestart() {
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteUser("ana@correo.com", "Clave1!");
        park.deleteGroup("G01");

        reload();

        assertTrue(park.users().findByEmail("ana@correo.com").isEmpty());
        IndividualTicket single = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertEquals("12345678Z", single.getUser().getDni());
        assertEquals("ana@correo.com", single.getUser().getEmail());
        GroupTicket group = (GroupTicket) park.findTicket("TG01").orElseThrow();
        assertNull(group.getGroup());
        assertEquals(2, group.getTickets().size());
        assertSame(single.getUser(), group.getTickets().get(0).getUser());
        assertEquals(new BigDecimal("80.00"), group.getPrice());
    }

    @Test
    void ticketFileFormat() throws IOException {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(List.of(
                "INDIVIDUAL;T001;2027-04-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "GROUP;TG01;2027-04-15;40.00;G01",
                "MEMBER;TG01;87654321X;pedro@correo.com;Pedro;Lopez Martin"),
                Files.readAllLines(dir.resolve(ParkStorage.TICKETS_FILE)));
        assertEquals(List.of("G01;Familia;87654321X"), Files.readAllLines(dir.resolve(ParkStorage.GROUPS_FILE)));
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Files.write(dir.resolve(ParkStorage.TICKETS_FILE), List.of(
                "INDIVIDUAL;T001;2027-04-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "INDIVIDUAL;broken",
                "MEMBER;TGX;1;2;3;4",
                "OTHER;x"));
        Files.write(dir.resolve(ParkStorage.ATTRACTIONS_FILE), List.of("A01;Dragon;20", "A02;Bad;x"));
        Files.write(dir.resolve(ParkStorage.GROUPS_FILE), List.of("G01;Familia;12345678Z,99999999R", "bad"));
        reload();
        List<Ticket> tickets = park.getTickets();
        assertEquals(1, tickets.size());
        assertTrue(park.findGroup("G01").isPresent());
        assertEquals(1, park.findGroup("G01").orElseThrow().getMembers().size());
        park.requireAttraction("A01");
        String text = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("WARNING"));
        assertTrue(text.contains("99999999R"));
    }
}
