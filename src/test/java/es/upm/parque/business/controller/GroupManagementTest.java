package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.UserField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GroupManagementTest {

    @TempDir
    Path dir;

    private TestContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ctx.user(1);
        ctx.user(2);
    }

    @Test
    void createsGroupsWithUniqueIds() {
        ok(ctx.groups().createGroup("G01", "Familia Garcia"));
        error(ctx.groups().createGroup("G01", "Otra"));
        error(ctx.groups().createGroup("", "Sin id"));
        error(ctx.groups().createGroup("G02", " "));
        assertEquals(1, ctx.groups().getGroups().size());
    }

    @Test
    void addsExistingUsersOnceAndSharesTheSameObjects() {
        ok(ctx.groups().createGroup("G01", "Familia Garcia"));
        ok(ctx.groups().addToGroup("G01", email(1)));
        error(ctx.groups().addToGroup("G01", email(1)));
        error(ctx.groups().addToGroup("G01", "nadie@correo.com"));
        error(ctx.groups().addToGroup("G99", email(1)));
        Group group = ctx.groups().find("G01");
        assertEquals(1, group.getMembers().size());
        assertSame(ctx.users().findByEmail(email(1)), group.getMembers().get(0));

        ok(ctx.users().updateUser(email(1), "Clave1!", UserField.NAME, "Ana Maria"));
        assertEquals("Ana Maria", group.getMembers().get(0).getName());
    }

    @Test
    void removesMembersAndDeletesGroups() {
        ctx.group("G01", 2);
        ok(ctx.groups().removeFromGroup("G01", email(1)));
        error(ctx.groups().removeFromGroup("G01", email(1)));
        ok(ctx.groups().deleteGroup("G01"));
        assertNull(ctx.groups().find("G01"));
        assertNotNull(ctx.users().findByEmail(email(2)));
        error(ctx.groups().deleteGroup("G01"));
    }

    @Test
    void groupsSurviveRestartLinkedToUsers() throws IOException {
        ctx.group("G01", 2);
        TestContext restarted = ctx.restart();
        Group group = restarted.groups().find("G01");
        assertEquals("Group G01", group.getName());
        assertEquals(2, group.getMembers().size());
        assertSame(restarted.users().findByEmail(email(2)), group.getMembers().get(1));
    }

    @Test
    void lockedWhileGroupTicketIsCurrentOrFuture() {
        ctx.group("G01", 1);
        ctx.user(3);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        error(ctx.groups().addToGroup("G01", email(2)));
        error(ctx.groups().removeFromGroup("G01", email(1)));
        error(ctx.groups().deleteGroup("G01"));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.groups().addToGroup("G01", email(2)));
        ok(ctx.park().setSystemDate("2026-10-16"));
        ok(ctx.groups().addToGroup("G01", email(2)));
        ok(ctx.groups().removeFromGroup("G01", email(1)));
        ok(ctx.groups().deleteGroup("G01"));
        assertNull(ctx.tickets().find("TG01"));
    }
}
