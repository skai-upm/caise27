package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User anaCopy = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void membersAreTrackedByIdentity() {
        Group group = new Group("G01", "Familia Garcia");
        assertEquals("G01", group.getId());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMembers().isEmpty());

        group.addMember(ana);
        assertTrue(group.hasMember(ana));
        assertFalse(group.hasMember(anaCopy));
        assertFalse(group.removeMember(anaCopy));
        assertTrue(group.removeMember(ana));
        assertFalse(group.hasMember(ana));
    }

    @Test
    void memberViewIsUnmodifiable() {
        Group group = new Group("G01", "F");
        group.addMember(ana);
        List<User> members = group.getMembers();
        assertThrows(UnsupportedOperationException.class, () -> members.add(anaCopy));
    }
}
