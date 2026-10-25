package es.upm.parque;

import static es.upm.parque.ParkTestSupport.email;
import static es.upm.parque.ParkTestSupport.error;
import static es.upm.parque.ParkTestSupport.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GroupManagementTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() throws IOException {
        park = ParkTestSupport.park(dir);
        ParkTestSupport.user(park, 1);
        ParkTestSupport.user(park, 2);
    }

    @Test
    void createsGroupsWithUniqueIds() {
        ok(park.createGroup("G01", "Familia Garcia"));
        error(park.createGroup("G01", "Otra"));
        error(park.createGroup("", "Sin id"));
        error(park.createGroup("G02", " "));
        assertEquals(1, park.getGroupManager().getGroups().size());
    }

    @Test
    void addsExistingUsersOnceAndSharesTheSameObjects() {
        ok(park.createGroup("G01", "Familia Garcia"));
        ok(park.addToGroup("G01", email(1)));
        error(park.addToGroup("G01", email(1)));
        error(park.addToGroup("G01", "nadie@correo.com"));
        error(park.addToGroup("G99", email(1)));
        Group group = park.getGroupManager().find("G01");
        assertEquals(1, group.getMembers().size());
        assertSame(park.getUserManager().findByEmail(email(1)), group.getMembers().get(0));

        ok(park.updateUser(email(1), "Clave1!", UserField.NAME, "Ana Maria"));
        assertEquals("Ana Maria", group.getMembers().get(0).getName());
    }

    @Test
    void removesMembersAndDeletesGroups() {
        ParkTestSupport.group(park, "G01", 2);
        ok(park.removeFromGroup("G01", email(1)));
        error(park.removeFromGroup("G01", email(1)));
        ok(park.deleteGroup("G01"));
        assertNull(park.getGroupManager().find("G01"));
        assertNotNull(park.getUserManager().findByEmail(email(2)));
        error(park.deleteGroup("G01"));
    }

    @Test
    void groupsSurviveRestartLinkedToUsers() throws IOException {
        ParkTestSupport.group(park, "G01", 2);
        ParkController restarted = ParkTestSupport.restart(dir);
        Group group = restarted.getGroupManager().find("G01");
        assertEquals("Group G01", group.getName());
        assertEquals(2, group.getMembers().size());
        assertSame(restarted.getUserManager().findByEmail(email(2)), group.getMembers().get(1));
    }

    @Test
    void lockedWhileGroupTicketIsCurrentOrFuture() {
        ParkTestSupport.group(park, "G01", 1);
        ParkTestSupport.user(park, 3);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        error(park.addToGroup("G01", email(2)));
        error(park.removeFromGroup("G01", email(1)));
        error(park.deleteGroup("G01"));
        ok(park.setSystemDate("2026-10-15"));
        error(park.addToGroup("G01", email(2)));
        ok(park.setSystemDate("2026-10-16"));
        ok(park.addToGroup("G01", email(2)));
        ok(park.removeFromGroup("G01", email(1)));
        ok(park.deleteGroup("G01"));
        assertNull(park.getTicketManager().find("TG01"));
    }
}
