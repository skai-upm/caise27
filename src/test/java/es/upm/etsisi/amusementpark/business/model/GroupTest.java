package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.TestData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void membershipIsByIdentityAndWithoutDuplicates() {
        Group g = new Group("G01", "Familia Garcia");
        User ana = TestData.user("12345678Z", "ana@correo.com");
        User anaCopy = TestData.user("12345678Z", "ana@correo.com");
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
        User ana = TestData.user("12345678Z", "ana@correo.com");
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().add(ana));
    }
}
