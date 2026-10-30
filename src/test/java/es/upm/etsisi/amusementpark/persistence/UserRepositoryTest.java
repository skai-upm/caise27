package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    @TempDir
    Path dir;

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void missingFileMeansNoUsers() {
        UserRepository repo = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void writesOneLinePerUserInSourceFormat() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserRepository(new UserFileStore(file)).add(ana());
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
    }

    @Test
    void usersSurviveReload() {
        Path file = dir.resolve("users.txt");
        UserRepository first = new UserRepository(new UserFileStore(file));
        first.add(ana());
        User u = first.findByEmail("ana@correo.com").orElseThrow();
        u.setPhone("699999999");
        first.update(u);

        UserRepository second = new UserRepository(new UserFileStore(file));
        User loaded = second.findByDni("12345678Z").orElseThrow();
        assertEquals("699999999", loaded.getPhone());
        assertEquals("Garcia Lopez", loaded.getSurnames());
    }

    @Test
    void removalIsPersisted() {
        Path file = dir.resolve("users.txt");
        UserRepository first = new UserRepository(new UserFileStore(file));
        first.add(ana());
        first.remove(first.findByEmail("ana@correo.com").orElseThrow());
        assertTrue(new UserRepository(new UserFileStore(file)).findAll().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "broken;line",
                "",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        UserRepository repo = new UserRepository(new UserFileStore(file));
        assertEquals(1, repo.findAll().size());
        assertTrue(repo.findByEmail("pedro@correo.com").isPresent());
    }
}
