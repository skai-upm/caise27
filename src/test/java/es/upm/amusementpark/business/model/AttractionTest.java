package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");

    @Test
    void tracksOccupantsAndCapacity() {
        Attraction<?> a = new GeneralAttraction("A01", "Dragon Rojo", 2);
        assertEquals(0, a.occupancy());
        assertEquals(2, a.freePlaces());
        a.enter(ana);
        assertTrue(a.isInside(ana));
        assertEquals(1, a.freePlaces());
        assertTrue(a.hasRoomFor(1));
        assertFalse(a.hasRoomFor(2));
        assertThrows(UnsupportedOperationException.class, () -> a.getOccupants().clear());
        assertTrue(a.exit(ana));
        assertFalse(a.exit(ana));
    }

    @Test
    void usersDoNotReferenceAttractions() {
        for (Field f : User.class.getDeclaredFields()) {
            assertFalse(f.getGenericType().getTypeName().contains("Attraction"), f.getName());
        }
    }
}
