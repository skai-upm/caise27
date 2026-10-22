package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.pass.Pass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionTypesTest {

    @TempDir
    Path dir;

    private Park park;
    private AttractionController attractions;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        attractions = park.getAttractionController();
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", "20", "GENERAL");
        attractions.create("A02", "Montana Individual", "12", "individual");
        attractions.create("A03", "Rafting Familiar", "12", "GROUP");
        attractions.create("A04", "Pasaje del Terror", "15", "SPECIAL");
        attractions.create("A05", "Por defecto", "5", null);
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
    }

    private Attraction<?> a(String id) {
        return attractions.find(id);
    }

    private long consumed() {
        return park.getRepositories().passes().findAll().stream().filter(Pass::isConsumed).count();
    }

    @Test
    void createsTypedAttractions() {
        assertEquals(AttractionType.GENERAL, a("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, a("A02").getType());
        assertEquals(AttractionType.GROUP, a("A03").getType());
        assertEquals(AttractionType.SPECIAL, a("A04").getType());
        assertEquals(AttractionType.GENERAL, a("A05").getType());
        assertThrows(ValidationException.class, () -> attractions.create("A06", "Noria", "10", "VIP"));
    }

    @Test
    void typesRestrictTicketKinds() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("A02", "TG01", null));
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "TG01", null));
        assertThrows(RuleViolationException.class, () -> attractions.enter("A03", "T001", null));
        assertTrue(a("A02").getOccupants().isEmpty());

        attractions.enter("A02", "T001", null);
        attractions.exit("A02", "ana@correo.com");
        attractions.enter("A01", "T001", null);
        attractions.exit("A01", "ana@correo.com");
        attractions.enter("A03", "TG01", null);
        assertEquals(3, a("A03").getOccupants().size());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() {
        park.getPassController().buySpeedPasses("SP01", "T001", "10");

        AttractionController.Entry entry = attractions.enter("A02", "T001", "SPEED");

        assertEquals(1, entry.consumedPasses().size());
        assertEquals(1, consumed());
        assertTrue(entry.consumedPasses().get(0).getId().startsWith("SP01:"));
    }

    @Test
    void speedEntryWithoutPassChangesNothing() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("A01", "T001", "SPEED"));
        assertTrue(a("A01").getOccupants().isEmpty());
        assertThrows(ValidationException.class, () -> attractions.enter("A01", "T001", "FAST"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        park.getPassController().buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.getPassController().buySpeedPasses("SP03", "TG01:87654321X", "1");

        assertThrows(RuleViolationException.class, () -> attractions.enter("A03", "TG01", "SPEED"));
        assertTrue(a("A03").getOccupants().isEmpty());
        assertEquals(0, consumed());

        park.getPassController().buySpeedPasses("SP04", "TG01:11111111H", "1");
        AttractionController.Entry entry = attractions.enter("A03", "TG01", "SPEED");

        assertEquals(3, entry.users().size());
        assertEquals(3, consumed());
    }

    @Test
    void groupSpeedEntryFailsOnCapacityWithoutConsuming() {
        attractions.create("A07", "Mini grupo", "2", "GROUP");
        park.getPassController().buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.getPassController().buySpeedPasses("SP03", "TG01:87654321X", "1");
        park.getPassController().buySpeedPasses("SP04", "TG01:11111111H", "1");

        assertThrows(RuleViolationException.class, () -> attractions.enter("A07", "TG01", "SPEED"));
        assertEquals(0, consumed());
    }

    @Test
    void specialEntryConsumesTheSpecialPass() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", null));
        park.getPassController().buySpecialPass("PE01", "T001");
        park.getPassController().buySpeedPasses("SP01", "T001", "1");

        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", "SPEED"));
        assertEquals(0, consumed());

        AttractionController.Entry entry = attractions.enter("A04", "T001", null);
        assertEquals(List.of("PE01"), entry.consumedPasses().stream().map(Pass::getId).toList());

        attractions.exit("A04", "ana@correo.com");
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", null));
        assertFalse(a("A04").isInside(park.getUserController().findByEmail("ana@correo.com")));
    }
}
