package es.upm.parque.presentation;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.UserManager;
import es.upm.parque.business.model.User;

/**
 * Parses the USER commands and formats their responses; the rules live in {@link UserManager}.
 */
class UserCommands {

    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";

    private final UserManager users;

    UserCommands(UserManager users) {
        this.users = users;
    }

    String create(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 8,
                "USER CREATE expects 7 fields: USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card");
        return ok(users.create(e[1], e[2], e[3], e[4], e[5], e[6], e[7]), "created");
    }

    String delete(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "USER DELETE expects 2 fields: USER DELETE;Email;Password");
        return ok(users.delete(e[1], e[2]), "deleted");
    }

    String update(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 5, "USER UPDATE expects 4 fields: USER UPDATE;Email;Password;Field;NewValue");
        return ok(users.update(e[1], e[2], e[3], e[4]), "updated");
    }

    private static String ok(User user, String action) {
        return "OK: user " + user.getDni() + " " + action;
    }
}
