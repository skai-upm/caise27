package es.upm.etsisi.amusementpark.business.attraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.ticket.AccessTicket;
import es.upm.etsisi.amusementpark.business.ticket.GroupAccess;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttractionTypesTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "h", "6", "1");
    private final IndividualTicket single = new IndividualTicket("T", DAY, ana, new BigDecimal("40.00"), 0);
    private final GroupTicket group = new GroupTicket("TG", DAY, null, List.of(
            new IndividualTicket("TG:1", DAY, ana, new BigDecimal("40.00"), 0)));

    @Test
    void genericBoundDecidesAcceptedTickets() {
        Map<AttractionType, Class<?>> bounds = Map.of(AttractionType.GENERAL, AccessTicket.class,
                AttractionType.INDIVIDUAL, IndividualAccess.class, AttractionType.GROUP, GroupAccess.class,
                AttractionType.SPECIAL, IndividualAccess.class);
        for (AttractionType type : AttractionType.values()) {
            Attraction<?> a = type.create("A", "N", 5);
            assertEquals(type, a.getType());
            ParameterizedType parent = (ParameterizedType) a.getClass().getGenericSuperclass();
            assertEquals(bounds.get(type), parent.getActualTypeArguments()[0]);
            assertEquals(bounds.get(type).isInstance(single), a.accepts(single));
            assertEquals(bounds.get(type).isInstance(group), a.accepts(group));
        }
        assertTrue(AttractionType.fromName("SPECIAL").isPresent());
        assertFalse(AttractionType.fromName("special").isPresent());
    }

    @Test
    void admitIsAllOrNothing() {
        Attraction<?> a = AttractionType.GENERAL.create("A", "N", 1);
        User pedro = new User("P", "L", "87654321X", "p@correo.com", "h", "6", "1");
        assertThrows(IllegalStateException.class, () -> a.admit(List.of(ana, pedro)));
        a.admit(List.of(ana));
        assertThrows(IllegalStateException.class, () -> a.admit(List.of(ana)));
        assertTrue(a.release(ana));
        assertFalse(a.release(ana));
        assertThrows(IllegalArgumentException.class, () -> AttractionType.GROUP.create("A", "N", 0));
    }
}
