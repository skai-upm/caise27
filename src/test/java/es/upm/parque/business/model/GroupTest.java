package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.Fixtures;

class GroupTest {

    @Test
    void managesMembersWithoutDuplicates() {
        Group group = new Group("G01", "Familia Garcia");
        User ana = Fixtures.ana();
        User pedro = Fixtures.pedro();
        assertEquals("G01", group.getId());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.addMember(ana));
        assertTrue(group.addMember(pedro));
        assertFalse(group.addMember(ana));
        assertEquals(List.of(ana, pedro), group.getMembers());
        assertTrue(group.contains(ana));
        assertTrue(group.removeMember(ana));
        assertFalse(group.removeMember(ana));
        assertFalse(group.contains(ana));
    }

    @Test
    void membersViewIsReadOnly() {
        Group group = new Group("G01", "Familia");
        List<User> members = group.getMembers();
        User ana = Fixtures.ana();
        assertThrows(UnsupportedOperationException.class, () -> members.add(ana));
    }
}
