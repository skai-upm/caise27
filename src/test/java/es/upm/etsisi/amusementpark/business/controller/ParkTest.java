package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.persistence.FileRepositories;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;

class ParkTest {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    static final LocalDate TOMORROW = TODAY.plusDays(1);

    @TempDir
    Path dir;

    ParkController park;

    static User user(int n) {
        String number = String.format("%08d", n);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        return new User("User" + n, "Surname", number + letter, "u" + n + "@correo.com", "Clave1!", "600000000",
                "1234567890123456");
    }

    @BeforeEach
    void setUp() throws IOException {
        park = ParkController.getInstance();
        park.configure(FileRepositories.in(dir));
        park.setCurrentDate(TODAY);
    }

    /** Creates n users and a group G01 holding all of them. */
    List<User> groupOf(int n) throws ParkException {
        park.createGroup("G01", "Familia");
        for (int i = 1; i <= n; i++) {
            User u = user(i);
            park.createUser(u);
            park.addToGroup("G01", u.getEmail());
        }
        return park.getUsers().getUsers();
    }

    // ---- F2-C01 -----------------------------------------------------------------------------

    @Test
    void singleInstanceWithStartDefaults() throws IOException {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        park.configure(FileRepositories.in(dir));
        assertEquals(LocalDate.now(ZoneId.systemDefault()), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void priceAppliesOnlyToNewTickets() throws ParkException {
        park.createUser(user(1));
        IndividualTicket first = park.createTicket("T001", "u1@correo.com", TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        IndividualTicket second = park.createTicket("T002", "u1@correo.com", TODAY);
        assertEquals(new BigDecimal("40.00"), first.getPrice());
        assertEquals(new BigDecimal("45.50"), second.getPrice());
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThrows(ParkException.class, () -> park.setBasePrice(BigDecimal.ZERO));
        assertThrows(ParkException.class, () -> park.setBasePrice(new BigDecimal("-3")));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    // ---- F2-C02 -----------------------------------------------------------------------------

    @Test
    void groupCreationAndMembership() throws ParkException {
        park.createUser(user(1));
        park.createGroup("G01", "Familia Garcia");
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
        park.addToGroup("G01", "u1@correo.com");
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "u1@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G99", "u1@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nobody@correo.com"));
        Group g = park.findGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(1, g.getMembers().size());
        park.removeFromGroup("G01", "u1@correo.com");
        assertTrue(g.getMembers().isEmpty());
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "u1@correo.com"));
    }

    @Test
    void activeGroupTicketLocksGroup() throws ParkException {
        groupOf(2);
        park.createUser(user(3));
        park.createGroupTicket("TG01", "G01", TODAY);
        Group g = park.findGroup("G01");
        assertTrue(park.isLocked(g));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "u3@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "u1@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertEquals(2, g.getMembers().size());

        park.setCurrentDate(TOMORROW);
        assertFalse(park.isLocked(g));
        park.addToGroup("G01", "u3@correo.com");
    }

    @Test
    void deletingFutureTicketUnlocksGroup() throws ParkException {
        groupOf(1);
        park.createGroupTicket("TG01", "G01", TOMORROW);
        assertTrue(park.isLocked(park.findGroup("G01")));
        park.deleteGroupTicket("TG01");
        assertFalse(park.isLocked(park.findGroup("G01")));
    }

