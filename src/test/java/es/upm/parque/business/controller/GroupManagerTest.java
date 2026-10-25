package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.TextFileGroupRepository;
import es.upm.parque.persistence.TextFileUserRepository;

class GroupManagerTest {

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;

    @BeforeEach
    void setUp() throws IOException {
        users = UserManager.initialize(new TextFileUserRepository(dir.resolve("users.txt")));
        users.create(Fixtures.ana());
        users.create(Fixtures.pedro());
        groups = GroupManager.initialize(new TextFileGroupRepository(dir.resolve("groups.txt")), users);
    }

    @Test
    void createsGroups() {
        assertEquals(GroupManager.GROUP_CREATED, groups.create("G01", "Familia Garcia"));
        assertEquals("Familia Garcia", groups.find("G01").getName());
        assertEquals(List.of(), groups.find("G01").getMembers());
        assertEquals(GroupManager.DUPLICATE_GROUP, groups.create("G01", "Otra"));
        assertEquals(UserManager.EMPTY_FIELD, groups.create("", "Otra"));
        assertEquals(UserManager.EMPTY_FIELD, groups.create("G02", " "));
    }

    @Test
    void addsExistingUsersOnlyOnce() {
        groups.create("G01", "Familia Garcia");
        assertEquals(GroupManager.MEMBER_ADDED, groups.addMember("G01", "ana@correo.com"));
        assertEquals(GroupManager.ALREADY_MEMBER, groups.addMember("G01", "ANA@correo.com"));
        assertEquals(GroupManager.USER_NOT_FOUND, groups.addMember("G01", "nadie@correo.com"));
        assertEquals(GroupManager.GROUP_NOT_FOUND, groups.addMember("G99", "ana@correo.com"));
        assertEquals(1, groups.find("G01").getMembers().size());
        assertSame(users.findByEmail("ana@correo.com"), groups.find("G01").getMembers().get(0));
    }

    @Test
    void membersReflectUserChanges() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        assertEquals("Ana Maria", groups.find("G01").getMembers().get(0).getName());
    }

    @Test
    void removesMembers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertEquals(GroupManager.NOT_MEMBER, groups.removeMember("G01", "pedro@correo.com"));
        assertEquals(GroupManager.NOT_MEMBER, groups.removeMember("G01", "nadie@correo.com"));
        assertEquals(GroupManager.GROUP_NOT_FOUND, groups.removeMember("G99", "ana@correo.com"));
        assertEquals(GroupManager.MEMBER_REMOVED, groups.removeMember("G01", "ana@correo.com"));
        assertEquals(List.of(), groups.find("G01").getMembers());
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void deletesGroupsButNotUsers() {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertEquals(GroupManager.GROUP_DELETED, groups.delete("G01"));
        assertEquals(GroupManager.GROUP_NOT_FOUND, groups.delete("G01"));
        assertNull(groups.find("G01"));
        assertNotNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void restartRestoresGroupsWithSameUserObjects() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        UserManager restartedUsers = UserManager.initialize(new TextFileUserRepository(dir.resolve("users.txt")));
        GroupManager restarted = GroupManager.initialize(new TextFileGroupRepository(dir.resolve("groups.txt")), restartedUsers);
        Group g01 = restarted.find("G01");
        assertEquals(2, g01.getMembers().size());
        assertSame(restartedUsers.findByEmail("ana@correo.com"), g01.getMembers().get(0));
        assertSame(restartedUsers.findByEmail("pedro@correo.com"), g01.getMembers().get(1));
    }

    @Test
    void deletedUserLeavesAllGroups() throws IOException {
        groups.create("G01", "Familia Garcia");
        groups.create("G02", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        groups.addMember("G02", "pedro@correo.com");
        User ana = users.findByEmail("ana@correo.com");
        groups.removeUserFromAll(ana);
        assertEquals(List.of(), groups.find("G01").getMembers());
        assertEquals(1, groups.find("G02").getMembers().size());
        assertEquals(List.of("G01;Familia Garcia;", "G02;Amigos;87654321X"),
                Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = Files.createFile(dir.resolve("blocked"));
        GroupManager broken = GroupManager.initialize(new TextFileGroupRepository(blocked.resolve("groups.txt")), users);
        assertEquals(UserManager.STORAGE_FAILURE, broken.create("G01", "Familia"));
    }
}
