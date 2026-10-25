package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.TestSupport;
import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.model.AttractionType;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Delivery3PersistenceTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() {
        park = TestSupport.park(dir);
        park.setCurrentDate("2027-04-15");
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createAttraction("A02", "Montana", "12", "INDIVIDUAL");
        park.createAttraction("A04", "Terror", "15", "SPECIAL");
    }

    private void reload() {
        park = TestSupport.park(dir);
        park.setCurrentDate("2027-04-15");
    }

    @Test
    void passesAndTypesSurviveRestart() {
        park.buySpeedPasses("SP01", "T001", "10");
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP02", "TG01:87654321X", "1");
        park.enterAttraction("A02", "T001", true);
        park.enterAttraction("A04", "T001", false);

        reload();

        assertEquals(AttractionType.SPECIAL, park.requireAttraction("A04").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.requireAttraction("A02").getType());
        assertEquals(0, park.requireAttraction("A04").getOccupancy());
        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertEquals(10, t001.getSpeedPasses().size());
        assertEquals(1, t001.getSpeedPasses().stream().filter(Pass::isConsumed).count());
        assertTrue(t001.getSpecialPass().orElseThrow().isConsumed());
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:5").orElseThrow().getPrice());
        Pass component = park.findPass("SP02:1").orElseThrow();
        assertSame(park.findTicket("TG01:87654321X").orElseThrow(), component.getTicket());
        assertEquals(new BigDecimal("8.00"), component.getPrice());
        assertFalse(component.isConsumed());
    }

    @Test
    void passFileFormat() throws IOException {
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpecialPass("PE01", "T001");
        assertEquals(List.of("SPEED;SP01:1;T001;8.00;false", "SPECIAL;PE01;T001;40.00;false"),
                Files.readAllLines(dir.resolve(ParkStorage.PASSES_FILE)));
        assertEquals(List.of("A02;Montana;12;INDIVIDUAL", "A04;Terror;15;SPECIAL"),
                Files.readAllLines(dir.resolve(ParkStorage.ATTRACTIONS_FILE)));
    }

    @Test
    void legacyAttractionLinesLoadAsGeneral() throws IOException {
        Files.write(dir.resolve(ParkStorage.ATTRACTIONS_FILE), List.of("A01;Dragon Rojo;20", "A05;Bad;5;VIP"));
        reload();
        assertEquals(AttractionType.GENERAL, park.requireAttraction("A01").getType());
        assertEquals(20, park.requireAttraction("A01").getCapacity());
    }

    @Test
    void deletedPassesDisappearFromFile() throws IOException {
        park.createTicket("T002", "ana@correo.com", "2027-04-20");
        park.buySpeedPasses("SP03", "T002", "1");
        park.deleteTicket("T002");
        reload();
        assertTrue(park.findPass("SP03:1").isEmpty());
        assertTrue(Files.readAllLines(dir.resolve(ParkStorage.PASSES_FILE)).isEmpty());
    }
}
