package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Loads the singleton park on a fresh temporary directory with Ana and Pedro, on 2027-04-15.
 */
abstract class ParkFixture {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    static final String ANA = "ana@correo.com";
    static final String PEDRO = "pedro@correo.com";

    @TempDir
    Path dir;

    /** Low-cost BCrypt so tests stay fast; production uses the default cost. */
    static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    ParkController park;

    @BeforeEach
    void loadPark() throws Exception {
        App.bootstrap(new Storage(dir), TODAY, FAST_HASHER);
        park = ParkController.getInstance();
        park.createUser("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
    }

    /**
     * Reloads the park from disk, as a new execution would.
     */
    void restart() {
        App.bootstrap(new Storage(dir), TODAY, FAST_HASHER);
    }

    CommandProcessor processor() {
        return new CommandProcessor(park, InvoiceController.getInstance(), new HelpGenerator(new BasicHelpFormat()),
                new HelpGenerator(new ExtendedHelpFormat()));
    }

    User user(String email) {
        return park.getUserController().findByEmail(email);
    }

    /**
     * Checks a stored user line against the expected line written with the plain password: every
     * field must match except the password, which must be a BCrypt hash of it.
     */
    static void assertStoredUser(String expectedWithPlainPassword, String storedLine) {
        String[] expected = expectedWithPlainPassword.split(";", -1);
        String[] stored = storedLine.split(";", -1);
        org.junit.jupiter.api.Assertions.assertEquals(expected.length, stored.length);
        for (int i = 0; i < expected.length; i++) {
            if (i == 4) {
                org.junit.jupiter.api.Assertions.assertTrue(stored[4].startsWith("$2a$"), stored[4]);
                org.junit.jupiter.api.Assertions.assertTrue(FAST_HASHER.matches(expected[4], stored[4]));
            } else {
                org.junit.jupiter.api.Assertions.assertEquals(expected[i], stored[i]);
            }
        }
    }

    static String validDni(int number) {
        String digits = String.format("%08d", number);
        return digits + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23);
    }

    /**
     * Creates {@code count} extra users (u0@correo.com ...) and adds them to the group.
     */
    void addMembers(String groupId, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            String email = "u" + i + "@correo.com";
            park.createUser("U" + i, "Test", validDni(10000000 + i), email, "Clave1!", "600000000",
                    "1111222233334444");
            park.addToGroup(groupId, email);
        }
    }
}
