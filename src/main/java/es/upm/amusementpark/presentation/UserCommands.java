package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.UserField;

/**
 * USER CREATE / DELETE / UPDATE commands.
 */
class UserCommands {

    private final UserController users;

    UserCommands(UserController users) {
        this.users = users;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(7, "USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card");
        users.create(in.get(1), in.get(2), in.get(3), in.get(4), in.get(5), in.get(6), in.get(7));
        return "USER CREATED";
    }

    String delete(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "USER DELETE;Email;Password");
        users.delete(in.get(1), in.get(2));
        return "USER DELETED";
    }

    String update(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(4, "USER UPDATE;Email;Password;Field;NewValue");
        UserField field = UserField.fromText(in.get(3));
        if (field == null) {
            throw new InvalidCommandException(
                    "Field cannot be modified. Use NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD");
        }
        users.update(in.get(1), in.get(2), field, in.get(4));
        return "USER UPDATED";
    }
}
