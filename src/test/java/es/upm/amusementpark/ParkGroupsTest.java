package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkGroupsTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() {
        park.reset();
        ana = new User("ana@correo.com", "12345678Z");
        pedro = new User("pedro@correo.com", "87654321X");
        park.registerUser(ana);
        park.registerUser(pedro);
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void userDirectoryLookup() {
        assertSame(ana, park.findUser("ana@correo.com"));
        assertNull(park.findUser("nadie@correo.com"));
    }

    @Test
    void createGroupAndAddUsersProgressively() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\n"
                + "GROUP ADD;G01;ana@correo.com\n"
                + "GROUP ADD;G01;pedro@correo.com\n");
        assertTrue(out.contains("GROUP CREATED"));
        assertEquals(2, out.split("USER ADDED TO GROUP", -1).length - 1);
        Group group = park.findGroup("G01");
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.hasMember(ana));
        assertTrue(group.hasMember(pedro));
    }

    @Test
    void duplicateGroupIdentifierRejected() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\nGROUP CREATE;G01;Otro\n");
        assertTrue(out.contains("Error: group already exists: G01"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void blankIdentifierOrNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> park.createGroup(" ", "Name"));
        assertThrows(IllegalArgumentException.class, () -> park.createGroup("G02", ""));
        assertTrue(park.getGroups().isEmpty());
    }

    @Test
    void sameUserTwiceRejected() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\n"
                + "GROUP ADD;G01;ana@correo.com\n"
                + "GROUP ADD;G01;ana@correo.com\n");
        assertTrue(out.contains("Error: user ana@correo.com already belongs to group G01"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void unknownUserOrGroupRejected() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\n"
                + "GROUP ADD;G01;nadie@correo.com\n"
                + "GROUP ADD;G99;ana@correo.com\n");
        assertTrue(out.contains("Error: user not found: nadie@correo.com"));
        assertTrue(out.contains("Error: group not found: G99"));
        assertTrue(park.findGroup("G01").getMembers().isEmpty());
    }

    @Test
    void removeMemberAndNonMember() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\n"
                + "GROUP ADD;G01;ana@correo.com\n"
                + "GROUP REMOVE;G01;ana@correo.com\n"
                + "GROUP REMOVE;G01;pedro@correo.com\n");
        assertTrue(out.contains("USER REMOVED FROM GROUP"));
        assertTrue(out.contains("Error: user pedro@correo.com does not belong to group G01"));
        assertFalse(park.findGroup("G01").hasMember(ana));
    }

    @Test
    void deleteGroupWithoutTickets() throws IOException {
        String out = run("GROUP CREATE;G01;Familia Garcia\nGROUP DELETE;G01\nGROUP DELETE;G99\n");
        assertTrue(out.contains("GROUP DELETED"));
        assertTrue(out.contains("Error: group not found: G99"));
        assertNull(park.findGroup("G01"));
    }

    @Test
    void groupWithoutTicketsIsNotLocked() {
        Group group = park.createGroup("G01", "Familia Garcia");
        assertFalse(park.hasActiveGroupTicket(group));
        park.setCurrentDate("2027-04-15");
        assertFalse(park.hasActiveGroupTicket(group));
    }

    @Test
    void wrongArgumentCountsRejected() throws IOException {
        String out = run("GROUP CREATE;G01\nGROUP ADD;G01\nGROUP REMOVE;G01;a;b\nGROUP DELETE\n");
        assertEquals(4, out.split("Error: invalid arguments", -1).length - 1);
        assertTrue(park.getGroups().isEmpty());
    }

    @Test
    void helpListsGroupCommands() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("GROUP CREATE;<Identifier>;<Name>"));
        assertTrue(out.contains("GROUP ADD;<Identifier>;<Email>"));
        assertTrue(out.contains("GROUP REMOVE;<Identifier>;<Email>"));
        assertTrue(out.contains("GROUP DELETE;<Identifier>"));
    }
}
