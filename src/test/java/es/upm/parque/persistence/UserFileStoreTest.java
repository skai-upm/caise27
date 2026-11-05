package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.model.User;

class UserFileStoreTest {

    @TempDir
    Path dir;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUser() throws Exception {
        Path file = dir.resolve("users.txt");
        new UserFileStore(file).save(List.of(ana(),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void loadMapsEveryFieldToItsAttribute() {
        Path file = dir.resolve("users.txt");
        UserFileStore store = new UserFileStore(file);
        store.save(List.of(ana()));
        User u = store.load().get(0);
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getTelephone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void missingFileLoadsNoUsers() {
        assertTrue(new UserFileStore(dir.resolve("absent.txt")).load().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("garbage", "",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "a;b;c;d;e;f;g;h"), StandardCharsets.UTF_8);
        List<User> users = new UserFileStore(file).load();
        assertEquals(1, users.size());
        assertEquals("12345678Z", users.get(0).getDni());
    }
}
