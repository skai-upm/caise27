package es.upm.park.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void membershipHasNoDuplicates() {
        Group g = new Group("G01", "Familia Garcia");
        assertEquals("G01", g.getId());
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMemberDnis().isEmpty());
        assertTrue(g.addMember("12345678Z"));
        assertFalse(g.addMember("12345678Z"));
        assertTrue(g.contains("12345678Z"));
        assertEquals(List.of("12345678Z"), g.getMemberDnis());
        assertThrows(UnsupportedOperationException.class, () -> g.getMemberDnis().clear());
        assertTrue(g.removeMember("12345678Z"));
        assertFalse(g.removeMember("12345678Z"));
    }
}
