package com.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTypesTest {
    static final LocalDate D = LocalDate.of(2026, 10, 15);
    static final BigDecimal P = new BigDecimal("40.00");
    final User ana = new User("Ana", "G", "a", "ana@correo.com", "x", "6", "1234567890123456");
    final User pedro = new User("Pedro", "L", "p", "pedro@correo.com", "x", "6", "1234567890123456");
    final IndividualTicket t001 = new IndividualTicket("T001", ana, D, P);
    final IndividualTicket constituent = new IndividualTicket("TG01-1", pedro, D, P);
    final GroupTicket tg01 = new GroupTicket("TG01", null, "G01", D, P, P, List.of(constituent));

    @Test
    void acceptedKindsPerType() {
        assertTrue(new GeneralAttraction("A", "n", 9).accepts(t001));
        assertTrue(new GeneralAttraction("A", "n", 9).accepts(tg01));
        assertTrue(new IndividualAttraction("A", "n", 9).accepts(t001));
        assertFalse(new IndividualAttraction("A", "n", 9).accepts(tg01));
        assertTrue(new IndividualAttraction("A", "n", 9).accepts(constituent));
        assertFalse(new GroupAttraction("A", "n", 9).accepts(t001));
        assertTrue(new GroupAttraction("A", "n", 9).accepts(tg01));
        assertTrue(new SpecialAttraction("A", "n", 9).accepts(t001));
        assertFalse(new SpecialAttraction("A", "n", 9).accepts(tg01));
    }

    @Test
    void rejectedKindAdmitsNobody() {
        Attraction<?> group = new GroupAttraction("A02", "Familiar", 30);
        assertThrows(IllegalArgumentException.class, () -> group.admit(t001, D));
        Attraction<?> individual = new IndividualAttraction("A03", "Solo", 30);
        assertThrows(IllegalArgumentException.class, () -> individual.admit(tg01, D));
        assertEquals(0, group.getPresent().size() + individual.getPresent().size());
        group.admit(tg01, D);
        individual.admit(t001, D);
        assertEquals(1, group.getPresent().size());
        assertEquals(1, individual.getPresent().size());
    }

    @Test
    void typedEntryOtherRulesStillApply() {
        GroupAttraction g = new GroupAttraction("A", "n", 1);
        g.enter(tg01, D);
        assertThrows(IllegalArgumentException.class, () -> g.enter(tg01, D), "already inside");
        IndividualAttraction i = new IndividualAttraction("B", "n", 5);
        assertThrows(IllegalArgumentException.class, () -> i.enter(t001, D.plusDays(1)), "date");
    }

    @Test
    void factoryAndParse() {
        for (AttractionType t : AttractionType.values()) {
            assertEquals(t, t.create("A", "n", 1).getType());
            assertEquals(t, AttractionType.parse(t.name()));
        }
        assertThrows(IllegalArgumentException.class, () -> AttractionType.parse("VIP"));
        assertThrows(IllegalArgumentException.class, () -> AttractionType.parse("group"));
    }

    @Test
    void boundedGenericsAndCapabilityInterfaces() {
        TypeVariable<?> tv = Attraction.class.getTypeParameters()[0];
        assertEquals(Admission.class, tv.getBounds()[0]);
        assertEquals(GroupAccess.class,
                ((ParameterizedType) GroupAttraction.class.getGenericSuperclass()).getActualTypeArguments()[0]);
        assertEquals(IndividualAccess.class,
                ((ParameterizedType) SpecialAttraction.class.getGenericSuperclass()).getActualTypeArguments()[0]);
        assertEquals(IndividualAccess.class,
                ((ParameterizedType) IndividualAttraction.class.getGenericSuperclass()).getActualTypeArguments()[0]);
        assertEquals(Admission.class,
                ((ParameterizedType) GeneralAttraction.class.getGenericSuperclass()).getActualTypeArguments()[0]);
        assertTrue(Admission.class.isInterface() && IndividualAccess.class.isInterface() && GroupAccess.class.isInterface());
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
    }
}
