package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.User;

class UserFileStoreTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserFileStore(file).save(List.of(ana, pedro));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void loadsWhatWasSaved() {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        store.save(List.of(ana, pedro));
        List<User> loaded = store.load();
        assertEquals(2, loaded.size());
        User a = loaded.get(0);
        assertEquals("Ana", a.getFirstName());
        assertEquals("Garcia Lopez", a.getSurnames());
        assertEquals("12345678Z", a.getDni());
        assertEquals("ana@correo.com", a.getEmail());
        assertEquals("Clave1!", a.getPassword());
        assertEquals("612345678", a.getPhone());
        assertEquals("1234567890123456", a.getCardNumber());
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStore(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "garbage\nAna;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n\n");
        assertEquals(1, new UserFileStore(file).load().size());
    }

    @Test
    void writeFailureIsReportedAsPersistenceException() {
        UserFileStore store = new UserFileStore(dir.resolve("missing-dir").resolve("users.txt"));
        assertThrows(PersistenceException.class, () -> store.save(List.of(ana)));
    }
}
