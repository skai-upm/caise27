package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.attraction.AttractionType;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;

import java.math.BigDecimal;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * Persistence of attraction types and passes (Delivery 3, F3-C09).
 */
class Delivery3PersistenceTest extends ParkTestSupport {

    @Test
    void typesAndPassesReconstructed() throws Exception {
        createAna();
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpecialPass("PE01", "T001");
        park.enterAttraction("A01", "T001", true);

        restart();

        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        Pass speed = park.findPass("SP01:1");
        Pass special = park.findPass("PE01");
        assertInstanceOf(SpeedPass.class, speed);
        assertInstanceOf(SpecialPass.class, special);
        assertSame(park.findTicket("T001"), speed.getTicket());
        assertSame(park.findTicket("T001"), special.getTicket());
        assertEquals(new BigDecimal("8.00"), speed.getPrice());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertTrue(speed.isConsumed());
        assertFalse(special.isConsumed());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void passFileFormat() throws Exception {
        createAna();
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpecialPass("PE01", "T001");
        assertEquals(java.util.List.of("SPEED;SP01:1;T001;8.00;false", "SPECIAL;PE01;T001;40.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));
    }

    @Test
    void delivery2AttractionLineLoadsAsGeneral() throws Exception {
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\nA02;Torre;5;GROUP\nA03;X;5;VIP\n");
        restart();
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A02").getType());
        assertEquals(20, park.findAttraction("A01").getCapacity());
    }

    @Test
    void consumedPassesKeptWhileTicketExists() throws Exception {
        createAna();
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.buySpecialPass("PE01", "T001");
        park.enterAttraction("A04", "T001", false);
        park.setCurrentDate(TOMORROW_TEXT);
        park.deleteUser("ana@correo.com", "Clave1!");
        restart();
        assertTrue(park.findPass("PE01").isConsumed());
    }
}