    @Test
    void deletingGroupKeepsHistoricalTickets() throws ParkException {
        groupOf(2);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TOMORROW);
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertSame(ticket, park.findTicket("TG01"));
        assertNull(ticket.getGroup());
        assertEquals(2, ticket.getComposition().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    // ---- F2-C03 -----------------------------------------------------------------------------

    @Test
    void ticketCreationRules() throws ParkException {
        park.createUser(user(1));
        assertThrows(ParkException.class, () -> park.createTicket("T001", "u1@correo.com", TODAY.minusDays(1)));
        assertThrows(ParkException.class, () -> park.createTicket("T001", "nobody@correo.com", TODAY));
        IndividualTicket t = park.createTicket("T001", "u1@correo.com", TODAY);
        assertSame(park.getUsers().findByEmail("u1@correo.com"), t.getUser());
        assertThrows(ParkException.class, () -> park.createTicket("T001", "u1@correo.com", TOMORROW));
    }

    @Test
    void sharedIdentifierSpace() throws ParkException {
        groupOf(1);
        park.createGroupTicket("TG01", "G01", TODAY);
        String partId = GroupTicket.compositionIdentifier("TG01", user(1));
        assertThrows(ParkException.class, () -> park.createTicket("TG01", "u1@correo.com", TODAY));
        assertThrows(ParkException.class, () -> park.createTicket(partId, "u1@correo.com", TODAY));
        assertNotNull(park.findTicket(partId));
    }

    @Test
    void ticketDeletionRules() throws ParkException {
        park.createUser(user(1));
        park.createTicket("T001", "u1@correo.com", TODAY);
        park.createTicket("T003", "u1@correo.com", TOMORROW);
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        park.deleteTicket("T003");
        assertNull(park.findTicket("T003"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T999"));
    }

    @Test
    void compositionAndGroupTicketsCannotBeDeletedAsIndividualTickets() throws ParkException {
        groupOf(2);
        park.createGroupTicket("TG01", "G01", TOMORROW);
        String partId = GroupTicket.compositionIdentifier("TG01", user(1));
        assertThrows(ParkException.class, () -> park.deleteTicket(partId));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG01"));
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getComposition().size());
    }

    @Test
    void activeIndividualTicketBlocksUserDeletion() throws ParkException {
        park.createUser(user(1));
        park.createTicket("T001", "u1@correo.com", TODAY);
        assertThrows(ParkException.class, () -> park.deleteUser("u1@correo.com", "Clave1!"));
        park.setCurrentDate(TOMORROW);
        park.deleteUser("u1@correo.com", "Clave1!");
        assertNull(park.getUsers().findByEmail("u1@correo.com"));
        assertNotNull(park.findTicket("T001"));
    }

    @Test
    void wrongCredentialsDoNotDeleteUser() throws ParkException {
        park.createUser(user(1));
        assertThrows(ParkException.class, () -> park.deleteUser("u1@correo.com", "Wrong1!"));
        assertNotNull(park.getUsers().findByEmail("u1@correo.com"));
    }

    // ---- F2-C04 -----------------------------------------------------------------------------

    @Test
    void groupTicketComposition() throws ParkException {
        List<User> members = groupOf(2);
        GroupTicket t = park.createGroupTicket("TG01", "G01", TODAY);
        assertSame(park.findGroup("G01"), t.getGroup());
        assertEquals(List.of("TG01:" + members.get(0).getDni(), "TG01:" + members.get(1).getDni()),
                t.getComposition().stream().map(IndividualTicket::getIdentifier).toList());
        assertSame(members.get(0), t.getComposition().get(0).getUser());
    }

    @Test
    void groupTicketRejections() throws ParkException {
        park.createGroup("G00", "Vacio");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG00", "G00", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG00", "G99", TODAY));
        groupOf(1);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG00", "G01", TODAY.minusDays(1)));
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void derivedIdentifierCollisionCreatesNothing() throws ParkException {
        List<User> members = groupOf(2);
        park.createTicket("TG05:" + members.get(1).getDni(), members.get(1).getEmail(), TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG05", "G01", TODAY));
        assertNull(park.findTicket("TG05"));
        assertNull(park.findTicket("TG05:" + members.get(0).getDni()));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void groupTicketDeletion() throws ParkException {
        List<User> members = groupOf(2);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createGroupTicket("TG02", "G01", TOMORROW);
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        park.deleteGroupTicket("TG02");
        assertNull(park.findTicket("TG02"));
        assertNull(park.findTicket("TG02:" + members.get(0).getDni()));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG02"));
    }

    @Test
    void compositionSurvivesMembershipChange() throws ParkException {
        groupOf(2);
        GroupTicket t = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TOMORROW);
        park.removeFromGroup("G01", "u1@correo.com");
        assertEquals(2, t.getComposition().size());
    }

    @Test
    void userDeletionRespectsGroupTickets() throws ParkException {
        groupOf(2);
        park.createGroupTicket("TG01", "G01", TOMORROW);
        assertThrows(ParkException.class, () -> park.deleteUser("u1@correo.com", "Clave1!"));
        park.deleteGroupTicket("TG01");
        park.deleteUser("u1@correo.com", "Clave1!");
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void lockedGroupBlocksDeletionOfMemberWithoutTicket() throws ParkException {
        groupOf(1);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TOMORROW);
        park.createUser(user(2));
        park.addToGroup("G01", "u2@correo.com");
        // moving the date back makes TG01 active again: G01 is locked, but user 2 holds no ticket
        park.setCurrentDate(TODAY);
        assertThrows(ParkException.class, () -> park.deleteUser("u2@correo.com", "Clave1!"));
        assertTrue(park.findGroup("G01").hasMember(park.getUsers().findByEmail("u2@correo.com")));
    }

    // ---- F2-C05..C07 ------------------------------------------------------------------------

    @Test
    void attractionCreation() throws ParkException {
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", 5, AttractionType.GENERAL));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Otra", 0, AttractionType.GENERAL));
        Attraction<?> a = park.requireAttraction("A01");
        assertEquals(20, a.getAvailablePlaces());
        assertTrue(a.getOccupants().isEmpty());
        assertThrows(ParkException.class, () -> park.requireAttraction("A99"));
    }

