package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/** Shared test fixtures. */
public final class TestData {

    public static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    public static final String CREATE_LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave3$;600000000;1111222233334444";

    /** BCrypt with the minimum work factor, to keep tests fast. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestData() {
    }

    public static User user(String dni, String email) {
        return new User("N", "S", dni, email, "Clave1!", "6", "1234567890123456");
    }

    /** A group G01 with {@code members} distinct users. */
    public static Group groupOf(int members) {
        Group g = new Group("G01", "Familia");
        for (int i = 0; i < members; i++) {
            g.addMember(user(String.format("%08dX", i), "u" + i + "@correo.com"));
        }
        return g;
    }

    /** A command processor over the given main controller with the default help generators. */
    public static CommandProcessor processor(ParkController park) {
        return new CommandProcessor(park, new HelpGenerator(CommandCatalog.COMMANDS, new SyntaxHelpFormatter()),
                new HelpGenerator(CommandCatalog.COMMANDS, new ExtendedHelpFormatter()));
    }

    private static final int PASSWORD_FIELD = 4;

    /**
     * Asserts that user-file lines equal the expected ones written with plaintext passwords, where the
     * stored password field must be a BCrypt hash of that plaintext.
     */
    public static void assertUserLines(List<String> expectedWithPlainPasswords, List<String> actual) {
        assertEquals(expectedWithPlainPasswords.size(), actual.size(), actual.toString());
        for (int i = 0; i < actual.size(); i++) {
            String[] e = expectedWithPlainPasswords.get(i).split(";", -1);
            String[] a = actual.get(i).split(";", -1);
            assertTrue(BCrypt.checkpw(e[PASSWORD_FIELD], a[PASSWORD_FIELD]), actual.get(i));
            e[PASSWORD_FIELD] = a[PASSWORD_FIELD];
            assertEquals(String.join(";", e), actual.get(i));
        }
    }

    /** User-file lines with the (salted, hence run-dependent) password hash masked. */
    public static List<String> maskPasswords(List<String> lines) {
        return lines.stream().map(l -> {
            String[] f = l.split(";", -1);
            f[PASSWORD_FIELD] = "*";
            return String.join(";", f);
        }).toList();
    }
}
