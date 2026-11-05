package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkController;
import java.io.PrintStream;

/**
 * {@code GROUP CREATE}, {@code GROUP ADD}, {@code GROUP REMOVE} and {@code GROUP DELETE} commands.
 */
public final class GroupCommands {

    private GroupCommands() {
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        processor.register("GROUP CREATE", line -> {
            String[] f = CommandSupport.fields(line, 3, "GROUP CREATE;<Identifier>;<Name>");
            park.createGroup(f[1], f[2]);
            out.println("GROUP CREATED");
            return true;
        });
        processor.register("GROUP ADD", line -> {
            String[] f = CommandSupport.fields(line, 3, "GROUP ADD;<Identifier>;<Email>");
            park.addUserToGroup(f[1], f[2]);
            out.println("USER ADDED TO GROUP");
            return true;
        });
        processor.register("GROUP REMOVE", line -> {
            String[] f = CommandSupport.fields(line, 3, "GROUP REMOVE;<Identifier>;<Email>");
            park.removeUserFromGroup(f[1], f[2]);
            out.println("USER REMOVED FROM GROUP");
            return true;
        });
        processor.register("GROUP DELETE", line -> {
            String[] f = CommandSupport.fields(line, 2, "GROUP DELETE;<Identifier>");
            park.deleteGroup(f[1]);
            out.println("GROUP DELETED");
            return true;
        });
    }
}
