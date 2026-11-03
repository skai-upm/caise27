package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    static User user(String dni, String email) {
        return new User("N", "S", dni, email, "Clave1!", "6", "1234567890123456");
    }

    @Test
    void membershipIsByIdentityAndWithoutDuplicates() {
        Group g = new Group("G01", "Familia Garcia");
        User ana = user("12345678Z", "ana@correo.com");
        User anaCopy = user("12345678Z", "ana@correo.com");
        assertTrue(g.addMember(ana));
        assertFalse(g.addMember(ana));
        assertTrue(g.contains(ana));
        assertFalse(g.contains(anaCopy));
        assertEquals(1, g.getMembers().size());
        assertFalse(g.removeMember(anaCopy));
        assertTrue(g.removeMember(ana));
        assertTrue(g.getMembers().isEmpty());
        assertEquals("G01", g.getId());
        assertEquals("Familia Garcia", g.getName());
    }

    @Test
    void membersViewIsUnmodifiable() {
        Group g = new Group("G01", "F");
        User ana = user("12345678Z", "ana@correo.com");
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().add(ana));
    }
}
