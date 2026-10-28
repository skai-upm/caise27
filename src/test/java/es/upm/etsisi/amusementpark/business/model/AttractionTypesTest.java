package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AttractionTypesTest {

    private final LocalDate date = LocalDate.of(2027, 4, 15);
    private final User ana = new User("1A", "ana@correo.com");
    private final IndividualTicket individual = new IndividualTicket("T001", date, ana, new BigDecimal("40.00"));
    private final GroupTicket group;

    AttractionTypesTest() {
        Group g = new Group("G01", "Familia");
        g.addMember(new User("2B", "pedro@correo.com"));
        g.addMember(new User("3C", "luis@correo.com"));
        group = GroupTicket.purchase("TG01", date, g, new BigDecimal("40.00"));
    }

    @Test
    void acceptedTicketTypes() {
        assertTrue(new GeneralAttraction("A", "a", 9).accepts(individual));
        assertTrue(new GeneralAttraction("A", "a", 9).accepts(group));
        assertTrue(new IndividualAttraction("A", "a", 9).accepts(individual));
        assertFalse(new IndividualAttraction("A", "a", 9).accepts(group));
        assertTrue(new GroupAttraction("A", "a", 9).accepts(group));
        assertFalse(new GroupAttraction("A", "a", 9).accepts(individual));
        assertTrue(new SpecialAttraction("A", "a", 9).accepts(individual));
        assertFalse(new SpecialAttraction("A", "a", 9).accepts(group));
    }

    @Test
    void rejectedTypeAdmitsNobody() {
        Attraction<?> attraction = new IndividualAttraction("A02", "Montana", 9);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enterWith(group, List.of(), false));
        assertTrue(attraction.getOccupants().isEmpty());
        assertEquals(2, new GroupAttraction("A03", "Rafting", 9).enterWith(group, List.of(), false).users().size());
    }

    @Test
    void specialAttractionNeedsUnconsumedSpecialPass() {
        SpecialAttraction attraction = new SpecialAttraction("A04", "Terror", 9);
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(individual, List.of(), false));
        SpecialPass consumed = new SpecialPass("PE0", individual);
        consumed.consume();
        assertThrows(OperationNotAllowedException.class, () -> attraction.enter(individual,
                List.of(consumed, new SpeedPass("SP:1", individual)), false));
        attraction.enter(individual, List.of(new SpecialPass("PE1", individual)), false);
        assertTrue(attraction.isInside(ana));
    }

    @Test
    void genericBoundsUseCapabilityInterfaces() {
        Map<Class<?>, Class<?>> expected = Map.of(GeneralAttraction.class, Admission.class,
                IndividualAttraction.class, IndividualAdmission.class, GroupAttraction.class, GroupAdmission.class,
                SpecialAttraction.class, IndividualAdmission.class);
        expected.forEach((type, bound) -> {
            ParameterizedType superType = (ParameterizedType) type.getGenericSuperclass();
            assertEquals(Attraction.class, superType.getRawType());
            assertEquals(bound, superType.getActualTypeArguments()[0]);
        });
        assertTrue(IndividualAdmission.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAdmission.class.isAssignableFrom(GroupTicket.class));
        assertFalse(IndividualAdmission.class.isAssignableFrom(GroupTicket.class));
    }

    @Test
    void typesParseAndCreate() {
        for (AttractionType type : AttractionType.values()) {
            assertEquals(type, AttractionType.parse(type.name()).create("A", "a", 1).getType());
        }
        assertThrows(InvalidValueException.class, () -> AttractionType.parse("VIP"));
        assertThrows(InvalidValueException.class, () -> AttractionType.parse("general"));
    }
}
