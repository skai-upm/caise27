package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
            "9876543210987654");
    private final User laura = new User("Laura", "Martin", "11111111H", "laura@correo.com", "Laura3$", "6",
            "1111222233334444");

    @Test
    void startsEmpty() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", attraction.getId());
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
        assertEquals(20, attraction.getFreePlaces());
    }

    @Test
    void entryIsAllOrNothing() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Mini", 2);
        List<User> three = List.of(ana, pedro, laura);
        assertFalse(attraction.hasRoomFor(3));
        assertThrows(IllegalStateException.class, () -> attraction.enter(three));
        assertTrue(attraction.getOccupants().isEmpty());
        attraction.enter(List.of(ana, pedro));
        assertEquals(0, attraction.getFreePlaces());
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupants().clear());
    }

    @Test
    void occupantsLeaveIndividually() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        attraction.enter(List.of(ana, pedro, laura));
        assertSame(pedro, attraction.findOccupant("PEDRO@correo.com"));
        assertNull(attraction.findOccupant("nobody@correo.com"));
        int position = attraction.exit(ana);
        assertEquals(0, position);
        assertEquals(-1, attraction.exit(ana));
        assertEquals(List.of(pedro, laura), attraction.getOccupants());
        assertEquals(18, attraction.getFreePlaces());
        attraction.restore(position, ana);
        assertEquals(List.of(ana, pedro, laura), attraction.getOccupants());
    }
}
