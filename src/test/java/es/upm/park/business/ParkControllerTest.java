package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    @TempDir
    Path dir;

    @Test
    void openUsesFilesOfTheDataDirectory() throws IOException {
        try (ParkController c = ParkController.open(dir, LocalDate.of(2026, 10, 1))) {
            assertTrue(c.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
            assertTrue(c.getTemporaryDirectory().isEmpty());
            assertEquals(LocalDate.of(2026, 10, 1), c.getCurrentDate());
            assertEquals(ParkController.DEFAULT_PRICE, c.getCurrentPrice());
        }
        assertTrue(Files.exists(dir.resolve("users.txt")), "close keeps normal data");
        try (ParkController again = ParkController.open(dir)) {
            assertEquals(1, again.users().getUsers().size());
        }
    }

    @Test
    void temporaryControllerStartsEmptyAndDeletesItsDirectoryOnClose() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456\n");
        Path tmpRoot = dir.resolve("tmp");
        ParkController c = ParkController.openTemporary(tmpRoot);
        Path runDir = c.getTemporaryDirectory().orElseThrow();
        assertTrue(runDir.startsWith(tmpRoot));
        assertTrue(c.users().getUsers().isEmpty());
        assertTrue(c.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654").success());
        assertTrue(Files.exists(runDir.resolve("users.txt")));
        c.close();
        c.close();
        assertFalse(Files.exists(runDir));
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
    }
}
