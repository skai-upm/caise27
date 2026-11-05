package es.upm.parque.command;

import es.upm.parque.model.Group;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;

/**
 * Executes the GROUP commands against the central park management.
 */
class GroupCommands {

    static final String GROUP_CREATE = "GROUP CREATE";
    static final String GROUP_ADD = "GROUP ADD";
    static final String GROUP_REMOVE = "GROUP REMOVE";
    static final String GROUP_DELETE = "GROUP DELETE";

    private static final String NO_GROUP = "no group with identifier ";
    private static final String HAS_ACTIVE_TICKET = " has an active group ticket and cannot be modified";

    private final ParkController park;

    GroupCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] e) {
        if (e.length != 3 || e[1].isEmpty() || e[2].isEmpty()) {
            return error("GROUP CREATE expects: GROUP CREATE;<id>;<name>");
        }
        if (park.getGroups().exists(e[1])) {
            return error("a group with identifier " + e[1] + " already exists");
        }
        park.getGroups().add(new Group(e[1], e[2]));
        return ok(e[1], "created");
    }

    String add(String[] e) {
        if (e.length != 3) {
            return error("GROUP ADD expects: GROUP ADD;<id>;<email>");
        }
        Group group = park.getGroups().findById(e[1]);
        String problem = modificationProblem(group, e[1]);
        if (problem != null) {
            return error(problem);
        }
        User user = park.getUsers().findByEmail(e[2]);
        if (user == null) {
            return error("no user with email " + e[2]);
        }
        if (!group.add(user)) {
            return error("user " + e[2] + " already belongs to group " + e[1]);
        }
        return ok(e[1], "now includes " + e[2]);
    }

    String remove(String[] e) {
        if (e.length != 3) {
            return error("GROUP REMOVE expects: GROUP REMOVE;<id>;<email>");
        }
        Group group = park.getGroups().findById(e[1]);
        String problem = modificationProblem(group, e[1]);
        if (problem != null) {
            return error(problem);
        }
        User user = park.getUsers().findByEmail(e[2]);
        if (user == null || !group.remove(user)) {
            return error("user " + e[2] + " is not a member of group " + e[1]);
        }
        return ok(e[1], "no longer includes " + e[2]);
    }

    String delete(String[] e) {
        if (e.length != 2) {
            return error("GROUP DELETE expects: GROUP DELETE;<id>");
        }
        Group group = park.getGroups().findById(e[1]);
        if (group == null) {
            return error(NO_GROUP + e[1]);
        }
        if (park.hasActiveGroupTicket(group)) {
            return error("group " + e[1] + " has a group ticket for today or a later date and cannot be deleted");
        }
        park.getGroups().remove(e[1]);
        return ok(e[1], "deleted");
    }

    /** Why the group cannot be modified now, or {@code null} if it can. */
    private String modificationProblem(Group group, String id) {
        if (group == null) {
            return NO_GROUP + id;
        }
        return park.hasActiveGroupTicket(group) ? "group " + id + HAS_ACTIVE_TICKET : null;
    }

    private static String ok(String id, String what) {
        return "OK: group " + id + " " + what;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
