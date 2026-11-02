package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Delivery 1 user rules, now served by the park controller. */
class ParkUserTest extends ParkTestSupport {

    private Path users;

    @BeforeEach
    void setUp() {
        users = dir.resolve("users.txt");
        ana();
    }

    private String snapshot() throws IOException {
        return Files.readString(users);
    }

    @Test
    void createsAndPersistsUser() throws IOException {
        assertEquals(1, park.getUsers().size());
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456" + System.lineSeparator(), snapshot());
    }

    @Test
    void rejectsDuplicateDni() {
        UserException e = assertThrows(UserException.class, () ->
                park.createUser("Otra", "Persona", "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals("DNI already registered", e.getMessage());
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void rejectsDuplicateEmail() {
        assertThrows(UserException.class, () ->
                park.createUser("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, park.getUsers().size());
    }

    @ParameterizedTest
    @CsvSource({
        "12345678A, x@correo.com, Clave1!, 1111222233334444, Invalid DNI",
        "87654321X, x@correo.es, Clave1!, 1111222233334444, Invalid email",
        "87654321X, x@correo.com, clave1!, 1111222233334444, Invalid password",
        "87654321X, x@correo.com, Clave1!, 1111 2222 3333 4444, Invalid card number"
    })
    void rejectsInvalidData(String dni, String email, String password, String card, String message) {
        UserException e = assertThrows(UserException.class, () -> park.createUser("X", "Y", dni, email, password, "600", card));
        assertEquals(message, e.getMessage());
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void rejectsBlankField() {
        assertThrows(UserException.class, () ->
                park.createUser(" ", "Y", "87654321X", "x@correo.com", "Clave1!", "600", "1111222233334444"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void userSurvivesRestartAndStillCountsForUniqueness() {
        Park again = restart();
        User ana = again.findUser("ana@correo.com");
        assertNotNull(ana);
        assertEquals("12345678Z", ana.getDni());
        assertThrows(UserException.class, this::ana);
    }

    @Test
    void deletesWithCorrectCredentials() throws IOException {
        pedro();
        park.deleteUser("pedro@correo.com", "Otra2#");
        assertNull(park.findUser("pedro@correo.com"));
        assertNull(restart().findUser("pedro@correo.com"));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void wrongPasswordDoesNotDelete() throws IOException {
        String before = snapshot();
        UserException e = assertThrows(UserException.class, () -> park.deleteUser("ana@correo.com", "Clave1?"));
        assertEquals("Incorrect password", e.getMessage());
        assertNotNull(park.findUser("ana@correo.com"));
        assertEquals(before, snapshot());
    }

    @Test
    void unknownEmailDoesNotDelete() {
        UserException e = assertThrows(UserException.class, () -> park.deleteUser("nadie@correo.com", "Clave1!"));
        assertEquals("User not found", e.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "NAME, Ana Maria",
        "SURNAME, Garcia Perez",
        "PASSWORD, Nueva2#",
        "PHONE, 699999999",
        "CARD, 9999888877776666"
    })
    void updatesPermittedFieldAndPersists(String field, String value) {
        park.updateUser("ana@correo.com", "Clave1!", field, value);
        User ana = restart().findUser("ana@correo.com");
        String actual = switch (field) {
            case "NAME" -> ana.getFirstName();
            case "SURNAME" -> ana.getSurnames();
            case "PASSWORD" -> ana.getPassword();
            case "PHONE" -> ana.getPhone();
            default -> ana.getCard();
        };
        assertEquals(value, actual);
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updatesEmailAndReindexes() {
        park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertNull(park.findUser("ana@correo.com"));
        Park again = restart();
        assertNotNull(again.findUser("ana.nueva@correo.com"));
        assertNull(again.findUser("ana@correo.com"));
    }

    @Test
    void updatingEmailToSameValueIsAllowed() {
        park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @ParameterizedTest
    @CsvSource({
        "DNI, 87654321X",
        "dni, 87654321X",
        "name, Ana Maria",
        "ADDRESS, Calle Mayor",
        "EMAIL, pedro@correo.com",
        "EMAIL, ana@correo",
        "PASSWORD, clave1!",
        "CARD, 1234",
        "NAME, ' '"
    })
    void rejectedUpdateLeavesUserUnchanged(String field, String value) throws IOException {
        pedro();
        String before = snapshot();
        assertThrows(UserException.class, () -> park.updateUser("ana@correo.com", "Clave1!", field, value));
        assertEquals(before, snapshot());
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                UserRepository.toLine(park.findUser("ana@correo.com")));
    }

    @Test
    void updateRequiresCredentials() {
        assertThrows(UserException.class, () -> park.updateUser("ana@correo.com", "Wrong1!", "PHONE", "699999999"));
        assertThrows(UserException.class, () -> park.updateUser("nadie@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", park.findUser("ana@correo.com").getPhone());
    }
}
