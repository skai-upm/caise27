package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.invoice.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionTypeTest extends ParkTestSupport {

    @BeforeEach
    void park() {
        attractions.create("A01", "Dragon Rojo", "20", "GENERAL");
        attractions.create("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        attractions.create("A03", "Rafting Familiar", "24", "GROUP");
        attractions.create("A04", "Pasaje del Terror", "15", "SPECIAL");
        groupWith("G01", 3);
        user(9);
        tickets.createIndividual("T001", email(9), "2027-04-10");
        tickets.createGroup("TG01", "G01", "2027-04-10");
    }

    private List<String> occupants(String id) {
        return park.getAttractions().findById(id).getOccupants().stream().map(User::getEmail).toList();
    }

    private long consumed() {
        return park.getPasses().findAll().stream().filter(Pass::isConsumed).count();
    }

    @Test
    void typesAndDefault() {
        assertEquals(AttractionType.GENERAL, attractions.create("A05", "X", "5").getType());
        assertInstanceOf(SpecialAttraction.class, park.getAttractions().findById("A04"));
        assertThrows(InvalidDataException.class, () -> attractions.create("A06", "X", "5", "VIP"));
    }

    @Test
    void acceptanceMatrix() {
        Attraction<?> general = park.getAttractions().findById("A01");
        Attraction<?> individual = park.getAttractions().findById("A02");
        Attraction<?> group = park.getAttractions().findById("A03");
        Attraction<?> special = park.getAttractions().findById("A04");
        Ticket t001 = park.getTickets().findById("T001");
        Ticket tg01 = park.getTickets().findById("TG01");
        assertTrue(general.accepts(t001) && general.accepts(tg01));
        assertTrue(individual.accepts(t001) && !individual.accepts(tg01));
        assertTrue(!group.accepts(t001) && group.accepts(tg01));
        assertTrue(special.accepts(t001) && !special.accepts(tg01));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A02", "TG01"));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "TG01"));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A03", "T001"));
        attractions.enter("A01", "TG01");
        attractions.enter("A02", "T001");
        assertEquals(3, occupants("A01").size());
    }

    @Test
    void genericsRestrictAtCompileTime() {
        IndividualAttraction individual = (IndividualAttraction) park.getAttractions().findById("A02");
        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001");
        assertTrue(individual.enter(t001, false, passes).isEmpty());
        assertEquals(List.of(email(9)), occupants("A02"));
    }

    @Test
    void individualSpeedConsumesExactlyOne() {
        passes.buySpeedPasses("SP01", "T001", "10");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T999;SPEED").output().startsWith("ERROR:"));
        String output = processor.process("ATTRACTION ENTER;A01;T001;SPEED").output();
        assertEquals("ENTERED ATTRACTION A01: " + email(9) + ". SPEEDPASS CONSUMED: SP01:1", output);
        assertEquals(1, consumed());
    }

    @Test
    void speedWithoutPassFailsWithoutChanges() {
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A02", "T001", true));
        assertTrue(occupants("A02").isEmpty());
    }

    @Test
    void failedSpeedEntryConsumesNothing() {
        attractions.create("A07", "Mini", "1", "GENERAL");
        user(8);
        tickets.createIndividual("T008", email(8), "2027-04-10");
        passes.buySpeedPasses("SP01", "T001", "1");
        attractions.enter("A07", "T008");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A07", "T001", true));
        assertEquals(0, consumed());
    }

    @Test
    void groupSpeedIsAtomic() {
        passes.buySpeedPasses("SP01", "TG01:" + dni(1), "1");
        passes.buySpeedPasses("SP02", "TG01:" + dni(2), "1");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A03", "TG01", true));
        assertTrue(occupants("A03").isEmpty());
        assertEquals(0, consumed());
        passes.buySpeedPasses("SP03", "TG01:" + dni(3), "10");
        AttractionController.Entry entry = attractions.enter("A03", "TG01", true);
        assertEquals(3, entry.users().size());
        assertEquals(List.of("SP01:1", "SP02:1", "SP03:1"), entry.consumedPasses().stream().map(Pass::getId).toList());
        assertEquals(3, consumed());
    }

    @Test
    void specialPassConsumedAutomatically() {
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001"));
        passes.buySpecialPass("PE01", "T001");
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001", true));
        assertFalse(park.getPasses().findById("PE01").isConsumed());
        assertEquals("ENTERED ATTRACTION A04: " + email(9) + ". SPECIAL PASS CONSUMED: PE01",
                processor.process("ATTRACTION ENTER;A04;T001").output());
        assertTrue(park.getPasses().findById("PE01").isConsumed());
        attractions.exit("A04", email(9));
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001"));
    }

    @Test
    void wrongOptionRejected() {
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;FAST").output().startsWith("ERROR:"));
        assertTrue(occupants("A01").isEmpty());
    }
}
