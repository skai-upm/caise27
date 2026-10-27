package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    private static User user(String email) {
        return new User("Ana", "Garcia Lopez", "12345678Z", email, "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void membersAreAddedOnceAndRemoved() {
        Group group = new Group("G01", "Familia Garcia");
        User ana = user("ana@correo.com");
        assertTrue(group.getMembers().isEmpty());
        assertTrue(group.addMember(ana));
        assertFalse(group.addMember(ana));
        assertEquals(1, group.getMembers().size());
        assertTrue(group.contains(ana));
        assertTrue(group.removeMember(ana));
        assertFalse(group.contains(ana));
    }

    @Test
    void memberListCannotBeModifiedFromOutside() {
        Group group = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> group.getMembers().add(user("x@correo.com")));
    }
}
