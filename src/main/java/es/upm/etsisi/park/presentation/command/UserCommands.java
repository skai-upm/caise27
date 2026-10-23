package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.business.controller.UserController;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.user.UserField;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** USER CREATE, USER DELETE and USER UPDATE. */
public class UserCommands implements CommandGroup {

    static final String CREATE = "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String DELETE = "USER DELETE;<email>;<password>";
    static final String UPDATE = "USER UPDATE;<email>;<password>;<field>;<value>";

    private final UserController users;

    public UserCommands(UserController users) {
        this.users = users;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("USER CREATE",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                (args, out) -> {
                    Args.require(args, CREATE, 7);
                    users.create(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
                    out.println("USER CREATED");
                }, CREATE));
        registry.register(CommandDefinition.of("USER DELETE",
                "Deletes a user after checking the password; not allowed with active tickets or locked groups.",
                (args, out) -> {
                    Args.require(args, DELETE, 2);
                    users.delete(args[0], args[1]);
                    out.println("USER DELETED");
                }, DELETE));
        registry.register(CommandDefinition.of("USER UPDATE",
                "Changes one field of a user after checking the password; the DNI cannot be changed.",
                (args, out) -> {
                    Args.require(args, UPDATE, 4);
                    UserField field = UserField.fromName(args[2]);
                    if (field == null) {
                        throw new ValidationException("Field '" + args[2] + "' cannot be modified. Modifiable fields: "
                                + UserField.names());
                    }
                    users.update(args[0], args[1], field, args[3]);
                    out.println("USER UPDATED");
                }, UPDATE).withNotes("<field> is one of: " + UserField.names()));
    }
}
