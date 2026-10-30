package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.controller.UserService;

import java.io.PrintStream;

/**
 * Interprets and executes text commands. Shared by every input mode.
 */
public class CommandProcessor {

    private final Park park;
    private final PrintStream out;

    public CommandProcessor(Park park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} when processing must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        try {
            return execute(line.trim());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + line);
            return true;
        }
    }

    private boolean execute(String line) {
        String[] p = line.split(";", -1);
        String operation = p[0].trim();
        UserService users = park.getUserService();
        switch (operation) {
            case "USER CREATE":
                run(p, 8, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        () -> users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
                return true;
            case "USER DELETE":
                run(p, 3, "USER DELETE;Correo;Contraseña", () -> park.deleteUser(p[1], p[2]));
                return true;
            case "USER UPDATE":
                run(p, 5, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        () -> users.update(p[1], p[2], p[3], p[4]));
                return true;
            case "SYSTEM DATE":
                run(p, 2, "SYSTEM DATE;yyyy-MM-dd", () -> park.setSystemDate(p[1]));
                return true;
            case "SYSTEM PRICE":
                run(p, 2, "SYSTEM PRICE;Precio", () -> park.setBasePrice(p[1]));
                return true;
            case "GROUP CREATE":
                run(p, 3, "GROUP CREATE;IdGrupo;Nombre", () -> park.createGroup(p[1], p[2]));
                return true;
            case "GROUP ADD":
                run(p, 3, "GROUP ADD;IdGrupo;Correo", () -> park.addToGroup(p[1], p[2]));
                return true;
            case "GROUP REMOVE":
                run(p, 3, "GROUP REMOVE;IdGrupo;Correo", () -> park.removeFromGroup(p[1], p[2]));
                return true;
            case "GROUP DELETE":
                run(p, 2, "GROUP DELETE;IdGrupo", () -> park.deleteGroup(p[1]));
                return true;
            case "TICKET CREATE":
                run(p, 4, "TICKET CREATE;IdTicket;Correo;Fecha", () -> park.createTicket(p[1], p[2], p[3]));
                return true;
            case "TICKET DELETE":
                run(p, 2, "TICKET DELETE;IdTicket", () -> park.deleteTicket(p[1]));
                return true;
            case "GROUPTICKET CREATE":
                run(p, 4, "GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha",
                        () -> park.createGroupTicket(p[1], p[2], p[3]));
                return true;
            case "GROUPTICKET DELETE":
                run(p, 2, "GROUPTICKET DELETE;IdTicket", () -> park.deleteGroupTicket(p[1]));
                return true;
            case "ATTRACTION CREATE":
                run(p, 4, "ATTRACTION CREATE;IdAtraccion;Nombre;Aforo",
                        () -> park.createAttraction(p[1], p[2], p[3]));
                return true;
            case "ATTRACTION ENTER":
                run(p, 3, "ATTRACTION ENTER;IdAtraccion;IdTicket", () -> park.enterAttraction(p[1], p[2]));
                return true;
            case "ATTRACTION EXIT":
                run(p, 3, "ATTRACTION EXIT;IdAtraccion;IdTicket", () -> park.exitAttraction(p[1], p[2]));
                return true;
            case "ATTRACTION STATUS":
                run(p, 2, "ATTRACTION STATUS;IdAtraccion", () -> park.attractionStatus(p[1]));
                return true;
            case "HELP":
                run(p, 1, "HELP", CommandProcessor::help);
                return true;
            case "EXIT":
                if (checkParams(p, 1, "EXIT")) {
                    out.println("Bye");
                    return false;
                }
                return true;
            default:
                out.println("ERROR: unknown command: " + line);
                return true;
        }
    }

    private void run(String[] parts, int expected, String syntax, java.util.function.Supplier<String> action) {
        if (checkParams(parts, expected, syntax)) {
            out.println(action.get());
        }
    }

    private static String help() {
        return String.join(System.lineSeparator(),
                "Available commands:",
                "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "  USER DELETE;Correo;Contraseña",
                "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
                "  SYSTEM DATE;yyyy-MM-dd",
                "  SYSTEM PRICE;Precio",
                "  GROUP CREATE;IdGrupo;Nombre",
                "  GROUP ADD;IdGrupo;Correo",
                "  GROUP REMOVE;IdGrupo;Correo",
                "  GROUP DELETE;IdGrupo",
                "  TICKET CREATE;IdTicket;Correo;Fecha",
                "  TICKET DELETE;IdTicket",
                "  GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha",
                "  GROUPTICKET DELETE;IdTicket",
                "  ATTRACTION CREATE;IdAtraccion;Nombre;Aforo",
                "  ATTRACTION ENTER;IdAtraccion;IdTicket",
                "  ATTRACTION EXIT;IdAtraccion;IdTicket",
                "  ATTRACTION STATUS;IdAtraccion",
                "  HELP",
                "  EXIT");
    }

    private boolean checkParams(String[] parts, int expected, String syntax) {
        if (parts.length != expected) {
            out.println("ERROR: wrong number of parameters. Usage: " + syntax);
            return false;
        }
        return true;
    }
}
