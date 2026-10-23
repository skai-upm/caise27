package es.upm.park.service;

import es.upm.park.model.User;
import es.upm.park.persistence.DataPaths;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Opens the park singleton on a fresh data directory with the users of the
 * assignment's Delivery 2 example and the system date 2027-04-15.
 */
public final class ParkFixture {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private ParkFixture() {
    }

    public static Park openEmpty(Path dir) {
        Park park = Park.getInstance();
        park.open(new DataPaths(dir));
        park.setCurrentDate(TODAY);
        return park;
    }

    public static Park openWithUsers(Path dir) throws OperationException {
        Park park = openEmpty(dir);
        UserService users = park.users();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        users.create("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "634567890",
                "1111222233334444");
        users.create("Marta", "Sanchez Perez", "00000000T", "marta@correo.com", "Marta4%", "645678901",
                "2222333344445555");
        return park;
    }

    /**
     * Creates extra valid users {@code userN@correo.com} with DNIs derived
     * from {@code 200000NN}.
     */
    public static void addUsers(Park park, int count) throws OperationException {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < count; i++) {
            int number = 20000000 + i;
            String dni = number + "" + letters.charAt(number % 23);
            park.users().create("User" + i, "Extra", dni, "user" + i + "@correo.com", "Clave1!", "600000000",
                    "1234567890123456");
        }
    }

    public static User user(Park park, String email) {
        return park.users().findByEmail(email);
    }

    public static BigDecimal price(String amount) {
        return new BigDecimal(amount);
    }
}
