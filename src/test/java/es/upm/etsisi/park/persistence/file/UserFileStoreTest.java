package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserFileStoreTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStore(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserFileStore(file).save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456")));

        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));

        List<User> users = new UserFileStore(file).load();

        assertEquals(2, users.size());
        assertEquals("pedro@correo.com", users.get(1).getEmail());
    }

    @Test
    void repositoryReloadsWhatItSaved() {
        Path file = dir.resolve("users.txt");
        UserRepository repository = new FileUserRepository(new UserFileStore(file));
        repository.add(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));

        UserRepository reloaded = new FileUserRepository(new UserFileStore(file));

        assertTrue(reloaded.findByDni("12345678Z").isPresent());
        assertTrue(reloaded.findByEmail("ANA@correo.com").isPresent());
    }
}
