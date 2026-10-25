package com.amusementpark;

import java.io.PrintStream;

/**
 * Interprets one text command at a time. Shared by interactive and file modes.
 */
public class CommandProcessor {
    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor"
                    + "   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  SYSTEM DATE;yyyy-MM-dd",
            "  SYSTEM PRICE;Precio",
            "  GROUP CREATE;Id;Nombre",
            "  GROUP ADD;Id;Correo",
            "  GROUP REMOVE;Id;Correo",
            "  GROUP DELETE;Id",
            "  TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "  TICKET DELETE;Id",
            "  GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "  GROUPTICKET DELETE;Id",
            "  ATTRACTION CREATE;Id;Nombre;Capacidad",
            "  ATTRACTION DELETE;Id",
            "  ATTRACTION ENTER;Id;IdTicket",
            "  ATTRACTION EXIT;Id;IdTicket",
            "  ATTRACTION STATUS;Id",
            "  HELP",
            "  EXIT");

    private final Park park;
    private final PrintStream out;

    public CommandProcessor(Park park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return false when processing must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        if (line == null || line.isBlank()) {
            return true;
        }
        String[] parts = line.trim().split(";", -1);
        String operation = parts[0].trim();
        switch (operation) {
            case "USER CREATE":
                if (hasParameters(parts, 7)) {
                    out.println(park.createUser(parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7]));
                }
                return true;
            case "USER DELETE":
                if (hasParameters(parts, 2)) {
                    out.println(park.deleteUser(parts[1], parts[2]));
                }
                return true;
            case "USER UPDATE":
                if (hasParameters(parts, 4)) {
                    out.println(park.updateUser(parts[1], parts[2], parts[3], parts[4]));
                }
                return true;
            case "SYSTEM DATE":
                if (hasParameters(parts, 1)) {
                    out.println(park.setDate(parts[1]));
                }
                return true;
            case "SYSTEM PRICE":
                if (hasParameters(parts, 1)) {
                    out.println(park.setPrice(parts[1]));
                }
                return true;
            case "GROUP CREATE":
                if (hasParameters(parts, 2)) {
                    out.println(park.createGroup(parts[1], parts[2]));
                }
                return true;
            case "GROUP ADD":
                if (hasParameters(parts, 2)) {
                    out.println(park.addToGroup(parts[1], parts[2]));
                }
                return true;
            case "GROUP REMOVE":
                if (hasParameters(parts, 2)) {
                    out.println(park.removeFromGroup(parts[1], parts[2]));
                }
                return true;
            case "GROUP DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(park.deleteGroup(parts[1]));
                }
                return true;
            case "TICKET CREATE":
                if (hasParameters(parts, 3)) {
                    out.println(park.createTicket(parts[1], parts[2], parts[3]));
                }
                return true;
            case "TICKET DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(park.deleteTicket(parts[1]));
                }
                return true;
            case "GROUPTICKET CREATE":
                if (hasParameters(parts, 3)) {
                    out.println(park.createGroupTicket(parts[1], parts[2], parts[3]));
                }
                return true;
            case "GROUPTICKET DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(park.deleteGroupTicket(parts[1]));
                }
                return true;
            case "ATTRACTION CREATE":
                if (hasParameters(parts, 3)) {
                    out.println(park.createAttraction(parts[1], parts[2], parts[3]));
                }
                return true;
            case "ATTRACTION DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(park.deleteAttraction(parts[1]));
                }
                return true;
            case "ATTRACTION ENTER":
                if (hasParameters(parts, 2)) {
                    out.println(park.enterAttraction(parts[1], parts[2]));
                }
                return true;
            case "ATTRACTION EXIT":
                if (hasParameters(parts, 2)) {
                    out.println(park.exitAttraction(parts[1], parts[2]));
                }
                return true;
            case "ATTRACTION STATUS":
                if (hasParameters(parts, 1)) {
                    out.println(park.attractionStatus(parts[1]));
                }
                return true;
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Bye");
                return false;
            default:
                out.println("ERROR: unknown command: " + operation + " (type HELP for the list)");
                return true;
        }
    }

    private boolean hasParameters(String[] parts, int expected) {
        if (parts.length - 1 != expected) {
            out.println("ERROR: " + parts[0].trim() + " expects " + expected
                    + " parameters but received " + (parts.length - 1));
            return false;
        }
        return true;
    }
}
