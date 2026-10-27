package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");

    @Test
    void userAppearsAtMostOnce() {
        Group g = new Group("G01", "Familia Garcia");
        assertTrue(g.addMember(ana));
        assertFalse(g.addMember(ana));
        assertEquals(1, g.getMembers().size());
        assertTrue(g.contains(ana));
    }

    @Test
    void membersCanBeRemovedButNotEditedFromOutside() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().clear());
        assertTrue(g.removeMember(ana));
        assertFalse(g.removeMember(ana));
        assertEquals(0, g.getMembers().size());
    }
}
