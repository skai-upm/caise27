package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Delivery 2 technical restrictions on the model structure.
 */
class ModelStructureTest {

    /** Types referenced by the instance fields of {@code type}, including collection element types. */
    private static List<Class<?>> referencedTypes(Class<?> type) {
        List<Class<?>> types = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            types.add(field.getType());
            Type generic = field.getGenericType();
            if (generic instanceof ParameterizedType parameterized) {
                for (Type argument : parameterized.getActualTypeArguments()) {
                    if (argument instanceof Class<?> c) {
                        types.add(c);
                    }
                }
            }
        }
        return types;
    }

    private static boolean references(Class<?> owner, Class<?> target) {
        return referencedTypes(owner).stream().anyMatch(target::isAssignableFrom);
    }

    @Test
    void usersReferenceNoTicketsGroupsOrAttractions() {
        assertFalse(references(User.class, Ticket.class));
        assertFalse(references(User.class, Group.class));
        assertFalse(references(User.class, Attraction.class));
    }

    @Test
    void groupsReferenceNoTickets() {
        assertFalse(references(Group.class, Ticket.class));
        assertTrue(references(Group.class, User.class));
    }

    @Test
    void mandatedDirections() {
        assertTrue(references(Attraction.class, User.class));
        assertTrue(references(IndividualTicket.class, User.class));
        assertTrue(references(GroupTicket.class, IndividualTicket.class));
        assertTrue(references(GroupTicket.class, Group.class));
        assertFalse(references(GroupTicket.class, User.class), "group tickets keep no parallel user collection");
    }

    @Test
    void ticketsShareAnAbstraction() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }

    @Test
    void singleParkController() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Constructor<?> constructor : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }
}
