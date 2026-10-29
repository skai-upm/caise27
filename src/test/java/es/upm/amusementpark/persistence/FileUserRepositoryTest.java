package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUserRepositoryTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private FileUserRepository repository(Path file) {
        return new FileUserRepository(file, new PrintStream(warnings, true));
    }

    @Test
    void savesOneLinePerUserInAssignmentFormat() throws IOException {
        Path file = dir.resolve("users.txt");
        repository(file).saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripsUsers() throws IOException {
        Path file = dir.resolve("users.txt");
        repository(file).saveAll(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456")));

        List<User> loaded = repository(file).load();

        assertEquals(1, loaded.size());
        User ana = loaded.get(0);
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPasswordHash());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(repository(dir.resolve("absent.txt")).load().isEmpty());
    }

    @Test
    void skipsMalformedLinesWithWarning() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, String.join("\n",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "broken;line",
                "",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));

        List<User> loaded = repository(file).load();

        assertEquals(2, loaded.size());
        assertTrue(warnings.toString().contains("WARNING"));
    }
}
