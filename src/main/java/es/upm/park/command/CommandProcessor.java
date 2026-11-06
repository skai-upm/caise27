package es.upm.park.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import es.upm.park.controller.ParkController;
import es.upm.park.model.UserField;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.service.ParkException;

/**
 * Interprets one text command (fields separated by ';') and returns the
 * message to show. Never throws for incorrect input.
 */
public class CommandProcessor {

    static final String BYE = "Bye";
    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>",
            "    <field>: " + Arrays.stream(UserField.values()).map(Enum::name).collect(Collectors.joining(", ")),
            "  GROUP CREATE;<id>;<name>",
            "  GROUP ADD;<id>;<email>",
            "  GROUP REMOVE;<id>;<email>",
            "  GROUP DELETE;<id>",
            "  SYSTEM DATE;<yyyy-MM-dd>",
            "  SYSTEM PRICE;<price>",
            "  HELP",
            "  EXIT");

    /** A command handler: expected number of fields and the action on them. */
    private record Command(int fields, Function<String[], String> action) {
    }

    private final ParkController park;
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
        commands.put("USER CREATE", new Command(7, this::createUser));
        commands.put("USER DELETE", new Command(2, this::deleteUser));
        commands.put("USER UPDATE", new Command(4, this::updateUser));
        commands.put("GROUP CREATE", new Command(2, this::createGroup));
        commands.put("GROUP ADD", new Command(2, this::addToGroup));
        commands.put("GROUP REMOVE", new Command(2, this::removeFromGroup));
        commands.put("GROUP DELETE", new Command(1, this::deleteGroup));
        commands.put("SYSTEM DATE", new Command(1, f -> "System date set to " + park.setCurrentDate(f[0])));
        commands.put("SYSTEM PRICE", new Command(1,
                f -> "System price set to " + park.setBasePrice(f[0]).toPlainString()));
        commands.put("HELP", new Command(0, f -> HELP_TEXT));
        commands.put("EXIT", new Command(0, this::exit));
    }

    /**
     * Processes a command line.
     *
     * @return the message to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        String name = parts[0].trim();
        Command command = commands.get(name);
        if (command == null) {
            return error("unknown command '" + name + "'");
        }
        if (parts.length != command.fields() + 1) {
            return error(name + " expects " + command.fields() + " fields");
        }
        try {
            return command.action().apply(Arrays.copyOfRange(parts, 1, parts.length));
        } catch (ParkException | PersistenceException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("command could not be processed");
        }
    }

    /**
     * Whether an EXIT command has been processed; no further command must
     * be processed afterwards.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String createUser(String[] f) {
        park.getUserService().create(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
        return "User created: " + f[2];
    }

    private String deleteUser(String[] f) {
        park.getUserService().delete(f[0], f[1]);
        return "User deleted: " + f[0];
    }

    private String updateUser(String[] f) {
        String dni = park.getUserService().update(f[0], f[1], f[2], f[3]).getDni();
        return "User updated: " + dni + " " + f[2];
    }

    private String createGroup(String[] f) {
        return "Group created: " + park.getGroupService().create(f[0], f[1]).getId();
    }

    private String addToGroup(String[] f) {
        park.getGroupService().addUser(f[0], f[1]);
        return "User " + f[1] + " added to group " + f[0];
    }

    private String removeFromGroup(String[] f) {
        park.getGroupService().removeUser(f[0], f[1]);
        return "User " + f[1] + " removed from group " + f[0];
    }

    private String deleteGroup(String[] f) {
        park.getGroupService().delete(f[0]);
        return "Group deleted: " + f[0];
    }

    private String exit(String[] f) {
        exitRequested = true;
        return BYE;
    }

    private static String error(String message) {
        return "Error: " + message;
    }
}
