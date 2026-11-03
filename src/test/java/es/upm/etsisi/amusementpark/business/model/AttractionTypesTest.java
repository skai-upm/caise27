package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestData;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttractionTypesTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    static final BigDecimal PRICE = new BigDecimal("40.00");

    final Ticket ticket = new Ticket("T001", TestData.user("12345678Z", "ana@correo.com"), TODAY, PRICE);
    final GroupTicket groupTicket = new GroupTicket("TG01", TestData.groupOf(2), TODAY, PRICE);

    @Test
    void eachTypeAdmitsTheRightKindsOfTicket() {
        assertTrue(new GeneralAttraction("A", "a", 9).admits(ticket));
        assertTrue(new GeneralAttraction("A", "a", 9).admits(groupTicket));
        assertTrue(new IndividualAttraction("A", "a", 9).admits(ticket));
        assertFalse(new IndividualAttraction("A", "a", 9).admits(groupTicket));
        assertFalse(new GroupAttraction("A", "a", 9).admits(ticket));
        assertTrue(new GroupAttraction("A", "a", 9).admits(groupTicket));
        assertTrue(new SpecialAttraction("A", "a", 9).admits(ticket));
        assertFalse(new SpecialAttraction("A", "a", 9).admits(groupTicket));
    }

    @Test
    void runtimeEntryRejectsUnsupportedKindsWithoutStateChange() {
        GroupAttraction group = new GroupAttraction("A02", "Coches", 10);
        Exception e = assertThrows(IllegalArgumentException.class, () -> group.enter(ticket, TODAY));
        assertEquals("Attraction A02 (GROUP) does not admit ticket T001", e.getMessage());
        assertTrue(group.getPresent().isEmpty());
        IndividualAttraction individual = new IndividualAttraction("A03", "Noria", 10);
        assertThrows(IllegalArgumentException.class, () -> individual.enter(groupTicket, TODAY));
        assertTrue(individual.getPresent().isEmpty());
    }

    @Test
    void typedEntryAcceptsCompatibleCapabilities() {
        IndividualAttraction individual = new IndividualAttraction("A03", "Noria", 10);
        individual.admit(ticket, TODAY);           // IndividualAccess: compiles
        GroupAttraction group = new GroupAttraction("A02", "Coches", 10);
        group.admit(groupTicket, TODAY);           // GroupAccess: compiles; group.admit(ticket, ...) would not
        SpecialAttraction special = new SpecialAttraction("A04", "Vip", 10);
        special.admit(ticket, TODAY);
        GeneralAttraction general = new GeneralAttraction("A01", "Tornado", 10);
        general.admit(ticket, TODAY);
        general.admit(groupTicket, TODAY);
        assertEquals(1, individual.getPresent().size());
        assertEquals(2, group.getPresent().size());
        assertEquals(3, general.getPresent().size());
    }

    @Test
    void genericBoundsDeclaredByEachType() {
        Map<Class<?>, Class<?>> bounds = Map.of(GeneralAttraction.class, AccessTicket.class,
                IndividualAttraction.class, IndividualAccess.class, GroupAttraction.class, GroupAccess.class,
                SpecialAttraction.class, IndividualAccess.class);
        bounds.forEach((attraction, capability) -> {
            ParameterizedType parent = (ParameterizedType) attraction.getGenericSuperclass();
            assertEquals(Attraction.class, parent.getRawType());
            assertEquals(capability, parent.getActualTypeArguments()[0], attraction.getSimpleName());
        });
        assertTrue(IndividualAccess.class.isAssignableFrom(Ticket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertFalse(GroupAccess.class.isAssignableFrom(Ticket.class));
    }

    @Test
    void typeFactoryAndParsing() {
        for (AttractionType t : AttractionType.values()) {
            Attraction<?> a = t.create("A", "n", 5);
            assertEquals(t, a.getType());
            assertEquals(5, a.getAvailable());
        }
        assertEquals(AttractionType.GROUP, AttractionType.parse(" group ").orElseThrow());
        assertTrue(AttractionType.parse("VIP").isEmpty());
        assertTrue(AttractionType.parse(null).isEmpty());
    }
}
