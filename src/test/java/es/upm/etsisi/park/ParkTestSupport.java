package es.upm.etsisi.park;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Starts the park singleton on an empty temporary directory with a fixed system date.
 */
abstract class ParkTestSupport {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    static final String TODAY_S = "2027-04-15";
    static final String YESTERDAY_S = "2027-04-14";
    static final String TOMORROW_S = "2027-04-16";
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @TempDir
    Path dir;

    Park park;

    @BeforeEach
    void startPark() {
        park = Park.getInstance();
        park.start(new ParkRepository(dir), TODAY);
    }

    /** Simulates a new execution reading the same data directory. */
    Park restart() {
        park.start(new ParkRepository(dir), TODAY);
        return park;
    }

    User ana() {
        return park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    User pedro() {
        return park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    static String dni(int number) {
        return String.format("%08d", number) + DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /** Creates {@code count} users user1..userN and a group containing all of them. */
    Group groupOf(String groupId, int count) {
        Group g = park.createGroup(groupId, "Group " + groupId);
        for (int i = 1; i <= count; i++) {
            String email = groupId.toLowerCase() + "u" + i + "@correo.com";
            park.createUser("U" + i, "Member", dni(groupId.hashCode() % 1000 * 1000 + i + 10_000_000), email,
                    "Clave1!", "600000000", "1111222233334444");
            park.addToGroup(groupId, email);
        }
        return g;
    }
}
