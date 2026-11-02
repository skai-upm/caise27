package es.upm.etsisi.park.business;

import es.upm.etsisi.park.ParkApplication;
import es.upm.etsisi.park.persistence.TextParkRepository;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Starts the park through the composition root on an empty temporary directory with a fixed system date.
 */
public abstract class ParkTestSupport {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    public static final String TODAY_S = "2027-04-15";
    public static final String YESTERDAY_S = "2027-04-14";
    public static final String TOMORROW_S = "2027-04-16";
    /** Cheap BCrypt cost so tests stay fast; production uses the default cost. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @TempDir
    protected Path dir;

    protected Park park;
    protected UserController users;
    protected GroupController groups;
    protected TicketController tickets;
    protected PassController passes;
    protected AttractionController attractions;
    protected InvoiceController invoices;
    protected ParkServices services;

    @BeforeEach
    void startPark() {
        restart();
    }

    /** Simulates a new execution reading the same data directory. */
    protected Park restart() {
        services = ParkApplication.start(new TextParkRepository(dir), TODAY, HASHER);
        park = services.park();
        users = services.users();
        groups = services.groups();
        tickets = services.tickets();
        passes = services.passes();
        attractions = services.attractions();
        invoices = services.invoices();
        return park;
    }

    protected User ana() {
        return users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    protected User pedro() {
        return users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    public static String dni(int number) {
        return String.format("%08d", number) + DNI_LETTERS.charAt(number % DNI_LETTERS.length());
    }

    /** Creates {@code count} users and a group containing all of them. */
    protected Group groupOf(String groupId, int count) {
        Group g = groups.create(groupId, "Group " + groupId);
        for (int i = 1; i <= count; i++) {
            String email = groupId.toLowerCase() + "u" + i + "@correo.com";
            users.create("U" + i, "Member", dni(Math.abs(groupId.hashCode() % 1000) * 1000 + i + 10_000_000), email,
                    "Clave1!", "600000000", "1111222233334444");
            groups.add(groupId, email);
        }
        return g;
    }
}
