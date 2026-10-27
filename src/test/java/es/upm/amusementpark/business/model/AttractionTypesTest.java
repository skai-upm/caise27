package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

class AttractionTypesTest {

    private final LocalDate day = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "1", "1");
    private final IndividualTicket individual = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
    private final GroupTicket group = new GroupTicket("TG01", day, new BigDecimal("40.00"), new Group("G", "g"),
            List.of(new User("P", "L", "87654321X", "p@correo.com", "x", "1", "1")));

    @Test
    void ticketsImplementCapabilities() {
        assertTrue(individual instanceof IndividualAccess);
        assertTrue(group instanceof GroupAccess);
        assertTrue(individual instanceof AttractionAccess && group instanceof AttractionAccess);
    }

    @Test
    void genericParameterRestrictsAccess() throws Exception {
        IndividualAttraction a2 = new IndividualAttraction("A02", "I", 5);
        IndividualAccess access = a2.accessFor(individual);
        assertSame(individual, access);
        assertEquals(List.of(ana), a2.admit(access, day));
        assertThrows(OperationNotAllowedException.class, () -> a2.accessFor(group));
        assertThrows(OperationNotAllowedException.class, () -> new GroupAttraction("A03", "G", 5).accessFor(individual));
        assertThrows(OperationNotAllowedException.class, () -> new SpecialAttraction("A04", "S", 5).accessFor(group));
        new GeneralAttraction("A01", "G", 5).accessFor(group);
    }

    @Test
    void subclassesBindTheTypeParameter() {
        assertEquals(AttractionAccess.class, bound(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, bound(IndividualAttraction.class));
        assertEquals(GroupAccess.class, bound(GroupAttraction.class));
        assertEquals(IndividualAccess.class, bound(SpecialAttraction.class));
    }

    @Test
    void admitIsAtomic() {
        GroupAttraction a = new GroupAttraction("A03", "G", 0);
        assertThrows(OperationNotAllowedException.class, () -> a.admit(group, day));
        assertEquals(0, a.occupancy());
        assertThrows(OperationNotAllowedException.class,
                () -> new GroupAttraction("A", "G", 5).admit(group, day.plusDays(1)));
    }

    private static Object bound(Class<?> type) {
        return ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
