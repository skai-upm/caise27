package es.upm.amusementpark;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Shared setup for tests that use the park controller: fresh data directory, fixed system date.
 */
abstract class ParkTestSupport {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 1);

    @TempDir
    Path dir;

    ParkController park;

    @BeforeEach
    void initPark() {
        park = ParkController.getInstance();
        park.initialize(dir);
        park.setCurrentDate(TODAY);
    }

    void reload() {
        park.initialize(dir);
        park.setCurrentDate(TODAY);
    }

    static String dni(int number) {
        return String.format("%08d", number) + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23);
    }

    /** Creates user number n with email u{n}@correo.com and password Clave1!. */
    User user(int n) {
        String email = "u" + n + "@correo.com";
        park.createUser("Name" + n, "Surname" + n, dni(n), email, "Clave1!", "600000000", "1234567890123456");
        return park.getUserService().findByEmail(email);
    }

    static String email(int n) {
        return "u" + n + "@correo.com";
    }
}
