package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupServiceTest {

    private final ParkController park = ParkController.getInstance();
    private GroupService groups;
    private UserService users;
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() throws UserOperationException {
        UserRepository userRepository = new UserRepository();
        park.initialize(userRepository, new GroupRepository(), LocalDate.of(2026, 10, 27));
        users = new UserService(userRepository);
        groups = new GroupService();
        ana = users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        pedro = users.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private Group g01() {
        return park.getGroups().findById("G01").orElseThrow();
    }

    @Test
    void groupIsCreatedEmptyAndFilledProgressively() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        assertTrue(g01().getMembers().isEmpty());
        assertEquals("Familia Garcia", g01().getName());
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
        assertEquals(2, g01().getMembers().size());
        assertTrue(g01().contains(ana) && g01().contains(pedro));
    }

    @Test
    void duplicateIdentifierIsRejected() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        assertThrows(GroupOperationException.class, () -> groups.createGroup("G01", "Otro"));
        assertEquals("Familia Garcia", g01().getName());
    }

    @Test
    void duplicateMemberIsRejected() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        assertThrows(GroupOperationException.class, () -> groups.addUser("G01", "ana@correo.com"));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void unknownUserOrGroupIsRejected() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        assertThrows(GroupOperationException.class, () -> groups.addUser("G01", "nadie@correo.com"));
        assertThrows(GroupOperationException.class, () -> groups.addUser("G99", "ana@correo.com"));
        assertThrows(GroupOperationException.class, () -> groups.deleteGroup("G99"));
    }

    @Test
    void memberCanBeRemovedButNotIfAbsent() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        groups.removeUser("G01", "ana@correo.com");
        assertFalse(g01().contains(ana));
        assertThrows(GroupOperationException.class, () -> groups.removeUser("G01", "pedro@correo.com"));
    }

    @Test
    void groupWithoutTicketsCanBeDeleted() throws GroupOperationException {
        groups.createGroup("G01", "Familia Garcia");
        groups.deleteGroup("G01");
        assertTrue(park.getGroups().findById("G01").isEmpty());
    }

    @Test
    void deletedUserLeavesGroups() throws Exception {
        groups.createGroup("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        users.deleteUser("ana@correo.com", "Clave1!");
        assertFalse(g01().contains(ana));
    }
}
