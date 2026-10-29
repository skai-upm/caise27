package es.upm.etsisi.amusementpark;

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

    ParkController park;

    @BeforeEach
    void loadPark() throws Exception {
        park = ParkController.getInstance();
        park.load(new Storage(dir), TODAY);
        park.createUser("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
    }

    /**
     * Reloads the park from disk, as a new execution would.
     */
    void restart() {
        park.load(new Storage(dir), TODAY);
    }

    User user(String email) {
        return park.getUserService().findByEmail(email);
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
