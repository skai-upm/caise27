package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.model.EntryResult;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.TextRepositories;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerEntryTest {

    private final ParkController controller = ParkController.getInstance();

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(new User("1A", "ana@correo.com"));
        controller.addUser(new User("2B", "pedro@correo.com"));
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.addUserToGroup("G01", "pedro@correo.com");
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.buySpeedPasses("SP01", "T001", "1");
        controller.buySpeedPasses("SPA", "TG01:1A", "1");
        controller.createAttraction("A01", "Dragon", "10");
        controller.createAttraction("A03", "Rafting", "10", "GROUP");
    }

    @AfterEach
    void detach() {
        controller.configure((es.upm.etsisi.amusementpark.business.repository.ParkRepositories) null);
    }

    @Test
    void wrongDateKeepsPasses() {
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A01", "T001", true));
        assertFalse(controller.findPass("SP01:1").orElseThrow().isConsumed());
    }

    @Test
    void groupSpeedEntryAtomicAndConsumedStatePersists() {
        controller.setCurrentDate("2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A03", "TG01", true));
        assertFalse(controller.findPass("SPA:1").orElseThrow().isConsumed());
        assertTrue(controller.findAttraction("A03").orElseThrow().getOccupants().isEmpty());
        controller.buySpeedPasses("SPP", "TG01:2B", "1");
        EntryResult result = controller.enterAttraction("A03", "TG01", true);
        assertEquals(2, result.consumedPasses().size());
        controller.configure(TextRepositories.inDirectory(directory));
        controller.saveState();
        controller.reset();
        controller.loadState();
        assertTrue(controller.getPasses().stream().filter(p -> p.getIdentifier().startsWith("SP"))
                .filter(p -> !p.getIdentifier().equals("SP01:1")).allMatch(Pass::isConsumed));
        assertFalse(controller.findPass("SP01:1").orElseThrow().isConsumed());
    }

    @Test
    void individualSpeedEntry() {
        controller.setCurrentDate("2027-04-15");
        EntryResult result = controller.enterAttraction("A01", "T001", true);
        assertEquals("SP01:1", result.consumedPasses().get(0).getIdentifier());
        controller.exitAttraction("A01", "ana@correo.com");
        assertThrows(OperationNotAllowedException.class, () -> controller.enterAttraction("A01", "T001", true));
        assertEquals(1, controller.enterAttraction("A01", "T001"));
    }
}
