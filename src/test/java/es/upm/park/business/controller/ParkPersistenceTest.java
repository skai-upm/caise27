package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.persistence.DataPaths;
import es.upm.park.business.model.attraction.AttractionType;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static es.upm.park.business.controller.ParkFixture.price;
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
        return ParkFixture.restart(dir);
    }

    @Test
    void restoresParkStateAfterRestart() throws ParkException {
        Park park = ParkFixture.openWithUsers(dir);
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentPrice(price("45.50"));
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
        attractions.enter("A01", "TG01", false);

        Park restored = reopen();

        assertEquals(3, restored.findGroup("G01").size());
        assertEquals(20, restored.findAttraction("A01").getCapacity());
        assertEquals(0, restored.findAttraction("A01").getOccupancy());
        GroupTicket tg01 = assertInstanceOf(GroupTicket.class, restored.findTicket("TG01"));
        assertSame(restored.findGroup("G01"), tg01.getGroup());
        assertEquals(price("108.00"), tg01.getPrice());
        assertSame(restored.findUserByEmail("pedro@correo.com"),
                ((IndividualTicket) restored.findTicket("TG01:87654321X")).getUser());
        IndividualTicket t002 = assertInstanceOf(IndividualTicket.class, restored.findTicket("T002"));
        assertEquals(price("45.50"), t002.getPrice());
        assertEquals(TODAY.plusDays(5), t002.getDate());
        assertTrue(restored.isLocked(restored.findGroup("G01")));
        assertEquals(price("40.00"), restored.getCurrentPrice());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurviveRestart() throws ParkException {
        Park park = ParkFixture.openWithUsers(dir);
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        UserController.getInstance().delete("ana@correo.com", "Clave1!");
        groups.delete("G01");

        Park restored = reopen();

        assertNull(restored.findUserByEmail("ana@correo.com"));
        IndividualTicket t001 = assertInstanceOf(IndividualTicket.class, restored.findTicket("T001"));
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        assertEquals(price("40.00"), t001.getPrice());
        GroupTicket tg01 = assertInstanceOf(GroupTicket.class, restored.findTicket("TG01"));
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertEquals(price("80.00"), tg01.getPrice());
        assertSame(restored.findUserByEmail("pedro@correo.com"),
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

    @Test
    void attractionTypesAndPassesSurviveRestart() throws ParkException {
        ParkFixture.openWithUsers(dir);
        AttractionController attractions = AttractionController.getInstance();
        PassController passes = PassController.getInstance();
        attractions.create("A04", "Pasaje del Terror", 15, AttractionType.SPECIAL);
        TicketController.getInstance().createTicket("T001", "ana@correo.com", TODAY);
        passes.buySpecialPass("PE01", "T001");
        passes.buySpeedPasses("SP01", "T001", 1);
        attractions.enter("A04", "T001", false);

        Park restored = reopen();

        assertEquals(AttractionType.SPECIAL, restored.findAttraction("A04").getType());
        Pass special = restored.findPass("PE01");
        assertInstanceOf(SpecialPass.class, special);
        assertTrue(special.isConsumed());
        assertEquals(price("40.00"), special.getPrice());
        assertSame(restored.findTicket("T001"), special.getTicket());
        Pass speed = restored.findPass("SP01:1");
        assertInstanceOf(SpeedPass.class, speed);
        assertFalse(speed.isConsumed());
        assertEquals(price("8.00"), speed.getPrice());
    }

    @Test
    void deliveryTwoAttractionLineIsGeneral() throws IOException {
        DataPaths paths = new DataPaths(dir);
        Files.write(paths.attractions(), List.of("A01;Dragon Rojo;20", "A02;Torre;12;INDIVIDUAL", "A03;X;5;VIP"));
        Park park = reopen();
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").getType());
        assertNull(park.findAttraction("A03"));
    }

    @Test
    void passesOfUnknownTicketsAreSkipped() throws IOException {
        DataPaths paths = new DataPaths(dir);
        Files.write(paths.passes(), List.of("SPEED;SP01:1;T404;8.00;false", "SPEED;SP02:1;T404;x;maybe"));
        Park park = reopen();
        assertNull(park.findPass("SP01:1"));
    }
}
