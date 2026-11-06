package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkController;
import java.io.PrintStream;

/**
 * {@code USER} commands.
 */
public final class UserCommands {

    private UserCommands() {
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        processor.register("USER CREATE", line -> {
            String[] f = CommandSupport.fields(line, 8,
                    "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
            park.registerUser(f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
            out.println("USER CREATED");
            return true;
        });
        processor.register("USER DELETE", line -> {
            String[] f = CommandSupport.fields(line, 3, "USER DELETE;<Email>;<Password>");
            park.deleteUser(f[1], f[2]);
            out.println("USER DELETED");
            return true;
        });
    }
}
