package es.upm.park.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.service.ParkException;

class ParkControllerPersistenceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path dir;

    private void populate() {
        park.start(dir, DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getGroupService().create("G01", "Familia Garcia");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.setBasePrice("45.50");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.setBasePrice("40");
        park.createGroupTicket("TG01", "G01", DAY);
        park.getAttractionService().create("A01", "Dragon Rojo", "20");
        park.enterAttraction("A01", "T001");
    }

    @Test
    void reconstructsGroupsTicketsAndAttractions() {
        populate();
        park.start(dir, DAY);
        assertEquals(List.of("12345678Z", "87654321X"), park.getGroupService().find("G01").getMemberDnis());
        IndividualTicket t1 = (IndividualTicket) park.getTicketService().find("T001");
        assertSame(park.getUserService().authenticate("ana@correo.com", "Clave1!"), t1.getUser());
        assertEquals(new BigDecimal("45.50"), t1.getPrice());
        GroupTicket tg = (GroupTicket) park.getTicketService().find("TG01");
        assertSame(park.getGroupService().find("G01"), tg.getGroup());
        assertEquals(new BigDecimal("80.00"), tg.getPrice());
        assertEquals(List.of("TG01", "TG01:12345678Z", "TG01:87654321X"), tg.ids());
        assertTrue(park.getAttractionService().occupantEmails("A01").isEmpty());
        assertEquals(20, park.getAttractionService().find("A01").getCapacity());
        ParkException e = assertThrows(ParkException.class, () -> park.getGroupService().addUser("G01", "x@y.com"));
        assertEquals("group G01 has an active group ticket", e.getMessage());
    }

    @Test
    void fiscalRecordsSurviveDeletedUsersAndGroups() {
        populate();
        park.setCurrentDate("2027-04-16");
        park.deleteUser("pedro@correo.com", "Otra2#");
        park.deleteGroup("G01");
        park.getGroupService().create("G01", "Nuevo");
        park.start(dir, LocalDate.of(2027, 4, 16));
        GroupTicket tg = (GroupTicket) park.getTicketService().find("TG01");
        assertNull(tg.getGroup());
        assertEquals("G01", tg.getGroupId());
        assertEquals("Pedro", tg.getComposition().get(1).getUser().getFirstName());
        assertEquals(new BigDecimal("80.00"), tg.getPrice());
        assertTrue(park.getGroupService().find("G01").getMemberDnis().isEmpty());
        assertThrows(ParkException.class, () -> park.getUserService().authenticate("pedro@correo.com", "Otra2#"));
    }

    @Test
    void groupMembersAreTheLoadedUserObjects() {
        populate();
        park.start(dir, DAY);
        es.upm.park.model.User ana = park.getUserService().authenticate("ana@correo.com", "Clave1!");
        assertSame(ana, park.getGroupService().find("G01").getMembers().get(0));
    }

    @Test
    void groupMemberFollowsUserUpdateWithoutRelinking() {
        populate();
        es.upm.park.model.User ana = park.getUserService().update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertSame(ana, park.getGroupService().find("G01").getMembers().get(0));
        assertEquals("ana.nueva@correo.com", park.getGroupService().find("G01").getMembers().get(0).getEmail());
    }
}
