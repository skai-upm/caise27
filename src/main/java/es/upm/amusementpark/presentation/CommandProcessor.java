package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets and executes one text command at a time. Both interactive and
 * file input go through {@link #process(String)}, so a command behaves the
 * same regardless of where it comes from.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "      Campo: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
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

    private static final String SEPARATOR = ";";
    private static final String USER = "user ";
    private static final String GROUP = "group ";
    private static final String TICKET = "ticket ";
    private static final String CREATED = " created";
    private static final String DELETED = " deleted";

    private final ParkController park;
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Executes a single command line.
     *
     * @return {@code false} if the command was {@code EXIT} and processing
     *         must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        if (line == null || line.isBlank()) {
            return true;
        }
        String[] parts = line.trim().split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            if (operation.equals("EXIT")) {
                checkParameters(operation, parts, 0);
                out.println("Bye.");
                return false;
            }
            dispatch(operation, parts);
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            out.println("ERROR: could not save data: " + e.getMessage());
        }
        return true;
    }

    private void dispatch(String operation, String[] parts) throws IOException {
        if (operation.equals("HELP")) {
            checkParameters(operation, parts, 0);
            out.println(HELP_TEXT);
        } else if (operation.startsWith("USER ")) {
            user(operation, parts);
        } else if (operation.startsWith("SYSTEM ")) {
            system(operation, parts);
        } else if (operation.startsWith("GROUP ")) {
            group(operation, parts);
        } else if (operation.startsWith("TICKET ")) {
            ticket(operation, parts);
        } else if (operation.startsWith("GROUPTICKET ")) {
            groupTicket(operation, parts);
        } else if (operation.startsWith("ATTRACTION ")) {
            attraction(operation, parts);
        } else {
            throw unknown(operation);
        }
    }

    private void user(String operation, String[] p) throws IOException {
        switch (operation) {
            case "USER CREATE" -> {
                checkParameters(operation, p, 7);
                park.getUsers().create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                ok(USER + p[4] + CREATED);
            }
            case "USER DELETE" -> {
                checkParameters(operation, p, 2);
                park.deleteUser(p[1], p[2]);
                ok(USER + p[1] + DELETED);
            }
            case "USER UPDATE" -> {
                checkParameters(operation, p, 4);
                UserField field = UserField.fromName(p[3]);
                if (field == null) {
                    throw new IllegalArgumentException("field " + p[3] + " cannot be updated");
                }
                park.getUsers().update(p[1], p[2], field, p[4]);
                ok(USER + p[1] + " updated");
            }
            default -> throw unknown(operation);
        }
    }

    private void system(String operation, String[] p) {
        switch (operation) {
            case "SYSTEM DATE" -> {
                checkParameters(operation, p, 1);
                park.setSystemDate(p[1]);
                ok("system date set to " + park.getSystemDate());
            }
            case "SYSTEM PRICE" -> {
                checkParameters(operation, p, 1);
                park.setBasePrice(p[1]);
                ok("ticket price set to " + park.getBasePrice().toPlainString());
            }
            default -> throw unknown(operation);
        }
    }

    private void group(String operation, String[] p) throws IOException {
        switch (operation) {
            case "GROUP CREATE" -> {
                checkParameters(operation, p, 2);
                park.createGroup(p[1], p[2]);
                ok(GROUP + p[1] + CREATED);
            }
            case "GROUP ADD" -> {
                checkParameters(operation, p, 2);
                park.addToGroup(p[1], p[2]);
                ok(USER + p[2] + " added to " + GROUP + p[1]);
            }
            case "GROUP REMOVE" -> {
                checkParameters(operation, p, 2);
                park.removeFromGroup(p[1], p[2]);
                ok(USER + p[2] + " removed from " + GROUP + p[1]);
            }
            case "GROUP DELETE" -> {
                checkParameters(operation, p, 1);
                park.deleteGroup(p[1]);
                ok(GROUP + p[1] + DELETED);
            }
            default -> throw unknown(operation);
        }
    }

    private void ticket(String operation, String[] p) throws IOException {
        switch (operation) {
            case "TICKET CREATE" -> {
                checkParameters(operation, p, 3);
                Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                out.println("OK TICKET CREATED -> " + ticket.getPrice().toPlainString());
            }
            case "TICKET DELETE" -> {
                checkParameters(operation, p, 1);
                park.deleteTicket(p[1]);
                ok(TICKET + p[1] + DELETED);
            }
            default -> throw unknown(operation);
        }
    }

    private void groupTicket(String operation, String[] p) throws IOException {
        switch (operation) {
            case "GROUPTICKET CREATE" -> {
                checkParameters(operation, p, 3);
                Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                out.println("OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString());
            }
            case "GROUPTICKET DELETE" -> {
                checkParameters(operation, p, 1);
                park.deleteGroupTicket(p[1]);
                ok(GROUP + TICKET + p[1] + DELETED);
            }
            default -> throw unknown(operation);
        }
    }

    private void attraction(String operation, String[] p) throws IOException {
        switch (operation) {
            case "ATTRACTION CREATE" -> {
                checkParameters(operation, p, 3);
                park.createAttraction(p[1], p[2], p[3]);
                ok("attraction " + p[1] + CREATED);
            }
            case "ATTRACTION ENTER" -> {
                checkParameters(operation, p, 2);
                park.enterAttraction(p[1], p[2]);
                ok(TICKET + p[2] + " entered attraction " + p[1]);
            }
            case "ATTRACTION EXIT" -> {
                checkParameters(operation, p, 2);
                int left = park.exitAttraction(p[1], p[2]);
                ok(left + " user(s) of " + TICKET + p[2] + " left attraction " + p[1]);
            }
            case "ATTRACTION STATUS" -> {
                checkParameters(operation, p, 1);
                out.println(status(park.getAttractions().get(p[1])));
            }
            default -> throw unknown(operation);
        }
    }

    private void ok(String message) {
        out.println("OK: " + message);
    }

    static String status(Attraction attraction) {
        List<String> present = new ArrayList<>();
        for (User user : attraction.getVisitors()) {
            present.add(user.getName() + " " + user.getSurname() + " <" + user.getEmail() + ">");
        }
        return String.join(System.lineSeparator(),
                "Attraction: " + attraction.getId() + " - " + attraction.getName(),
                "Capacity: " + attraction.getCapacity(),
                "Users present: " + attraction.getVisitors().size(),
                "Available places: " + attraction.getAvailablePlaces(),
                "Present: " + (present.isEmpty() ? "(none)" : String.join(", ", present)));
    }

    private static IllegalArgumentException unknown(String operation) {
        return new IllegalArgumentException("unknown command '" + operation + "' (type HELP)");
    }

    private static void checkParameters(String operation, String[] parts, int expected) {
        int actual = parts.length - 1;
        if (actual != expected) {
            throw new IllegalArgumentException(operation + " expects " + expected
                    + " parameter(s) but received " + actual);
        }
    }
}
