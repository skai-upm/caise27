package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileStorageTest {

    private static final String ANA_LINE = "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO_LINE =
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void savesOneLinePerUserInAssignmentFormat() throws IOException {
        Path file = dir.resolve("data/users.txt");
        new UserFileStorage(file).save(List.of(ana()));
        assertEquals(List.of(ANA_LINE), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void loadsWhatWasSaved() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, PEDRO_LINE), StandardCharsets.UTF_8);
        List<User> users = new UserFileStorage(file).load();
        assertEquals(2, users.size());
        assertEquals("87654321X", users.get(1).getDni());
        assertEquals("Otra2#", users.get(1).getPassword());
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserFileStorage(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(ANA_LINE, "broken;line", ""), StandardCharsets.UTF_8);
        UserFileStorage storage = new UserFileStorage(file);
        assertEquals(1, storage.load().size());
        assertEquals(1, storage.getSkippedLines());
    }
}
