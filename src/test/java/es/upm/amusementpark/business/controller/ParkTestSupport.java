package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.StorageLocation;
import es.upm.amusementpark.presentation.ApplicationBootstrap;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.help.ConciseHelpFormatter;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Runs commands against a {@link ParkController} stored in a directory and returns their output.
 */
public class ParkTestSupport {

    public static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    /** Low-cost BCrypt hasher to keep tests fast. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private final Path dir;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private ParkController park;
    private CommandProcessor processor;

    public ParkTestSupport(Path dir) throws IOException {
        this.dir = dir;
        restart();
    }

    /** Simulates a new execution on the same storage directory. */
    public final void restart() throws IOException {
        processor = ApplicationBootstrap.start(new StorageLocation(dir), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                HASHER, new ConciseHelpFormatter(), new ExtendedHelpFormatter());
        park = ParkController.getInstance();
    }

    public ParkController park() {
        return park;
    }

    /** Runs the commands and returns the output of the last one (trimmed). */
    public String run(String... commands) {
        String last = "";
        for (String c : commands) {
            buffer.reset();
            processor.process(c);
            last = buffer.toString(StandardCharsets.UTF_8).trim();
        }
        return last;
    }

    /** Creates a valid user number {@code i} (DNI computed with the checksum) and returns its email. */
    public String createUser(int i) {
        int number = 10000000 + i;
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23);
        String email = "user" + i + "@correo.com";
        run("USER CREATE;User" + i + ";Test;" + number + letter + ";" + email + ";Clave1!;600000000;1234567890123456");
        return email;
    }

    /** Creates group {@code id} with {@code size} new users. */
    public void createGroup(String id, int size, int firstUser) {
        run("GROUP CREATE;" + id + ";Grupo " + id);
        for (int i = 0; i < size; i++) {
            run("GROUP ADD;" + id + ";" + createUser(firstUser + i));
        }
    }
}
