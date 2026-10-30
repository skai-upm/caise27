package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.UserController;

/**
 * USER CREATE / DELETE / UPDATE.
 */
public class UserCommands implements CommandModule {

    private final UserController users;

    public UserCommands(UserController users) {
        this.users = users;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> {
                    users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    return "USER CREATED";
                });
        registry.register("USER DELETE;<Email>;<Password>",
                "Deletes a user after checking the password; refused while the user has active tickets or is in a locked group.",
                p -> {
                    users.delete(p[1], p[2]);
                    return "USER DELETED";
                });
        registry.register("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the password; DNI cannot change.",
                p -> {
                    users.update(p[1], p[2], p[3], p[4]);
                    return "USER UPDATED";
                });
    }
}
