package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.controller.UserController;
import es.upm.etsisi.amusementpark.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Base for tests that need a freshly initialised park with Ana and Pedro registered.
 */
public abstract class ParkFixture {

    protected static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    @TempDir
    protected Path dir;

    protected ParkController park;
    protected UserController users;
    protected User ana;
    protected User pedro;

    @BeforeEach
    void initializePark() {
        park = ParkController.getInstance();
        park.initialize(dir);
        park.setDate(TODAY.toString());
        users = new UserController(park);
        ana = users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        pedro = users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    /** Reloads every repository from disk, as a new execution would. */
    protected void restart() {
        park.initialize(dir);
        park.setDate(TODAY.toString());
    }

    /** Registers an extra valid user whose DNI is derived from the given number. */
    protected User createUser(int n) {
        String number = String.format("%08d", n);
        String dni = number + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        return users.create("User" + n, "Test", dni, "user" + n + "@correo.com", "Clave1!", "600000000", "1111222233334444");
    }
}
