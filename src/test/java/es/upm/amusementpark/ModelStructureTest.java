package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Checks the relationship directions and abstractions required by the
 * Delivery 2 technical restrictions.
 */
class ModelStructureTest {

    /** Types referenced by the fields of a class, including collection element types. */
    private static Set<Type> referencedTypes(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getGenericType)
                .map(t -> t instanceof ParameterizedType p ? p.getActualTypeArguments()[0] : t)
                .collect(Collectors.toSet());
    }

    @Test
    void usersDoNotKnowTicketsGroupsOrAttractions() {
        Set<Type> types = referencedTypes(User.class);
        for (Class<?> forbidden : new Class<?>[] {Ticket.class, IndividualTicket.class, GroupTicket.class,
                Group.class, Attraction.class}) {
            assertFalse(types.contains(forbidden), forbidden.getSimpleName());
        }
    }

    @Test
    void groupsDoNotKnowTicketsButKnowTheirUsers() {
        Set<Type> types = referencedTypes(Group.class);
        assertFalse(types.contains(GroupTicket.class));
        assertFalse(types.contains(Ticket.class));
        assertTrue(types.contains(User.class));
    }

    @Test
    void ticketsKnowTheirUserAndGroupTicketsTheirGroupAndComposition() {
        assertTrue(referencedTypes(IndividualTicket.class).contains(User.class));
        Set<Type> groupTicket = referencedTypes(GroupTicket.class);
        assertTrue(groupTicket.contains(Group.class));
        assertTrue(groupTicket.contains(IndividualTicket.class));
        assertFalse(groupTicket.contains(User.class));
    }

    @Test
    void attractionsKnowTheirUsers() {
        assertTrue(referencedTypes(Attraction.class).contains(User.class));
    }

    @Test
    void ticketTypesShareAnAbstraction() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }
}
