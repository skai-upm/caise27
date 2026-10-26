package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int n) {
        return new User("U" + n, "S", "0000000" + n + "X", "u" + n + "@correo.com", "Clave1!", "600",
            "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, int members) {
        Group group = new Group("G" + id, "Grupo");
        for (int i = 0; i < members; i++) {
            group.addMember(user(10 + i));
        }
        return new GroupTicket(id, DAY, group, PRICE);
    }

    @Test
    void admittedUsersAndValidity() {
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        assertEquals(1, t.getAdmittedUsers().size());
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.minusDays(1)));
        assertFalse(t.isValidOn(DAY.plusDays(1)));
        t.detachOwner();
        assertTrue(t.getAdmittedUsers().isEmpty());
        assertEquals(3, groupTicket("TG01", 3).getAdmittedUsers().size());
    }

    @Test
    void individualAndGroupEntryAreHandledUniformly() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 20);
        List<Admission> admissions = List.of(new IndividualTicket("T001", DAY, PRICE, user(1)), groupTicket("TG01", 2));
        for (Admission admission : admissions) {
            a.enter(admission, DAY);
        }
        assertEquals(3, a.getPresent().size());
        assertEquals(17, a.getAvailable());
    }

    @Test
    void wrongDateIsRejected() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.minusDays(1)));
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAtomic() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 4);
        a.enter(new IndividualTicket("T001", DAY, PRICE, user(1)), DAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(groupTicket("TG01", 4), DAY));
        assertEquals(1, a.getPresent().size());
        a.enter(groupTicket("TG02", 3), DAY);
        assertEquals(0, a.getAvailable());
        assertThrows(IllegalArgumentException.class,
            () -> a.enter(new IndividualTicket("T002", DAY, PRICE, user(2)), DAY));
    }

    @Test
    void sameUserCannotEnterTwiceAndExitFreesPlaces() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 5);
        User ana = user(1);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, ana);
        a.enter(t, DAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY));
        assertEquals(1, a.exit(t));
        assertEquals(5, a.getAvailable());
        assertThrows(IllegalArgumentException.class, () -> a.exit(t));
        a.enter(t, DAY);
        a.remove(ana);
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void ticketWithoutRegisteredUsersIsRejected() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 5);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        t.detachOwner();
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY));
    }

    @Test
    void typesRestrictTicketKinds() {
        IndividualTicket individual = new IndividualTicket("T001", DAY, PRICE, user(1));
        GroupTicket group = groupTicket("TG01", 2);
        Attraction<?> groupOnly = AttractionType.GROUP.create("A02", "Carrusel", 10);
        Attraction<?> individualOnly = AttractionType.INDIVIDUAL.create("A03", "Noria", 10);
        Attraction<?> general = AttractionType.GENERAL.create("A01", "Tornado", 10);

        assertThrows(IllegalArgumentException.class, () -> groupOnly.enter(individual, DAY));
        assertThrows(IllegalArgumentException.class, () -> individualOnly.enter(group, DAY));
        groupOnly.enter(group, DAY);
        individualOnly.enter(individual, DAY);
        individualOnly.enter(group.getComponents().get(0), DAY);
        general.enter(individual, DAY);
        general.enter(group, DAY);
        assertEquals(2, groupOnly.getPresent().size());
        assertEquals(2, individualOnly.getPresent().size());
        assertEquals(3, general.getPresent().size());
        assertEquals(AttractionType.GROUP, groupOnly.getType());
    }

    @Test
    void specialAttractionRequiresAndConsumesSpecialPass() {
        Attraction<?> special = AttractionType.SPECIAL.create("A04", "VIP", 10);
        assertTrue(special instanceof SpecialAttraction);
        IndividualTicket withPass = new IndividualTicket("T001", DAY, PRICE, user(1));
        IndividualTicket withoutPass = new IndividualTicket("T002", DAY, PRICE, user(2));
        SpecialPass pass = withPass.addSpecialPass();

        assertThrows(IllegalArgumentException.class, () -> special.enter(withoutPass, DAY));
        assertThrows(IllegalArgumentException.class, () -> special.enter(groupTicket("TG01", 2), DAY));
        assertThrows(IllegalArgumentException.class, () -> special.enter(withPass, DAY.plusDays(1)));
        assertEquals(PassStatus.AVAILABLE, pass.getStatus());
        special.enter(withPass, DAY);
        assertEquals(PassStatus.CONSUMED, pass.getStatus());
        special.exit(withPass);
        assertThrows(IllegalArgumentException.class, () -> special.enter(withPass, DAY));
        assertTrue(special.getPresent().isEmpty());
    }

    @Test
    void specialPassIsNotConsumedWhenCapacityIsShort() {
        Attraction<?> special = AttractionType.SPECIAL.create("A04", "VIP", 1);
        IndividualTicket first = new IndividualTicket("T001", DAY, PRICE, user(1));
        first.addSpecialPass();
        special.enter(first, DAY);
        IndividualTicket second = new IndividualTicket("T002", DAY, PRICE, user(2));
        SpecialPass pass = second.addSpecialPass();
        assertThrows(IllegalArgumentException.class, () -> special.enter(second, DAY));
        assertEquals(PassStatus.AVAILABLE, pass.getStatus());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOneSpeedPass() {
        Attraction<?> a = AttractionType.INDIVIDUAL.create("A03", "Noria", 10);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        List<SpeedPass> passes = t.addSpeedPasses(10);
        EntryResult result = a.enter(t, DAY, true);
        assertEquals(1, result.usersEntered());
        assertEquals(List.of(passes.get(0)), result.consumedPasses());
        assertEquals(PassStatus.CONSUMED, passes.get(0).getStatus());
        assertEquals(PassStatus.AVAILABLE, passes.get(1).getStatus());
    }

    @Test
    void normalEntryConsumesNothing() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 10);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        SpeedPass pass = t.addSpeedPasses(1).get(0);
        assertTrue(a.enter(t, DAY).consumedPasses().isEmpty());
        assertEquals(PassStatus.AVAILABLE, pass.getStatus());
    }

    @Test
    void groupSpeedEntryConsumesOnePerMemberAtomically() {
        GroupTicket group = groupTicket("TG01", 2);
        SpeedPass first = group.getComponents().get(0).addSpeedPasses(1).get(0);
        Attraction<?> a = AttractionType.GROUP.create("A02", "Carrusel", 10);
        assertThrows(IllegalArgumentException.class, () -> a.enter(group, DAY, true));
        assertEquals(PassStatus.AVAILABLE, first.getStatus());
        assertTrue(a.getPresent().isEmpty());

        SpeedPass second = group.getComponents().get(1).addSpeedPasses(1).get(0);
        EntryResult result = a.enter(group, DAY, true);
        assertEquals(2, result.usersEntered());
        assertEquals(List.of(first, second), result.consumedPasses());
        assertEquals(PassStatus.CONSUMED, second.getStatus());
    }

    @Test
    void failedSpeedEntryConsumesNothing() {
        Attraction<?> a = AttractionType.GENERAL.create("A01", "Tornado", 1);
        a.enter(new IndividualTicket("T009", DAY, PRICE, user(9)), DAY);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        SpeedPass pass = t.addSpeedPasses(1).get(0);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY, true));
        assertEquals(PassStatus.AVAILABLE, pass.getStatus());
    }

    @Test
    void specialAttractionRejectsSpeedAndReportsSpecialPass() {
        Attraction<?> special = AttractionType.SPECIAL.create("A04", "VIP", 10);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        SpecialPass pass = t.addSpecialPass();
        SpeedPass speed = t.addSpeedPasses(1).get(0);
        assertThrows(IllegalArgumentException.class, () -> special.enter(t, DAY, true));
        assertEquals(PassStatus.AVAILABLE, pass.getStatus());
        assertEquals(PassStatus.AVAILABLE, speed.getStatus());
        assertTrue(special.getPresent().isEmpty());
        assertEquals(List.of(pass), special.enter(t, DAY, false).consumedPasses());
    }
}
