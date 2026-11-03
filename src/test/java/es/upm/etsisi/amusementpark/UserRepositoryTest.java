package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    private static final String ANA = "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private UserRepository repository(Path file) {
        return new UserRepository(file, new PrintStream(warnings, true));
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repository = repository(file);
        repository.save(List.of(UserRepository.parse(ANA), UserRepository.parse(PEDRO)));
        assertEquals(List.of(ANA, PEDRO), Files.readAllLines(file));
    }

    @Test
    void roundTripsUsers() throws Exception {
        Path file = dir.resolve("users.txt");
        repository(file).save(List.of(UserRepository.parse(ANA)));
        List<User> loaded = repository(file).load();
        assertEquals(1, loaded.size());
        assertEquals(ANA, UserRepository.format(loaded.get(0)));
    }

    @Test
    void missingFileMeansNoUsers() throws ParkException {
        assertEquals(0, repository(dir.resolve("absent.txt")).load().size());
    }

    @Test
    void skipsMalformedAndDuplicateLinesWithWarning() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA, "", "garbage", "Bad;Dni;12345678A;b@correo.com;Clave1!;1;1234567890123456",
                "Dup;Email;87654321X;ana@correo.com;Clave1!;1;1234567890123456", PEDRO));
        List<User> loaded = repository(file).load();
        assertEquals(2, loaded.size());
        assertEquals("Pedro", loaded.get(1).getFirstName());
        assertTrue(warnings.toString().contains("line 3"));
        assertTrue(warnings.toString().contains("line 5"));
    }

    @Test
    void unreadableFileIsReportedAsParkException() throws IOException {
        Path directory = Files.createDirectory(dir.resolve("users.txt"));
        assertThrows(ParkException.class, () -> repository(directory).load());
    }

    @Test
    void saveFailureKeepsPreviousFile() throws Exception {
        Path file = dir.resolve("users.txt");
        repository(file).save(List.of(UserRepository.parse(ANA)));
        Files.createDirectory(dir.resolve("users.txt.tmp"));
        Files.writeString(dir.resolve("users.txt.tmp").resolve("block"), "x");
        assertThrows(ParkException.class, () -> repository(file).save(List.of()));
        assertEquals(List.of(ANA), Files.readAllLines(file));
    }
}
