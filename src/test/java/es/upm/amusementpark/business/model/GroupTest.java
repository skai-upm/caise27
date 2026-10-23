package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
            "9876543210987654");

    @Test
    void startsEmpty() {
        Group group = new Group("G01", "Familia Garcia");
        assertEquals("G01", group.getId());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.isEmpty());
    }

    @Test
    void addsMembersOnce() {
        Group group = new Group("G01", "Familia");
        assertTrue(group.add(ana));
        assertTrue(group.add(pedro));
        assertFalse(group.add(ana));
        assertEquals(List.of(ana, pedro), group.getMembers());
        assertThrows(UnsupportedOperationException.class, () -> group.getMembers().add(ana));
    }

    @Test
    void removeReturnsPositionAndUndoRestoresIt() {
        Group group = new Group("G01", "Familia");
        group.add(ana);
        group.add(pedro);
        int position = group.remove(ana);
        assertEquals(0, position);
        assertEquals(-1, group.remove(ana));
        group.add(position, ana);
        assertEquals(List.of(ana, pedro), group.getMembers());
        group.add(0, ana);
        assertEquals(2, group.getMembers().size());
    }
}
