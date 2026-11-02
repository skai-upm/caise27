package es.upm.etsisi.amusementpark.park;

import es.upm.etsisi.amusementpark.persistence.ParkStorage;
import es.upm.etsisi.amusementpark.user.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

/** Re-initialises the park singleton on a temporary directory for tests. */
public final class ParkFixture {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private ParkFixture() {
    }

    public static Park reset(Path dir) throws IOException {
        return reset(dir, TODAY);
    }

    public static Park reset(Path dir, LocalDate date) throws IOException {
        Park park = Park.getInstance();
        park.initialize(new ParkStorage(dir, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)), date);
        return park;
    }

    /** A valid user whose DNI number is {@code n} (0..99999999). */
    public static User user(int n) {
        String number = String.format("%08d", n);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        return new User("User" + n, "Surname", number + letter, "u" + n + "@correo.com",
                "Clave1!", "600000000", "1234567890123456");
    }

    public static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    public static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
                "Otra2#", "623456789", "9876543210987654");
    }
}
