package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.model.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileRepositoryTest {

    @TempDir
    Path dir;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    private static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
                "Otra2#", "623456789", "9876543210987654");
    }

    @Test
    void missingFileMeansNoUsers() {
        UserFileRepository repository = new UserFileRepository(dir.resolve("users.txt"));
        assertTrue(repository.loadAll().isEmpty());
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserFileRepository(file).saveAll(List.of(ana(), pedro()));
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"), lines);
    }

    @Test
    void roundTripsUsers() {
        Path file = dir.resolve("users.txt");
        new UserFileRepository(file).saveAll(List.of(ana(), pedro()));
        List<User> loaded = new UserFileRepository(file).loadAll();
        assertEquals(2, loaded.size());
        User first = loaded.get(0);
        assertEquals("Ana", first.getFirstName());
        assertEquals("Garcia Lopez", first.getSurnames());
        assertEquals("12345678Z", first.getDni());
        assertEquals("ana@correo.com", first.getEmail());
        assertEquals("Clave1!", first.getPasswordHash());
        assertEquals("612345678", first.getPhone());
        assertEquals("1234567890123456", first.getCard());
        assertEquals("pedro@correo.com", loaded.get(1).getEmail());
    }

    @Test
    void savingEmptyCollectionClearsFile() throws IOException {
        Path file = dir.resolve("users.txt");
        UserFileRepository repository = new UserFileRepository(file);
        repository.saveAll(List.of(ana()));
        repository.saveAll(List.of());
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    void skipsMalformedAndBlankLinesWithWarning() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line"), StandardCharsets.UTF_8);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        List<User> users = new UserFileRepository(file, new PrintStream(err, true, StandardCharsets.UTF_8)).loadAll();
        assertEquals(1, users.size());
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("line 3"));
    }
}
