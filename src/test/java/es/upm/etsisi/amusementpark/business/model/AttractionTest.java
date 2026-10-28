package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("12345678Z", "ana@correo.com");
    private final User pedro = new User("87654321X", "pedro@correo.com");

    @Test
    void newAttractionIsEmpty() {
        Attraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", attraction.getIdentifier());
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @Test
    void capacityNeverExceeded() {
        Attraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 1);
        attraction.addOccupant(ana);
        assertThrows(ParkException.class, () -> attraction.addOccupant(pedro));
        assertEquals(1, attraction.getOccupants().size());
    }

    @Test
    void noDuplicateOccupantsAndRemoval() {
        Attraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 5);
        attraction.addOccupant(ana);
        assertThrows(ParkException.class, () -> attraction.addOccupant(ana));
        attraction.removeOccupant(ana);
        assertThrows(ParkException.class, () -> attraction.removeOccupant(ana));
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupants().add(pedro));
    }

    @Test
    void admitIsAllOrNothing() {
        Attraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 2);
        User luis = new User("11111111H", "luis@correo.com");
        attraction.admit(List.of(ana));
        assertThrows(ParkException.class, () -> attraction.admit(List.of(pedro, luis)));
        assertEquals(List.of(ana), attraction.getOccupants());
        assertThrows(ParkException.class, () -> attraction.admit(List.of(pedro, ana)));
        assertEquals(1, attraction.getOccupants().size());
        attraction.admit(List.of(pedro));
        assertEquals(2, attraction.getOccupants().size());
    }
}
