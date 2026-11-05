package es.upm.parque.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Guards the relationship directions and the ticket hierarchy required by the assignment.
 */
class RelationshipDirectionTest {

    private static boolean refersTo(Field field, Class<?> target) {
        if (target.isAssignableFrom(field.getType())) {
            return true;
        }
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            for (Type argument : parameterized.getActualTypeArguments()) {
                if (argument instanceof Class<?> c && target.isAssignableFrom(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertNoReference(Class<?> owner, Class<?>... targets) {
        for (Field field : owner.getDeclaredFields()) {
            for (Class<?> target : targets) {
                assertFalse(refersTo(field, target), owner.getSimpleName() + "." + field.getName());
            }
        }
    }

    @Test
    void usersKnowNoTicketsGroupsOrAttractions() {
        assertNoReference(User.class, Ticket.class, Group.class, Attraction.class);
    }

    @Test
    void groupsKnowNoTickets() {
        assertNoReference(Group.class, Ticket.class);
    }

    @Test
    void ticketsShareACommonAbstractionAndGroupTicketsUseComposition() throws Exception {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
        Field composition = GroupTicket.class.getDeclaredField("composition");
        assertEquals(List.class, composition.getType());
        assertTrue(refersTo(composition, IndividualTicket.class));
        assertTrue(refersTo(IndividualTicket.class.getDeclaredField("user"), User.class));
        assertTrue(refersTo(GroupTicket.class.getDeclaredField("group"), Group.class));
        assertTrue(refersTo(Attraction.class.getDeclaredField("occupants"), User.class));
    }
}
