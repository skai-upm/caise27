package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.User;
import es.upm.parque.business.ParkController;
import es.upm.parque.persistence.UserRepository;

class AttractionAccessTest {

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = TestSupport.processor(new UserRepository());
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

    private Attraction<?> a(String id) {
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

    private void groupTicketForToday() {
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-15");
    }

    @Test
    void wholeGroupEntersTogether() {
        groupTicketForToday();
        String r = processor.process("ATTRACTION ENTER;A01;TG01");
        assertTrue(r.startsWith("OK") && r.contains("ana@correo.com") && r.contains("pedro@correo.com"), r);
        assertEquals(2, a("A01").getOccupants().size());
    }

    @Test
    void groupNeedingMoreThanFreeCapacityAdmitsNobody() {
        groupTicketForToday();
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR"));
        assertTrue(a("A02").getOccupants().isEmpty());
    }

    @Test
    void groupTicketMustBeForToday() {
        groupTicketForToday();
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void exitFreesCapacityAndGroupMembersLeaveIndividually() {
        processor.process("ATTRACTION ENTER;A02;T001");
        assertTrue(processor.process("ATTRACTION EXIT;A02;ana@correo.com").startsWith("OK"));
        assertFalse(a("A02").isInside(ana()));
        assertTrue(processor.process("ATTRACTION ENTER;A02;T002").startsWith("OK"));

        groupTicketForToday();
        processor.process("ATTRACTION ENTER;A01;TG01");
        assertTrue(processor.process("ATTRACTION EXIT;A01;ana@correo.com").startsWith("OK"));
        assertFalse(a("A01").isInside(ana()));
        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void invalidExitsLeaveOccupantsUnchanged() {
        processor.process("ATTRACTION ENTER;A01;T001");
        for (String bad : new String[] {"ATTRACTION EXIT;A99;ana@correo.com", "ATTRACTION EXIT;A01;nadie@correo.com",
                "ATTRACTION EXIT;A01;pedro@correo.com", "ATTRACTION EXIT;A01"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void statusShowsCountsAndOccupantsRegardlessOfEntryMode() {
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000002;1234567890123458");
        groupTicketForToday();
        processor.process("TICKET CREATE;T004;luis@correo.com;2027-04-15");
        processor.process("ATTRACTION ENTER;A01;TG01");
        processor.process("ATTRACTION EXIT;A01;pedro@correo.com");
        processor.process("ATTRACTION ENTER;A01;T004");
        String r = processor.process("ATTRACTION STATUS;A01");
        assertTrue(r.startsWith("OK") && r.contains("A01") && r.contains("Dragon Rojo") && r.contains("capacity 20")
                && r.contains("inside 2") && r.contains("available 18") && r.contains("ana@correo.com")
                && r.contains("luis@correo.com"), r);
        String empty = processor.process("ATTRACTION STATUS;A02");
        assertTrue(empty.contains("capacity 1") && empty.contains("inside 0") && empty.contains("available 1"), empty);
        assertTrue(processor.process("ATTRACTION STATUS;A99").startsWith("ERROR"));
        assertEquals(2, a("A01").getOccupants().size());
    }

    @Test
    void helpListsAccessCommand() {
        String help = processor.process("HELP");
        assertTrue(help.contains("ATTRACTION ENTER;<attraction>;<ticket>"));
        assertTrue(help.contains("ATTRACTION EXIT;<attraction>;<email>"));
        assertTrue(help.contains("ATTRACTION STATUS;<attractionIdentifier>"));
    }
}
