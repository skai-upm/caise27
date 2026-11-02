package es.upm.etsisi.amusementpark.park;

import static es.upm.etsisi.amusementpark.park.ParkTest.TODAY;
import static es.upm.etsisi.amusementpark.park.ParkTest.TOMORROW;
import static es.upm.etsisi.amusementpark.park.ParkTest.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.OperationException;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.user.User;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    Park park;

    @BeforeEach
    void setUp() throws IOException {
        park = Park.getInstance();
        park.load(DataPaths.in(dir));
        park.setCurrentDate(TODAY);
    }

    private void restart() throws IOException {
        park.load(DataPaths.in(dir));
        park.setCurrentDate(TODAY);
    }

    @Test
    void restartReconstructsRelationships() throws Exception {
        User ana = user(1);
        User pedro = user(2);
        park.createUser(ana);
        park.createUser(pedro);
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", ana.getEmail());
        park.addToGroup("G01", pedro.getEmail());
        park.createTicket("T001", ana.getEmail(), TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        park.createGroupTicket("TG01", "G01", TOMORROW);
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.enter("A01", "T001");

        restart();

        User ana2 = park.getUsers().findByEmail(ana.getEmail());
        Group g = park.findGroup("G01");
        assertEquals(List.of(ana2, park.getUsers().findByEmail(pedro.getEmail())), g.getMembers());
        IndividualTicket t = (IndividualTicket) park.findTicket("T001");
        assertSame(ana2, t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        GroupTicket gt = (GroupTicket) park.findTicket("TG01");
        assertSame(g, gt.getGroup());
        assertEquals(2, gt.getComposition().size());
        assertSame(ana2, gt.getComposition().get(0).getUser());
        assertEquals(new BigDecimal("91.00"), gt.getPrice());
        assertEquals(20, park.requireAttraction("A01").getCapacity());
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertTrue(park.getLoadWarnings().isEmpty());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurviveRestart() throws Exception {
        User ana = user(1);
        park.createUser(ana);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ana.getEmail());
        park.createTicket("T001", ana.getEmail(), TODAY);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TOMORROW);
        park.deleteGroup("G01");
        park.deleteUser(ana.getEmail(), "Clave1!");

        restart();

        assertNull(park.getUsers().findByEmail(ana.getEmail()));
        IndividualTicket t = (IndividualTicket) park.findTicket("T001");
        assertEquals(ana.getDni(), t.getUser().getDni());
        assertEquals(ana.getEmail(), t.getUser().getEmail());
        GroupTicket gt = (GroupTicket) park.findTicket("TG01");
        assertNull(gt.getGroup());
        assertEquals(1, gt.getComposition().size());
        assertSame(t.getUser(), gt.getComposition().get(0).getUser());
        assertNull(park.findGroup("G01"));
    }

    @Test
    void removedMembershipAndDeletedTicketsPersist() throws Exception {
        park.createUser(user(1));
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", user(1).getEmail());
        park.createTicket("T009", user(1).getEmail(), TOMORROW);
        park.deleteTicket("T009");
        park.removeFromGroup("G01", user(1).getEmail());

        restart();

        assertTrue(park.findGroup("G01").getMembers().isEmpty());
        assertNull(park.findTicket("T009"));
    }

    @Test
    void writeFailureLeavesStateUnchanged() throws Exception {
        park.createUser(user(1));
        Path tickets = dir.resolve("tickets.txt");
        Files.createDirectory(tickets);
        assertThrows(OperationException.class, () -> park.createTicket("T001", user(1).getEmail(), TODAY));
        assertNull(park.findTicket("T001"));
    }

    @Test
    void malformedLinesAreSkippedWithWarnings() throws Exception {
        Files.write(dir.resolve("groups.txt"), List.of("G01;Familia;99999999R", "bad"), StandardCharsets.UTF_8);
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;x"), StandardCharsets.UTF_8);
        Files.write(dir.resolve("tickets.txt"), List.of("I;T1;not-a-date;40.00;0;1;a;b;c", "C;TG;x"),
                StandardCharsets.UTF_8);
        restart();
        assertNotNull(park.findGroup("G01"));
        assertTrue(park.getAttractions().isEmpty());
        assertTrue(park.getTickets().isEmpty());
        assertEquals(5, park.getLoadWarnings().size());
    }
}
