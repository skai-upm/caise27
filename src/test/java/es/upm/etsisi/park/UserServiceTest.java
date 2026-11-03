package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

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

    private static final String ANA_LINE = "Ana;Garcia Lopez;12345678Z;ana@correo.com;*;612345678;1234567890123456";

    @TempDir
    Path dir;
    private Path file;
    private UserController service;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        TestParks.start(dir);
        service = UserController.getInstance();
    }

    private static Park park() {
        return Park.getInstance();
    }

    private void createAna() throws ParkException {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() throws ParkException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private Park reload() throws IOException {
        return TestParks.start(dir);
    }

    @Test
    void createsValidUniqueUser() throws Exception {
        createAna();
        assertEquals(1, park().getUsers().size());
        assertEquals(List.of(ANA_LINE), TestParks.users(file));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12345678A|ana@correo.com|Clave1!|1234567890123456",
            "12345678Z|ana@correo.es|Clave1!|1234567890123456",
            "12345678Z|ana@correo.com|clave1!|1234567890123456",
            "12345678Z|ana@correo.com|Clave1!|123456789012345"})
    void rejectsInvalidData(String dni, String email, String password, String card) {
        assertThrows(ParkException.class,
                () -> service.create("Ana", "Garcia Lopez", dni, email, password, "612345678", card));
        assertTrue(park().getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void rejectsDuplicateDni() throws Exception {
        createAna();
        assertThrows(ParkException.class, () -> service.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, park().getUsers().size());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        createAna();
        assertThrows(ParkException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, park().getUsers().size());
    }

    @Test
    void usersSurviveRestartAndStillCountForUniqueness() throws Exception {
        createAna();
        reload();
        UserController restarted = UserController.getInstance();
        User ana = park().findUser("ana@correo.com");
        assertNotNull(ana);
        assertEquals(ANA_LINE, TestParks.line(ana));
        assertThrows(ParkException.class, () -> restarted.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1111222233334444"));
    }

    @Test
    void deletesWithCorrectCredentials() throws Exception {
        createAna();
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(park().findUser("pedro@correo.com"));
        assertEquals(List.of(ANA_LINE), TestParks.users(file));
    }

    @Test
    void doesNotDeleteWithWrongPassword() throws Exception {
        createAna();
        assertThrows(ParkException.class, () -> service.delete("ana@correo.com", "Wrong1!"));
        assertNotNull(park().findUser("ana@correo.com"));
        assertEquals(List.of(ANA_LINE), TestParks.users(file));
    }

    @Test
    void doesNotDeleteUnknownEmail() throws Exception {
        createAna();
        assertThrows(ParkException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, park().getUsers().size());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "NAME|Ana Maria|Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;*;612345678;1234567890123456",
            "SURNAME|Garcia|Ana;Garcia;12345678Z;ana@correo.com;*;612345678;1234567890123456",
            "PASSWORD|Nueva2#|Ana;Garcia Lopez;12345678Z;ana@correo.com;*;612345678;1234567890123456",
            "PHONE|699999999|Ana;Garcia Lopez;12345678Z;ana@correo.com;*;699999999;1234567890123456",
            "CARD|9999888877776666|Ana;Garcia Lopez;12345678Z;ana@correo.com;*;612345678;9999888877776666",
            "EMAIL|ana.nueva@correo.com|Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;*;612345678;1234567890123456"})
    void updatesPermittedFieldsAndPersists(String field, String value, String expectedLine) throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", field, value);
        assertEquals(List.of(expectedLine), TestParks.users(file));
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
        assertEquals(List.of(ANA_LINE), TestParks.users(file));
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
        List<String> before = TestParks.users(file);
        assertThrows(ParkException.class, () -> service.update("ana@correo.com", password, field, value));
        assertEquals(ANA_LINE, TestParks.line(park().findUser("ana@correo.com")));
        assertEquals(before, TestParks.users(file));
    }

    @Test
    void updateOfUnknownUserIsRejected() {
        assertThrows(ParkException.class,
                () -> service.update("nadie@correo.com", "Clave1!", "PHONE", "699999999"));
    }

    @Test
    void failedWriteRollsBackEveryOperation() throws Exception {
        createAna();
        Park park = TestParks.start(new FailingRepository(dir));
        UserController broken = UserController.getInstance();
        assertThrows(ParkException.class, () -> broken.create("Pedro", "Lopez Martin", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, park.getUsers().size());

        assertThrows(ParkException.class, () -> broken.delete("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUser("ana@correo.com"));

        assertThrows(ParkException.class,
                () -> broken.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertNull(park.findUser("ana.nueva@correo.com"));
        assertEquals(ANA_LINE, TestParks.line(park.findUser("ana@correo.com")));
    }
}
