package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.persistence.FlyWorkspace;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();
    private ParkController park;

    @BeforeEach
    void setUp() throws ParkException {
        park = restart();
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createAttraction("A01", "Dragon Rojo", "20", null);
    }

    private ParkController restart() throws ParkException {
        return TestSupport.initialize(dir, new PrintStream(warnings, true));
    }

    @Test
    void reconstructsGroupsTicketsAndAttractions() throws ParkException {
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.setPrice("45.50");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.enter("A01", "T001", null);

        ParkController reloaded = restart();
        User ana = reloaded.getUsers().get(0);
        Group group = reloaded.getGroups().get(0);
        assertEquals("Familia Garcia", group.getName());
        assertEquals(reloaded.getUsers(), group.getMembers());

        IndividualTicket ticket = (IndividualTicket) reloaded.findTicket("T001");
        assertSame(ana, ticket.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), ticket.getDate());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());

        GroupTicket groupTicket = (GroupTicket) reloaded.findTicket("TG01");
        assertSame(group, groupTicket.getGroup());
        assertEquals(new BigDecimal("91.00"), groupTicket.getPrice());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                groupTicket.getComposition().stream().map(Ticket::getIdentifier).toList());
        assertSame(ana, groupTicket.getComposition().get(0).getUser());
        assertTrue(reloaded.isLocked(group));

        Attraction attraction = reloaded.requireAttraction("A01");
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @Test
    void historicalTicketsSurviveUserAndGroupDeletion() throws ParkException {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createTicket("T001", "pedro@correo.com", "2027-04-15");
        park.setDate("2027-04-16");
        park.deleteGroup("G01");
        park.deleteUser("pedro@correo.com", "Otra2#");

        ParkController reloaded = restart();
        assertEquals(1, reloaded.getUsers().size());
        GroupTicket groupTicket = (GroupTicket) reloaded.findTicket("TG01");
        assertNull(groupTicket.getGroup());
        assertEquals(new BigDecimal("80.00"), groupTicket.getPrice());
        User detached = groupTicket.getComposition().get(1).getUser();
        assertEquals("87654321X", detached.getDni());
        assertEquals("pedro@correo.com", detached.getEmail());
        assertEquals("Pedro", detached.getFirstName());
        assertFalse(reloaded.getUsers().contains(detached));
        assertSame(detached, ((IndividualTicket) reloaded.findTicket("T001")).getUser());
    }

    @Test
    void skipsMalformedRecordsWithWarnings() throws Exception {
        Files.write(dir.resolve("groups.txt"), List.of("G01;Familia;12345678Z;00000000T", "", ";nameless", "G01;dup"));
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;20", "A02;Bad;0", "A03;Short", "A01;Dup;3"));
        Files.write(dir.resolve("tickets.txt"), List.of(
                "IT;T001;2027-04-15;40.00;0;;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "",
                "IT;T001;2027-04-15;40.00;0;;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "IT;T002;bad-date;40.00;0;;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "IT;T003;2027-04-15;40.00;2;;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "IT;T004;2027-04-15;40.00;0;;12345678A;ana@correo.com;Ana;Garcia Lopez",
                "IT;X:1;2027-04-15;40.00;0;NOPE;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "GT;EMPTY;2027-04-15;G01",
                "XX;T005;2027-04-15"));

        ParkController reloaded = restart();
        assertEquals(1, reloaded.getGroups().size());
        assertEquals(1, reloaded.getGroups().get(0).getMembers().size());
        assertEquals(1, reloaded.getAttractions().size());
        assertEquals(1, reloaded.getTickets().size());
        String warningText = warnings.toString();
        assertTrue(warningText.contains("unknown member 00000000T"));
        assertTrue(warningText.contains("invalid group record at line 3"));
        assertTrue(warningText.contains("invalid attraction record at line 2"));
        assertTrue(warningText.contains("invalid ticket record at line 3"));
        assertTrue(warningText.contains("invalid ticket record at line 7"));
        assertTrue(warningText.contains("group ticket EMPTY without composition"));
    }

    @Test
    void flyWorkspaceIsUniqueAndRemoved() throws ParkException {
        LocalDateTime now = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        FlyWorkspace first = FlyWorkspace.create(dir.resolve("tmp"), now);
        FlyWorkspace second = FlyWorkspace.create(dir.resolve("tmp"), now);
        assertEquals(dir.resolve("tmp").resolve("20260826_190315"), first.getDirectory());
        assertNotSame(first.getDirectory(), second.getDirectory());
        assertEquals(dir.resolve("tmp").resolve("20260826_190315_1"), second.getDirectory());

        ParkController controller = TestSupport.initialize(first.getDirectory());
        controller.createGroup("G01", "Fly");
        assertTrue(Files.exists(first.getDirectory().resolve("groups.txt")));

        first.close();
        assertFalse(Files.exists(first.getDirectory()));
        assertTrue(Files.exists(dir.resolve("tmp")));
        second.close();
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
