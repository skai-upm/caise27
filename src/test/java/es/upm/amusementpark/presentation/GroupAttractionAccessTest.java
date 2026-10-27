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

class GroupAttractionAccessTest {

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("TICKET CREATE;T003;luis@correo.com;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A02;Torre del Cielo;1");
    }

    private Attraction<?> attraction(String id) {
        return park.getAttractions().findById(id);
    }

    private User user(String email) {
        return park.getUsers().findByEmail(email);
    }

    @Test
    void wholeGroupEntersTogether() {
        assertEquals("ENTERED A01: ana@correo.com pedro@correo.com", processor.process("ATTRACTION ENTER;A01;TG01"));
        assertTrue(attraction("A01").isInside(user("ana@correo.com")));
        assertTrue(attraction("A01").isInside(user("pedro@correo.com")));
    }

    @Test
    void groupEntryIsIndivisible() {
        assertEquals("ERROR: Not enough capacity in A02", processor.process("ATTRACTION ENTER;A02;TG01"));
        assertEquals(0, attraction("A02").occupancy());
        processor.process("ATTRACTION CREATE;A04;Rio;5");
        processor.process("GROUP CREATE;G02;Solo Ana");
        processor.process("GROUP ADD;G02;ana@correo.com");
        processor.process("GROUPTICKET CREATE;TG02;G02;2027-04-15");
        processor.process("ATTRACTION ENTER;A04;TG02");
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertFalse(attraction("A04").isInside(user("pedro@correo.com")));
    }

    @Test
    void groupTicketMustBeForCurrentDate() {
        processor.process("SYSTEM DATE;2027-04-14");
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertEquals(0, attraction("A01").occupancy());
    }

    @Test
    void membersLeaveIndividually() {
        processor.process("ATTRACTION ENTER;A01;TG01");
        assertEquals("EXITED A01: ana@correo.com", processor.process("ATTRACTION EXIT;A01;ana@correo.com"));
        assertFalse(attraction("A01").isInside(user("ana@correo.com")));
        assertTrue(attraction("A01").isInside(user("pedro@correo.com")));
        assertEquals(19, attraction("A01").freePlaces());
        assertTrue(processor.process("ATTRACTION EXIT;A01;luis@correo.com").startsWith("ERROR"));
        assertEquals("ERROR: Attraction not found", processor.process("ATTRACTION EXIT;A99;ana@correo.com"));
    }

    @Test
    void statusReflectsAttractionState() {
        processor.process("ATTRACTION ENTER;A01;T003");
        processor.process("ATTRACTION ENTER;A01;TG01");
        assertEquals(String.join("\n",
                "Attraction: A01",
                "Name: Dragon Rojo",
                "Type: GENERAL",
                "Capacity: 20",
                "Users inside: 3",
                "Available places: 17",
                "Users: luis@correo.com, ana@correo.com, pedro@correo.com"),
                processor.process("ATTRACTION STATUS;A01"));
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("Users inside: 0\nAvailable places: 1\nUsers: (none)"));
        assertEquals("ERROR: Attraction not found", processor.process("ATTRACTION STATUS;A99"));
    }

    @Test
    void helpListsExitAndStatus() {
        String help = processor.process("HELP");
        assertTrue(help.contains("ATTRACTION EXIT;<attraction>;<email>"));
        assertTrue(help.contains("ATTRACTION STATUS;<attractionIdentifier>"));
    }
}
