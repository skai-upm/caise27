package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.user.UserController;
import es.upm.etsisi.amusementpark.business.user.UserField;

/**
 * USER CREATE / DELETE / UPDATE.
 */
public final class UserCommands {

    private UserCommands() {
    }

    public static void register(CommandRegistry registry, UserController users) {
        registry.register(CommandSpec.of("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> {
                    users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    return CommandResult.message("USER CREATED");
                }));
        registry.register(CommandSpec.of("USER DELETE;<email>;<password>",
                "Deletes a user after checking the password, unless they hold active tickets or belong to a locked group.",
                p -> {
                    users.delete(p[1], p[2]);
                    return CommandResult.message("USER DELETED");
                }));
        registry.register(CommandSpec.of("USER UPDATE;<email>;<password>;<field>;<value>",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user; the DNI cannot change.",
                p -> {
                    UserField field = UserField.fromName(p[3])
                            .orElseThrow(() -> new CommandSyntaxException("field '" + p[3] + "' cannot be modified"));
                    users.update(p[1], p[2], field, p[4]);
                    return CommandResult.message("USER UPDATED");
                }));
    }
}
