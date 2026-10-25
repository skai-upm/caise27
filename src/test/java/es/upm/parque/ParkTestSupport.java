package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.User;
import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs commands against the application wired for a temporary data directory.
 */
abstract class ParkTestSupport {

    static final String ANA = "ana@correo.com";
    static final String PEDRO = "pedro@correo.com";

    /**
     * Minimum BCrypt cost, to keep tests fast.
     */
    static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    @TempDir
    Path dataDir;

    CommandProcessor processor;

    @BeforeEach
    void startPark() {
        restart();
    }

    void restart() {
        processor = ParkApplication.start(dataDir, FAST_HASHER);
    }

    ParkController park() {
        return ParkController.getInstance();
    }

    UserController users() {
        return UserController.getInstance();
    }

    GroupController groups() {
        return GroupController.getInstance();
    }

    TicketController tickets() {
        return TicketController.getInstance();
    }

    PassController passes() {
        return PassController.getInstance();
    }

    AttractionController attractions() {
        return AttractionController.getInstance();
    }

    String run(String command) {
        return processor.process(command).output();
    }

    void ok(String command) {
        String output = run(command);
        assertTrue(output.startsWith("OK"), command + " -> " + output);
    }

    void fails(String command) {
        String output = run(command);
        assertTrue(output.startsWith("ERROR"), command + " -> " + output);
    }

    void createAna() {
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;" + ANA + ";Clave1!;612345678;1234567890123456");
    }

    void createPedro() {
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;" + PEDRO + ";Otra2#;623456789;9876543210987654");
    }

    /**
     * Creates {@code count} extra users user1..userN and returns their emails.
     */
    String[] createUsers(int count) {
        String[] emails = new String[count];
        for (int i = 1; i <= count; i++) {
            int number = 10000000 + i;
            char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23);
            emails[i - 1] = "user" + i + "@correo.com";
            ok("USER CREATE;User" + i + ";Test;" + number + letter + ";" + emails[i - 1]
                    + ";Clave1!;600000000;1234567890123456");
        }
        return emails;
    }

    User user(String email) {
        return users().findByEmail(email);
    }
}
