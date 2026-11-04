package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationContextTest {

    @TempDir
    Path dir;

    @Test
    void startUsesFilesOfTheDataDirectory() throws IOException {
        try (ApplicationContext context = ApplicationContext.start(dir, LocalDate.of(2026, 10, 1))) {
            ParkController c = context.getController();
            assertSame(ParkController.getInstance(), c);
            assertTrue(c.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
            assertTrue(context.getTemporaryDirectory().isEmpty());
            assertEquals(dir, context.getDataDirectory());
            assertEquals(LocalDate.of(2026, 10, 1), c.getCurrentDate());
            assertEquals(ParkController.DEFAULT_PRICE, c.getCurrentPrice());
        }
        assertTrue(Files.exists(dir.resolve("users.txt")), "close keeps normal data");
        try (ApplicationContext again = ApplicationContext.start(dir)) {
            assertEquals(1, again.getController().users().getUsers().size());
        }
    }

    @Test
    void normalAndFlyUseDifferentPathsWithTheSameControllers() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456\n");
        ParkController normal;
        try (ApplicationContext context = ApplicationContext.start(dir)) {
            normal = context.getController();
            assertEquals(1, normal.users().getUsers().size());
        }
        Path tmpRoot = dir.resolve("tmp");
        ApplicationContext fly = ApplicationContext.startTemporary(tmpRoot);
        Path runDir = fly.getTemporaryDirectory().orElseThrow();
        assertNotEquals(dir, fly.getDataDirectory());
        assertTrue(runDir.startsWith(tmpRoot));
        assertSame(normal, fly.getController(), "same singleton controller");
        assertSame(UserManager.getInstance(), fly.getController().users());
        assertTrue(fly.getController().users().getUsers().isEmpty());
        assertTrue(fly.getController().createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6",
                "9876543210987654").success());
        assertTrue(Files.exists(runDir.resolve("users.txt")));
        fly.close();
        fly.close();
        assertFalse(Files.exists(runDir));
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
    }
}
