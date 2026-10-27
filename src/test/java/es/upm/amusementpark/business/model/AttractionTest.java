package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static User user(String email) {
        return new User("Ana", "Garcia Lopez", "12345678Z", email, "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void newAttractionIsEmpty() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 20);
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
        assertEquals(20, attraction.getAvailablePlaces());
    }

    @Test
    void occupantsAreKeptByTheAttraction() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 20);
        User ana = user("ana@correo.com");
        attraction.enter(List.of(ana));
        assertTrue(attraction.isInside(ana));
        assertEquals(19, attraction.getAvailablePlaces());
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupants().clear());
        assertTrue(attraction.exit(ana));
        assertFalse(attraction.exit(ana));
    }

    @Test
    void usersDoNotReferenceAttractions() {
        assertTrue(Arrays.stream(User.class.getDeclaredFields())
                .noneMatch(f -> f.getType() == Attraction.class || f.getType().isAssignableFrom(List.class)));
    }
}
