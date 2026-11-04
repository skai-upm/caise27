package com.parque.business.model;

import com.parque.business.controller.*;
import com.parque.business.service.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroupTest {
    final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");

    @Test
    void membersAreReferencesWithoutDuplicates() {
        Group g = new Group("G01", "Familia Garcia");
        assertTrue(g.addMember(ana));
        assertFalse(g.addMember(ana));
        assertEquals(1, g.getMembers().size());
        assertSame(ana, g.getMembers().get(0));
        assertTrue(g.contains(ana));
        User copy = new User(ana);
        assertFalse(g.contains(copy), "membership is by identity, not by equal values");
        assertTrue(g.removeMember(ana));
        assertFalse(g.removeMember(ana));
        assertEquals(0, g.getMembers().size());
    }

    @Test
    void membersViewIsReadOnly() {
        Group g = new Group("G01", "Familia Garcia");
        assertThrows(UnsupportedOperationException.class, () -> g.getMembers().add(ana));
    }

    @Test
    void copySharesUserReferences() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        Group copy = new Group(g);
        assertEquals("G01", copy.getId());
        assertEquals("Familia Garcia", copy.getName());
        assertSame(ana, copy.getMembers().get(0));
        copy.removeMember(ana);
        assertTrue(g.contains(ana), "copy is independent of the original list");
    }
}
