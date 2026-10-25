package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.StoragePaths;
import es.upm.parque.presentation.CommandProcessor;

/**
 * Starts the application on a test directory (with a fast BCrypt encoder) and offers helpers shared by tests.
 */
public final class TestContext {

    public static final LocalDate START = LocalDate.of(2026, 10, 1);
    public static final BCryptPasswordEncoder FAST_ENCODER = new BCryptPasswordEncoder(4);

    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private final Path dir;
    private final CommandProcessor processor;

    private TestContext(Path dir, LocalDate startDate) throws IOException {
        this.dir = dir;
        this.processor = ParkApplication.start(new StoragePaths(dir), startDate, FAST_ENCODER);
    }

    public static TestContext start(Path dir) throws IOException {
        return new TestContext(dir, START);
    }

    public static TestContext start(Path dir, LocalDate startDate) throws IOException {
        return new TestContext(dir, startDate);
    }

    /**
     * Simulates a new execution on the same data directory.
     */
    public TestContext restart() throws IOException {
        return new TestContext(dir, START);
    }

    public CommandProcessor processor() {
        return processor;
    }

    public String run(String line) {
        return processor.process(line);
    }

    public ParkController park() {
        return ParkController.getInstance();
    }

    public UserController users() {
        return UserController.getInstance();
    }

    public GroupController groups() {
        return GroupController.getInstance();
    }

    public TicketController tickets() {
        return TicketController.getInstance();
    }

    public PassController passes() {
        return PassController.getInstance();
    }

    public AttractionController attractions() {
        return AttractionController.getInstance();
    }

    public InvoiceController invoices() {
        return InvoiceController.getInstance();
    }

    public static void ok(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    public static void error(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    /**
     * A valid DNI for the given number.
     */
    public static String dni(int number) {
        return String.format("%08d", number) + DNI_LETTERS.charAt(number % 23);
    }

    public static String email(int number) {
        return "user" + number + "@correo.com";
    }

    /**
     * Creates user {@code number} with password Clave1!.
     */
    public User user(int number) {
        ok(users().createUser("Name" + number, "Surname" + number, dni(number), email(number), "Clave1!",
                "600000000", "1234567890123456"));
        return users().findByEmail(email(number));
    }

    /**
     * Creates group {@code id} with users 1..members (creating missing users).
     */
    public Group group(String id, int members) {
        ok(groups().createGroup(id, "Group " + id));
        for (int i = 1; i <= members; i++) {
            if (users().findByEmail(email(i)) == null) {
                user(i);
            }
            ok(groups().addToGroup(id, email(i)));
        }
        return groups().find(id);
    }
}
