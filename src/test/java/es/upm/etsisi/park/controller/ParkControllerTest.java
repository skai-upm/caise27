package es.upm.etsisi.park.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.service.UserException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    private static final String TODAY = "2027-04-15";
    private static final String FUTURE = "2027-04-20";

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        configure();
        park.setCurrentDate(TODAY);
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "634567890",
                "1111222233334444");
    }

    private void configure() {
        park.configure(ParkStorage.inDirectory(dir, new PrintStream(new ByteArrayOutputStream())));
    }

    private Group groupWith(String id, String... emails) {
        Group group = park.createGroup(id, "Group " + id);
        for (String email : emails) {
            park.addToGroup(id, email);
        }
        return group;
    }

    private User user(String email) {
        return park.users().findByEmail(email).orElseThrow();
    }

    // ----- Singleton and settings -----

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void configureResetsDateAndPrice() {
        park.setCurrentPrice("45.50");
        configure();
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void setsDateAndPrice() {
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertEquals(new BigDecimal("45.50"), park.setCurrentPrice("45.50"));
        assertEquals(new BigDecimal("12.00"), park.setCurrentPrice("12"));
    }

    @Test
    void rejectsInvalidDateAndPrice() {
        assertThrows(UserException.class, () -> park.setCurrentDate("2027-13-45"));
        assertThrows(UserException.class, () -> park.setCurrentDate("15/04/2027"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        for (String price : new String[] {"-3", "0", "abc", "1.234", ""}) {
            assertThrows(UserException.class, () -> park.setCurrentPrice(price), price);
        }
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    // ----- Groups -----

    @Test
    void groupMembership() {
        Group group = groupWith("G01", "ana@correo.com", "pedro@correo.com");
        assertEquals(2, group.getMembers().size());
        assertThrows(UserException.class, () -> park.addToGroup("G01", "ana@correo.com"));
        assertThrows(UserException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(UserException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(UserException.class, () -> park.createGroup("G01", "Otro"));
        assertThrows(UserException.class, () -> park.createGroup("G02", " "));
        park.removeFromGroup("G01", "ana@correo.com");
        assertFalse(group.contains(user("ana@correo.com")));
        assertThrows(UserException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
    }

    @Test
    void activeGroupTicketLocksGroup() {
        groupWith("G02", "pedro@correo.com", "laura@correo.com");
        park.createGroupTicket("TG02", "G02", FUTURE);
        assertThrows(UserException.class, () -> park.removeFromGroup("G02", "laura@correo.com"));
        assertThrows(UserException.class, () -> park.addToGroup("G02", "ana@correo.com"));
        assertThrows(UserException.class, () -> park.deleteGroup("G02"));
        park.deleteGroupTicket("TG02");
        park.removeFromGroup("G02", "laura@correo.com");
        park.deleteGroup("G02");
        assertTrue(park.findGroup("G02").isEmpty());
    }

    @Test
    void historicalGroupTicketDoesNotLockAndSurvivesGroupDeletion() {
        groupWith("G01", "ana@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(UserException.class, () -> park.deleteGroup("G01"));
        park.setCurrentDate("2027-04-16");
        park.removeFromGroup("G01", "ana@correo.com");
        park.deleteGroup("G01");
        assertNull(ticket.getGroup());
        assertTrue(park.findTicket("TG01").isPresent());
        assertEquals(1, ticket.getTickets().size());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }

    // ----- Tickets -----

    @Test
    void individualTicketRules() {
        IndividualTicket ticket = park.createTicket("T001", "ana@correo.com", TODAY);
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertThrows(UserException.class, () -> park.createTicket("T001", "pedro@correo.com", TODAY));
        assertThrows(UserException.class, () -> park.createTicket("T009", "ana@correo.com", "2027-04-14"));
        assertThrows(UserException.class, () -> park.createTicket("T010", "nadie@correo.com", TODAY));
        assertThrows(UserException.class, () -> park.createTicket("T011", "ana@correo.com", "tomorrow"));
        assertThrows(UserException.class, () -> park.deleteTicket("T001"));
        park.setCurrentPrice("45.50");
        park.createTicket("T002", "pedro@correo.com", FUTURE);
        assertEquals(new BigDecimal("45.50"), park.findTicket("T002").orElseThrow().getPrice());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        park.deleteTicket("T002");
        assertTrue(park.findTicket("T002").isEmpty());
        assertThrows(UserException.class, () -> park.deleteTicket("T404"));
    }

    @Test
    void groupTicketCompositionAndPrice() {
        groupWith("G01", "ana@correo.com", "pedro@correo.com", "laura@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(3, ticket.getTickets().size());
        assertEquals("TG01:12345678Z", ticket.getTickets().get(0).getId());
        assertEquals(new BigDecimal("36.00"), ticket.getTickets().get(0).getPrice());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertTrue(park.findTicket("TG01:87654321X").isPresent());
        assertThrows(UserException.class, () -> park.deleteTicket("TG01:87654321X"));
        assertThrows(UserException.class, () -> park.deleteTicket("TG01"));
        assertThrows(UserException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(UserException.class, () -> park.deleteGroupTicket("T404"));
    }

    @Test
    void groupTicketRejections() {
        park.createGroup("G03", "Vacio");
        assertThrows(UserException.class, () -> park.createGroupTicket("TG03", "G03", TODAY));
        groupWith("G01", "ana@correo.com");
        assertThrows(UserException.class, () -> park.createGroupTicket("TG04", "G01", "2027-04-01"));
        assertThrows(UserException.class, () -> park.createGroupTicket("TG04", "G99", TODAY));
        park.createTicket("TG01", "pedro@correo.com", TODAY);
        assertThrows(UserException.class, () -> park.createGroupTicket("TG01", "G01", TODAY));
        park.createTicket("TG05:12345678Z", "pedro@correo.com", TODAY);
        assertThrows(UserException.class, () -> park.createGroupTicket("TG05", "G01", TODAY));
        assertTrue(park.findTicket("TG05").isEmpty());
        park.createGroupTicket("TG06", "G01", TODAY);
        assertThrows(UserException.class, () -> park.createTicket("TG06:12345678Z", "ana@correo.com", TODAY));
    }

    @Test
    void groupCompositionIsFixedAtPurchase() {
        groupWith("G01", "ana@correo.com", "pedro@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate("2027-04-16");
        park.removeFromGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "laura@correo.com");
        assertEquals(2, ticket.getTickets().size());
        assertTrue(ticket.involves(user("pedro@correo.com")));
        assertFalse(ticket.involves(user("laura@correo.com")));
    }

    // ----- User deletion -----

    @Test
    void activeTicketsBlockUserDeletion() {
        park.createTicket("T002", "ana@correo.com", FUTURE);
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        park.deleteTicket("T002");
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.users().findByEmail("ana@correo.com").isEmpty());
    }

    @Test
    void ticketForTodayBlocksUserDeletion() {
        park.createTicket("T001", "ana@correo.com", TODAY);
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
    }

    @Test
    void groupTicketComponentBlocksUserDeletion() {
        groupWith("G01", "ana@correo.com", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", FUTURE);
        assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
    }

    @Test
    void lockedGroupBlocksUserDeletionEvenWithoutOwnTicket() {
        groupWith("G01", "ana@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", FUTURE);
        park.setCurrentDate("2027-04-25");
        park.addToGroup("G01", "pedro@correo.com");
        park.setCurrentDate(TODAY);
        assertFalse(ticket.involves(user("pedro@correo.com")));
        assertFalse(park.hasActiveTicket(user("pedro@correo.com")));
        assertThrows(UserException.class, () -> park.deleteUser("pedro@correo.com", "Otra2#"));
        assertTrue(park.findGroup("G01").orElseThrow().contains(user("pedro@correo.com")));
    }

    @Test
    void historicalTicketsDoNotBlockAndAreRetained() {
        groupWith("G02", "ana@correo.com", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentDate("2027-04-16");
        park.deleteUser("ana@correo.com", "Clave1!");
        Group group = park.findGroup("G02").orElseThrow();
        assertTrue(group.contains(user("pedro@correo.com")));
        assertEquals(1, group.getMembers().size());
        IndividualTicket ticket = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertEquals("12345678Z", ticket.getUser().getDni());
    }

    // ----- Attractions -----

    @Test
    void attractionCreationValidation() {
        Attraction attraction = park.createAttraction("A01", "Dragon Rojo", "20");
        assertEquals(20, attraction.getCapacity());
        assertThrows(UserException.class, () -> park.createAttraction("A01", "Otra", "5"));
        assertThrows(UserException.class, () -> park.createAttraction("A03", "Noria", "0"));
        assertThrows(UserException.class, () -> park.createAttraction("A03", "Noria", "abc"));
        assertThrows(UserException.class, () -> park.createAttraction("A03", "", "5"));
        assertThrows(UserException.class, () -> park.requireAttraction("A99"));
    }

    @Test
    void individualAndGroupEntry() {
        Attraction attraction = park.createAttraction("A01", "Dragon Rojo", "20");
        groupWith("G01", "ana@correo.com", "pedro@correo.com", "laura@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(3, park.enterAttraction("A01", "TG01").size());
        assertEquals(17, attraction.getAvailablePlaces());
        assertThrows(UserException.class, () -> park.enterAttraction("A01", "TG01"));
        park.exitAttraction("A01", "ana@correo.com");
        assertEquals(18, attraction.getAvailablePlaces());
        assertThrows(UserException.class, () -> park.exitAttraction("A01", "ana@correo.com"));
        assertEquals(1, park.enterAttraction("A01", "TG01:12345678Z").size());
    }

    @Test
    void entryRequiresTicketForToday() {
        park.createAttraction("A02", "Torre del Cielo", "12");
        park.createTicket("T002", "ana@correo.com", FUTURE);
        assertThrows(UserException.class, () -> park.enterAttraction("A02", "T002"));
        assertThrows(UserException.class, () -> park.enterAttraction("A02", "T404"));
        park.setCurrentDate(FUTURE);
        park.enterAttraction("A02", "T002");
        park.setCurrentDate("2027-05-01");
        assertTrue(park.requireAttraction("A02").isInside(user("ana@correo.com")));
    }

    @Test
    void groupEntryIsAtomic() {
        Attraction small = park.createAttraction("A03", "Mini", "2");
        groupWith("G01", "ana@correo.com", "pedro@correo.com", "laura@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(UserException.class, () -> park.enterAttraction("A03", "TG01"));
        assertEquals(0, small.getOccupancy());
    }
}
