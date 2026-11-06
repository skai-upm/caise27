package es.upm.park.business.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupTest {

    @Test
    void newGroupIsEmpty() {
        Group group = new Group("G01", "Familia Garcia");
        assertEquals("G01", group.getId());
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.getMemberEmails().isEmpty());
    }

    @Test
    void memberAppearsAtMostOnce() {
        Group group = new Group("G01", "Familia Garcia");
        assertTrue(group.addMember("ana@correo.com"));
        assertFalse(group.addMember("ana@correo.com"));
        assertEquals(List.of("ana@correo.com"), List.copyOf(group.getMemberEmails()));
    }

    @Test
    void removingNonMemberReportsNoChange() {
        Group group = new Group("G01", "Familia Garcia");
        assertFalse(group.removeMember("pedro@correo.com"));
        group.addMember("pedro@correo.com");
        assertTrue(group.removeMember("pedro@correo.com"));
        assertFalse(group.hasMember("pedro@correo.com"));
    }
}
