package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void membershipHasNoDuplicates() {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User anaCopy = ana.copy();
        Group g = new Group("G01", "Familia Garcia");
        assertEquals("G01", g.getId());
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMemberDnis().isEmpty());
        assertTrue(g.addMember(ana));
        assertFalse(g.addMember(anaCopy));
        assertTrue(g.contains(anaCopy));
        assertEquals(List.of(ana), g.getMembers());
        assertEquals(List.of("12345678Z"), g.getMemberDnis());
        assertThrows(UnsupportedOperationException.class, () -> g.getMemberDnis().clear());
        assertTrue(g.removeMember(anaCopy));
        assertFalse(g.removeMember(ana));
    }
}
