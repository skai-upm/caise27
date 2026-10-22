package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    @Test
    void parkStateSurvivesRestart() {
        Park park = TestPark.withUsers(dir);
        park.getGroupService().create("G01", "Familia Garcia");
        park.getGroupService().addMember("G01", "ana@correo.com");
        park.getGroupService().addMember("G01", "pedro@correo.com");
        park.getGroupService().addMember("G01", "laura@correo.com");
        park.getAttractionService().create("A01", "Dragon Rojo", "20");
        park.getSystemService().setPrice("45.50");
        park.getTicketService().createIndividual("T001", "marta@correo.com", "2027-04-20");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");
        park.getAttractionService().enter("A01", "TG01");

        park.load(dir);

        Group group = park.getGroups().findById("G01").orElseThrow();
        assertEquals(3, group.getMembers().size());
        assertSame(park.getUsers().findByEmail("ana@correo.com").orElseThrow(), group.getMembers().get(0));

        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001").orElseThrow();
        assertSame(park.getUsers().findByEmail("marta@correo.com").orElseThrow(), t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());
        assertEquals(LocalDate.of(2027, 4, 20), t001.getDate());

        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01").orElseThrow();
        assertSame(group, tg01.getGroup());
        assertEquals(3, tg01.getTickets().size());
        assertEquals(new BigDecimal("122.85"), tg01.getPrice());
        assertTrue(park.getTickets().findById("TG01:12345678Z").isPresent());

        assertEquals(20, park.getAttractions().findById("A01").orElseThrow().getCapacity());
        assertTrue(park.getAttractions().findById("A01").orElseThrow().getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), park.getState().getBasePrice());
    }

    @Test
    void historicalRecordsOfDeletedUsersAndGroupsSurviveRestart() {
        Park park = TestPark.withUsers(dir);
        park.getGroupService().create("G01", "Familia");
        park.getGroupService().addMember("G01", "laura@correo.com");
        park.getTicketService().createIndividual("T001", "marta@correo.com", "2027-04-15");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemService().setDate("2027-04-16");
        park.getGroupService().delete("G01");
        park.getUserService().delete("marta@correo.com", "Marta4%");
        park.getUserService().delete("laura@correo.com", "Laura3$");

        park.load(dir);

        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001").orElseThrow();
        assertEquals("00000000T", t001.getUser().getDni());
        assertEquals("marta@correo.com", t001.getUser().getEmail());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01").orElseThrow();
        assertNull(tg01.getGroup());
        assertEquals("11111111H", tg01.getTickets().get(0).getUser().getDni());
        assertTrue(park.getUsers().findByEmail("marta@correo.com").isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;20", "bad", "A02;Torre;x"));
        Files.write(dir.resolve("tickets.txt"), List.of("INDIVIDUAL;T1;not-a-date;40.00;0;1;a;b;c", "???"));
        Files.write(dir.resolve("groups.txt"), List.of("G01;Fam;99999999R", "broken"));

        Park park = TestPark.empty(dir);

        assertEquals(1, park.getAttractions().findAll().size());
        assertTrue(park.getTickets().findAll().isEmpty());
        assertTrue(park.getGroups().findById("G01").orElseThrow().isEmpty());
    }
}
