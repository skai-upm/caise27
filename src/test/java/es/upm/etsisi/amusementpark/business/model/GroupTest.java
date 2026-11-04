package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");

    @Test
    void addsEachUserOnlyOnce() {
        Group g = new Group("G01", "Familia Garcia");
        assertTrue(g.addMember(ana));
        assertFalse(g.addMember(ana));
        assertTrue(g.addMember(pedro));
        assertEquals(2, g.getMembers().size());
        assertTrue(g.contains(ana));
    }

    @Test
    void removesMembers() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertTrue(g.removeMember(ana));
        assertFalse(g.removeMember(ana));
        assertTrue(g.getMembers().isEmpty());
    }

    @Test
    void membershipIsByIdentityNotByValue() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertFalse(g.contains(new User(ana)));
        assertSame(ana, g.getMembers().get(0));
    }

    @Test
    void membersViewIsReadOnly() {
        Group g = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().add(ana));
    }

    @Test
    void copyKeepsSameMemberReferences() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        Group copy = new Group(g);
        g.removeMember(ana);
        assertTrue(copy.contains(ana));
        assertEquals("Familia Garcia", copy.getName());
    }
}
