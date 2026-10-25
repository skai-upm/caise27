package es.upm.parque;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Drives a {@link CommandProcessor} over a {@link ParkController} stored in a test directory.
 */
class ParkTestSupport {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    static final String CREATE_LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis1#;600000001;1111222233334444";

    final Path dir;
    final ParkController park;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor;

    ParkTestSupport(Path dir, LocalDate today) {
        this.dir = dir;
        this.park = new ParkController(dir, today);
        this.processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Runs the commands and returns the last output line. */
    String run(String... lines) {
        buffer.reset();
        for (String line : lines) {
            processor.process(line);
        }
        String[] out = buffer.toString(StandardCharsets.UTF_8).split(System.lineSeparator());
        return out[out.length - 1];
    }

    /** Runs the commands and returns their whole output. */
    String output(String... lines) {
        buffer.reset();
        for (String line : lines) {
            processor.process(line);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** A new controller on the same data directory, simulating a restart. */
    ParkTestSupport restart(LocalDate today) {
        return new ParkTestSupport(dir, today);
    }

    /** Creates users u0..u(n-1) with valid data and adds them to the group. */
    void addMembers(String groupId, int n) {
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
