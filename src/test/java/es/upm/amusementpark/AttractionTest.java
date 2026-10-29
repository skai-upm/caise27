package es.upm.amusementpark;

import es.upm.amusementpark.business.exception.ParkException;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("ana@correo.com", "12345678Z");
    private final User pedro = new User("pedro@correo.com", "87654321X");

    @Test
    void newAttractionIsEmpty() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", attraction.getIdentifier());
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @Test
    void capacityBoundRespected() {
        Attraction<?> attraction = new GeneralAttraction("A03", "Mini", 1);
        assertTrue(attraction.addOccupant(ana));
        assertTrue(attraction.isFull());
        assertFalse(attraction.addOccupant(pedro));
        assertEquals(List.of(ana), attraction.getOccupants());
    }

    @Test
    void sameUserNotTwice() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        attraction.addOccupant(ana);
        assertFalse(attraction.addOccupant(new User("ana@correo.com", "12345678Z")));
        assertTrue(attraction.removeOccupant(ana));
        assertFalse(attraction.removeOccupant(ana));
    }

    @Test
    void nonPositiveCapacityRejected() {
        assertThrows(ParkException.class, () -> new GeneralAttraction("A01", "X", 0));
    }

    @Test
    void usersHoldNoAttractionReference() {
        for (Field field : User.class.getDeclaredFields()) {
            assertFalse(Attraction.class.isAssignableFrom(field.getType()));
        }
    }
}
