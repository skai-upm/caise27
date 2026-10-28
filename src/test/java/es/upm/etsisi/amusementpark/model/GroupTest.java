package es.upm.etsisi.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void newGroupIsEmpty() {
        Group group = new Group("G01", "Familia Garcia");
        assertEquals("G01", group.getIdentifier());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMembers().isEmpty());
    }

    @Test
    void userAppearsAtMostOnce() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(new User("ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> group.addMember(new User("ana@correo.com")));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    void removeMemberAndNonMember() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(new User("ana@correo.com"));
        group.removeMember("ana@correo.com");
        assertFalse(group.hasMember("ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> group.removeMember("ana@correo.com"));
    }

    @Test
    void membersCannotBeModifiedFromOutside() {
        Group group = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> group.getMembers().add(new User("x@y.com")));
    }
}
