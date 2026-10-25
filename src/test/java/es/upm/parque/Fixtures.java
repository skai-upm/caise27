package es.upm.parque;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.User;

/**
 * Shared test data used by tests in every package.
 */
public final class Fixtures {

    public static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    public static final LocalDateTime START = LocalDateTime.of(2026, 8, 26, 19, 3, 15);

    private Fixtures() {
    }

    public static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    public static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    /**
     * A valid user number n (n &lt; 1000), with email u&lt;n&gt;@correo.com and password Clave1!.
     */
    public static User numbered(int n) {
        int number = 10_000_000 + n;
        String dni = number + String.valueOf("TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23));
        return new User("U" + n, "Apellido", dni, "u" + n + "@correo.com", "Clave1!", "600000000",
                "1234567890123456");
    }

    /**
     * Unregistered users with distinct placeholder DNIs, for model-level tests.
     */
    public static List<User> users(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456"));
        }
        return users;
    }
}
