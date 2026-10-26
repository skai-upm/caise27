package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.ParkException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionPassEntryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");
    private static final BigDecimal SPEED_PRICE = new BigDecimal("8.00");

    private int nextPass = 1;

    private static User user(String name, String dni) {
        return new User(name, "X", dni, name.toLowerCase() + "@correo.com", "h", "6", "1234567890123456");
    }

    private IndividualTicket ticket(String id, User user, int speedPasses) {
        IndividualTicket t = new IndividualTicket(id, DAY, user, PRICE);
        for (int i = 0; i < speedPasses; i++) {
            t.addSpeedPass(new SpeedPass("SP" + nextPass++, SPEED_PRICE, false));
        }
        return t;
    }

    private GroupTicket group(IndividualTicket... members) {
        return new GroupTicket("TG01", DAY, new Group("G01", "F"), PRICE, List.of(members));
    }

    private final User ana = user("Ana", "12345678Z");
    private final User pedro = user("Pedro", "87654321X");

    @Test
    void normalEntryConsumesNothing() {
        IndividualTicket t = ticket("T001", ana, 2);
        EntryResult result = new GeneralAttraction("A01", "Tornado", 10).admit(t);
        assertEquals(1, result.admitted());
        assertTrue(result.consumed().isEmpty());
        assertTrue(t.getSpeedPasses().stream().noneMatch(Pass::isConsumed));
    }

    @Test
    void individualSpeedConsumesExactlyOne() {
        for (Attraction<?> a : List.of(new GeneralAttraction("A01", "G", 10), new IndividualAttraction("A03", "I", 10))) {
            IndividualTicket t = ticket("T001", ana, 2);
            EntryResult result = a.admit(t, true);
            assertEquals(1, result.consumed().size());
            assertTrue(result.consumed().get(0).isConsumed());
            assertEquals(1, t.getSpeedPasses().stream().filter(Pass::isConsumed).count());
        }
    }

    @Test
    void individualSpeedWithoutPassIsRejected() {
        IndividualTicket t = ticket("T001", ana, 1);
        t.getSpeedPasses().get(0).consume();
        GeneralAttraction a = new GeneralAttraction("A01", "G", 10);
        assertThrows(ParkException.class, () -> a.admit(t, true));
        assertEquals(0, a.getOccupancy());
    }

    @Test
    void groupSpeedConsumesOnePerMember() {
        for (Attraction<?> a : List.of(new GeneralAttraction("A01", "G", 10), new GroupAttraction("A02", "Gr", 10))) {
            IndividualTicket t1 = ticket("TG01-1", ana, 1);
            IndividualTicket t2 = ticket("TG01-2", pedro, 3);
            EntryResult result = a.admit(group(t1, t2), true);
            assertEquals(2, result.admitted());
            assertEquals(2, result.consumed().size());
            assertTrue(t1.getSpeedPasses().get(0).isConsumed());
            assertEquals(1, t2.getSpeedPasses().stream().filter(Pass::isConsumed).count());
        }
    }

    @Test
    void groupSpeedIsAtomicWhenAMemberLacksAPass() {
        IndividualTicket t1 = ticket("TG01-1", ana, 1);
        IndividualTicket t2 = ticket("TG01-2", pedro, 0);
        GroupAttraction a = new GroupAttraction("A02", "Gr", 10);
        GroupTicket g = group(t1, t2);
        assertThrows(ParkException.class, () -> a.admit(g, true));
        assertEquals(0, a.getOccupancy());
        assertFalse(t1.getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void capacityFailureConsumesNothing() {
        IndividualTicket t1 = ticket("TG01-1", ana, 1);
        IndividualTicket t2 = ticket("TG01-2", pedro, 1);
        GeneralAttraction a = new GeneralAttraction("A01", "G", 1);
        GroupTicket g = group(t1, t2);
        assertThrows(ParkException.class, () -> a.admit(g, true));
        assertFalse(t1.getSpeedPasses().get(0).isConsumed());
        assertFalse(t2.getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void specialAttractionConsumesTheSpecialPassOnlyOnSuccess() {
        IndividualTicket t = ticket("T001", ana, 1);
        SpecialAttraction full = new SpecialAttraction("A04", "Magia", 1);
        full.admit(ticketWithSpecial("T009", pedro));
        t.setSpecialPass(new SpecialPass("XP1", PRICE, false));
        assertThrows(ParkException.class, () -> full.admit(t));
        assertFalse(t.getSpecialPass().isConsumed());

        SpecialAttraction special = new SpecialAttraction("A05", "Magia", 10);
        EntryResult result = special.admit(t);
        assertEquals(List.of(t.getSpecialPass()), result.consumed());
        assertTrue(t.getSpecialPass().isConsumed());
        special.release(t);
        assertThrows(ParkException.class, () -> special.admit(t));
    }

    private IndividualTicket ticketWithSpecial(String id, User user) {
        IndividualTicket t = ticket(id, user, 0);
        t.setSpecialPass(new SpecialPass("XP9", PRICE, false));
        return t;
    }

    @Test
    void specialAttractionRejectsMissingPassAndSpeed() {
        SpecialAttraction special = new SpecialAttraction("A04", "Magia", 10);
        IndividualTicket noPass = ticket("T001", ana, 2);
        assertThrows(ParkException.class, () -> special.admit(noPass));
        IndividualTicket withPass = ticketWithSpecial("T002", pedro);
        ParkException e = assertThrows(ParkException.class, () -> special.admit(withPass, true));
        assertTrue(e.getMessage().contains("SPEED"));
        assertFalse(withPass.getSpecialPass().isConsumed());
        assertEquals(0, special.getOccupancy());
    }

    @Test
    void individualTicketsOfAdmissions() {
        IndividualTicket t1 = ticket("TG01-1", ana, 0);
        IndividualTicket t2 = ticket("TG01-2", pedro, 0);
        assertEquals(List.of(t1), t1.getIndividualTickets());
        assertEquals(List.of(t1, t2), group(t1, t2).getIndividualTickets());
        assertNull(t1.nextUnusedSpeedPass());
    }
}
