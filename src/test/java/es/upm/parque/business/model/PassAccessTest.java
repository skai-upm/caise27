package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PassAccessTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");
    private int counter;

    private IndividualTicket ticket(int speedPasses) {
        int n = counter++;
        User u = new User("U" + n, "S", "dni" + n, "u" + n + "@correo.com", "h", "6", "1234567890123456");
        IndividualTicket t = new IndividualTicket("T" + n, DAY, u, PRICE);
        for (int i = 1; i <= speedPasses; i++) {
            t.addSpeedPass(new SpeedPass(t.getId() + "-SP" + i, SpeedPass.priceFor(t), false));
        }
        return t;
    }

    private GroupTicket group(IndividualTicket... parts) {
        Group g = new Group("G", "Grupo");
        for (IndividualTicket p : parts) {
            g.addMember(p.getUser());
        }
        return GroupTicket.purchase("TG", DAY, g, List.of(parts));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        IndividualTicket t = ticket(2);
        for (AttractionType type : new AttractionType[] {AttractionType.GENERAL, AttractionType.INDIVIDUAL}) {
            Attraction<?> a = type.create("A", "A", 10);
            List<Pass> consumed = a.admit(t, true);
            assertEquals(1, consumed.size());
            a.exit(t);
        }
        assertTrue(t.getSpeedPasses().get(0).isConsumed());
        assertTrue(t.getSpeedPasses().get(1).isConsumed());
        assertThrows(IllegalArgumentException.class, () -> AttractionType.GENERAL.create("A", "A", 10).admit(t, true));
    }

    @Test
    void groupSpeedEntryConsumesOnePerMemberAtomically() {
        GroupTicket complete = group(ticket(1), ticket(1), ticket(2));
        Attraction<?> groupOnly = AttractionType.GROUP.create("A", "A", 10);
        assertEquals(3, groupOnly.admit(complete, true).size());
        assertEquals(3, groupOnly.getPresent().size());

        IndividualTicket withoutPass = ticket(0);
        IndividualTicket withPass = ticket(1);
        GroupTicket incomplete = group(withPass, withoutPass);
        Attraction<?> general = AttractionType.GENERAL.create("B", "B", 10);
        assertThrows(IllegalArgumentException.class, () -> general.admit(incomplete, true));
        assertEquals(0, general.getPresent().size());
        assertFalse(withPass.getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void normalEntryConsumesNothing() {
        IndividualTicket t = ticket(1);
        assertEquals(0, AttractionType.GENERAL.create("A", "A", 10).admit(t).size());
        assertFalse(t.getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void failedEntriesConsumeNothing() {
        IndividualTicket t = ticket(1);
        Attraction<?> full = AttractionType.GENERAL.create("A", "A", 1);
        full.admit(ticket(0));
        assertThrows(IllegalArgumentException.class, () -> full.admit(t, true));
        assertFalse(t.getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void specialAttractionUsesTheSpecialPassOnce() {
        IndividualTicket t = ticket(1);
        Attraction<?> special = AttractionType.SPECIAL.create("S", "S", 10);
        assertThrows(IllegalArgumentException.class, () -> special.admit(t));
        SpecialPass pass = new SpecialPass(t.getId() + "-SPECIAL", SpecialPass.priceFor(t), false);
        t.setSpecialPass(pass);
        assertThrows(IllegalArgumentException.class, () -> special.admit(t, true));
        assertFalse(pass.isConsumed());
        assertFalse(t.getSpeedPasses().get(0).isConsumed());
        List<Pass> consumed = special.admit(t);
        assertSame(pass, consumed.get(0));
        assertTrue(pass.isConsumed());
        special.exit(t);
        assertThrows(IllegalArgumentException.class, () -> special.admit(t));
    }

    @Test
    void specialPassNotConsumedWhenFull() {
        IndividualTicket t = ticket(0);
        t.setSpecialPass(new SpecialPass(t.getId() + "-SPECIAL", SpecialPass.priceFor(t), false));
        Attraction<?> special = AttractionType.SPECIAL.create("S", "S", 1);
        IndividualTicket other = ticket(0);
        other.setSpecialPass(new SpecialPass(other.getId() + "-SPECIAL", SpecialPass.priceFor(other), false));
        special.admit(other);
        assertThrows(IllegalArgumentException.class, () -> special.admit(t));
        assertFalse(t.getSpecialPass().isConsumed());
        assertEquals(new ArrayList<>(List.of(other.getUser())), new ArrayList<>(special.getPresent()));
    }
}
