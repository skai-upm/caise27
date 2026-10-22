package es.upm.etsisi.park;

import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.business.security.PasswordHasher;
import es.upm.etsisi.park.persistence.DataPaths;
import es.upm.etsisi.park.persistence.file.FileRepositories;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Test fixture: loads the park singleton from a fresh directory with the assignment's sample users.
 */
public final class TestPark {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    /** Cheap BCrypt cost so tests stay fast. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestPark() {
    }

    /** (Re)configures the park singleton with fresh repositories over {@code dir}, as a restart would. */
    public static Park configure(Path dir) {
        Park park = Park.getInstance();
        park.configure(FileRepositories.open(new DataPaths(dir)), HASHER);
        return park;
    }

    /** Loads an empty park from {@code dir} with the date set to {@link #TODAY}. */
    public static Park empty(Path dir) {
        Park park = configure(dir);
        park.getState().setCurrentDate(TODAY);
        return park;
    }

    /** Loads a park with Ana, Pedro, Laura and Marta registered. */
    public static Park withUsers(Path dir) {
        Park park = empty(dir);
        park.getUserController().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        park.getUserController().create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        park.getUserController().create("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "634567890", "1111222233334444");
        park.getUserController().create("Marta", "Sanchez Perez", "00000000T", "marta@correo.com", "Marta4%", "645678901", "2222333344445555");
        return park;
    }

    /** Registers {@code count} extra users u0..u(count-1) and adds them to the group. */
    public static void addGeneratedMembers(Park park, String groupId, int count) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < count; i++) {
            int number = 20000000 + i;
            String dni = number + String.valueOf(letters.charAt(number % 23));
            String email = "u" + i + "@park.com";
            park.getUserController().create("U" + i, "Gen", dni, email, "Clave1!", "600000000", "1234567890123456");
            park.getGroupController().addMember(groupId, email);
        }
    }
}
