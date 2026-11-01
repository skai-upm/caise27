package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.SpecialAttraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.AppBootstrap;

public class AttractionTypeTest {

    @TempDir
    Path dir;

    private ParkController park;
    private Ticket individual;
    private Ticket group;

    @BeforeEach
    public void setUp() throws IOException, OperationException {
        park = AppBootstrap.start(new StoragePaths(dir), LocalDate.of(2026, 10, 1), new BCryptPasswordHasher(4));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "600000000",
                "1234567890123456");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "luis@correo.com");
        individual = park.createTicket("T001", "ana@correo.com", "2026-10-01");
        group = park.createGroupTicket("TG01", "G01", "2026-10-01");
        park.createAttraction("GEN", "General", "10");
        park.createAttraction("IND", "Individual", "10", "INDIVIDUAL");
        park.createAttraction("GRP", "Group", "10", "GROUP");
        park.createAttraction("SPC", "Special", "10", "SPECIAL");
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    @Test
    public void typesCreateMatchingSubclasses() throws OperationException {
        assertInstanceOf(GeneralAttraction.class, park.attractionStatus("GEN"));
        assertEquals(AttractionType.GENERAL, park.attractionStatus("GEN").getType());
        assertInstanceOf(IndividualAttraction.class, park.attractionStatus("IND"));
        assertInstanceOf(GroupAttraction.class, park.attractionStatus("GRP"));
        assertInstanceOf(SpecialAttraction.class, park.attractionStatus("SPC"));
    }

    @Test
    public void admissionMatrix() throws OperationException {
        assertTrue(park.attractionStatus("GEN").admits(individual));
        assertTrue(park.attractionStatus("GEN").admits(group));
        assertTrue(park.attractionStatus("IND").admits(individual));
        assertFalse(park.attractionStatus("IND").admits(group));
        assertFalse(park.attractionStatus("GRP").admits(individual));
        assertTrue(park.attractionStatus("GRP").admits(group));
        assertTrue(park.attractionStatus("SPC").admits(individual));
        assertFalse(park.attractionStatus("SPC").admits(group));
        assertTrue(park.attractionStatus("GRP").admits(park.getTickets().find("TG01")));
        assertTrue(park.attractionStatus("IND").admits(park.getTickets().find("TG01-1")));
    }

    @Test
    public void incompatibleTicketsAreRejectedOnEntry() throws OperationException {
        assertThrows(OperationException.class, () -> park.enterAttraction("IND", "TG01"));
        assertThrows(OperationException.class, () -> park.enterAttraction("SPC", "TG01"));
        assertThrows(OperationException.class, () -> park.enterAttraction("GRP", "T001"));
        park.enterAttraction("GEN", "T001");
        park.enterAttraction("GEN", "TG01");
        park.enterAttraction("IND", "T001");
        park.enterAttraction("GRP", "TG01");
        assertEquals(3, park.attractionStatus("GEN").getOccupancy());
        assertEquals(0, park.attractionStatus("SPC").getOccupancy());
    }

    @Test
    public void unknownTypeRejected() {
        assertThrows(OperationException.class, () -> park.createAttraction("X", "X", "5", "VIP"));
        assertThrows(OperationException.class, () -> park.createAttraction("X", "X", "5", "general"));
    }

    @Test
    public void genericBoundIsPartOfTheType() {
        Attraction<?> special = AttractionType.SPECIAL.create("S", "S", 1);
        assertTrue(special.admits(individual));
        assertFalse(special.admits(group));
    }
}
