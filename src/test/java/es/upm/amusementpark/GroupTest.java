package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class GroupTest {

    private final User ana = new User("ana@correo.com", "12345678Z");

    @Test
    void newGroupIsEmpty() {
        Group group = new Group("G01", "Familia Garcia");
        assertEquals("G01", group.getIdentifier());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMembers().isEmpty());
    }

    @Test
    void sameUserCannotBeAddedTwice() {
        Group group = new Group("G01", "Familia Garcia");
        assertTrue(group.addMember(ana));
        assertFalse(group.addMember(new User("ana@correo.com", "12345678Z")));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    void removingNonMemberFails() {
        Group group = new Group("G01", "Familia Garcia");
        assertFalse(group.removeMember(ana));
        group.addMember(ana);
        assertTrue(group.removeMember(ana));
        assertFalse(group.hasMember(ana));
    }

    @Test
    void membersViewIsUnmodifiable() {
        Group group = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> group.getMembers().add(ana));
    }

    @Test
    void lockStateIsNotStored() {
        for (Field field : Group.class.getDeclaredFields()) {
            assertFalse(field.getType() == boolean.class || field.getType() == Boolean.class,
                    "unexpected stored flag: " + field.getName());
        }
    }
}
