package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.StoragePaths;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/** Runs commands through a real {@link CommandProcessor} backed by a temporary data directory. */
abstract class ParkTestSupport {

    static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    ParkController park;
    private CommandProcessor processor;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @BeforeEach
    void startPark() throws IOException {
        restart();
        run("SYSTEM DATE;2026-10-01");
    }

    /** Builds a fresh controller over the same data directory, as a new execution would. */
    void restart() throws IOException {
        park = new ParkController(new StoragePaths(dir), LocalDate.of(2026, 10, 1));
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Executes one command and returns what it printed, without the trailing line break. */
    String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).strip();
    }

    /** Creates a user with a DNI derived from {@code n} and email {@code u<n>@correo.com}. */
    void createUser(int n) {
        String digits = String.format("%08d", n);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        String result = run("USER CREATE;U" + n + ";Apellido;" + digits + letter + ";u" + n
                + "@correo.com;Clave1!;600000000;1234567890123456");
        if (!result.startsWith("OK")) {
            throw new IllegalStateException(result);
        }
    }

    static boolean ok(String output) {
        return output.startsWith("OK");
    }

    static boolean error(String output) {
        return output.startsWith("ERROR:");
    }
}