    @Test
    void individualEntry() throws ParkException {
        park.createUser(user(1));
        park.createAttraction("A01", "Dragon Rojo", 1, AttractionType.GENERAL);
        park.createTicket("T001", "u1@correo.com", TODAY);
        park.createTicket("T002", "u1@correo.com", TOMORROW);
        assertThrows(ParkException.class, () -> park.enter("A01", "T002", false));
        assertThrows(ParkException.class, () -> park.enter("A01", "T999", false));
        assertThrows(ParkException.class, () -> park.enter("A99", "T001", false));
        assertEquals(1, park.enter("A01", "T001", false).users().size());
        Attraction<?> a = park.requireAttraction("A01");
        assertTrue(a.isInside(park.getUsers().findByEmail("u1@correo.com")));
        assertThrows(ParkException.class, () -> park.enter("A01", "T001", false));

        park.setCurrentDate(TOMORROW);
        assertEquals(1, a.getOccupants().size());
    }

    @Test
    void fullAttractionRejectsEntry() throws ParkException {
        park.createUser(user(1));
        park.createUser(user(2));
        park.createAttraction("A01", "Mini", 1, AttractionType.GENERAL);
        park.createTicket("T001", "u1@correo.com", TODAY);
        park.createTicket("T002", "u2@correo.com", TODAY);
        park.enter("A01", "T001", false);
        assertThrows(ParkException.class, () -> park.enter("A01", "T002", false));
        assertEquals(1, park.requireAttraction("A01").getOccupants().size());
    }

    @Test
    void groupEntryIsAllOrNothing() throws ParkException {
        groupOf(4);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createAttraction("A01", "Tres", 3, AttractionType.GENERAL);
        park.createAttraction("A02", "Cuatro", 4, AttractionType.GENERAL);
        assertThrows(ParkException.class, () -> park.enter("A01", "TG01", false));
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertEquals(4, park.enter("A02", "TG01", false).users().size());
    }

    @Test
    void exitReleasesOnePlace() throws ParkException {
        groupOf(2);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        park.enter("A01", "TG01", false);
        park.exit("A01", "u1@correo.com");
        Attraction<?> a = park.requireAttraction("A01");
        assertEquals(List.of("u2@correo.com"), a.getOccupants().stream().map(User::getEmail).toList());
        assertEquals(19, a.getAvailablePlaces());
        assertThrows(ParkException.class, () -> park.exit("A01", "u1@correo.com"));
        assertThrows(ParkException.class, () -> park.exit("A99", "u2@correo.com"));
    }

    @Test
    void compositionTicketAdmitsItsUser() throws ParkException {
        List<User> members = groupOf(2);
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        assertEquals(1, park.enter("A01", "TG01:" + members.get(0).getDni(), false).users().size());
    }

    @Test
    void deletedUserCannotEnterWithHistoricalTicket() throws ParkException {
        park.createUser(user(1));
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        park.createTicket("T001", "u1@correo.com", TODAY);
        park.setCurrentDate(TOMORROW);
        park.deleteUser("u1@correo.com", "Clave1!");
        park.setCurrentDate(TODAY);
        assertThrows(ParkException.class, () -> park.enter("A01", "T001", false));
    }

    @Test
    void deletedUserLeavesAttractions() throws ParkException {
        park.createUser(user(1));
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        park.createTicket("T001", "u1@correo.com", TODAY);
        park.enter("A01", "T001", false);
        park.setCurrentDate(TOMORROW);
        park.deleteUser("u1@correo.com", "Clave1!");
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
    }
}
