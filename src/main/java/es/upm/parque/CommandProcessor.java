package es.upm.parque;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Interprets one text command and executes it. Shared by interactive and file modes.
 */
public class CommandProcessor {

    /**
     * Outcome of processing a command: the text to show and whether processing must stop.
     */
    public record Result(String output, boolean exit) {

        static Result ok(String output) {
            return new Result(output, false);
        }

        static Result error(String message) {
            return new Result("ERROR: " + message, false);
        }
    }

    /**
     * A registered command: its syntax for HELP, its parameter count and its action.
     */
    private record Command(String syntax, int parameters, Function<String[], String> action) {
    }

    private static final String EXIT = "EXIT";

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandProcessor(ParkController controller) {
        register("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                p -> controller.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        register("USER DELETE;Correo;Contraseña", p -> controller.deleteUser(p[1], p[2]));
        register("USER UPDATE;Correo;Contraseña;Campo;NuevoValor", p -> updateUser(controller, p));
        register("SYSTEM DATE;yyyy-MM-dd", p -> controller.setSystemDate(p[1]));
        register("SYSTEM PRICE;Precio", p -> controller.setTicketPrice(p[1]));
        register("GROUP CREATE;IdGrupo;Nombre", p -> controller.createGroup(p[1], p[2]));
        register("GROUP ADD;IdGrupo;Correo", p -> controller.addGroupMember(p[1], p[2]));
        register("GROUP REMOVE;IdGrupo;Correo", p -> controller.removeGroupMember(p[1], p[2]));
        register("GROUP DELETE;IdGrupo", p -> controller.deleteGroup(p[1]));
        register("TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd", p -> controller.createTicket(p[1], p[2], p[3]));
        register("TICKET DELETE;IdEntrada", p -> controller.deleteTicket(p[1]));
        register("GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd", p -> controller.createGroupTicket(p[1], p[2], p[3]));
        register("GROUPTICKET DELETE;IdEntrada", p -> controller.deleteGroupTicket(p[1]));
        register("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad", p -> controller.createAttraction(p[1], p[2], p[3]));
        register("ATTRACTION DELETE;IdAtraccion", p -> controller.deleteAttraction(p[1]));
        register("ATTRACTION ENTER;IdAtraccion;IdEntrada", p -> controller.enterAttraction(p[1], p[2]));
        register("ATTRACTION EXIT;IdAtraccion;IdEntrada", p -> controller.exitAttraction(p[1], p[2]));
        register("ATTRACTION STATUS;IdAtraccion", p -> controller.attractionStatus(p[1]));
        register("HELP", p -> helpText());
        register(EXIT, p -> "OK EXIT");
    }

    private void register(String syntax, Function<String[], String> action) {
        String[] parts = syntax.split(";");
        commands.put(parts[0], new Command(syntax, parts.length - 1, action));
    }

    public Result process(String line) {
        String[] parts = line.trim().split(";", -1);
        String name = parts[0].trim();
        Command command = commands.get(name);
        if (command == null) {
            return Result.error("unknown command '" + name + "'. Type HELP to list the commands");
        }
        if (parts.length - 1 != command.parameters()) {
            return Result.error("wrong number of parameters. Usage: " + command.syntax());
        }
        try {
            return new Result(command.action().apply(parts), EXIT.equals(name));
        } catch (ParkException e) {
            return Result.error(e.getMessage());
        } catch (UncheckedIOException e) {
            return Result.error("storage failure: " + e.getMessage());
        }
    }

    private static String updateUser(ParkController controller, String[] p) {
        UserField field = UserField.fromName(p[3]);
        if (field == null) {
            throw new ParkException("field '" + p[3] + "' cannot be updated. Valid fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
        }
        return controller.updateUser(p[1], p[2], field, p[4]);
    }

    private String helpText() {
        StringBuilder text = new StringBuilder("Available commands:");
        for (Command command : commands.values()) {
            text.append(System.lineSeparator()).append("  ").append(command.syntax());
        }
        text.append(System.lineSeparator()).append("USER UPDATE fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
        return text.toString();
    }
}
