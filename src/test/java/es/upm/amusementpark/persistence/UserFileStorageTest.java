package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.User;

class UserFileStorageTest {

    static final String ANA_LINE = "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String PEDRO_LINE = "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUser() throws Exception {
        Path file = dir.resolve("users.txt");
        new UserFileStorage(file).save(List.of(ana(), pedro()));
        assertEquals(List.of(ANA_LINE, PEDRO_LINE), Files.readAllLines(file));
    }

    @Test
    void loadsWhatWasSaved() throws Exception {
        Path file = dir.resolve("users.txt");
        UserFileStorage storage = new UserFileStorage(file);
        storage.save(List.of(ana(), pedro()));
        List<String> warnings = new ArrayList<>();
        List<User> users = storage.load(warnings);
        assertEquals(2, users.size());
        User loaded = users.get(0);
        assertEquals("Ana", loaded.getFirstName());
        assertEquals("Garcia Lopez", loaded.getSurnames());
        assertEquals("12345678Z", loaded.getDni());
        assertEquals("ana@correo.com", loaded.getEmail());
        assertEquals("Clave1!", loaded.getPassword());
        assertEquals("612345678", loaded.getPhone());
        assertEquals("1234567890123456", loaded.getCardNumber());
        assertEquals("87654321X", users.get(1).getDni());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void missingFileMeansNoUsers() throws Exception {
        assertTrue(new UserFileStorage(dir.resolve("none.txt")).load(new ArrayList<>()).isEmpty());
    }

    @Test
    void corruptLinesAreSkippedWithWarning() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, "broken;line", PEDRO_LINE));
        List<String> warnings = new ArrayList<>();
        List<User> users = new UserFileStorage(file).load(warnings);
        assertEquals(2, users.size());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("line 2"));
    }
}
