package es.upm.etsisi.amusementpark.attraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.park.ParkFixture;
import es.upm.etsisi.amusementpark.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    @Test
    void admitAllOrNothing() {
        Attraction a = new Attraction("A01", "Dragon", 2);
        User u1 = ParkFixture.user(1);
        User u2 = ParkFixture.user(2);
        User u3 = ParkFixture.user(3);
        assertFalse(a.canAdmit(List.of(u1, u2, u3)));
        assertThrows(IllegalStateException.class, () -> a.admit(List.of(u1, u2, u3)));
        assertTrue(a.getOccupants().isEmpty());
        a.admit(List.of(u1));
        assertFalse(a.canAdmit(List.of(u1)));
        a.admit(List.of(u2));
        assertEquals(0, a.getFreePlaces());
        assertTrue(a.release(u1));
        assertFalse(a.release(u1));
        assertEquals(List.of(u2), a.getOccupants());
        assertEquals("Dragon", a.getName());
    }

    @Test
    void capacityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Attraction("A", "B", 0));
    }
}
