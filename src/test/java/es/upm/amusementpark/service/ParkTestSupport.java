package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.ParkStorage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Initialises the park singleton on a fresh temporary directory with system date {@link #TODAY}.
 */
abstract class ParkTestSupport {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    static final String TODAY_TEXT = "2027-04-15";
    static final String TOMORROW_TEXT = "2027-04-16";
    static final String YESTERDAY_TEXT = "2027-04-14";
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @TempDir
    Path dir;

    Park park;

    @BeforeEach
    void initPark() throws IOException {
        park = Park.getInstance();
        park.initialize(storage(), TODAY);
    }

    ParkStorage storage() {
        return ParkStorage.persistent(dir, new PrintStream(OutputStream.nullOutputStream()));
    }

    /**
     * Simulates a new execution on the same data directory, keeping the current system date.
     */
    void restart() throws IOException {
        park.initialize(storage(), park.getCurrentDate());
    }

    User createAna() throws OperationException {
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        return park.findUser("ana@correo.com");
    }

    User createPedro() throws OperationException {
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        return park.findUser("pedro@correo.com");
    }

    /**
     * Creates a valid user {@code user<n>@correo.com} with password {@code Clave1!}.
     */
    User createUser(int n) throws OperationException {
        String dni = String.format("%08d", n) + DNI_LETTERS.charAt(n % 23);
        String email = "user" + n + "@correo.com";
        park.createUser("User" + n, "Test", dni, email, "Clave1!", "600000000", "1111222233334444");
        return park.findUser(email);
    }

    /**
     * Creates group {@code id} with {@code size} fresh users numbered from {@code firstUser}.
     */
    void createGroupWith(String id, int firstUser, int size) throws OperationException {
        park.createGroup(id, "Group " + id);
        for (int i = 0; i < size; i++) {
            park.addToGroup(id, createUser(firstUser + i).getEmail());
        }
    }
}
