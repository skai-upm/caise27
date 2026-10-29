package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.StorageConfiguration;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Test wiring: file repositories in a given directory and a low-cost BCrypt hasher.
 */
public final class TestParks {

    /** BCrypt with the minimum cost factor, so tests stay fast while still using real hashes. */
    public static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    private TestParks() {
    }

    public static Park initialize(Path dataDir, LocalDate today) throws PersistenceException {
        StorageConfiguration storage =
                StorageConfiguration.persistent(dataDir, new PrintStream(OutputStream.nullOutputStream()));
        return ParkInitializer.initialize(storage.createRepositories(), FAST_HASHER, today);
    }
}
