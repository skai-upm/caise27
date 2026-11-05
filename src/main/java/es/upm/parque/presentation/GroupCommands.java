package es.upm.parque.presentation;

import es.upm.parque.business.GroupManager;
import es.upm.parque.business.ParkController;
import es.upm.parque.business.ParkException;

/**
 * Parses the GROUP commands and formats their responses; the rules live in {@link GroupManager}.
 */
class GroupCommands {

    static final String GROUP_CREATE = "GROUP CREATE";
    static final String GROUP_ADD = "GROUP ADD";
    static final String GROUP_REMOVE = "GROUP REMOVE";
    static final String GROUP_DELETE = "GROUP DELETE";

    private final GroupManager groups;

    GroupCommands(ParkController park) {
        this.groups = new GroupManager(park);
    }

    String create(String[] e) throws InvalidCommandException, ParkException {
        if (e.length != 3 || e[1].isEmpty() || e[2].isEmpty()) {
            throw new InvalidCommandException("GROUP CREATE expects: GROUP CREATE;<id>;<name>");
        }
        return ok(groups.create(e[1], e[2]).getId(), "created");
    }

    String add(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "GROUP ADD expects: GROUP ADD;<id>;<email>");
        return ok(groups.add(e[1], e[2]).getId(), "now includes " + e[2]);
    }

    String remove(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "GROUP REMOVE expects: GROUP REMOVE;<id>;<email>");
        return ok(groups.remove(e[1], e[2]).getId(), "no longer includes " + e[2]);
    }

    String delete(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "GROUP DELETE expects: GROUP DELETE;<id>");
        return ok(groups.delete(e[1]).getId(), "deleted");
    }

    private static String ok(String id, String what) {
        return "OK: group " + id + " " + what;
    }
}
