package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int i) {
        return new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static GroupTicket groupTicket(int members) {
        Group group = new Group("G", "Grupo");
        List<IndividualTicket> parts = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            User u = user(100 + i);
            group.addMember(u);
            parts.add(new IndividualTicket("TG-" + i, DAY, u, PRICE));
        }
        return GroupTicket.purchase("TG", DAY, group, parts);
    }

    @Test
    void individualAndGroupEntryUseTheSameLogic() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 20);
        a.enter(new IndividualTicket("T001", DAY, user(1), PRICE));
        a.enter(groupTicket(3));
        assertEquals(4, a.getPresent().size());
        assertEquals(16, a.getAvailable());
    }

    @Test
    void groupEntryIsAtomic() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 4);
        a.enter(new IndividualTicket("T001", DAY, user(1), PRICE));
        assertThrows(IllegalArgumentException.class, () -> a.enter(groupTicket(4)));
        assertEquals(1, a.getPresent().size());
        a.enter(groupTicket(3));
        assertEquals(0, a.getAvailable());
        assertThrows(IllegalArgumentException.class,
                () -> a.enter(new IndividualTicket("T002", DAY, user(2), PRICE)));
    }

    @Test
    void userCannotEnterTwiceAndExitFreesPlaces() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 5);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        a.enter(t);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t));
        GroupTicket gt = groupTicket(2);
        a.enter(gt);
        assertEquals(2, a.exit(gt));
        assertEquals(4, a.getAvailable());
        assertThrows(IllegalArgumentException.class, () -> a.exit(gt));
        assertTrue(a.getPresent().contains(t.getUser()));
    }

    @Test
    void typesRestrictTheAcceptedTickets() {
        IndividualTicket individual = new IndividualTicket("T001", DAY, user(1), PRICE);
        GroupTicket group = groupTicket(2);
        Attraction<?> general = AttractionType.GENERAL.create("A1", "G", 10);
        Attraction<?> onlyIndividual = AttractionType.INDIVIDUAL.create("A2", "I", 10);
        Attraction<?> onlyGroup = AttractionType.GROUP.create("A3", "Gr", 10);
        Attraction<?> special = AttractionType.SPECIAL.create("A4", "S", 10);
        general.admit(individual);
        general.admit(group);
        assertEquals(3, general.getPresent().size());
        onlyIndividual.admit(individual);
        assertThrows(IllegalArgumentException.class, () -> onlyIndividual.admit(group));
        onlyGroup.admit(group);
        assertThrows(IllegalArgumentException.class, () -> onlyGroup.admit(individual));
        individual.setSpecialPass(new SpecialPass("T001-SPECIAL", SpecialPass.priceFor(individual), false));
        special.admit(individual);
        assertThrows(IllegalArgumentException.class, () -> special.admit(group));
        assertEquals(1, special.getPresent().size());
        assertEquals(AttractionType.SPECIAL, special.getType());
        assertTrue(special instanceof SpecialAttraction);
    }

    @Test
    void typedEntryAcceptsTheBoundCapability() {
        IndividualAttraction a = new IndividualAttraction("A2", "I", 1);
        IndividualAccess access = new IndividualTicket("T001", DAY, user(1), PRICE);
        a.enter(access);
        GroupAttraction g = new GroupAttraction("A3", "G", 5);
        GroupAccess groupAccess = groupTicket(3);
        g.enter(groupAccess);
        assertEquals(3, g.getPresent().size());
        assertEquals(1, g.exit(new IndividualTicket("X", DAY, groupAccess.getUsers().get(0), PRICE)));
    }
}
