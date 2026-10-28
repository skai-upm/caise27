package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class PassConsumptionTest {

    private final LocalDate date = LocalDate.of(2027, 4, 15);
    private final User ana = new User("1A", "ana@correo.com");
    private final User pedro = new User("2B", "pedro@correo.com");
    private final IndividualTicket ticket = new IndividualTicket("T001", date, ana, new BigDecimal("40.00"));

    @Test
    void speedEntryConsumesExactlyOnePass() {
        SpeedPass first = new SpeedPass("SP01:1", ticket);
        SpeedPass second = new SpeedPass("SP02:1", ticket);
        EntryResult result = new GeneralAttraction("A01", "Dragon", 5).enter(ticket, List.of(first, second), true);
        assertEquals(List.of(ana), result.users());
        assertEquals(List.of(first), result.consumedPasses());
        assertTrue(first.isConsumed());
        assertFalse(second.isConsumed());
    }

    @Test
    void normalEntryConsumesNothing() {
        SpeedPass pass = new SpeedPass("SP01:1", ticket);
        assertTrue(new IndividualAttraction("A02", "Montana", 5).enter(ticket, List.of(pass), false)
                .consumedPasses().isEmpty());
        assertFalse(pass.isConsumed());
    }

    @Test
    void failedEntryKeepsPasses() {
        SpeedPass consumed = new SpeedPass("SP01:1", ticket);
        consumed.consume();
        GeneralAttraction attraction = new GeneralAttraction("A01", "Dragon", 1);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(ticket, List.of(consumed), true));
        attraction.admit(List.of(pedro));
        SpeedPass available = new SpeedPass("SP02:1", ticket);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(ticket, List.of(available), true));
        assertFalse(available.isConsumed());
        assertFalse(attraction.isInside(ana));
    }

    @Test
    void groupSpeedEntryIsIndivisible() {
        Group group = new Group("G01", "Familia");
        group.addMember(ana);
        group.addMember(pedro);
        GroupTicket groupTicket = GroupTicket.purchase("TG01", date, group, new BigDecimal("40.00"));
        IndividualTicket anaPart = groupTicket.getIndividualTickets().get(0);
        IndividualTicket pedroPart = groupTicket.getIndividualTickets().get(1);
        SpeedPass anaPass = new SpeedPass("SPA:1", anaPart);
        GroupAttraction attraction = new GroupAttraction("A03", "Rafting", 10);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(groupTicket, List.of(anaPass), true));
        assertFalse(anaPass.isConsumed());
        assertTrue(attraction.getOccupants().isEmpty());
        SpeedPass pedroPass = new SpeedPass("SPP:1", pedroPart);
        EntryResult result = attraction.enter(groupTicket, List.of(anaPass, pedroPass), true);
        assertEquals(2, result.users().size());
        assertTrue(anaPass.isConsumed() && pedroPass.isConsumed());
    }

    @Test
    void specialPassConsumedOnceAndSpeedRejected() {
        SpecialPass pass = new SpecialPass("PE01", ticket);
        SpecialAttraction attraction = new SpecialAttraction("A04", "Terror", 5);
        assertThrows(OperationNotAllowedException.class,
                () -> attraction.enter(ticket, List.of(pass, new SpeedPass("SP:1", ticket)), true));
        assertFalse(pass.isConsumed());
        EntryResult result = attraction.enter(ticket, List.of(pass), false);
        assertEquals(List.of(pass), result.consumedPasses());
        assertTrue(pass.isConsumed());
        attraction.removeOccupant(ana);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(ticket, List.of(pass), false));
    }
}
