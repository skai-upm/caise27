package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParkControllerTest {

    static final LocalDate TODAY = TestSupport.TODAY;

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() throws ParkException {
        park = restart();
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private ParkController restart() throws ParkException {
        return TestSupport.initialize(dir);
    }

    private int enter(String attraction, String ticket) throws ParkException {
        return park.enter(attraction, ticket, null).admitted().size();
    }

    private void createPedro() throws ParkException {
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private User ana() {
        return park.getUsers().get(0);
    }

    // ------------------------------------------------------------- Delivery 1

    @Test
    void createsAndPersistsUser() throws ParkException {
        assertEquals("Ana", ana().getFirstName());
        assertEquals(1, restart().getUsers().size());
    }

    @Test
    void rejectsDuplicateDniAndEmail() throws IOException {
        String before = Files.readString(dir.resolve("users.txt"));
        assertThrows(ParkException.class, () -> park.createUser("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(ParkException.class, () -> park.createUser("Pedro", "Lopez Martin", "87654321X",
                "ana@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, park.getUsers().size());
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
    }

    @Test
    void rejectsInvalidCreationData() {
        assertThrows(ParkException.class, () -> park.createUser("P", "L", "87654321A", "p@correo.com",
                "Otra2#", "1", "9876543210987654"));
        assertThrows(ParkException.class, () -> park.createUser("P", "L", "87654321X", "p@correo.es",
                "Otra2#", "1", "9876543210987654"));
        assertThrows(ParkException.class, () -> park.createUser("P", "L", "87654321X", "p@correo.com",
                "otra2#", "1", "9876543210987654"));
        assertThrows(ParkException.class, () -> park.createUser("P", "L", "87654321X", "p@correo.com",
                "Otra2#", "1", "98765432109876"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void deletesWithCorrectCredentials() throws ParkException {
        park.deleteUser("ana@correo.com", "Clave1!");
        assertEquals(0, park.getUsers().size());
        assertEquals(0, restart().getUsers().size());
    }

    @Test
    void deleteRejectsWrongPasswordOrUnknownEmail() {
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Wrong1!"));
        assertThrows(ParkException.class, () -> park.deleteUser("nadie@correo.com", "Clave1!"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void updatesEveryModifiableField() throws ParkException {
        park.updateUser("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        park.updateUser("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        park.updateUser("ana@correo.com", "Clave1!", "PHONE", "699999999");
        park.updateUser("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        park.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        park.updateUser("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");
        park.updateUser("ana.nueva@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User reloaded = restart().getUsers().get(0);
        assertEquals("Ana Maria", reloaded.getFirstName());
        assertEquals("Garcia", reloaded.getSurnames());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("9999888877776666", reloaded.getCard());
        assertTrue(TestSupport.HASHER.matches("Nueva2#", reloaded.getPasswordHash()));
        assertEquals("ana.nueva@correo.com", reloaded.getEmail());
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateRejectionsLeaveUserUnchanged() throws ParkException, IOException {
        createPedro();
        String before = Files.readString(dir.resolve("users.txt"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "AGE", "30"));
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana@x.es"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Bad1!", "NAME", "X"));
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
        assertEquals("Ana", ana().getFirstName());
    }

    @Test
    void failedSaveUndoesTheChange() throws Exception {
        Files.createDirectory(dir.resolve("groups.txt.tmp"));
        Files.writeString(dir.resolve("groups.txt.tmp").resolve("block"), "x");
        assertThrows(ParkException.class, () -> park.updateUser("ana@correo.com", "Clave1!", "NAME", "Changed"));
        assertEquals("Ana", ana().getFirstName());
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Familia"));
        assertTrue(park.getGroups().isEmpty());
    }

    // ---------------------------------------------------------------- F2-C01

    @Test
    void isASingleton() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void systemDateAndPrice() throws ParkException {
        assertEquals(TODAY, park.getToday());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        park.setDate("2027-05-01");
        park.setPrice("45.50");
        assertEquals(LocalDate.of(2027, 5, 1), park.getToday());
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
        park.setPrice("45");
        assertEquals(new BigDecimal("45.00"), park.getBasePrice());

        for (String invalid : new String[] {"0", "-5", "abc", "1.234", "", "1.", ".5", "4.5.0", "0.00"}) {
            assertThrows(ParkException.class, () -> park.setPrice(invalid), invalid);
        }
        assertThrows(ParkException.class, () -> park.setDate("15/04/2027"));
        assertThrows(ParkException.class, () -> park.setDate("2027-02-30"));
        assertEquals(new BigDecimal("45.00"), park.getBasePrice());
        assertEquals(LocalDate.of(2027, 5, 1), park.getToday());

        assertEquals(new BigDecimal("40.00"), restart().getBasePrice());
        assertEquals(TODAY, park.getToday());
    }

    // ---------------------------------------------------------------- F2-C02

    @Test
    void manageGroupMembership() throws ParkException {
        park.createGroup("G01", "Familia Garcia");
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otra"));
        assertThrows(ParkException.class, () -> park.createGroup("", "Otra"));
        assertThrows(ParkException.class, () -> park.createGroup("G02", " "));

        park.addToGroup("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertEquals(1, park.getGroups().get(0).getMembers().size());

        park.removeFromGroup("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertTrue(park.getGroups().get(0).getMembers().isEmpty());
    }

    @Test
    void groupLockFollowsActiveGroupTickets() throws ParkException {
        createPedro();
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-20");
        Group group = park.getGroups().get(0);
        assertTrue(park.isLocked(group));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));

        park.deleteGroupTicket("TG01");
        assertFalse(park.isLocked(group));
        park.addToGroup("G01", "pedro@correo.com");

        park.createGroupTicket("TG02", "G01", "2027-04-15");
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG02"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));

        park.setDate("2027-04-16");
        assertFalse(park.isLocked(group));
        park.removeFromGroup("G01", "pedro@correo.com");
        park.deleteGroup("G01");
        assertTrue(park.getGroups().isEmpty());
        GroupTicket historical = (GroupTicket) park.findTicket("TG02");
        assertNull(historical.getGroup());
        assertEquals(2, historical.getComposition().size());
        assertEquals(new BigDecimal("80.00"), historical.getPrice());
    }

    // ---------------------------------------------------------------- F2-C03

    @Test
    void individualTicketLifecycle() throws ParkException {
        assertThrows(ParkException.class, () -> park.createTicket("T000", "ana@correo.com", "2027-04-14"));
        IndividualTicket today = park.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertEquals(new BigDecimal("40.00"), today.getPrice());
        assertSame(ana(), today.getUser());

        park.setPrice("45.50");
        IndividualTicket future = park.createTicket("T002", "ana@correo.com", "2027-04-16");
        assertEquals(new BigDecimal("45.50"), future.getPrice());
        assertEquals(new BigDecimal("40.00"), today.getPrice());

        assertThrows(ParkException.class, () -> park.createTicket("T001", "ana@correo.com", "2027-04-16"));
        assertThrows(ParkException.class, () -> park.createTicket("T003", "nadie@correo.com", "2027-04-16"));
        assertThrows(ParkException.class, () -> park.createTicket("T003", "ana@correo.com", "16-04-2027"));
        assertThrows(ParkException.class, () -> park.createTicket("", "ana@correo.com", "2027-04-16"));

        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T999"));
        park.deleteTicket("T002");
        assertNull(park.findTicket("T002"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void userDeletionRespectsActiveTickets() throws ParkException {
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        park.setDate("2027-04-16");
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.getUsers().isEmpty());
        IndividualTicket historical = (IndividualTicket) park.findTicket("T001");
        assertEquals("12345678Z", historical.getUser().getDni());
    }

    // ---------------------------------------------------------------- F2-C04

    private void createMembers(int count) throws ParkException {
        park.createGroup("G", "Grupo");
        park.addToGroup("G", "ana@correo.com");
        for (int i = 1; i < count; i++) {
            String number = String.format("%08d", i);
            String dni = number + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(i % 23);
            park.createUser("U" + i, "S", dni, "u" + i + "@correo.com", "Clave1!", "1", "1234567890123456");
            park.addToGroup("G", "u" + i + "@correo.com");
        }
    }

    @Test
    void groupDiscountBrackets() {
        assertEquals(new BigDecimal("0.00"), GroupTicket.discountFor(2));
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(3));
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(5));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(6));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(10));
        assertEquals(new BigDecimal("0.50"), GroupTicket.discountFor(11));
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "6,192.00", "11,220.00"})
    void groupTicketTotals(int members, String total) throws ParkException {
        createMembers(members);
        GroupTicket ticket = park.createGroupTicket("TG", "G", "2027-04-20");
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getComposition().size());
        assertTrue(ticket.getHolders().contains(ana()));
    }

    @Test
    void groupTicketCompositionAndConflicts() throws ParkException {
        createPedro();
        park.createGroup("G01", "Familia");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G99", "2027-04-15"));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-14"));

        park.createTicket("TG01:87654321X", "pedro@correo.com", "2027-04-20");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertEquals(1, park.getTickets().size());

        GroupTicket ticket = park.createGroupTicket("TG02", "G01", "2027-04-15");
        assertSame(park.getGroups().get(0), ticket.getGroup());
        assertEquals("TG02:12345678Z", ticket.getComposition().get(0).getIdentifier());
        assertSame(ticket.getComposition().get(1), park.findTicket("TG02:87654321X"));
        assertThrows(ParkException.class, () -> park.createTicket("TG02", "ana@correo.com", "2027-04-20"));
        assertThrows(ParkException.class, () -> park.createTicket("TG02:12345678Z", "ana@correo.com", "2027-04-20"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG02:12345678Z"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG02"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01:87654321X"));
    }

    @Test
    void userDeletionRespectsLockedGroups() throws ParkException {
        createPedro();
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-20");
        park.createGroup("G02", "Amigos");
        park.addToGroup("G02", "pedro@correo.com");

        assertThrows(ParkException.class, () -> park.deleteUser("pedro@correo.com", "Otra2#"));
        park.setDate("2027-04-21");
        park.deleteUser("pedro@correo.com", "Otra2#");
        assertEquals(1, park.getGroups().get(0).getMembers().size());
        assertTrue(park.getGroups().get(1).getMembers().isEmpty());
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getComposition().size());
    }

    // ---------------------------------------------------------- F2-C05..C07

    @Test
    void attractionsAndAccess() throws ParkException {
        createPedro();
        park.createAttraction("A01", "Dragon Rojo", "2", null);
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", "5", null));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Otra", "0", null));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Otra", "x", null));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "", "5", null));
        Attraction attraction = park.requireAttraction("A01");
        assertTrue(attraction.getOccupants().isEmpty());

        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createTicket("T002", "pedro@correo.com", "2027-04-16");
        assertThrows(ParkException.class, () -> enter("A01", "T002"));
        assertThrows(ParkException.class, () -> enter("A99", "T001"));
        assertThrows(ParkException.class, () -> enter("A01", "T999"));
        assertEquals(1, enter("A01", "T001"));
        assertThrows(ParkException.class, () -> enter("A01", "T001"));
        assertEquals(List.of(ana()), attraction.getOccupants());

        park.setDate("2027-04-16");
        assertEquals(1, attraction.getOccupants().size());
        assertEquals(1, enter("A01", "T002"));
        assertEquals(0, attraction.getAvailablePlaces());

        park.exit("A01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.exit("A01", "ana@correo.com"));
        assertEquals(1, attraction.getAvailablePlaces());
    }

    @Test
    void fullAttractionRejectsEntry() throws ParkException {
        createPedro();
        park.createAttraction("A01", "Mini", "1", null);
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createTicket("T002", "pedro@correo.com", "2027-04-15");
        enter("A01", "T001");
        assertThrows(ParkException.class, () -> enter("A01", "T002"));
        assertEquals(1, park.requireAttraction("A01").getOccupants().size());
    }

    @Test
    void groupEntryIsAllOrNothing() throws ParkException {
        createMembers(4);
        park.createAttraction("A01", "Small", "3", null);
        park.createAttraction("A02", "Big", "4", null);
        park.createGroupTicket("TG01", "G", "2027-04-15");
        assertThrows(ParkException.class, () -> enter("A01", "TG01"));
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());

        assertEquals(4, enter("A02", "TG01"));
        park.exit("A02", "u1@correo.com");
        assertEquals(3, park.requireAttraction("A02").getOccupants().size());
        assertEquals(1, park.requireAttraction("A02").getAvailablePlaces());
    }

    @Test
    void deletedHolderCannotEnter() throws ParkException {
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createAttraction("A01", "Dragon", "5", null);
        park.setDate("2027-04-16");
        park.deleteUser("ana@correo.com", "Clave1!");
        park.setDate("2027-04-15");
        assertThrows(ParkException.class, () -> enter("A01", "T001"));
    }
}
