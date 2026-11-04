package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
            "Clave1!", "612345678", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
            "Otra2#", "623456789", "9876543210987654");

    @Test
    void holdsIdNameAndMembers() {
        Group g = new Group("G01", "Familia Garcia");
        assertEquals("G01", g.getId());
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());
        assertTrue(g.addMember(ana));
        assertTrue(g.addMember(pedro));
        assertSame(ana, g.getMembers().get(0));
    }

    @Test
    void sameUserCannotBeAddedTwice() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertFalse(g.addMember(ana));
        assertEquals(1, g.getMembers().size());
    }

    @Test
    void membershipIsByIdentity() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertTrue(g.contains(ana));
        assertFalse(g.contains(new User(ana)));
    }

    @Test
    void removeMember() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        assertTrue(g.removeMember(ana));
        assertFalse(g.removeMember(ana));
        assertTrue(g.getMembers().isEmpty());
    }

    @Test
    void membersViewIsUnmodifiable() {
        Group g = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().add(ana));
    }

    @Test
    void copySharesUsersButNotList() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        Group copy = new Group(g);
        copy.addMember(pedro);
        assertEquals(1, g.getMembers().size());
        assertSame(ana, copy.getMembers().get(0));
    }
}
