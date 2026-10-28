package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

class PassConsumptionTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "x", "6", "9876543210987654");
    private final IndividualTicket single = new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
    private final GroupTicket group = new GroupTicket("TG01", DAY, new Group("G01", "g"), List.of(ana, pedro),
            new BigDecimal("40.00"));
    private final List<Pass> passes = new ArrayList<>();
    private final PassLookup lookup = t -> passes.stream().filter(p -> p.getTicket() == t).toList();

    private Pass speed(String id, IndividualTicket t) {
        Pass p = new SpeedPass(id, t);
        passes.add(p);
        return p;
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() throws Exception {
        Pass first = speed("SP01:1", single);
        Pass second = speed("SP01:2", single);
        EntryResult result = new GeneralAttraction("A01", "a", 5).enter(single, DAY, lookup, true);
        assertEquals(List.of(ana), result.users());
        assertEquals(List.of(first), result.consumed());
        assertTrue(first.isConsumed());
        assertFalse(second.isConsumed());
    }

    @Test
    void speedEntryWithoutPassOrWhenFullChangesNothing() throws Exception {
        IndividualAttraction ride = new IndividualAttraction("A02", "a", 1);
        assertThrows(OperationNotAllowedException.class, () -> ride.enter(single, DAY, lookup, true));
        ride.addOccupant(pedro);
        Pass pass = speed("SP01:1", single);
        assertThrows(OperationNotAllowedException.class, () -> ride.enter(single, DAY, lookup, true));
        assertFalse(pass.isConsumed());
        assertEquals(List.of(pedro), ride.getOccupants());
    }

    @Test
    void groupSpeedEntryIsAtomic() throws Exception {
        GroupAttraction rafting = new GroupAttraction("A03", "r", 10);
        Pass anaPass = speed("SP01:1", group.individualTickets().get(0));
        assertThrows(OperationNotAllowedException.class, () -> rafting.enter(group, DAY, lookup, true));
        assertFalse(anaPass.isConsumed());
        assertTrue(rafting.getOccupants().isEmpty());
        Pass pedroPass = speed("SP02:1", group.individualTickets().get(1));
        speed("SP02:2", group.individualTickets().get(1));
        EntryResult result = rafting.enter(group, DAY, lookup, true);
        assertEquals(List.of(ana, pedro), result.users());
        assertEquals(List.of(anaPass, pedroPass), result.consumed());
        assertEquals(1, passes.stream().filter(p -> !p.isConsumed()).count());
    }

    @Test
    void groupSpeedEntryWithoutCapacityConsumesNothing() {
        GeneralAttraction small = new GeneralAttraction("A01", "a", 1);
        group.individualTickets().forEach(t -> speed("SP-" + t.getId(), t));
        assertThrows(OperationNotAllowedException.class, () -> small.enter(group, DAY, lookup, true));
        assertTrue(passes.stream().noneMatch(Pass::isConsumed));
    }

    @Test
    void specialAttractionConsumesTheSpecialPassAutomatically() throws Exception {
        SpecialAttraction terror = new SpecialAttraction("A04", "t", 15);
        SpecialPass special = new SpecialPass("PE01", single);
        passes.add(special);
        assertThrows(OperationNotAllowedException.class, () -> terror.enter(single, DAY, lookup, true));
        assertFalse(special.isConsumed());
        EntryResult result = terror.enter(single, DAY, lookup, false);
        assertEquals(List.of(special), result.consumed());
        assertTrue(special.isConsumed());
        terror.removeOccupant(ana);
        assertThrows(OperationNotAllowedException.class, () -> terror.enter(single, DAY, lookup, false));
    }
}
