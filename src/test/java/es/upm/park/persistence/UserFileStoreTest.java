package es.upm.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.model.User;

class UserFileStoreTest {

    @TempDir
    Path dir;

    private static final String ANA_LINE =
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO_LINE =
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @Test
    void writesOneLinePerUserInSourceFormat() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserFileStore(file).save(List.of(new User("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456")));
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void mapsPersistedLineToFields() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, PEDRO_LINE), StandardCharsets.UTF_8);
        List<User> users = new UserFileStore(file).load();
        assertEquals(2, users.size());
        User pedro = users.get(1);
        assertEquals("Pedro", pedro.getFirstName());
        assertEquals("Lopez Martin", pedro.getSurnames());
        assertEquals("87654321X", pedro.getDni());
        assertEquals("pedro@correo.com", pedro.getEmail());
        assertEquals("Otra2#", pedro.getPassword());
        assertEquals("623456789", pedro.getTelephone());
        assertEquals("9876543210987654", pedro.getCardNumber());
    }

    @Test
    void roundTripPreservesUsers() {
        Path file = dir.resolve("sub/users.txt");
        UserFileStore store = new UserFileStore(file);
        List<User> users = List.of(new User("A", "B", "12345678Z", "a@b.com", "Clave1!", "", "1234567890123456"));
        store.save(users);
        User loaded = store.load().get(0);
        assertEquals("", loaded.getTelephone());
        assertEquals("12345678Z", loaded.getDni());
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStore(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, "broken;line", "", PEDRO_LINE + ";extra"), StandardCharsets.UTF_8);
        List<User> users = new UserFileStore(file).load();
        assertEquals(1, users.size());
        assertEquals("12345678Z", users.get(0).getDni());
    }

    @Test
    void unreadableOrUnwritablePathRaisesPersistenceException() {
        UserFileStore store = new UserFileStore(dir);
        assertThrows(PersistenceException.class, store::load);
        assertThrows(PersistenceException.class, () -> store.save(List.of()));
    }
}
