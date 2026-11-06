package es.upm.park.business.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionTest {

    @Test
    void newAttractionHasDataAndNoOccupants() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertEquals("A01", attraction.getId());
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupantEmails().isEmpty());
    }

    @Test
    void occupantsAreUniqueAndViewIsReadOnly() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertTrue(attraction.enter("ana@correo.com"));
        assertFalse(attraction.enter("ana@correo.com"));
        assertEquals(List.of("ana@correo.com"), List.copyOf(attraction.getOccupantEmails()));
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupantEmails().add("x"));
        assertTrue(attraction.leave("ana@correo.com"));
        assertFalse(attraction.isInside("ana@correo.com"));
    }

    @Test
    void capacityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralAttraction("A02", "Torre", 0));
    }
}
