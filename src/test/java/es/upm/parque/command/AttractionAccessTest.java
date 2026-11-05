package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.model.Attraction;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;
import es.upm.parque.repository.UserRepository;

class AttractionAccessTest {

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserRepository());
        park = ParkController.getInstance();
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457");
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;pedro@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T003;ana@correo.com;2027-04-16");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A02;Torre;1");
        processor.process("SYSTEM DATE;2027-04-15");
    }

    private Attraction a(String id) {
        return park.getAttractions().findById(id);
    }

    private User ana() {
        return park.getUsers().findByEmail("ana@correo.com");
    }

    @Test
    void holdersComeFromTheCommonAbstraction() {
        assertEquals(List.of("12345678Z"),
                park.getTickets().findById("T001").getHolders().stream().map(User::getDni).toList());
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals(2, park.getTickets().findById("TG01").getHolders().size());
    }

    @Test
    void validTicketAdmitsUser() {
        String r = processor.process("ATTRACTION ENTER;A01;T001");
        assertTrue(r.startsWith("OK") && r.contains("ana@correo.com"), r);
        assertTrue(a("A01").isInside(ana()));
    }

    @Test
    void ticketMustBeForToday() {
        assertTrue(processor.process("ATTRACTION ENTER;A01;T003").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void fullAttractionRejectsEntry() {
        assertTrue(processor.process("ATTRACTION ENTER;A02;T002").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A02;T001").startsWith("ERROR"));
        assertEquals(1, a("A02").getOccupants().size());
        assertFalse(a("A02").isInside(ana()));
    }

    @Test
    void unknownAttractionTicketOrMalformed() {
        assertTrue(processor.process("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T999").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION ENTER;A01").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void alreadyInsideIsRejectedAndNotDuplicated() {
        processor.process("ATTRACTION ENTER;A01;T001");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void presenceSurvivesDateChange() {
        processor.process("ATTRACTION ENTER;A01;T001");
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION LIST;A01").contains("ana@correo.com"));
    }

    @Test
    void groupTicketNotYetAccepted() {
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void helpListsAccessCommand() {
        assertTrue(processor.process("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>"));
    }
}
