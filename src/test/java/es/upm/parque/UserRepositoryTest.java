package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserRepository(dir.resolve("none.txt")).loadAll().isEmpty());
    }

    @Test
    void roundTripWithExactLineFormat() throws IOException {
        Path file = dir.resolve("sub").resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        repo.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = repo.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Pedro", loaded.get(1).getName());
        assertEquals("9876543210987654", loaded.get(1).getCard());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("bad;line", "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456", ""));
        assertEquals(1, new UserRepository(file).loadAll().size());
    }

    @Test
    void unreadableOrUnwritableFileRaisesUncheckedIo() {
        UserRepository repo = new UserRepository(dir);
        List<User> none = List.of();
        assertThrows(UncheckedIOException.class, repo::loadAll);
        assertThrows(UncheckedIOException.class, () -> repo.saveAll(none));
    }
}
