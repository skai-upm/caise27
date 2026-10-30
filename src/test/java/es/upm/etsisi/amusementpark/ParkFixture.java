package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Base for tests that need a freshly initialised park with Ana and Pedro registered.
 */
public abstract class ParkFixture {

    protected static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    /** Low-cost BCrypt so the suite stays fast; production uses the default cost. */
    public static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    @TempDir
    protected Path dir;

    protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
    protected CommandProcessor processor;
    protected ParkController park;
    protected UserController users;
    protected User ana;
    protected User pedro;

    @BeforeEach
    void initializePark() {
        start();
        users = UserController.getInstance();
        ana = users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        pedro = users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    /** Reloads every repository from disk, as a new execution would. */
    protected void restart() {
        start();
    }

    private void start() {
        processor = ApplicationInitializer.initialize(dir, FAST_HASHER,
                new PrintStream(output, true, StandardCharsets.UTF_8));
        park = ParkController.getInstance();
        park.setDate(TODAY.toString());
    }

    protected String output() {
        return output.toString(StandardCharsets.UTF_8);
    }

    /** Registers an extra valid user whose DNI is derived from the given number. */
    protected User createUser(int n) {
        String number = String.format("%08d", n);
        String dni = number + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        return users.create("User" + n, "Test", dni, "user" + n + "@correo.com", "Clave1!", "600000000", "1111222233334444");
    }
}
