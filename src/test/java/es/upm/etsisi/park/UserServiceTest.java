package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        service = new UserService(new UserRepository(file));
        createAna(service);
    }

    private static void createAna(UserService s) {
        s.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private UserService restart() {
        return new UserService(new UserRepository(file));
    }

    private String snapshot() throws IOException {
        return Files.readString(file);
    }

    // --- creation ---

    @Test
    void createsAndPersistsUser() throws IOException {
        assertEquals(1, service.getUsers().size());
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456" + System.lineSeparator(), snapshot());
    }

    @Test
    void rejectsDuplicateDni() {
        UserException e = assertThrows(UserException.class, () ->
                service.create("Otra", "Persona", "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals("DNI already registered", e.getMessage());
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void rejectsDuplicateEmail() {
        assertThrows(UserException.class, () ->
                service.create("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, service.getUsers().size());
    }

    @ParameterizedTest
    @CsvSource({
        "12345678A, x@correo.com, Clave1!, 1111222233334444, Invalid DNI",
        "87654321X, x@correo.es, Clave1!, 1111222233334444, Invalid email",
        "87654321X, x@correo.com, clave1!, 1111222233334444, Invalid password",
        "87654321X, x@correo.com, Clave1!, 1111 2222 3333 4444, Invalid card number"
    })
    void rejectsInvalidData(String dni, String email, String password, String card, String message) {
        UserException e = assertThrows(UserException.class, () ->
                service.create("X", "Y", dni, email, password, "600", card));
        assertEquals(message, e.getMessage());
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void rejectsBlankField() {
        assertThrows(UserException.class, () ->
                service.create(" ", "Y", "87654321X", "x@correo.com", "Clave1!", "600", "1111222233334444"));
        assertEquals(1, service.getUsers().size());
    }

    // --- persistence ---

    @Test
    void userSurvivesRestartAndStillCountsForUniqueness() {
        UserService again = restart();
        User ana = again.findByEmail("ana@correo.com");
        assertNotNull(ana);
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
        assertThrows(UserException.class, () -> createAna(again));
    }

    @Test
    void failedSaveRollsBackEveryMutation() {
        FailingRepository failing = new FailingRepository(file);
        UserService s = new UserService(failing);
        failing.fail = true;

        assertThrows(UserException.class, () -> s.create("Pedro", "Lopez Martin", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertNull(s.findByEmail("pedro@correo.com"));

        assertThrows(UserException.class, () -> s.update("ana@correo.com", "Clave1!", "EMAIL", "ana2@correo.com"));
        assertNotNull(s.findByEmail("ana@correo.com"));
        assertNull(s.findByEmail("ana2@correo.com"));
        assertEquals("ana@correo.com", s.findByEmail("ana@correo.com").getEmail());

        assertThrows(UserException.class, () -> s.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", s.findByEmail("ana@correo.com").getPhone());

        assertThrows(UserException.class, () -> s.delete("ana@correo.com", "Clave1!"));
        assertNotNull(s.findByEmail("ana@correo.com"));
    }

    // --- deletion ---

    @Test
    void deletesWithCorrectCredentials() throws IOException {
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(restart().findByEmail("pedro@correo.com"));
        assertEquals(1, Files.readAllLines(file).size());
    }

    @Test
    void wrongPasswordDoesNotDelete() throws IOException {
        String before = snapshot();
        UserException e = assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Clave1?"));
        assertEquals("Incorrect password", e.getMessage());
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertEquals(before, snapshot());
    }

    @Test
    void unknownEmailDoesNotDelete() {
        UserException e = assertThrows(UserException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals("User not found", e.getMessage());
        assertEquals(1, service.getUsers().size());
    }

    // --- updates ---

    @ParameterizedTest
    @CsvSource({
        "NAME, Ana Maria",
        "SURNAME, Garcia Perez",
        "PASSWORD, Nueva2#",
        "PHONE, 699999999",
        "CARD, 9999888877776666"
    })
    void updatesPermittedFieldAndPersists(String field, String value) {
        service.update("ana@correo.com", "Clave1!", field, value);
        User ana = restart().findByEmail("ana@correo.com");
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
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertNull(service.findByEmail("ana@correo.com"));
        UserService again = restart();
        assertNotNull(again.findByEmail("ana.nueva@correo.com"));
        assertNull(again.findByEmail("ana@correo.com"));
    }

    @Test
    void updatingEmailToSameValueIsAllowed() {
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
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
        "CARD, 1234"
    })
    void rejectedUpdateLeavesUserUnchanged(String field, String value) throws IOException {
        createPedro();
        String before = snapshot();
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", field, value));
        assertEquals(before, snapshot());
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456", UserRepository.toLine(ana));
    }

    @Test
    void blankUpdateValueRejected() {
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "NAME", ""));
        assertEquals("Ana", service.findByEmail("ana@correo.com").getFirstName());
    }

    @Test
    void updateRequiresCredentials() {
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Wrong1!", "PHONE", "699999999"));
        assertThrows(UserException.class, () -> service.update("nadie@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", service.findByEmail("ana@correo.com").getPhone());
    }

    private static final class FailingRepository extends UserRepository {
        boolean fail;

        FailingRepository(Path file) {
            super(file);
        }

        @Override
        public void save(Collection<User> users) {
            if (fail) {
                throw new UserException("disk full");
            }
            super.save(users);
        }

        @Override
        public List<User> load() {
            return List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        }
    }
}
