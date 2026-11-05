package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.park.Attraction;
import es.upm.amusementpark.park.Group;
import es.upm.amusementpark.park.GroupTicket;
import es.upm.amusementpark.park.IndividualTicket;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.park.Ticket;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guards the Delivery 2 technical restrictions on relationships and abstractions.
 */
class Delivery2RestrictionsTest {

    private static List<String> fieldTypes(Class<?> type) {
        List<String> types = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                types.add(field.getGenericType().getTypeName());
            }
        }
        return types;
    }

    private static boolean mentions(List<String> types, Class<?> target) {
        return types.stream().anyMatch(t -> t.contains(target.getName()));
    }

    @Test
    void groupsAndAttractionsHoldNoTickets() {
        assertFalse(mentions(fieldTypes(Group.class), Ticket.class));
        assertFalse(mentions(fieldTypes(Group.class), GroupTicket.class));
        assertFalse(mentions(fieldTypes(Attraction.class), Ticket.class));
    }

    @Test
    void ticketsHoldNoAttractions() {
        for (Class<?> type : List.of(Ticket.class, IndividualTicket.class, GroupTicket.class)) {
            assertFalse(mentions(fieldTypes(type), Attraction.class), type.getName());
        }
    }

    @Test
    void groupTicketComposesIndividualTicketsAndKnowsItsGroup() {
        List<String> types = fieldTypes(GroupTicket.class);
        assertTrue(types.contains("java.util.List<" + IndividualTicket.class.getName() + ">"));
        assertTrue(types.contains(Group.class.getName()));
    }

    @Test
    void ticketTypesShareTheAbstraction() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }

    @Test
    void singleCentralManager() {
        assertTrue(ParkController.getInstance() == ParkController.getInstance());
        for (var constructor : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }
}
