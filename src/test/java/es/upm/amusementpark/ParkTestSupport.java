package es.upm.amusementpark;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Opens the park singleton on a fresh temporary directory before each test and offers helpers.
 */
abstract class ParkTestSupport {

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @TempDir
    Path dir;

    Park park;
    UserService users;
    GroupService groups;
    TicketService tickets;
    AttractionService attractions;
    SystemService system;

    @BeforeEach
    void openPark() {
        park = Park.getInstance();
        park.open(dir);
        users = new UserService();
        groups = new GroupService();
        tickets = new TicketService();
        attractions = new AttractionService();
        system = new SystemService();
        system.setDate("2027-04-10");
    }

    static String dni(int n) {
        return String.format("%08d", n) + DNI_LETTERS.charAt(n % 23);
    }

    static String email(int n) {
        return "u" + n + "@correo.com";
    }

    User user(int n) {
        return users.create("User" + n, "Surname" + n, dni(n), email(n), "Clave1!", "600000000", "1234567890123456");
    }

    Group groupWith(String id, int members) {
        Group group = groups.create(id, "Group " + id);
        for (int n = 1; n <= members; n++) {
            User member = park.getUsers().findByEmail(email(n));
            if (member == null) {
                user(n);
            }
            groups.add(id, email(n));
        }
        return group;
    }

    /**
     * Reloads the whole park from disk, as a new execution would.
     */
    void restart() {
        park.open(dir);
    }
}
