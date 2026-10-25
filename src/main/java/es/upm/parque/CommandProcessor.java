package es.upm.parque;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interprets one text command per call. Shared by interactive and file modes.
 */
public class CommandProcessor {

    private static final String EXIT = "EXIT";

    @FunctionalInterface
    private interface Handler {
        void run(String[] params);
    }

    private record Command(String syntax, int params, Handler handler) {
    }

    private final ParkController park;
    private final PrintStream out;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
        registerCommands();
    }

    private void registerCommands() {
        register("USER CREATE", "Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta", p -> {
            park.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
            out.println("OK USER CREATED");
        });
        register("USER DELETE", "Correo;Contraseña", p -> {
            park.deleteUser(p[0], p[1]);
            out.println("OK USER DELETED");
        });
        register("USER UPDATE", "Correo;Contraseña;Campo;NuevoValor", p -> {
            park.updateUser(p[0], p[1], p[2], p[3]);
            out.println("OK USER UPDATED");
        });
        register("SYSTEM DATE", "yyyy-MM-dd", p -> out.println("OK SYSTEM DATE -> " + park.setSystemDate(p[0])));
        register("SYSTEM PRICE", "Precio", p -> out.println("OK SYSTEM PRICE -> "
                + park.setTicketPrice(p[0]).toPlainString()));
        register("GROUP CREATE", "IdGrupo;Nombre", p -> {
            park.createGroup(p[0], p[1]);
            out.println("OK GROUP CREATED");
        });
        register("GROUP ADD", "IdGrupo;Correo", p -> {
            park.addToGroup(p[0], p[1]);
            out.println("OK GROUP MEMBER ADDED");
        });
        register("GROUP REMOVE", "IdGrupo;Correo", p -> {
            park.removeFromGroup(p[0], p[1]);
            out.println("OK GROUP MEMBER REMOVED");
        });
        register("GROUP DELETE", "IdGrupo", p -> {
            park.deleteGroup(p[0]);
            out.println("OK GROUP DELETED");
        });
        register("TICKET CREATE", "IdEntrada;Correo;Fecha", p -> out.println("OK TICKET CREATED -> "
                + park.createTicket(p[0], p[1], p[2]).getPrice().toPlainString()));
        register("TICKET DELETE", "IdEntrada", p -> {
            park.deleteTicket(p[0]);
            out.println("OK TICKET DELETED");
        });
        register("GROUPTICKET CREATE", "IdEntrada;IdGrupo;Fecha", p -> out.println("OK GROUP TICKET CREATED -> "
                + park.createGroupTicket(p[0], p[1], p[2]).getPrice().toPlainString()));
        register("GROUPTICKET DELETE", "IdEntrada", p -> {
            park.deleteGroupTicket(p[0]);
            out.println("OK GROUP TICKET DELETED");
        });
        register("ATTRACTION CREATE", "IdAtraccion;Nombre;Capacidad", p -> {
            park.createAttraction(p[0], p[1], p[2]);
            out.println("OK ATTRACTION CREATED");
        });
        register("ATTRACTION DELETE", "IdAtraccion", p -> {
            park.deleteAttraction(p[0]);
            out.println("OK ATTRACTION DELETED");
        });
        register("ATTRACTION ENTER", "IdAtraccion;IdEntrada", p -> out.println("OK ATTRACTION ENTERED -> "
                + park.enterAttraction(p[0], p[1])));
        register("ATTRACTION EXIT", "IdAtraccion;IdEntrada", p -> out.println("OK ATTRACTION EXITED -> "
                + park.exitAttraction(p[0], p[1])));
        register("ATTRACTION STATUS", "IdAtraccion", p -> printStatus(park.getAttraction(p[0])));
        register("HELP", "", p -> printHelp());
        register(EXIT, "", p -> out.println("OK EXIT"));
    }

    private void register(String name, String syntax, Handler handler) {
        int params = syntax.isEmpty() ? 0 : syntax.split(";").length;
        String fullSyntax = syntax.isEmpty() ? name : name + ";" + syntax;
        commands.put(name, new Command(fullSyntax, params, handler));
    }

    /**
     * Processes a command line and prints its result.
     *
     * @return false when the command was EXIT, true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String name = parts[0].trim().toUpperCase();
        String[] params = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].trim();
        }
        Command command = commands.get(name);
        if (command == null) {
            out.println("ERROR Unknown command: " + parts[0].trim() + " (type HELP)");
            return true;
        }
        if (params.length != command.params()) {
            out.println("ERROR " + name + " expects " + command.params() + " parameter(s) but received "
                    + params.length + ". Usage: " + command.syntax());
            return true;
        }
        try {
            command.handler().run(params);
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR Unexpected failure: " + e.getMessage());
        }
        return !EXIT.equals(name);
    }

    private void printHelp() {
        out.println("Available commands:");
        for (Command c : commands.values()) {
            out.println("  " + c.syntax());
        }
        out.println("  (USER UPDATE Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD; dates as yyyy-MM-dd)");
    }

    private void printStatus(Attraction a) {
        out.println("ATTRACTION " + a.getId() + " - " + a.getName());
        out.println("  Capacity: " + a.getCapacity());
        out.println("  Present: " + a.getPresent().size());
        out.println("  Available: " + a.getAvailable());
        out.println("  Users:");
        if (a.getPresent().isEmpty()) {
            out.println("    (none)");
        }
        for (User u : a.getPresent()) {
            out.println("    - " + u.getName() + " " + u.getSurname() + " (" + u.getDni() + ")");
        }
    }
}
