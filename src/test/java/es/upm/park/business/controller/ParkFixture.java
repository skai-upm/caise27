package es.upm.park.business.controller;

import es.upm.park.App;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.User;
import es.upm.park.business.security.BCryptPasswordHasher;
import es.upm.park.business.security.PasswordHasher;
import es.upm.park.persistence.DataPaths;
import es.upm.park.persistence.file.FileRepositories;
import es.upm.park.presentation.CommandProcessor;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Wires the application singletons on a fresh data directory (file
 * repositories, low-cost BCrypt) with the system date 2027-04-15, optionally
 * with the users of the assignment's examples.
 */
public final class ParkFixture {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    public static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    private ParkFixture() {
    }

    public static CommandProcessor.Controllers initialize(Path dir) {
        CommandProcessor.Controllers controllers =
                App.initialize(FileRepositories.create(new DataPaths(dir)), FAST_HASHER);
        controllers.park().setCurrentDate(TODAY);
        return controllers;
    }

    public static Park openEmpty(Path dir) {
        return initialize(dir).park();
    }

    public static Park openWithUsers(Path dir) throws ParkException {
        Park park = openEmpty(dir);
        UserController users = UserController.getInstance();
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
     * Reloads the stored state as a new execution would, keeping the test date.
     */
    public static Park restart(Path dir) {
        return openEmpty(dir);
    }

    /**
     * Creates extra valid users {@code userN@correo.com} with DNIs derived
     * from {@code 200000NN}.
     */
    public static void addUsers(int count) throws ParkException {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < count; i++) {
            int number = 20000000 + i;
            String dni = number + "" + letters.charAt(number % 23);
            UserController.getInstance().create("User" + i, "Extra", dni, "user" + i + "@correo.com", "Clave1!",
                    "600000000", "1234567890123456");
        }
    }

    public static User user(Park park, String email) {
        return park.findUserByEmail(email);
    }

    public static BigDecimal price(String amount) {
        return new BigDecimal(amount);
    }
}
