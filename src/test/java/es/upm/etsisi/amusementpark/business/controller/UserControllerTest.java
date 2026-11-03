package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserControllerTest {

    @TempDir
    Path dir;
    Path file;
    UserController service;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        service = UserController.initialize(new UserFileStorage(file));
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private List<String> fileLines() throws IOException {
        return Files.readAllLines(file);
    }

    @Test
    void validCreationIsStoredAndPersisted() throws IOException {
        assertEquals(1, service.getUsers().size());
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"), fileLines());
    }

    @ParameterizedTest
    @CsvSource({
        "Pedro,Lopez,87654321A,pedro@correo.com,Otra2#,623,9876543210987654",
        "Pedro,Lopez,87654321X,pedro@correo.es,Otra2#,623,9876543210987654",
        "Pedro,Lopez,87654321X,pedro@correo.com,Otra22,623,9876543210987654",
        "Pedro,Lopez,87654321X,pedro@correo.com,Otra2#,623,987654321098765",
        "'',Lopez,87654321X,pedro@correo.com,Otra2#,623,9876543210987654",
        "Pedro,'',87654321X,pedro@correo.com,Otra2#,623,9876543210987654",
        "Pedro,Lopez,87654321X,pedro@correo.com,Otra2#,'',9876543210987654",
        "Pedro,Lopez,12345678Z,otro@correo.com,Otra2#,623,9876543210987654",
        "Pedro,Lopez,87654321X,ana@correo.com,Otra2#,623,9876543210987654",
        "Pedro,Lopez,87654321X,ANA@correo.com,Otra2#,623,9876543210987654"
    })
    void invalidOrDuplicateCreationIsRejectedAtomically(String name, String surname, String dni, String email,
                                                        String password, String phone, String card) throws IOException {
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class,
                () -> service.create(name, surname, dni, email, password, phone, card));
        assertEquals(1, service.getUsers().size());
        assertEquals(before, fileLines());
    }

    @Test
    void deleteRequiresCorrectCredentials() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> service.delete("ana@correo.com", "Mala1!"));
        assertThrows(IllegalArgumentException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
        service.delete("ana@correo.com", "Clave1!");
        assertTrue(service.getUsers().isEmpty());
        assertTrue(fileLines().isEmpty());
    }

    @Test
    void updatesEveryEditableField() throws IOException {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        User u = service.getUsers().get(0);
        assertEquals("12345678Z", u.getDni());
        assertEquals(List.of("Ana Maria;Garcia;12345678Z;ana.nueva@correo.com;Nueva2#;699999999;9999888877776666"),
                fileLines());
        assertThrows(IllegalArgumentException.class, () -> service.delete("ana.nueva@correo.com", "Clave1!"));
    }

    @Test
    void invalidUpdatesLeaveValuesUnchanged() throws IOException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234 5678 9012 3456"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.NAME, " "));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals(before, fileLines());
        assertEquals("ana@correo.com", service.findByEmail("ana@correo.com").orElseThrow().getEmail());
    }

    @Test
    void keepingOwnEmailIsAllowed() {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals("ana@correo.com", service.getUsers().get(0).getEmail());
    }

    @Test
    void failedSaveRevertsInMemoryChange() throws IOException {
        Files.delete(file);
        Files.createDirectory(file); // writing to a directory path fails
        assertThrows(RuntimeException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.NAME, "Otra"));
        assertEquals("Ana", service.getUsers().get(0).getName());
        assertThrows(RuntimeException.class,
                () -> service.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, service.getUsers().size());
        assertThrows(RuntimeException.class, () -> service.delete("ana@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
    }
}
