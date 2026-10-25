package es.upm.etsisi.park;

import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.presentation.CommandProcessor;
import es.upm.etsisi.park.presentation.ParkInitializer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Wires the application for tests: fresh controllers on a temporary directory and a fast BCrypt hasher.
 */
public final class TestSupport {

    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestSupport() {
    }

    public static ParkController park(Path dir) {
        return park(dir, new PrintStream(new ByteArrayOutputStream()));
    }

    public static ParkController park(Path dir, PrintStream warnings) {
        return ParkInitializer.initialize(ParkStorage.inDirectory(dir, warnings), HASHER, LocalDate.now());
    }

    public static CommandProcessor processor(ParkController park) {
        return ParkInitializer.commandProcessor(park);
    }
}
