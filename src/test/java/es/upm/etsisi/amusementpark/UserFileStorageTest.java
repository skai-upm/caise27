package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStorage(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        UserFileStorage storage = new UserFileStorage(file);
        storage.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripAndSkipsBlankOrMalformedLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line"));
        List<User> users = new UserFileStorage(file).load();
        assertEquals(1, users.size());
        User ana = users.get(0);
        assertEquals("Garcia Lopez", ana.getSurname());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("1234567890123456", ana.getCard());
    }
}
