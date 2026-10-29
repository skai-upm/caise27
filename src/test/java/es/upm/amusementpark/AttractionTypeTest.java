package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.GeneralAccess;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAccess;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialAttraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionTypeTest extends ParkTestSupport {

    private PassController passes;
    private GroupTicket tg;

    @BeforeEach
    void setUpPark() {
        passes = PassController.getInstance();
        for (int i = 1; i <= 3; i++) {
            user(i);
        }
        park.createTicket("T001", email(1), TODAY.toString());
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(2));
        park.addToGroup("G01", email(3));
        tg = park.createGroupTicket("TG01", "G01", TODAY.toString());
        park.createAttraction("A01", "Dragon Rojo", "20", "GENERAL");
        park.createAttraction("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        park.createAttraction("A03", "Rafting Familiar", "24", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
    }

    private int inside(String attraction) {
        return park.getAttraction(attraction).getOccupants().size();
    }

    @Test
    void typesAndGenericBounds() {
        assertInstanceOf(GeneralAttraction.class, park.getAttraction("A01"));
        assertInstanceOf(IndividualAttraction.class, park.getAttraction("A02"));
        assertInstanceOf(GroupAttraction.class, park.getAttraction("A03"));
        assertInstanceOf(SpecialAttraction.class, park.getAttraction("A04"));
        assertEquals(GeneralAccess.class, park.getAttraction("A01").getAccessType());
        assertEquals(IndividualAccess.class, park.getAttraction("A02").getAccessType());
        assertEquals(GroupAccess.class, park.getAttraction("A03").getAccessType());
        assertEquals(IndividualAccess.class, park.getAttraction("A04").getAccessType());
        assertInstanceOf(IndividualAccess.class, park.findTicket("T001"));
        assertInstanceOf(GroupAccess.class, tg);
        assertFalse(tg instanceof IndividualAccess);
        assertEquals(AttractionType.SPECIAL, park.getAttraction("A04").getType());
    }

    @Test
    void invalidType() {
        assertThrows(ParkException.class, () -> park.createAttraction("A05", "X", "10", "VIP"));
        assertEquals(null, park.getAttraction("A05"));
    }

    @Test
    void acceptedTicketTypes() {
        park.enterAttraction("A01", "T001", false);
        park.enterAttraction("A01", "TG01", false);
        assertEquals(3, inside("A01"));

        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01", false));
        park.enterAttraction("A02", "T001", false);
        assertEquals(1, inside("A02"));

        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "T001", false));
        park.enterAttraction("A03", "TG01", false);
        assertEquals(2, inside("A03"));

        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "TG01", false));
        assertEquals(0, inside("A04"));
    }

    @Test
    void individualSpeedEntryConsumesOnePass() {
        passes.buySpeedPasses("SP02", "T001", "10");
        EntryResult result = park.enterAttraction("A02", "T001", true);
        assertEquals(1, result.consumedPasses().size());
        Pass consumed = result.consumedPasses().get(0);
        assertEquals("SP02:1", consumed.getId());
        assertTrue(consumed.isConsumed());
        assertEquals(1, ((IndividualTicket) park.findTicket("T001")).getSpeedPasses().stream()
                .filter(Pass::isConsumed).count());
    }

    @Test
    void speedEntryWithoutPassRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001", true));
        assertEquals(0, inside("A01"));
    }

    @Test
    void failedSpeedEntryDoesNotConsume() {
        passes.buySpeedPasses("SP01", "T001", "1");
        park.createAttraction("A09", "Full", "1", "GENERAL");
        park.enterAttraction("A09", "TG01:" + dni(2), false);
        assertThrows(ParkException.class, () -> park.enterAttraction("A09", "T001", true));
        assertFalse(park.findPass("SP01:1").isConsumed());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        List<IndividualTicket> members = tg.getComposition();
        passes.buySpeedPasses("SPA", members.get(0).getId(), "1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "TG01", true));
        assertEquals(0, inside("A03"));
        assertFalse(park.findPass("SPA:1").isConsumed());

        passes.buySpeedPasses("SPB", members.get(1).getId(), "1");
        EntryResult result = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, result.entered().size());
        assertEquals(2, result.consumedPasses().size());
        assertTrue(park.findPass("SPA:1").isConsumed());
        assertTrue(park.findPass("SPB:1").isConsumed());
    }

    @Test
    void groupSpeedEntryAtGeneral() {
        for (IndividualTicket member : tg.getComposition()) {
            passes.buySpeedPasses("S" + member.getUser().getDni(), member.getId(), "1");
        }
        assertEquals(2, park.enterAttraction("A01", "TG01", true).consumedPasses().size());
    }

    @Test
    void specialEntryConsumesSpecialPass() {
        passes.buySpecialPass("PE01", "T001");
        EntryResult result = park.enterAttraction("A04", "T001", false);
        assertEquals("PE01", result.consumedPasses().get(0).getId());
        assertTrue(park.findPass("PE01").isConsumed());
        park.exitAttraction("A04", email(1));
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001", false), "already consumed");
        assertEquals(0, inside("A04"));
    }

    @Test
    void specialEntryRejections() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001", false), "no Special Pass");
        passes.buySpecialPass("PE01", "T001");
        passes.buySpeedPasses("SP01", "T001", "1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001", true), "SPEED not allowed");
        assertFalse(park.findPass("PE01").isConsumed());
        assertFalse(park.findPass("SP01:1").isConsumed());
        assertEquals(0, inside("A04"));
    }

    @Test
    void failedSpecialEntryKeepsPass() {
        passes.buySpecialPass("PE01", "T001");
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001", false));
        assertFalse(park.findPass("PE01").isConsumed());
    }
}
