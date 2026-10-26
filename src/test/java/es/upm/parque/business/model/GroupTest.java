package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void userCannotBeAddedTwice() {
        Group group = new Group("G01", "Familia Garcia");
        assertTrue(group.addMember(ana));
        assertFalse(group.addMember(ana));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    void membersAreTheSameObjects() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        ana.setName("Ana Maria");
        assertSame(ana, group.getMembers().get(0));
        assertEquals("Ana Maria", group.getMembers().get(0).getName());
    }

    @Test
    void membersViewIsReadOnly() {
        Group group = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> group.getMembers().add(ana));
    }

    @Test
    void removeMember() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        assertTrue(group.removeMember(ana));
        assertFalse(group.removeMember(ana));
        assertFalse(group.hasMember(ana));
    }
}
