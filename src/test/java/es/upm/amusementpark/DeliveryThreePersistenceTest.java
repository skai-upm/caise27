package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;

import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import org.junit.jupiter.api.Test;

class DeliveryThreePersistenceTest extends ParkTestSupport {

    @Test
    void passesAndTypesSurviveRestart() {
        PassController passes = PassController.getInstance();
        user(1);
        user(2);
        park.createTicket("T001", email(1), TODAY.toString());
        park.createGroup("G01", "G");
        park.addToGroup("G01", email(2));
        park.createGroupTicket("TG01", "G01", "2027-04-20");
        passes.buySpeedPasses("SP02", "T001", "10");
        passes.buySpecialPass("PE01", "T001");
        passes.buySpecialPass("PEG", "TG01:" + dni(2));
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createAttraction("A01", "Dragon Rojo", "20", "GENERAL");
        park.enterAttraction("A01", "T001", true);

        reload();

        assertEquals(AttractionType.SPECIAL, park.getAttraction("A04").getType());
        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertEquals(10, t001.getSpeedPasses().size());
        assertTrue(t001.getSpeedPasses().get(0).isConsumed(), "consumed state persisted");
        assertFalse(t001.getSpeedPasses().get(1).isConsumed());
        assertEquals(new BigDecimal("8.00"), t001.getSpeedPasses().get(5).getPrice());
        SpecialPass pe = t001.getSpecialPass().orElseThrow();
        assertEquals("PE01", pe.getId());
        assertSame(t001, pe.getTicket());
        Pass memberPass = park.findPass("PEG");
        assertEquals("TG01:" + dni(2), memberPass.getTicket().getId());
        assertEquals(new BigDecimal("40.00"), memberPass.getPrice());
        assertTrue(park.getAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void legacyAttractionLineIsGeneral() throws Exception {
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\n");
        reload();
        assertEquals(AttractionType.GENERAL, park.getAttraction("A01").getType());
    }

    @Test
    void refundedPassNoLongerPersisted() throws Exception {
        user(1);
        park.createTicket("T001", email(1), "2027-04-15");
        PassController.getInstance().buySpeedPasses("SP01", "T001", "1");
        PassController.getInstance().refundSpeedPass("SP01:1");
        assertFalse(Files.readString(dir.resolve("passes.txt")).contains("SP01:1"));
    }
}
