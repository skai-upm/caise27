package es.upm.amusementpark.park;

import es.upm.amusementpark.cli.CommandProcessor;
import java.io.PrintStream;

/**
 * {@code GROUP CREATE}, {@code GROUP ADD}, {@code GROUP REMOVE} and {@code GROUP DELETE} commands.
 */
public final class GroupCommands {

    private final ParkController park;
    private final PrintStream out;

    private GroupCommands(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        GroupCommands commands = new GroupCommands(park, out);
        processor.register("GROUP CREATE", line -> commands.run(line, 3, "GROUP CREATE;<Identifier>;<Name>",
                f -> park.createGroup(f[1], f[2]), "GROUP CREATED"));
        processor.register("GROUP ADD", line -> commands.run(line, 3, "GROUP ADD;<Identifier>;<Email>",
                f -> park.addUserToGroup(f[1], f[2]), "USER ADDED TO GROUP"));
        processor.register("GROUP REMOVE", line -> commands.run(line, 3, "GROUP REMOVE;<Identifier>;<Email>",
                f -> park.removeUserFromGroup(f[1], f[2]), "USER REMOVED FROM GROUP"));
        processor.register("GROUP DELETE", line -> commands.run(line, 2, "GROUP DELETE;<Identifier>",
                f -> park.deleteGroup(f[1]), "GROUP DELETED"));
    }

    @FunctionalInterface
    private interface Operation {
        void apply(String[] fields);
    }

    private boolean run(String line, int expectedFields, String usage, Operation operation, String success) {
        String[] fields = line.split(";", -1);
        if (fields.length != expectedFields) {
            out.println("ERROR: Invalid syntax. Usage: " + usage);
            return true;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        try {
            operation.apply(fields);
            out.println(success);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }
}
