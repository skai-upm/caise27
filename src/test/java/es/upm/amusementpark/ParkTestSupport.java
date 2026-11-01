package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Shared fixture for command-level tests: a park stored in a temporary directory
 * that starts on a fixed date, plus helpers to run commands and restart.
 */
abstract class ParkTestSupport {

    static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @TempDir
    Path tempDir;

    Park park;
    CommandProcessor processor;

    @BeforeEach
    void startPark() throws IOException {
        park = new Park(new StorageConfig(tempDir), START);
        processor = new CommandProcessor(park);
    }

    /** Simulates a new execution on the same storage, starting on {@link #START}. */
    void restart() throws IOException {
        startPark();
    }

    String run(String command) {
        return processor.execute(command).output();
    }

    void ok(String command) {
        String output = run(command);
        assertTrue(output.startsWith("OK"), command + " -> " + output);
    }

    void error(String command) {
        String output = run(command);
        assertTrue(output.startsWith("ERROR:"), command + " -> " + output);
    }

    void expect(String expected, String command) {
        assertEquals(expected, run(command), command);
    }

    static String email(int n) {
        return "user" + n + "@correo.com";
    }

    static String dni(int n) {
        int number = 10000000 + n;
        return number + String.valueOf(DNI_LETTERS.charAt(number % 23));
    }

    /** Registers users 1..count with password {@code Clave1!}. */
    void createUsers(int count) {
        for (int n = 1; n <= count; n++) {
            ok("USER CREATE;Name" + n + ";Surname " + n + ";" + dni(n) + ";" + email(n)
                    + ";Clave1!;600000000;1234567890123456");
        }
    }

    /** Creates group {@code id} with users 1..members. */
    void createGroupWithUsers(String id, int members) {
        ok("GROUP CREATE;" + id + ";Group " + id);
        for (int n = 1; n <= members; n++) {
            ok("GROUP ADD;" + id + ";" + email(n));
        }
    }
}
