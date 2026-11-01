package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserStorageTest {

    @TempDir
    Path dir;

    @Test
    public void missingFileMeansNoUsers() throws IOException {
        assertTrue(new UserStorage(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    public void roundTripUsesSemicolonLines() throws IOException {
        Path file = dir.resolve("users.txt");
        UserStorage storage = new UserStorage(file);
        storage.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = storage.load();
        assertEquals(2, loaded.size());
        assertEquals("Garcia Lopez", loaded.get(0).getSurname());
        assertEquals("9876543210987654", loaded.get(1).getCard());
    }

    @Test
    public void blankAndMalformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "broken;line"));
        assertEquals(1, new UserStorage(file).load().size());
    }
}
