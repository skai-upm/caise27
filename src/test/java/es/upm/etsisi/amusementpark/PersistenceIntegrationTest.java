package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.User;
import es.upm.etsisi.amusementpark.persistence.ParkStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenceIntegrationTest {

    @TempDir
    Path workDir;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private long temporaryDirectories() throws Exception {
        Path tmp = workDir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> children = Files.list(tmp)) {
            return children.count();
        }
    }

    @Test
    void stateSurvivesRestartButPriceAndOccupantsDoNot() {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20\nGROUP CREATE;G01;Familia\nSYSTEM PRICE;45.50\nEXIT\n");
        assertTrue(Files.exists(workDir.resolve("data").resolve("attractions.txt")));
        String out = run("ATTRACTION STATUS;A01\nGROUP CREATE;G01;Otra\nEXIT\n");
        assertTrue(out.contains("NAME: Dragon Rojo"), out);
        assertTrue(out.contains("USERS INSIDE: 0"), out);
        assertTrue(out.contains("ERROR: Group already exists: G01"), out);
        assertTrue(ParkController.getInstance().getCurrentPrice().toPlainString().equals("40.00"));
    }

    @Test
    void lockedGroupStaysLockedAfterRestart() {
        // no USER CREATE command exists yet: store the users of a previous execution directly
        ParkController controller = ParkController.getInstance();
        controller.reset();
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.addUser(new User("87654321X", "pedro@correo.com"));
        controller.createGroup("G02", "Amigos");
        controller.addUserToGroup("G02", "ana@correo.com");
        new ParkStorage(workDir.resolve("data")).save(controller);
        run("GROUPTICKET CREATE;TG01;G02;2099-01-01\nEXIT\n");
        String out = run("GROUP ADD;G02;pedro@correo.com\nEXIT\n");
        assertTrue(out.contains("ERROR: Group G02 has an active group ticket"), out);
    }

    @Test
    void flyModeWithFileUsesAndDeletesTemporaryDirectory() throws Exception {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20\nEXIT\n");
        Path file = workDir.resolve("input.txt");
        Files.writeString(file, "ATTRACTION LIST;A01\nATTRACTION CREATE;A02;Noria;5\nEXIT\n");
        String out = run("", "--fly", file.toString());
        assertTrue(out.contains("ERROR: Attraction not found: A01"), out);
        assertTrue(out.contains("ATTRACTION CREATED"), out);
        assertTrue(Files.exists(workDir.resolve("tmp")));
        assertTrue(temporaryDirectories() == 0);
        assertFalse(Files.readString(workDir.resolve("data").resolve("attractions.txt")).contains("A02"));
    }

    @Test
    void flyModeInteractiveAndEndOfFileCleanup() throws Exception {
        run("ATTRACTION CREATE;A09;Noria;5\nEXIT\n", "--fly");
        assertTrue(temporaryDirectories() == 0);
        Path file = workDir.resolve("input.txt");
        Files.writeString(file, "ATTRACTION CREATE;A09;Noria;5\n");
        run("", "--fly", file.toString());
        assertTrue(temporaryDirectories() == 0);
        assertFalse(Files.exists(workDir.resolve("data")));
    }

    @Test
    void argumentsWithoutFlyUnchanged() throws Exception {
        Path file = workDir.resolve("input.txt");
        Files.writeString(file, "HELP\nEXIT\n");
        assertTrue(run("", file.toString()).contains("Available commands:"));
        assertTrue(run("HELP\nEXIT\n").contains("Available commands:"));
    }
}
