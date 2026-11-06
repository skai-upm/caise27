package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.Group;
import es.upm.park.persistence.repository.GroupRepository;
import es.upm.park.persistence.repository.UserRepository;

class GroupServiceTest {

    private final Set<String> lockedIds = new HashSet<>();
    private GroupRepository groups;
    private GroupService service;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        UserService userService = new UserService(users);
        userService.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        userService.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        groups = new GroupRepository();
        service = new GroupService(groups, users, g -> lockedIds.contains(g.getId()));
    }

    private void assertRejected(String message, Runnable action) {
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
    }

    @Test
    void createsEmptyGroupWithUniqueId() {
        Group g = service.create("G01", "Familia Garcia");
        assertTrue(g.getMemberDnis().isEmpty());
        assertRejected("group G01 already exists", () -> service.create("G01", "Otro"));
        assertEquals("Familia Garcia", service.find("G01").getName());
        assertRejected("group identifier and name are required", () -> service.create(" ", "X"));
        assertRejected("group identifier and name are required", () -> service.create("G02", ""));
        assertEquals(1, groups.findAll().size());
    }

    @Test
    void addsExistingUsersOnce() {
        service.create("G01", "F");
        service.addUser("G01", "ana@correo.com");
        service.addUser("G01", "pedro@correo.com");
        assertRejected("user ana@correo.com already in group G01", () -> service.addUser("G01", "ana@correo.com"));
        assertEquals(List.of("12345678Z", "87654321X"), service.find("G01").getMemberDnis());
        assertRejected("no group G99", () -> service.addUser("G99", "ana@correo.com"));
        assertRejected("no user with email nadie@correo.com", () -> service.addUser("G01", "nadie@correo.com"));
    }

    @Test
    void removesMembers() {
        service.create("G01", "F");
        service.addUser("G01", "ana@correo.com");
        service.removeUser("G01", "ana@correo.com");
        assertFalse(service.find("G01").getMemberDnis().contains("12345678Z"));
        assertRejected("user pedro@correo.com not in group G01", () -> service.removeUser("G01", "pedro@correo.com"));
    }

    @Test
    void lockedGroupCannotChangeOrBeDeleted() {
        service.create("G01", "F");
        service.addUser("G01", "ana@correo.com");
        lockedIds.add("G01");
        assertTrue(service.isLocked(service.find("G01")));
        assertRejected("group G01 has an active group ticket", () -> service.addUser("G01", "pedro@correo.com"));
        assertRejected("group G01 has an active group ticket", () -> service.removeUser("G01", "ana@correo.com"));
        assertRejected("group G01 has an active group ticket", () -> service.delete("G01"));
        assertEquals(List.of("12345678Z"), service.find("G01").getMemberDnis());
        lockedIds.clear();
        service.addUser("G01", "pedro@correo.com");
        service.delete("G01");
        assertRejected("no group G01", () -> service.find("G01"));
    }
}
