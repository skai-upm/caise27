package es.upm.park.persistence;

import es.upm.park.business.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private UserRepository repository() {
        return new UserRepository(dir.resolve("users.txt"), new PrintStream(warnings, true, StandardCharsets.UTF_8));
    }

    @Test
    void roundTripKeepsEveryField() throws IOException {
        repository().save(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$10$abc/def.",
                "612345678", "1234567890123456")));
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$abc/def.;612345678;1234567890123456",
                Files.readString(dir.resolve("users.txt")).strip());
        User loaded = repository().load().get(0);
        assertEquals("Ana", loaded.getFirstName());
        assertEquals("Garcia Lopez", loaded.getSurnames());
        assertEquals("12345678Z", loaded.getDni());
        assertEquals("ana@correo.com", loaded.getEmail());
        assertEquals("$2a$10$abc/def.", loaded.getPassword());
        assertEquals("612345678", loaded.getPhone());
        assertEquals("1234567890123456", loaded.getCard());
    }

    @Test
    void malformedLineIsSkippedWithWarning() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;h;612345678;1234567890123456\nRoto;linea\n");
        assertEquals(1, repository().load().size());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("Roto;linea"));
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(repository().load().isEmpty());
    }
}
