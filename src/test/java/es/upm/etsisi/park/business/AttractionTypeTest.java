package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionTypeTest extends ParkTestSupport {

    @BeforeEach
    void setUp() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        tickets.create("T001", "ana@correo.com", TODAY_S);
        tickets.createGroupTicket("TG01", "G01", TODAY_S);
        attractions.create("GEN", "General", "20", null);
        attractions.create("IND", "Individual", "20", "INDIVIDUAL");
        attractions.create("GRP", "Group", "20", "GROUP");
        attractions.create("SPE", "Special", "20", "SPECIAL");
    }

    @Test
    void creationTypes() {
        assertEquals(AttractionType.GENERAL, park.findAttraction("GEN").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("IND").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("GRP").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("SPE").getType());
        attractions.create("EXP", "Explicit", "3", "GENERAL");
        assertTrue(park.findAttraction("EXP") instanceof GeneralAttraction);
    }

    @Test
    void unknownTypeRejected() {
        assertThrows(RuleViolationException.class, () -> attractions.create("A05", "Otra", "10", "VIP"));
        assertThrows(RuleViolationException.class, () -> attractions.create("A05", "Otra", "10", "general"));
        assertNull(park.findAttraction("A05"));
    }

    @Test
    void generalAcceptsBoth() {
        assertEquals(1, attractions.enter("GEN", "T001", false).entered());
        attractions.exit("GEN", "ana@correo.com");
        assertEquals(2, attractions.enter("GEN", "TG01", false).entered());
    }

    @Test
    void individualAcceptsOnlyIndividual() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("IND", "TG01", false));
        assertTrue(park.findAttraction("IND").getOccupants().isEmpty());
        assertEquals(1, attractions.enter("IND", "T001", false).entered());
        assertEquals(1, attractions.enter("IND", "TG01:87654321X", false).entered(), "constituent used individually");
    }

    @Test
    void groupAcceptsOnlyGroup() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("GRP", "T001", false));
        assertThrows(RuleViolationException.class, () -> attractions.enter("GRP", "TG01:87654321X", false));
        assertEquals(2, attractions.enter("GRP", "TG01", false).entered());
    }

    @Test
    void specialRejectsGroupTickets() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("SPE", "TG01", false));
    }

    @Test
    void genericBoundsDetermineAcceptedTickets() {
        TypeVariable<?> t = Attraction.class.getTypeParameters()[0];
        assertEquals(AccessTicket.class, t.getBounds()[0]);
        assertEquals(AccessTicket.class, typeArgument(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAccess.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
    }

    private static Class<?> typeArgument(Class<?> attraction) {
        return (Class<?>) ((ParameterizedType) attraction.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
