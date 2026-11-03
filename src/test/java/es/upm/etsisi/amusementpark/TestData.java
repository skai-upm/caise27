package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.SyntaxHelpFormatter;

/** Shared test fixtures. */
public final class TestData {

    public static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    public static final String CREATE_LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave3$;600000000;1111222233334444";

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
}
