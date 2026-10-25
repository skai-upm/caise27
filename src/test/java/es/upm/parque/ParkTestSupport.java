package es.upm.parque;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.presentation.CommandProcessor;

/**
 * Initialises the application on a test directory and drives its {@link CommandProcessor}.
 */
public class ParkTestSupport {

    public static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    /** Low BCrypt cost to keep tests fast. */
    public static final int TEST_LOG_ROUNDS = 4;

    public final Path dir;
    public final ParkController park = ParkController.getInstance();
    public final UserController users = UserController.getInstance();
    public final GroupController groups = GroupController.getInstance();
    public final TicketController tickets = TicketController.getInstance();
    public final PassController passes = PassController.getInstance();
    public final AttractionController attractions = AttractionController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor;

    public ParkTestSupport(Path dir, LocalDate today) {
        this.dir = dir;
        this.processor = ParkInitializer.initialize(dir, today, new BCryptPasswordHasher(TEST_LOG_ROUNDS),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Runs the commands and returns the last output line. */
    public String run(String... lines) {
        String[] out = output(lines).split(System.lineSeparator());
        return out[out.length - 1];
    }

    /** Runs the commands and returns their whole output. */
    public String output(String... lines) {
        buffer.reset();
        for (String line : lines) {
            processor.process(line);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** Re-initialises the application on the same data directory, simulating a restart. */
    public ParkTestSupport restart(LocalDate today) {
        return new ParkTestSupport(dir, today);
    }

    /** Creates users u0..u(n-1) with valid data and adds them to the group. */
    public void addMembers(String groupId, int n) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < n; i++) {
            int number = 20000000 + i;
            String dni = number + "" + letters.charAt(number % 23);
            String email = "u" + i + "@correo.com";
            run("USER CREATE;U" + i + ";Test;" + dni + ";" + email + ";Clave1!;600000000;1234567890123456",
                    "GROUP ADD;" + groupId + ";" + email);
        }
    }
}
