package es.upm.etsisi.amusementpark.park;

import static es.upm.etsisi.amusementpark.park.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserException;
import es.upm.etsisi.amusementpark.user.UserField;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParkTest {

    @TempDir
    Path dir;

    private Park park;

    @BeforeEach
    void setUp() throws Exception {
        park = ParkFixture.reset(dir);
        park.createUser(ParkFixture.ana());
        park.createUser(ParkFixture.pedro());
    }

    private Group group(String id, User... members) throws Exception {
        park.createGroup(id, "Group " + id);
        for (User m : members) {
            park.addToGroup(id, m.getEmail());
        }
        return park.findGroup(id).orElseThrow();
    }

    private User ana() {
        return park.getUsers().get(0);
    }

    // ---- singleton and settings ----

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> c : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void initialDateAndPrice() {
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void priceChangeAffectsOnlyLaterTickets() throws Exception {
        IndividualTicket first = park.createTicket("T1", "ana@correo.com", TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        IndividualTicket second = park.createTicket("T2", "ana@correo.com", TODAY);
        assertEquals(new BigDecimal("40.00"), first.getPrice());
        assertEquals(new BigDecimal("45.50"), second.getPrice());
        assertThrows(ParkException.class, () -> park.setBasePrice(BigDecimal.ZERO));
        assertThrows(ParkException.class, () -> park.setBasePrice(null));
    }

    @Test
    void priceAndDateResetOnInitialize() throws Exception {
        park.setBasePrice(new BigDecimal("50.00"));
        park.setCurrentDate(TODAY.plusDays(3));
        park = ParkFixture.reset(dir);
        assertEquals(Park.INITIAL_PRICE, park.getBasePrice());
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals(2, park.getUsers().size());
    }

    // ---- groups ----

    @Test
    void groupMembership() throws Exception {
        Group g = group("G01", ana());
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
        assertThrows(ParkException.class, () -> park.createGroup("", "X"));
        assertThrows(ParkException.class, () -> park.createGroup("G02", " "));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "pedro@correo.com"));
        assertEquals(1, g.getMembers().size());
        park.removeFromGroup("G01", "ana@correo.com");
        assertTrue(g.getMembers().isEmpty());
        assertEquals("Group G01", g.getName());
    }

    @Test
    void groupLockDerivedFromActiveGroupTickets() throws Exception {
        Group g = group("G01", ana());
        park.createGroupTicket("TG01", "G01", TODAY);
        assertTrue(park.isLocked(g));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        park.setCurrentDate(TODAY.plusDays(1));
        assertFalse(park.isLocked(g));
        park.addToGroup("G01", "pedro@correo.com");
        assertEquals(1, ((GroupTicket) park.findTicket("TG01").orElseThrow()).getIndividualTickets().size());
    }

    @Test
    void deletingOnlyFutureGroupTicketUnlocksGroup() throws Exception {
        Group g = group("G01", ana());
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(2));
        park.createGroupTicket("TG02", "G01", TODAY.plusDays(3));
        park.deleteGroupTicket("TG01");
        assertTrue(park.isLocked(g));
        park.deleteGroupTicket("TG02");
        assertFalse(park.isLocked(g));
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
        park.removeFromGroup("G01", "ana@correo.com");
    }

    @Test
    void deleteGroupKeepsHistoricalTicketWithoutGroup() throws Exception {
        group("G01", ana());
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G01");
        assertTrue(park.findGroup("G01").isEmpty());
        assertNull(ticket.getGroup());
        assertSame(ticket, park.findTicket("TG01").orElseThrow());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
    }

    // ---- tickets ----

    @Test
    void individualTicketRules() throws Exception {
        assertThrows(ParkException.class, () -> park.createTicket("T0", "ana@correo.com", TODAY.minusDays(1)));
        assertThrows(ParkException.class, () -> park.createTicket("T0", "nadie@correo.com", TODAY));
        assertThrows(ParkException.class, () -> park.createTicket(" ", "ana@correo.com", TODAY));
        IndividualTicket today = park.createTicket("T1", "ana@correo.com", TODAY);
        assertSame(ana(), today.getUser());
        assertThrows(ParkException.class, () -> park.createTicket("T1", "pedro@correo.com", TODAY));
        park.createTicket("T2", "ana@correo.com", TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.deleteTicket("T1"));
        assertThrows(ParkException.class, () -> park.deleteTicket("NOPE"));
        park.deleteTicket("T2");
        assertTrue(park.findTicket("T2").isEmpty());
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void ticketDeleteRejectsGroupTicketsAndConstituents() throws Exception {
        group("G01", ana());
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(5));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG01"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG01:12345678Z"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01:12345678Z"));
        assertThrows(ParkException.class, () -> park.createTicket("TG01:12345678Z", "pedro@correo.com", TODAY));
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void groupTicketDiscounts(int members, String total) throws Exception {
        park.createGroup("G", "Big");
        for (int i = 1; i <= members; i++) {
            User u = ParkFixture.user(i);
            park.createUser(u);
            park.addToGroup("G", u.getEmail());
        }
        GroupTicket ticket = park.createGroupTicket("TG", "G", TODAY);
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getIndividualTickets().size());
        assertEquals("TG:" + ParkFixture.user(1).getDni(), ticket.getIndividualTickets().get(0).getId());
    }

    @Test
    void groupTicketRejections() throws Exception {
        park.createGroup("G0", "Empty");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG", "G0", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG", "G9", TODAY));
        group("G01", ana(), park.getUsers().get(1));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG", "G01", TODAY.minusDays(1)));
        park.createTicket("TG01:87654321X", "pedro@correo.com", TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", TODAY));
        assertTrue(park.findTicket("TG01").isEmpty());
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
        park.createTicket("T5", "ana@correo.com", TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("T5", "G01", TODAY));
        assertEquals(2, park.getTickets().size());
    }

    // ---- user deletion ----

    @Test
    void userWithActiveTicketCannotBeDeleted() throws Exception {
        park.createTicket("T1", "ana@correo.com", TODAY);
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertEquals(1, park.getUsers().size());
        IndividualTicket history = (IndividualTicket) park.findTicket("T1").orElseThrow();
        assertEquals("12345678Z", history.getUser().getDni());
    }

    @Test
    void userWithActiveGroupTicketCannotBeDeleted() throws Exception {
        group("G01", ana());
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(1));
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertEquals(2, park.getUsers().size());
    }

    @Test
    void lockedGroupMembershipBlocksDeletionEvenWithoutOwnTicket() throws Exception {
        Group g = group("G01", ana());
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(1));
        park.setCurrentDate(TODAY.plusDays(2));
        park.addToGroup("G01", "pedro@correo.com");
        park.setCurrentDate(TODAY);
        assertTrue(park.isLocked(g));
        assertThrows(UserException.class, () -> park.deleteUser("pedro@correo.com", "Otra2#"));
        assertTrue(g.getMembers().stream().anyMatch(u -> u.getEmail().equals("pedro@correo.com")));
    }

    @Test
    void deletionRemovesFromGroupsAndAttractions() throws Exception {
        Group g = group("G01", ana(), park.getUsers().get(1));
        park.createAttraction("A01", "Dragon", 5);
        park.createTicket("T1", "ana@correo.com", TODAY);
        park.enter("A01", "T1");
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertEquals(1, g.getMembers().size());
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertThrows(UserException.class, () -> park.deleteUser("pedro@correo.com", "bad"));
        assertThrows(UserException.class, () -> park.deleteUser("nadie@correo.com", "x"));
    }

    @Test
    void usersUpdateThroughPark() throws Exception {
        park.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        assertEquals("699999999", ana().getPhone());
    }

    // ---- attractions ----

    @Test
    void attractionCreation() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", 20);
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", 5));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Noria", 0));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "", 3));
        assertThrows(ParkException.class, () -> park.requireAttraction("A09"));
        Attraction a = park.requireAttraction("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertEquals(1, park.getAttractions().size());
    }

    @Test
    void individualEntryRules() throws Exception {
        park.createAttraction("A01", "Dragon", 1);
        park.createTicket("T1", "ana@correo.com", TODAY);
        park.createTicket("T2", "pedro@correo.com", TODAY);
        park.createTicket("T3", "pedro@correo.com", TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.enter("A01", "T3"));
        assertThrows(ParkException.class, () -> park.enter("A01", "T9"));
        assertThrows(ParkException.class, () -> park.enter("A09", "T1"));
        assertEquals(List.of(ana()), park.enter("A01", "T1"));
        assertThrows(ParkException.class, () -> park.enter("A01", "T1"));
        assertThrows(ParkException.class, () -> park.enter("A01", "T2"));
        park.setCurrentDate(TODAY.plusDays(1));
        assertEquals(List.of(ana()), park.requireAttraction("A01").getOccupants());
    }

    @Test
    void groupEntryIsAtomicAndExitIsIndividual() throws Exception {
        park.createUser(ParkFixture.user(1));
        park.createUser(ParkFixture.user(2));
        group("G01", ana(), park.getUsers().get(1), park.getUsers().get(2), park.getUsers().get(3));
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createAttraction("A01", "Small", 3);
        park.createAttraction("A02", "Big", 20);
        assertThrows(ParkException.class, () -> park.enter("A01", "TG01"));
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertEquals(4, park.enter("A02", "TG01").size());
        park.exit("A02", "ana@correo.com");
        Attraction a = park.requireAttraction("A02");
        assertEquals(3, a.getOccupants().size());
        assertEquals(17, a.getFreePlaces());
        assertThrows(ParkException.class, () -> park.exit("A02", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.exit("A09", "ana@correo.com"));
        assertEquals(List.of(ana()), park.enter("A01", "TG01:12345678Z"));
    }

    // ---- persistence failure ----

    @Test
    void saveFailureUndoesInMemoryChange() throws Exception {
        Path tickets = dir.resolve("tickets.txt");
        Files.deleteIfExists(tickets);
        Files.createDirectories(tickets.resolve("blocker"));
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "X", 3));
        assertTrue(park.getAttractions().isEmpty());
        assertThrows(ParkException.class, () -> park.createGroup("G01", "X"));
        assertTrue(park.getGroups().isEmpty());
        assertThrows(ParkException.class, () -> park.createTicket("T1", "ana@correo.com", TODAY));
        assertTrue(park.getTickets().isEmpty());
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertEquals(2, park.getUsers().size());
        assertFalse(Files.readString(dir.resolve("attractions.txt")).contains("A01"));
    }

    @Test
    void failedUserDeletionRestoresGroupsAndAttractions() throws Exception {
        Group g = group("G01", ana());
        park.createAttraction("A01", "X", 3);
        park.createTicket("T1", "ana@correo.com", TODAY);
        park.enter("A01", "T1");
        park.setCurrentDate(TODAY.plusDays(1));
        Path tickets = dir.resolve("tickets.txt");
        Files.delete(tickets);
        Files.createDirectories(tickets.resolve("blocker"));
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertEquals(List.of(ana()), g.getMembers());
        assertEquals(List.of(ana()), park.requireAttraction("A01").getOccupants());
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertEquals(1, g.getMembers().size());
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "pedro@correo.com"));
        assertEquals(1, g.getMembers().size());
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertTrue(park.findGroup("G01").isPresent());
    }
}
