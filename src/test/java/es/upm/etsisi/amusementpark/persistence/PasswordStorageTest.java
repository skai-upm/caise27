package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.User;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordStorageTest {

    private final ParkController controller = ParkController.getInstance();

    @TempDir
    Path directory;

    @AfterEach
    void detach() {
        controller.configure((es.upm.etsisi.amusementpark.business.repository.ParkRepositories) null);
    }

    @Test
    void onlyProtectedValueIsStoredAndVerifiedAfterReload() throws Exception {
        controller.configure(TextRepositories.inDirectory(directory));
        controller.reset();
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.setUserPassword("ana@correo.com", "Clave1!");
        controller.saveState();
        String file = Files.readString(directory.resolve("users.txt"));
        assertFalse(file.contains("Clave1!"));
        assertTrue(file.startsWith(";;12345678Z;ana@correo.com;$2a$"), file);
        controller.reset();
        assertTrue(controller.loadState().isEmpty());
        controller.authenticate("ana@correo.com", "Clave1!");
    }

    @Test
    void earlierTwoFieldFilesStillLoad() throws Exception {
        Files.writeString(directory.resolve("users.txt"), "12345678Z;ana@correo.com\n");
        controller.configure(TextRepositories.inDirectory(directory));
        controller.reset();
        assertTrue(controller.loadState().isEmpty());
        assertFalse(controller.findUser("ana@correo.com").orElseThrow().hasPassword());
    }
}
