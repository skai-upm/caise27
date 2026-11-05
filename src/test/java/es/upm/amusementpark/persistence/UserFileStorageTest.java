package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileStorageTest {

    private static final String ANA = "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO = "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStorage(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void attributesMappedToFields() throws IOException {
        Path f = dir.resolve("users.txt");
        Files.write(f, List.of(ANA), StandardCharsets.UTF_8);
        User u = new UserFileStorage(f).load().get(0);
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getTelephone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void malformedLineIgnored() throws IOException {
        Path f = dir.resolve("users.txt");
        Files.write(f, List.of("bad;line", ANA), StandardCharsets.UTF_8);
        assertEquals(1, new UserFileStorage(f).load().size());
    }

    @Test
    void usersWrittenOnePerLineAndRecoveredAfterRestart() throws IOException {
        Path f = dir.resolve("users.txt");
        UserService first = new UserService(new UserRepository(new UserFileStorage(f)));
        first.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        first.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertEquals(List.of(ANA, PEDRO), Files.readAllLines(f, StandardCharsets.UTF_8));

        UserRepository restarted = new UserRepository(new UserFileStorage(f));
        assertEquals(2, restarted.count());
        UserService second = new UserService(restarted);
        Assertions.assertThrows(UserOperationException.class, () ->
                second.createUser("Luis", "M", "12345678Z", "luis@correo.com", "Clave1!", "6", "1111222233334444"));
        assertEquals(List.of(ANA, PEDRO), Files.readAllLines(f, StandardCharsets.UTF_8));
    }
}
