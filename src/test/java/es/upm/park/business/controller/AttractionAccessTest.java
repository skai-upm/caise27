package es.upm.park.business.controller;

import es.upm.park.business.exception.AccessDeniedException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.attraction.AttractionType;
import es.upm.park.business.model.attraction.EntryResult;
import es.upm.park.business.model.attraction.GeneralAttraction;
import es.upm.park.business.model.attraction.GroupAttraction;
import es.upm.park.business.model.attraction.IndividualAttraction;
import es.upm.park.business.model.attraction.SpecialAttraction;
import es.upm.park.business.model.access.GeneralAccess;
import es.upm.park.business.model.access.GroupAccess;
import es.upm.park.business.model.access.IndividualAccess;
import es.upm.park.business.model.pass.Pass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionAccessTest {

    @TempDir
    Path dir;

    private Park park;
    private AttractionController attractions;
    private PassController passes;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        attractions = AttractionController.getInstance();
        passes = PassController.getInstance();
        TicketController tickets = TicketController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        attractions.create("A02", "Montana Individual", 12, AttractionType.INDIVIDUAL);
        attractions.create("A03", "Rafting Familiar", 12, AttractionType.GROUP);
        attractions.create("A04", "Pasaje del Terror", 15, AttractionType.SPECIAL);
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createTicket("T005", "marta@correo.com", TODAY);
        tickets.createGroupTicket("TG01", "G01", TODAY);
    }

    private long unconsumed(String ticketId) {
        return passes.speedPassesOf(ticketId).stream().filter(p -> !p.isConsumed()).count();
    }

    @Test
    void typesUseBoundedGenericsOverAccessCapabilities() {
        assertEquals(GeneralAccess.class, new GeneralAttraction("X", "x", 1).getAccessType());
        assertEquals(IndividualAccess.class, new IndividualAttraction("X", "x", 1).getAccessType());
        assertEquals(GroupAccess.class, new GroupAttraction("X", "x", 1).getAccessType());
        assertEquals(IndividualAccess.class, new SpecialAttraction("X", "x", 1).getAccessType());
        assertInstanceOf(IndividualAccess.class, park.findTicket("T001"));
        assertInstanceOf(GroupAccess.class, park.findTicket("TG01"));
        assertInstanceOf(IndividualAccess.class, park.findTicket("TG01:12345678Z"));
    }

    @Test
    void acceptedTicketKindsPerType() throws ParkException {
        Attraction<?> general = park.findAttraction("A01");
        assertTrue(general.accepts(park.findTicket("T001")));
        assertTrue(general.accepts(park.findTicket("TG01")));
        assertFalse(park.findAttraction("A02").accepts(park.findTicket("TG01")));
        assertFalse(park.findAttraction("A03").accepts(park.findTicket("T001")));
        assertFalse(park.findAttraction("A04").accepts(park.findTicket("TG01")));

        assertThrows(AccessDeniedException.class, () -> attractions.enter("A02", "TG01", false));
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A03", "T001", false));
        assertEquals(3, attractions.enter("A03", "TG01", false).entered().size());
    }

    @Test
    void componentTicketCountsAsIndividual() throws ParkException {
        assertEquals(1, attractions.enter("A02", "TG01:87654321X", false).entered().size());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() throws ParkException {
        passes.buySpeedPasses("SP01", "T001", 10);
        EntryResult result = attractions.enter("A02", "T001", true);
        assertEquals(1, result.consumedPasses().size());
        assertTrue(result.consumedPasses().get(0).isConsumed());
        assertEquals(9, unconsumed("T001"));
    }

    @Test
    void speedEntryWithoutPassChangesNothing() {
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A01", "T001", true));
        assertEquals(0, park.findAttraction("A01").getOccupancy());
    }

    @Test
    void groupSpeedEntryIsAtomic() throws ParkException {
        passes.buySpeedPasses("SP02", "TG01:12345678Z", 1);
        passes.buySpeedPasses("SP03", "TG01:87654321X", 1);

        assertThrows(AccessDeniedException.class, () -> attractions.enter("A03", "TG01", true));
        assertEquals(0, park.findAttraction("A03").getOccupancy());
        assertEquals(1, unconsumed("TG01:12345678Z"));
        assertEquals(1, unconsumed("TG01:87654321X"));

        passes.buySpeedPasses("SP04", "TG01:11111111H", 1);
        EntryResult result = attractions.enter("A03", "TG01", true);
        assertEquals(3, result.entered().size());
        assertEquals(3, result.consumedPasses().size());
        assertEquals(0, unconsumed("TG01:12345678Z") + unconsumed("TG01:87654321X") + unconsumed("TG01:11111111H"));
    }

    @Test
    void groupSpeedEntryAtGeneralAttraction() throws ParkException {
        passes.buySpeedPasses("SP02", "TG01:12345678Z", 1);
        passes.buySpeedPasses("SP03", "TG01:87654321X", 1);
        passes.buySpeedPasses("SP04", "TG01:11111111H", 1);
        assertEquals(3, attractions.enter("A01", "TG01", true).consumedPasses().size());
    }

    @Test
    void capacityFailureConsumesNothing() throws ParkException {
        attractions.create("A05", "Mini", 2, AttractionType.GROUP);
        passes.buySpeedPasses("SP02", "TG01:12345678Z", 1);
        passes.buySpeedPasses("SP03", "TG01:87654321X", 1);
        passes.buySpeedPasses("SP04", "TG01:11111111H", 1);
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A05", "TG01", true));
        assertEquals(1, unconsumed("TG01:11111111H"));
    }

    @Test
    void specialEntryConsumesSpecialPassAutomatically() throws ParkException {
        passes.buySpecialPass("PE01", "T001");
        EntryResult result = attractions.enter("A04", "T001", false);
        Pass consumed = result.consumedPasses().get(0);
        assertEquals("PE01", consumed.getId());
        assertTrue(consumed.isConsumed());

        attractions.exit("A04", "ana@correo.com");
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A04", "T001", false));
        assertEquals(0, park.findAttraction("A04").getOccupancy());
    }

    @Test
    void specialRejections() throws ParkException {
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A04", "T005", false));
        passes.buySpecialPass("PE05", "T005");
        passes.buySpeedPasses("SP05", "T005", 1);
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A04", "T005", true));
        assertFalse(passes.specialPassOf("T005").orElseThrow().isConsumed());
        assertEquals(1, unconsumed("T005"));
        assertThrows(AccessDeniedException.class, () -> attractions.enter("A04", "TG01", false));
    }

    @Test
    void attractionTypeParsing() {
        assertEquals(AttractionType.SPECIAL, AttractionType.parse(" special "));
        assertNull(AttractionType.parse("VIP"));
        assertInstanceOf(GroupAttraction.class, AttractionType.GROUP.create("A", "B", 3));
    }
}
