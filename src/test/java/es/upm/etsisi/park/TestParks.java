package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/** Starts the park singleton on an isolated data directory for a test. */
final class TestParks {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private TestParks() {
    }

    static Park start(Path dataDir) throws IOException {
        Park park = Park.getInstance();
        park.start(new ParkStorage(dataDir), TODAY);
        return park;
    }
}
