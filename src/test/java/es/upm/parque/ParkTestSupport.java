package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Helpers shared by controller tests.
 */
final class ParkTestSupport {

    static final LocalDate START = LocalDate.of(2026, 10, 1);

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private ParkTestSupport() {
    }

    static ParkController park(Path dir) throws IOException {
        return new ParkController(new StoragePaths(dir), START);
    }

    static ParkController restart(Path dir) throws IOException {
        return new ParkController(new StoragePaths(dir), START);
    }

    static void ok(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    static void error(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    static void equalsResult(String expected, String result) {
        assertEquals(expected, result);
    }

    /**
     * A valid DNI for the given number.
     */
    static String dni(int number) {
        return String.format("%08d", number) + DNI_LETTERS.charAt(number % 23);
    }

    static String email(int number) {
        return "user" + number + "@correo.com";
    }

    /**
     * Creates user {@code number} with password Clave1!.
     */
    static User user(ParkController park, int number) {
        ok(park.createUser("Name" + number, "Surname" + number, dni(number), email(number), "Clave1!", "600000000",
                "1234567890123456"));
        return park.getUserManager().findByEmail(email(number));
    }

    /**
     * Creates group {@code id} with users 1..members.
     */
    static Group group(ParkController park, String id, int members) {
        ok(park.createGroup(id, "Group " + id));
        for (int i = 1; i <= members; i++) {
            if (park.getUserManager().findByEmail(email(i)) == null) {
                user(park, i);
            }
            ok(park.addToGroup(id, email(i)));
        }
        return park.getGroupManager().find(id);
    }
}
