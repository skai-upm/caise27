package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.controller.ApplicationInitializer;

class AttractionAccessTest {

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A03;Mini;1");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;pedro@correo.com;2027-04-15");
    }

    private Attraction<?> attraction(String id) {
        return park.getAttractions().findById(id);
    }

    private User user(String email) {
        return park.getUsers().findByEmail(email);
    }

    @Test
    void entersWithValidIndividualTicket() {
        assertEquals("ENTERED A01: ana@correo.com", processor.process("ATTRACTION ENTER;A01;T001"));
        assertTrue(attraction("A01").isInside(user("ana@correo.com")));
    }

    @Test
    void ticketMustBeForCurrentDate() {
        processor.process("SYSTEM DATE;2027-04-14");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertEquals(0, attraction("A01").occupancy());
    }

    @Test
    void unknownTicketOrAttraction() {
        assertEquals("ERROR: Ticket not found", processor.process("ATTRACTION ENTER;A01;T999"));
        assertEquals("ERROR: Attraction not found", processor.process("ATTRACTION ENTER;A99;T001"));
        assertTrue(processor.process("ATTRACTION ENTER;A01").startsWith("ERROR"));
    }

    @Test
    void cannotEnterTwice() {
        processor.process("ATTRACTION ENTER;A01;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertEquals(1, attraction("A01").occupancy());
    }

    @Test
    void capacityIsRespected() {
        processor.process("ATTRACTION ENTER;A03;T002");
        assertEquals("ERROR: Not enough capacity in A03", processor.process("ATTRACTION ENTER;A03;T001"));
        assertFalse(attraction("A03").isInside(user("ana@correo.com")));
    }

    @Test
    void presenceDoesNotDependOnLaterDateChanges() {
        processor.process("ATTRACTION ENTER;A01;T001");
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(attraction("A01").isInside(user("ana@correo.com")));
    }

    @Test
    void deletedUserLeavesAttractions() {
        processor.process("ATTRACTION ENTER;A01;T001");
        processor.process("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("No users in A01", processor.process("ATTRACTION LIST;A01"));
    }

    @Test
    void helpListsEnter() {
        assertTrue(processor.process("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>"));
    }
}
