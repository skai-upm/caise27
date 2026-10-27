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

    private final LocalDate day = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "1", "1");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "x", "1", "1");
    private final IndividualTicket t001 = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
    private final GroupTicket tg01 = new GroupTicket("TG01", day, new BigDecimal("40.00"), new Group("G01", "g"),
            List.of(ana, pedro));
    private final List<Pass> store = new ArrayList<>();
    private final PassLookup lookup = ticket -> store.stream().filter(p -> p.getTicket() == ticket).toList();

    @Test
    void speedEntryConsumesExactlyOne() throws Exception {
        store.add(new SpeedPass("SP01:1", t001));
        store.add(new SpeedPass("SP02:1", t001));
        EntryResult r = new GeneralAttraction("A01", "G", 5).admit(t001, day, true, lookup);
        assertEquals(1, r.consumedPasses().size());
        assertEquals(1, store.stream().filter(Pass::isConsumed).count());
    }

    @Test
    void failedEntryConsumesNothing() {
        SpeedPass sp = new SpeedPass("SP01:1", t001);
        store.add(sp);
        assertThrows(OperationNotAllowedException.class,
                () -> new IndividualAttraction("A02", "I", 0).admit(t001, day, true, lookup));
        assertFalse(sp.isConsumed());
        assertThrows(OperationNotAllowedException.class,
                () -> new IndividualAttraction("A02", "I", 5).admit(t001, day.plusDays(1), true, lookup));
        assertFalse(sp.isConsumed());
    }

    @Test
    void groupNeedsOnePassPerComponent() throws Exception {
        SpeedPass first = new SpeedPass("SPA:1", tg01.getComposition().get(0));
        store.add(first);
        GroupAttraction a03 = new GroupAttraction("A03", "R", 10);
        assertThrows(OperationNotAllowedException.class, () -> a03.admit(tg01, day, true, lookup));
        assertFalse(first.isConsumed());
        assertEquals(0, a03.occupancy());
        store.add(new SpeedPass("SPB:1", tg01.getComposition().get(1)));
        EntryResult r = a03.admit(tg01, day, true, lookup);
        assertEquals(2, r.users().size());
        assertTrue(store.stream().allMatch(Pass::isConsumed));
    }

    @Test
    void specialAttractionRules() throws Exception {
        SpecialAttraction a04 = new SpecialAttraction("A04", "Terror", 5);
        assertThrows(OperationNotAllowedException.class, () -> a04.admit(t001, day, false, lookup));
        SpecialPass pe = new SpecialPass("PE01", t001);
        store.add(pe);
        assertThrows(OperationNotAllowedException.class, () -> a04.admit(t001, day, true, lookup));
        assertFalse(pe.isConsumed());
        EntryResult r = a04.admit(t001, day, false, lookup);
        assertEquals(List.of(pe), r.consumedPasses());
        assertTrue(pe.isConsumed());
        a04.exit(ana);
        assertThrows(OperationNotAllowedException.class, () -> a04.admit(t001, day, false, lookup));
    }
}
