package es.upm.park.service;

import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.persistence.DataPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static es.upm.park.service.ParkFixture.TODAY;
import static es.upm.park.service.ParkFixture.price;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    private Park reopen() {
        Park park = Park.getInstance();
        park.open(new DataPaths(dir));
        park.setCurrentDate(TODAY);
        return park;
    }

    @Test
    void restoresParkStateAfterRestart() throws OperationException {
        Park park = ParkFixture.openWithUsers(dir);
        GroupService groups = new GroupService(park);
        TicketService tickets = new TicketService(park);
        AttractionService attractions = new AttractionService(park);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", 20);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentPrice(price("45.50"));
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
        attractions.enter("A01", "TG01");

        Park restored = reopen();

        assertEquals(3, restored.findGroup("G01").size());
        assertEquals(20, restored.findAttraction("A01").getCapacity());
        assertEquals(0, restored.findAttraction("A01").getOccupancy());
        GroupTicket tg01 = assertInstanceOf(GroupTicket.class, restored.findTicket("TG01"));
        assertSame(restored.findGroup("G01"), tg01.getGroup());
        assertEquals(price("108.00"), tg01.getPrice());
        assertSame(restored.users().findByEmail("pedro@correo.com"),
                ((IndividualTicket) restored.findTicket("TG01:87654321X")).getUser());
        IndividualTicket t002 = assertInstanceOf(IndividualTicket.class, restored.findTicket("T002"));
        assertEquals(price("45.50"), t002.getPrice());
        assertEquals(TODAY.plusDays(5), t002.getDate());
        assertTrue(restored.isLocked(restored.findGroup("G01")));
        assertEquals(price("40.00"), restored.getCurrentPrice());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurviveRestart() throws OperationException {
        Park park = ParkFixture.openWithUsers(dir);
        GroupService groups = new GroupService(park);
        TicketService tickets = new TicketService(park);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");
        groups.delete("G01");

        Park restored = reopen();

        assertNull(restored.users().findByEmail("ana@correo.com"));
        IndividualTicket t001 = assertInstanceOf(IndividualTicket.class, restored.findTicket("T001"));
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        assertEquals(price("40.00"), t001.getPrice());
        GroupTicket tg01 = assertInstanceOf(GroupTicket.class, restored.findTicket("TG01"));
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertEquals(price("80.00"), tg01.getPrice());
        assertSame(restored.users().findByEmail("pedro@correo.com"),
                tg01.getComposition().get(1).getUser());
    }

    @Test
    void missingFilesStartEmpty() {
        Park park = reopen();
        assertTrue(park.tickets().isEmpty());
        assertNull(park.findGroup("G01"));
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        DataPaths paths = new DataPaths(dir);
        Files.write(paths.attractions(), List.of("A01;Dragon Rojo;20", "A02;Broken;x", "bad"));
        Files.write(paths.tickets(), List.of("GROUP;TG9;2027-04-15;", "UNKNOWN;x",
                "INDIVIDUAL;T1;not-a-date;40.00;0;;1;a@b.com;A;B"));
        Files.write(paths.groups(), List.of("G01;Grupo;99999999R", "onlyid"));

        Park park = reopen();

        assertNotNull(park.findAttraction("A01"));
        assertNull(park.findAttraction("A02"));
        assertTrue(park.tickets().isEmpty());
        assertEquals(0, park.findGroup("G01").size());
        assertFalse(park.isTicketIdTaken("TG9"));
    }
}
