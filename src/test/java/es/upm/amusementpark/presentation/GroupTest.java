package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.model.Group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupTest extends ParkTestSupport {

    @BeforeEach
    void users() {
        run(ANA);
        run(PEDRO);
    }

    private Group g01() {
        return park.getGroups().find("G01");
    }

    @Test
    void createsGroupAndRejectsDuplicateOrBlank() {
        assertTrue(ok(run("GROUP CREATE;G01;Familia Garcia")));
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(g01().getMembers().isEmpty());
        assertTrue(error(run("GROUP CREATE;G01;Otra")));
        assertTrue(error(run("GROUP CREATE;G02; ")));
        assertEquals("Familia Garcia", g01().getName());
    }

    @Test
    void membersAreTheRegisteredUsers() {
        run("GROUP CREATE;G01;Familia Garcia");
        assertTrue(ok(run("GROUP ADD;G01;ana@correo.com")));
        run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertSame(park.getUsers().findByEmail("ana@correo.com"), g01().getMembers().get(0));
        assertEquals("699999999", g01().getMembers().get(0).getPhone());
    }

    @Test
    void rejectsUnknownOrDuplicateMembersAndUnknownGroups() {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(error(run("GROUP ADD;G01;nadie@correo.com")));
        assertTrue(error(run("GROUP ADD;G01;ana@correo.com")));
        assertTrue(error(run("GROUP ADD;G99;ana@correo.com")));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void removesMembersAndDeletesGroupsWithoutDeletingUsers() {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(error(run("GROUP REMOVE;G01;pedro@correo.com")));
        assertTrue(ok(run("GROUP REMOVE;G01;ana@correo.com")));
        assertTrue(g01().getMembers().isEmpty());
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(ok(run("GROUP DELETE;G01")));
        assertNull(g01());
        assertTrue(error(run("GROUP DELETE;G01")));
        assertEquals(2, park.getUsers().getUsers().size());
    }

    @Test
    void restartRestoresMembershipWithoutDuplicates() throws IOException {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP CREATE;G02;Vacio");
        run("GROUP CREATE;G03;Borrado");
        run("GROUP DELETE;G03");

        restart();

        assertEquals(2, park.getUsers().getUsers().size());
        assertEquals(List.of(park.getUsers().findByEmail("ana@correo.com"),
                park.getUsers().findByEmail("pedro@correo.com")), g01().getMembers());
        assertTrue(park.getGroups().find("G02").getMembers().isEmpty());
        assertNull(park.getGroups().find("G03"));
    }

    @Test
    void deletedUserLeavesGroups() throws IOException {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(ok(run("USER DELETE;ana@correo.com;Clave1!")));
        assertTrue(g01().getMembers().isEmpty());
        restart();
        assertTrue(g01().getMembers().isEmpty());
    }
}
