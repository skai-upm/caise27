package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileStoreTest {

    @TempDir
    Path dir;

    private static final String ANA_LINE =
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO_LINE =
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new UserFileStore(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        UserFileStore store = new UserFileStore(file);
        store.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(ANA_LINE, PEDRO_LINE), Files.readAllLines(file));
    }

    @Test
    void roundTripRestoresAllFields() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, PEDRO_LINE));
        List<User> users = new UserFileStore(file).load();
        assertEquals(2, users.size());
        User ana = users.get(0);
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCardNumber());
    }

    @Test
    void skipsBlankAndMalformedLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, "", "broken;line", PEDRO_LINE));
        List<User> users = new UserFileStore(file).load();
        assertEquals(2, users.size());
        assertEquals("87654321X", users.get(1).getDni());
    }

    @Test
    void savingEmptyListClearsFile() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE));
        UserFileStore store = new UserFileStore(file);
        store.save(List.of());
        assertTrue(store.load().isEmpty());
        try (var files = Files.list(dir)) {
            assertEquals(1, files.count(), "no temporary files left behind");
        }
    }
}
