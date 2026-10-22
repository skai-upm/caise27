package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.command.GroupTicketCommandsTest.CREATE_LAURA;
import static es.upm.amusementpark.command.UserDeletionTest.CREATE_MARTA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.service.Park;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Group locks derived from active group tickets, and the deletion rules of
 * tickets, group tickets, groups and users.
 */
class GroupLockTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
        run(CREATE_MARTA);
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private List<String> members(String groupId) {
        return park.findGroup(groupId).getMembers().stream().map(u -> u.getEmail()).toList();
    }

    @Test
    void activeGroupTicketLocksTheGroup() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(park.isLocked(park.findGroup("G01")));
        assertTrue(run("GROUP ADD;G01;marta@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP DELETE;G01").startsWith("ERROR"));
        assertEquals(3, members("G01").size());
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void lockEndsWhenTicketsBecomeHistorical() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SYSTEM DATE;2027-04-16");
        assertFalse(park.isLocked(park.findGroup("G01")));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;laura@correo.com"));
        assertEquals(3, ((GroupTicket) park.findTicket("TG01")).getComposition().size());
        assertEquals("GROUP DELETED", run("GROUP DELETE;G01"));
        GroupTicket historical = (GroupTicket) park.findTicket("TG01");
        assertNull(historical.getGroup());
        assertEquals(3, historical.getComposition().size());

        Park reopened = TestParks.open(dir, LocalDate.of(2027, 4, 16));
        GroupTicket reloaded = (GroupTicket) reopened.findTicket("TG01");
        assertNull(reloaded.getGroup());
        assertEquals(3, reloaded.getComposition().size());
    }

    @Test
    void deletingFutureGroupTicketUnlocksTheGroup() {
        run("GROUPTICKET CREATE;TG02;G01;2027-04-20");
        assertTrue(run("GROUP REMOVE;G01;laura@correo.com").startsWith("ERROR"));
        assertEquals("GROUPTICKET DELETED", run("GROUPTICKET DELETE;TG02"));
        assertNull(park.findTicket("TG02"));
        assertNull(park.findTicket("TG02:12345678Z"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;laura@correo.com"));
    }

    @Test
    void remainingActiveTicketKeepsTheLock() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("GROUPTICKET CREATE;TG02;G01;2027-04-20");
        run("GROUPTICKET DELETE;TG02");
        assertTrue(run("GROUP ADD;G01;marta@correo.com").startsWith("ERROR"));
    }

    @Test
    void groupTicketsDatedTodayOrEarlierCannotBeDeleted() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertNotNull(park.findTicket("TG01"));
        assertTrue(run("GROUPTICKET DELETE;NOPE").startsWith("ERROR"));
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        assertTrue(run("GROUPTICKET DELETE;T002").startsWith("ERROR"));
        assertNotNull(park.findTicket("T002"));
    }

    @Test
    void ticketDeleteRejectsGroupTicketsAndComponents() {
        run("GROUPTICKET CREATE;TG02;G01;2027-04-20");
        String whole = run("TICKET DELETE;TG02");
        assertTrue(whole.startsWith("ERROR") && whole.contains("GROUPTICKET DELETE"));
        String component = run("TICKET DELETE;TG02:87654321X");
        assertTrue(component.startsWith("ERROR") && component.contains("TG02"));
        assertNotNull(park.findTicket("TG02:87654321X"));
        assertTrue(run("TICKET DELETE;NOPE").startsWith("ERROR"));
    }

    @Test
    void userInActiveGroupTicketCannotBeDeleted() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(run("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR"));
        assertNotNull(park.findUserByEmail("pedro@correo.com"));
    }

    @Test
    void ticketReactivatedByEarlierDateBlocksDeletion() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SYSTEM DATE;2027-04-16");
        run("GROUP REMOVE;G01;laura@correo.com");
        run("GROUP ADD;G01;marta@correo.com");
        run("GROUPTICKET CREATE;TG02;G01;2027-04-16");
        run("SYSTEM DATE;2027-04-14");
        // Laura is no longer a member of G01 but her TG01 ticket is active again.
        assertTrue(run("USER DELETE;laura@correo.com;Laura3$").startsWith("ERROR"));
    }

    @Test
    void deletedUserIsRemovedFromUnlockedGroups() throws Exception {
        run("GROUP CREATE;G02;Visita Mayo");
        run("GROUP ADD;G02;laura@correo.com");
        assertEquals("USER DELETED", run("USER DELETE;laura@correo.com;Laura3$"));
        assertTrue(park.findGroup("G02").isEmpty());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), members("G01"));
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X", "G02;Visita Mayo"),
                Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void failedUserDeletionRestoresMemberships() {
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertEquals(List.of("ana@correo.com", "pedro@correo.com", "laura@correo.com"), members("G01"));
    }

    @Test
    void failedGroupDeletionRestoresGroupAndReferences() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SYSTEM DATE;2027-04-16");
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("GROUP DELETE;G01").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertSame(park.findGroup("G01"), ((GroupTicket) park.findTicket("TG01")).getGroup());
    }

    @Test
    void sourceExampleGroupAndTicketSequence() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("SYSTEM PRICE SET TO 45.50 EUR", run("SYSTEM PRICE;45.50"));
        assertEquals("TICKET CREATED. Price: 45.50 EUR", run("TICKET CREATE;T002;marta@correo.com;2027-04-20"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertEquals("USER DELETED", run("USER DELETE;marta@correo.com;Marta4%"));
        assertEquals("GROUP CREATED", run("GROUP CREATE;G02;Visita Mayo"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G02;pedro@correo.com"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G02;laura@correo.com"));
        assertEquals("GROUPTICKET CREATED. Tickets: 2, discount: 0%, total: 91.00 EUR",
                run("GROUPTICKET CREATE;TG02;G02;2027-04-20"));
        assertEquals("GROUPTICKET DELETED", run("GROUPTICKET DELETE;TG02"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G02;laura@correo.com"));
        assertEquals("GROUP DELETED", run("GROUP DELETE;G02"));
        assertNull(park.findGroup("G02"));
    }
}
