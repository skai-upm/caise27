package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserServiceTest {

    private static final String ANA_LINE = "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        service = new UserService(TestParks.start(dir));
    }

    private static Park park() {
        return Park.getInstance();
    }

    private void createAna() throws ValidationException {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() throws ValidationException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private Park reload() throws IOException {
        return TestParks.start(dir);
    }

    @Test
    void createsValidUniqueUser() throws Exception {
        createAna();
        assertEquals(1, park().getUsers().size());
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12345678A|ana@correo.com|Clave1!|1234567890123456",
            "12345678Z|ana@correo.es|Clave1!|1234567890123456",
            "12345678Z|ana@correo.com|clave1!|1234567890123456",
            "12345678Z|ana@correo.com|Clave1!|123456789012345"})
    void rejectsInvalidData(String dni, String email, String password, String card) {
        assertThrows(ValidationException.class,
                () -> service.create("Ana", "Garcia Lopez", dni, email, password, "612345678", card));
        assertTrue(park().getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void rejectsDuplicateDni() throws Exception {
        createAna();
        assertThrows(ValidationException.class, () -> service.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, park().getUsers().size());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        createAna();
        assertThrows(ValidationException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, park().getUsers().size());
    }

    @Test
    void usersSurviveRestartAndStillCountForUniqueness() throws Exception {
        createAna();
        UserService restarted = new UserService(reload());
        User ana = park().findUser("ana@correo.com");
        assertNotNull(ana);
        assertEquals(ANA_LINE, UserRepository.toLine(ana));
        assertThrows(ValidationException.class, () -> restarted.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
    }

    @Test
    void deletesWithCorrectCredentials() throws Exception {
        createAna();
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(park().findUser("pedro@correo.com"));
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @Test
    void doesNotDeleteWithWrongPassword() throws Exception {
        createAna();
        assertThrows(ValidationException.class, () -> service.delete("ana@correo.com", "Wrong1!"));
        assertNotNull(park().findUser("ana@correo.com"));
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @Test
    void doesNotDeleteUnknownEmail() throws Exception {
        createAna();
        assertThrows(ValidationException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, park().getUsers().size());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "NAME|Ana Maria|Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "SURNAME|Garcia|Ana;Garcia;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "PASSWORD|Nueva2#|Ana;Garcia Lopez;12345678Z;ana@correo.com;Nueva2#;612345678;1234567890123456",
            "PHONE|699999999|Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
            "CARD|9999888877776666|Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;9999888877776666",
            "EMAIL|ana.nueva@correo.com|Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;612345678;1234567890123456"})
    void updatesPermittedFieldsAndPersists(String field, String value, String expectedLine) throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", field, value);
        assertEquals(List.of(expectedLine), Files.readAllLines(file));
        assertEquals(1, reload().getUsers().size());
    }

    @Test
    void updatedEmailIsUsedForLaterCommands() throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        assertNull(park().findUser("ana@correo.com"));
        assertNotNull(reload().findUser("ana.nueva@correo.com"));
        service.delete("ana.nueva@correo.com", "Clave1!");
        assertTrue(park().getUsers().isEmpty());
    }

    @Test
    void updatingEmailToItsOwnValueIsAllowed() throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Clave1!|DNI|87654321X",
            "Clave1!|AGE|30",
            "Clave1!|name|Ana Maria",
            "Clave1!|EMAIL|pedro@correo.com",
            "Clave1!|EMAIL|ana@correo.es",
            "Clave1!|PASSWORD|clave",
            "Clave1!|CARD|1234",
            "Wrong1!|PHONE|699999999"})
    void rejectedUpdatesLeaveUserUnchanged(String password, String field, String value) throws Exception {
        createAna();
        createPedro();
        List<String> before = Files.readAllLines(file);
        assertThrows(ValidationException.class, () -> service.update("ana@correo.com", password, field, value));
        assertEquals(ANA_LINE, UserRepository.toLine(park().findUser("ana@correo.com")));
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void updateOfUnknownUserIsRejected() {
        assertThrows(ValidationException.class,
                () -> service.update("nadie@correo.com", "Clave1!", "PHONE", "699999999"));
    }

    @Test
    void failedWriteRollsBackEveryOperation() throws Exception {
        createAna();
        Park park = Park.getInstance();
        park.start(new ParkStorage(dir) {
            @Override
            public void saveAll(java.util.Collection<User> u, java.util.Collection<Group> g,
                                java.util.Collection<Ticket> t, java.util.Collection<Attraction> a) throws IOException {
                throw new IOException("disk full");
            }
        }, LocalDate.of(2027, 4, 15));
        UserService broken = new UserService(park);
        assertThrows(ValidationException.class, () -> broken.create("Pedro", "Lopez Martin", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, park.getUsers().size());

        assertThrows(ValidationException.class, () -> broken.delete("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUser("ana@correo.com"));

        assertThrows(ValidationException.class,
                () -> broken.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertNull(park.findUser("ana.nueva@correo.com"));
        assertEquals(ANA_LINE, UserRepository.toLine(park.findUser("ana@correo.com")));
    }
}
