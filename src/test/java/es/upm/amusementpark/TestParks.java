package es.upm.amusementpark;

import es.upm.amusementpark.persistence.ParkStore;
import es.upm.amusementpark.service.Park;
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

    private TestParks() {
    }

    public static Park open(Path directory) {
        return open(directory, TODAY);
    }

    public static Park open(Path directory, LocalDate today) {
        Park park = Park.getInstance();
        try {
            park.open(new ParkStore(directory,
                    new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)), today);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return park;
    }
}
