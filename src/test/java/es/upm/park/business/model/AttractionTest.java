package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "p@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void newAttractionExposesDataAndIsEmpty() {
        Attraction a = new Attraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", a.getId());
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> a.getOccupants().clear());
    }

    @Test
    void occupantsRespectCapacityAndUniqueness() {
        Attraction a = new Attraction("A01", "X", 1);
        assertTrue(a.enter(ana));
        assertFalse(a.enter(ana));
        assertTrue(a.isFull());
        assertFalse(a.enter(pedro));
        assertTrue(a.contains(ana));
        assertEquals(List.of(ana), a.getOccupants());
        assertTrue(a.leave(ana));
        assertFalse(a.leave(ana));
    }
}
