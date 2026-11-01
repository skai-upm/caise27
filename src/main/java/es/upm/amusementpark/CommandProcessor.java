package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets and executes text commands. Interactive and file modes both feed
 * their lines through this class, so every command has a single implementation.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
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
            "  TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
            "  TICKET DELETE;IdTicket",
            "  GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
            "  GROUPTICKET DELETE;IdTicket",
            "  ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
            "  ATTRACTION ENTER;IdAtraccion;IdTicket",
            "  ATTRACTION EXIT;IdAtraccion;IdTicket",
            "  ATTRACTION STATUS;IdAtraccion",
            "  HELP",
            "  EXIT");

    private final ParkController park;
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            if (!process(line)) {
                return;
            }
        }
    }

    /**
     * Executes one command line.
     *
     * @return false if the command was EXIT, true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String operation = parts[0].trim();
        String[] p = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            p[i - 1] = parts[i].trim();
        }
        try {
            switch (operation) {
                case "USER CREATE" -> {
                    requireParams(operation, p, 7);
                    park.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
                    out.println("User created: " + p[3]);
                }
                case "USER DELETE" -> {
                    requireParams(operation, p, 2);
                    park.deleteUser(p[0], p[1]);
                    out.println("User deleted: " + p[0]);
                }
                case "USER UPDATE" -> {
                    requireParams(operation, p, 4);
                    park.updateUser(p[0], p[1], p[2], p[3]);
                    out.println("User updated: " + p[2]);
                }
                case "SYSTEM DATE" -> {
                    requireParams(operation, p, 1);
                    park.setSystemDate(p[0]);
                    out.println("System date set to " + park.getSystemDate());
                }
                case "SYSTEM PRICE" -> {
                    requireParams(operation, p, 1);
                    park.setBasePrice(p[0]);
                    out.println("Ticket price set to " + park.getBasePrice().toPlainString());
                }
                case "GROUP CREATE" -> {
                    requireParams(operation, p, 2);
                    park.createGroup(p[0], p[1]);
                    out.println("Group created: " + p[0]);
                }
                case "GROUP ADD" -> {
                    requireParams(operation, p, 2);
                    park.addToGroup(p[0], p[1]);
                    out.println("User " + p[1] + " added to group " + p[0]);
                }
                case "GROUP REMOVE" -> {
                    requireParams(operation, p, 2);
                    park.removeFromGroup(p[0], p[1]);
                    out.println("User " + p[1] + " removed from group " + p[0]);
                }
                case "GROUP DELETE" -> {
                    requireParams(operation, p, 1);
                    park.deleteGroup(p[0]);
                    out.println("Group deleted: " + p[0]);
                }
                case "TICKET CREATE" -> {
                    requireParams(operation, p, 3);
                    Ticket t = park.createTicket(p[0], p[1], p[2]);
                    out.println("OK TICKET CREATED -> " + t.getPrice().toPlainString());
                }
                case "TICKET DELETE" -> {
                    requireParams(operation, p, 1);
                    park.deleteTicket(p[0]);
                    out.println("Ticket deleted: " + p[0]);
                }
                case "GROUPTICKET CREATE" -> {
                    requireParams(operation, p, 3);
                    Ticket t = park.createGroupTicket(p[0], p[1], p[2]);
                    out.println("OK GROUP TICKET CREATED -> " + t.getPrice().toPlainString());
                }
                case "GROUPTICKET DELETE" -> {
                    requireParams(operation, p, 1);
                    park.deleteGroupTicket(p[0]);
                    out.println("Group ticket deleted: " + p[0]);
                }
                case "ATTRACTION CREATE" -> {
                    requireParams(operation, p, 3);
                    park.createAttraction(p[0], p[1], p[2]);
                    out.println("Attraction created: " + p[0]);
                }
                case "ATTRACTION ENTER" -> {
                    requireParams(operation, p, 2);
                    park.enterAttraction(p[0], p[1]);
                    out.println("Ticket " + p[1] + " entered attraction " + p[0]);
                }
                case "ATTRACTION EXIT" -> {
                    requireParams(operation, p, 2);
                    int left = park.exitAttraction(p[0], p[1]);
                    out.println(left + " user(s) of ticket " + p[1] + " left attraction " + p[0]);
                }
                case "ATTRACTION STATUS" -> {
                    requireParams(operation, p, 1);
                    out.println(status(park.attractionStatus(p[0])));
                }
                case "HELP" -> {
                    requireParams(operation, p, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireParams(operation, p, 0);
                    out.println("Bye");
                    return false;
                }
                default -> out.println("Error: unknown command '" + operation + "'. Type HELP for the list of commands.");
            }
        } catch (OperationException e) {
            out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private static String status(Attraction a) {
        List<String> users = new ArrayList<>();
        for (User u : a.getPresentUsers()) {
            users.add(u.getEmail() + " (" + u.getName() + " " + u.getSurname() + ")");
        }
        return String.join(System.lineSeparator(),
                "Attraction: " + a.getId() + " - " + a.getName(),
                "Capacity: " + a.getCapacity(),
                "Users present: " + a.getOccupancy(),
                "Available places: " + a.getAvailable(),
                "Present: " + (users.isEmpty() ? "none" : String.join(", ", users)));
    }

    private static void requireParams(String operation, String[] params, int expected)
            throws OperationException {
        if (params.length != expected) {
            throw new OperationException(operation + " expects " + expected
                    + " parameter(s) but received " + params.length + ". Type HELP for the syntax.");
        }
    }
}
