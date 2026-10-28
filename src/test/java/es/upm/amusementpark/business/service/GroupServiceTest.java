package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

class GroupServiceTest {

    private final UserService users = new UserService(new UserRepository(), new InMemoryUserStorage());
    private final Set<String> groupsWithActiveTicket = new HashSet<>();
    private LocalDate checkedDate;
    private final GroupService groups = new GroupService(new GroupRepository(), users, (id, today) -> {
        checkedDate = today;
        return groupsWithActiveTicket.contains(id);
    }, () -> LocalDate.of(2027, 4, 15));

    @BeforeEach
    void users() throws ParkException {
        users.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        users.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    }

    private void assertRejected(String reason, org.junit.jupiter.api.function.Executable op) {
        ParkException e = assertThrows(ParkException.class, op);
        assertTrue(e.getMessage().contains(reason), e.getMessage());
    }

    @Test
    void createEmptyGroupAndAddMembers() throws ParkException {
        groups.createGroup("G01", "Familia Garcia");
        assertTrue(groups.findById("G01").orElseThrow().getMembers().isEmpty());
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        assertEquals(List.of("12345678Z", "87654321X"), groups.findById("G01").orElseThrow().getMembers().stream().map(es.upm.amusementpark.business.model.User::getDni).toList());
        assertEquals("Familia Garcia", groups.findById("G01").orElseThrow().getName());
    }

    @Test
    void creationRules() throws ParkException {
        groups.createGroup("G01", "Familia Garcia");
        assertRejected("already exists", () -> groups.createGroup("G01", "Otro"));
        assertRejected("required", () -> groups.createGroup("", "Otro"));
        assertRejected("required", () -> groups.createGroup("G02", ""));
        assertEquals("Familia Garcia", groups.findById("G01").orElseThrow().getName());
    }

    @Test
    void membershipRules() throws ParkException {
        groups.createGroup("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        assertRejected("already belongs", () -> groups.addMember("G01", "ana@correo.com"));
        assertRejected("User not found", () -> groups.addMember("G01", "nadie@correo.com"));
        assertRejected("Group not found", () -> groups.addMember("G99", "ana@correo.com"));
        assertRejected("does not belong", () -> groups.removeMember("G01", "pedro@correo.com"));
        groups.removeMember("G01", "ana@correo.com");
        assertTrue(groups.findById("G01").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void membershipFollowsTheUserAfterEmailChange() throws ParkException {
        groups.createGroup("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        users.updateUser("ana@correo.com", "Clave1!", es.upm.amusementpark.business.model.UserField.EMAIL, "ana2@correo.com");
        groups.removeMember("G01", "ana2@correo.com");
        assertFalse(groups.findById("G01").orElseThrow().hasMember(users.findByDni("12345678Z").orElseThrow()));
    }

    @Test
    void lockIsDerivedFromActiveTicketsOnEveryCheck() throws ParkException {
        groups.createGroup("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groupsWithActiveTicket.add("G01");
        assertTrue(groups.isLocked(groups.findById("G01").orElseThrow()));
        assertEquals(LocalDate.of(2027, 4, 15), checkedDate);
        assertRejected("locked", () -> groups.addMember("G01", "pedro@correo.com"));
        assertRejected("locked", () -> groups.removeMember("G01", "ana@correo.com"));
        assertRejected("active group ticket", () -> groups.deleteGroup("G01"));
        groupsWithActiveTicket.clear();
        groups.addMember("G01", "pedro@correo.com");
        groups.deleteGroup("G01");
        assertTrue(groups.findById("G01").isEmpty());
    }

    @Test
    void deleteUnknownGroupFails() {
        assertRejected("Group not found", () -> groups.deleteGroup("G01"));
    }

    @Test
    void deletedUserLeavesAllGroups() throws ParkException {
        groups.createGroup("G01", "A");
        groups.createGroup("G02", "B");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G02", "ana@correo.com");
        var ana = users.findByDni("12345678Z").orElseThrow();
        groups.removeUserFromAllGroups(ana);
        assertTrue(groups.findAll().stream().noneMatch(g -> g.hasMember(ana)));
    }
}
