package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Opens the park singleton on a test data directory.
 */
public final class TestParks {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    /** Minimum BCrypt work factor, to keep the test suite fast. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestParks() {
    }

    public static Park open(Path directory) {
        return open(directory, TODAY);
    }

    public static Park open(Path directory, LocalDate today) {
        return start(ApplicationContext.fileRepositories(directory,
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)), today);
    }

    public static Park start(ApplicationContext.Repositories repositories, LocalDate today) {
        try {
            return ApplicationContext.start(repositories, today, HASHER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
