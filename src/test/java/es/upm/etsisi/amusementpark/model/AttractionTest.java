package es.upm.etsisi.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("12345678Z", "ana@correo.com");
    private final User pedro = new User("87654321X", "pedro@correo.com");

    @Test
    void newAttractionIsEmpty() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", attraction.getIdentifier());
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @Test
    void capacityNeverExceeded() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 1);
        attraction.addOccupant(ana);
        assertThrows(IllegalStateException.class, () -> attraction.addOccupant(pedro));
        assertEquals(1, attraction.getOccupants().size());
    }

    @Test
    void noDuplicateOccupantsAndRemoval() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 5);
        attraction.addOccupant(ana);
        assertThrows(IllegalStateException.class, () -> attraction.addOccupant(ana));
        attraction.removeOccupant(ana);
        assertThrows(IllegalStateException.class, () -> attraction.removeOccupant(ana));
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupants().add(pedro));
    }
}
