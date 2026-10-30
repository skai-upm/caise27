package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.EntryResult;
import es.upm.etsisi.amusementpark.business.model.Pass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttractionControllerTest extends ParkFixture {

    private AttractionController attractions;
    private TicketController tickets;
    private PassController passes;

    @BeforeEach
    void setUp() {
        attractions = AttractionController.getInstance();
        tickets = TicketController.getInstance();
        passes = PassController.getInstance();
        attractions.create("A01", "Dragon Rojo", "20");
        attractions.create("A02", "Torre del Cielo", "1");
        attractions.create("AI", "Montana Rusa Individual", "12", "INDIVIDUAL");
        attractions.create("AG", "Rafting Familiar", "24", "GROUP");
        attractions.create("AS", "Pasaje del Terror", "15", "SPECIAL");
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    private List<Pass> unconsumed() {
        return park.passes().findAll().stream().filter(p -> !p.isConsumed()).toList();
    }

    @Test
    void createRejections() {
        assertThrows(ParkException.class, () -> attractions.create("A01", "Otra", "5"));
        assertThrows(ParkException.class, () -> attractions.create("A03", "Noria", "0"));
        assertThrows(ParkException.class, () -> attractions.create("A03", "Noria", "abc"));
        assertThrows(ParkException.class, () -> attractions.create("A03", "Noria", "10", "VIP"));
        assertTrue(park.attractions().findById("A03").isEmpty());
    }

    @Test
    void typesAreCreated() {
        assertEquals(AttractionType.GENERAL, attractions.find("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, attractions.find("AI").getType());
        assertEquals(AttractionType.GROUP, attractions.find("AG").getType());
        assertEquals(AttractionType.SPECIAL, attractions.find("AS").getType());
    }

    @Test
    void individualEntryAndExit() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        assertEquals(1, attractions.enter("A01", "T001", false).entered().size());
        Attraction<?> a = attractions.find("A01");
        assertEquals(List.of(ana), a.getOccupants());
        attractions.exit("A01", "ana@correo.com");
        assertTrue(a.getOccupants().isEmpty());
        assertEquals(20, a.getAvailablePlaces());
    }

    @Test
    void groupEntryAndIndividualExit() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertEquals(2, attractions.enter("A01", "TG01", false).entered().size());
        Attraction<?> a = attractions.find("A01");
        assertEquals(18, a.getAvailablePlaces());
        attractions.exit("A01", "pedro@correo.com");
        assertEquals(List.of(ana), a.getOccupants());
    }

    @Test
    void groupEntryIsAtomic() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertThrows(ParkException.class, () -> attractions.enter("A02", "TG01", false));
        assertTrue(attractions.find("A02").getOccupants().isEmpty());
    }

    @Test
    void ticketMustBeValidToday() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.plusDays(1).toString());
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T001", false));
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T999", false));
        assertThrows(ParkException.class, () -> attractions.enter("A99", "T001", false));
        assertTrue(attractions.find("A01").getOccupants().isEmpty());
    }

    @Test
    void presenceIndependentOfLaterDateChange() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        attractions.enter("A01", "T001", false);
        park.setDate(TODAY.plusDays(3).toString());
        assertEquals(List.of(ana), attractions.find("A01").getOccupants());
    }

    @Test
    void exitOfUserNotInside() {
        assertThrows(ParkException.class, () -> attractions.exit("A01", "ana@correo.com"));
    }

    @Test
    void attractionsReloadEmptyWithType() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        attractions.enter("A01", "T001", false);
        restart();
        Attraction<?> a = AttractionController.getInstance().find("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertEquals(AttractionType.SPECIAL, AttractionController.getInstance().find("AS").getType());
    }

    @Test
    void legacyAttractionLineLoadsAsGeneral() throws Exception {
        Files.write(dir.resolve("attractions.txt"), List.of("A09;Vieja;5"));
        restart();
        assertEquals(AttractionType.GENERAL, AttractionController.getInstance().find("A09").getType());
    }

    @Test
    void typeRestrictions() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AI", "TG01", false));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AS", "TG01", false));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AG", "T001", false));
        assertEquals(1, attractions.enter("AI", "T001", false).entered().size());
        assertEquals(2, attractions.enter("AG", "TG01", false).entered().size());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        passes.createSpeedPasses("SP02", "T001", "10");
        EntryResult result = attractions.enter("AI", "T001", true);
        assertEquals(1, result.consumedPasses().size());
        assertTrue(result.consumedPasses().get(0).isConsumed());
        assertEquals(9, unconsumed().size());
    }

    @Test
    void speedEntryWithoutPassChangesNothing() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A01", "T001", true));
        assertTrue(attractions.find("A01").getOccupants().isEmpty());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        passes.createSpeedPasses("SPA", "TG01:12345678Z", "1");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AG", "TG01", true));
        assertTrue(attractions.find("AG").getOccupants().isEmpty());
        assertEquals(1, unconsumed().size());

        passes.createSpeedPasses("SPB", "TG01:87654321X", "1");
        EntryResult result = attractions.enter("AG", "TG01", true);
        assertEquals(2, result.entered().size());
        assertEquals(2, result.consumedPasses().size());
        assertTrue(unconsumed().isEmpty());
    }

    @Test
    void groupSpeedEntryWithoutCapacityConsumesNothing() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        passes.createSpeedPasses("SPA", "TG01:12345678Z", "1");
        passes.createSpeedPasses("SPB", "TG01:87654321X", "1");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A02", "TG01", true));
        assertEquals(2, unconsumed().size());
    }

    @Test
    void specialEntryConsumesSpecialPassOnce() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AS", "T001", false));
        passes.createSpecialPass("PE01", "T001");
        passes.createSpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AS", "T001", true));
        assertEquals(2, unconsumed().size());

        EntryResult result = attractions.enter("AS", "T001", false);
        assertEquals("PE01", result.consumedPasses().get(0).getId());
        attractions.exit("AS", "ana@correo.com");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("AS", "T001", false));
        assertTrue(park.passes().findById("PE01").orElseThrow().isConsumed());
    }

    @Test
    void consumptionSurvivesRestart() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        passes.createSpecialPass("PE01", "T001");
        attractions.enter("AS", "T001", false);
        restart();
        Pass pass = park.passes().findById("PE01").orElseThrow();
        assertTrue(pass.isConsumed());
        assertEquals("T001", pass.getTicket().getId());
    }
}
