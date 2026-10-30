package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest extends ParkFixture {

    private User reloaded(String email) {
        restart();
        return park.users().findByEmail(email).orElseThrow();
    }

    @Test
    void createStoresUser() {
        assertEquals(2, park.users().findAll().size());
        assertEquals("Ana", reloaded("ana@correo.com").getFirstName());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12345678A|new@correo.com|Clave1!|1234567890123456|Invalid DNI",
            "11111111H|new@correo.es|Clave1!|1234567890123456|Invalid email",
            "11111111H|new@correo.com|clave1!|1234567890123456|Invalid password",
            "11111111H|new@correo.com|Clave1!|123456789012345|Invalid card number",
            "12345678Z|new@correo.com|Clave1!|1234567890123456|DNI already registered",
            "11111111H|ana@correo.com|Clave1!|1234567890123456|Email already registered"})
    void createRejectsInvalidOrDuplicateData(String dni, String email, String password, String card, String message) {
        ParkException e = assertThrows(ParkException.class,
                () -> users.create("Luis", "Perez", dni, email, password, "600000000", card));
        assertEquals(message, e.getMessage());
        assertEquals(2, park.users().findAll().size());
    }

    @Test
    void deleteWithCorrectPassword() {
        users.delete("pedro@correo.com", "Otra2#");
        assertTrue(park.users().findByEmail("pedro@correo.com").isEmpty());
        restart();
        assertTrue(park.users().findByEmail("pedro@correo.com").isEmpty());
    }

    @Test
    void deleteWithWrongPasswordKeepsUser() {
        assertThrows(ParkException.class, () -> users.delete("pedro@correo.com", "Wrong1!"));
        assertTrue(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void deleteUnknownUser() {
        assertThrows(ParkException.class, () -> users.delete("nobody@correo.com", "Clave1!"));
        assertEquals(2, park.users().findAll().size());
    }

    @Test
    void deleteRejectedWithActiveIndividualTicket() {
        TicketController.getInstance().createTicket("T001", "pedro@correo.com", TODAY.toString());
        ParkException e = assertThrows(ParkException.class,
                () -> users.delete("pedro@correo.com", "Otra2#"));
        assertEquals("User has active tickets", e.getMessage());
        assertTrue(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void deleteRejectedWithActiveGroupTicketAndLockedGroup() {
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia");
        groups.addUser("G01", "pedro@correo.com");
        TicketController.getInstance().createGroupTicket("TG01", "G01", TODAY.plusDays(1).toString());
        assertThrows(ParkException.class, () -> users.delete("pedro@correo.com", "Otra2#"));
        assertTrue(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void deleteRejectedWhenInLockedGroupWithoutOwnTicket() {
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        TicketController.getInstance().createGroupTicket("TG01", "G01", TODAY.toString());
        // Pedro is a member not covered by the ticket; commands cannot reach this state, the model can.
        park.groups().findById("G01").orElseThrow().add(pedro);

        ParkException e = assertThrows(ParkException.class,
                () -> users.delete("pedro@correo.com", "Otra2#"));
        assertEquals("User belongs to locked group G01", e.getMessage());
        assertTrue(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void deleteRemovesUserFromGroupsAndKeepsHistory() {
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia");
        groups.addUser("G01", "pedro@correo.com");
        groups.addUser("G01", "ana@correo.com");
        TicketController.getInstance().createGroupTicket("TG01", "G01", TODAY.toString());
        park.setDate(TODAY.plusDays(1).toString());

        users.delete("pedro@correo.com", "Otra2#");

        assertEquals(1, park.groups().findById("G01").orElseThrow().getMembers().size());
        assertEquals(2, park.tickets().findById("TG01").orElseThrow().getHolders().size());
    }

    @Test
    void updateEachFieldAndPersist() {
        users.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        users.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        users.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        users.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        users.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        users.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User u = reloaded("ana.nueva@correo.com");
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia", u.getSurnames());
        assertEquals("699999999", u.getPhone());
        assertEquals("9999888877776666", u.getCardNumber());
        assertTrue(FAST_HASHER.matches("Nueva2#", u.getPasswordHash()));
        assertFalse(FAST_HASHER.matches("Clave1!", u.getPasswordHash()));
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() {
        assertDoesNotThrow(() -> users.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "EMAIL|pedro@correo.com|Email already registered",
            "EMAIL|ana@correo|Invalid email",
            "PASSWORD|clave1!|Invalid password",
            "CARD|1234 5678 9012 3456|Invalid card number",
            "DNI|87654321X|Field cannot be modified: DNI",
            "AGE|30|Field cannot be modified: AGE"})
    void updateRejectsInvalidChanges(String field, String value, String message) {
        ParkException e = assertThrows(ParkException.class,
                () -> users.update("ana@correo.com", "Clave1!", field, value));
        assertEquals(message, e.getMessage());
        User u = reloaded("ana@correo.com");
        assertEquals("12345678Z", u.getDni());
        assertTrue(FAST_HASHER.matches("Clave1!", u.getPasswordHash()));
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void updateRequiresCredentials() {
        assertThrows(ParkException.class,
                () -> users.update("ana@correo.com", "Wrong1!", "PHONE", "600000000"));
        assertThrows(ParkException.class,
                () -> users.update("nobody@correo.com", "Clave1!", "PHONE", "600000000"));
        assertEquals("612345678", reloaded("ana@correo.com").getPhone());
    }
}
