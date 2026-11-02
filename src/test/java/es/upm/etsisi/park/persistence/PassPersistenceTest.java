package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.AttractionType;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.Park;
import es.upm.etsisi.park.business.ParkTestSupport;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.SpecialPass;
import es.upm.etsisi.park.business.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class PassPersistenceTest extends ParkTestSupport {

    @Test
    void passesAndTypesRoundTrip() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        tickets.create("T001", "ana@correo.com", TODAY_S);
        GroupTicket tg = tickets.createGroupTicket("TG01", "G01", TODAY_S);
        passes.createSpeedPasses("SP01", "T001", "10");
        passes.createSpecialPass("PE01", tg.getComposition().get(1).getId());
        attractions.create("A01", "General", "20", null);
        attractions.create("A04", "Terror", "15", "SPECIAL");
        attractions.enter("A01", "T001", true);

        Park p = restart();
        Pass consumed = p.findPass("SP01:1");
        assertTrue(consumed instanceof SpeedPass);
        assertTrue(consumed.isConsumed());
        assertFalse(p.findPass("SP01:2").isConsumed());
        assertSame(p.findTicket("T001"), consumed.getTicket());
        assertEquals(new BigDecimal("8.00"), consumed.getPrice());
        Pass special = p.findPass("PE01");
        assertTrue(special instanceof SpecialPass);
        assertSame(p.findTicket("TG01:87654321X"), special.getTicket());
        assertEquals(11, p.getPasses().size());
        assertEquals(AttractionType.SPECIAL, p.findAttraction("A04").getType());
        assertEquals(AttractionType.GENERAL, p.findAttraction("A01").getType());
        assertTrue(p.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void legacyAttractionLineLoadsAsGeneralAndBadPassLinesSkipped() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A02;Bad;3;VIP2X;extra"));
        Files.write(dir.resolve("passes.txt"), List.of("S;SP1:1;MISSING;8.00;false", "X;bad", "Q;P;T;1;false"));
        Park p = restart();
        assertEquals(AttractionType.GENERAL, p.findAttraction("A01").getType());
        assertEquals(1, p.getAttractions().size());
        assertTrue(p.getPasses().isEmpty());
    }

    @Test
    void unknownPassKindSkipped() throws IOException {
        ana();
        tickets.create("T001", "ana@correo.com", TODAY_S);
        Files.write(dir.resolve("passes.txt"), List.of("Q;Z1;T001;1.00;false", "P;PE;T001;40.00;true"));
        Park p = restart();
        assertEquals(1, p.getPasses().size());
        assertTrue(p.findPass("PE").isConsumed());
    }
}
