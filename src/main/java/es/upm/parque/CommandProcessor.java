package es.upm.parque;

import java.util.Arrays;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...} and executes them on the
 * {@link ParkController}. The same processor is used for interactive input and for command files.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
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
            "  ATTRACTION ENTER;IdAtraccion;IdEntrada",
            "  ATTRACTION EXIT;IdAtraccion;IdEntrada",
            "  ATTRACTION STATUS;IdAtraccion",
            "  HELP",
            "  EXIT");

    private final ParkController park;
    private boolean finished;

    public CommandProcessor(ParkController park) {
        this.park = park;
    }

    /**
     * Returns true once EXIT has been processed; no further commands should be supplied.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Executes one command line and returns the message to show, or {@code null} for a blank line.
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split(SEPARATOR, -1);
        String operation = parts[0].trim();
        String[] p = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
        }
        switch (operation) {
            case "USER CREATE":
                return p.length == 7 ? park.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6])
                        : usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
            case "USER DELETE":
                return p.length == 2 ? park.deleteUser(p[0], p[1]) : usage("USER DELETE;Correo;Contraseña");
            case "USER UPDATE":
                return p.length == 4 ? userUpdate(p) : usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
            case "SYSTEM DATE":
                return p.length == 1 ? park.setSystemDate(p[0]) : usage("SYSTEM DATE;yyyy-MM-dd");
            case "SYSTEM PRICE":
                return p.length == 1 ? park.setTicketPrice(p[0]) : usage("SYSTEM PRICE;Precio");
            case "GROUP CREATE":
                return p.length == 2 ? park.createGroup(p[0], p[1]) : usage("GROUP CREATE;Id;Nombre");
            case "GROUP ADD":
                return p.length == 2 ? park.addToGroup(p[0], p[1]) : usage("GROUP ADD;Id;Correo");
            case "GROUP REMOVE":
                return p.length == 2 ? park.removeFromGroup(p[0], p[1]) : usage("GROUP REMOVE;Id;Correo");
            case "GROUP DELETE":
                return p.length == 1 ? park.deleteGroup(p[0]) : usage("GROUP DELETE;Id");
            case "TICKET CREATE":
                return p.length == 3 ? park.createTicket(p[0], p[1], p[2])
                        : usage("TICKET CREATE;Id;Correo;yyyy-MM-dd");
            case "TICKET DELETE":
                return p.length == 1 ? park.deleteTicket(p[0]) : usage("TICKET DELETE;Id");
            case "GROUPTICKET CREATE":
                return p.length == 3 ? park.createGroupTicket(p[0], p[1], p[2])
                        : usage("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
            case "GROUPTICKET DELETE":
                return p.length == 1 ? park.deleteGroupTicket(p[0]) : usage("GROUPTICKET DELETE;Id");
            case "ATTRACTION CREATE":
                return p.length == 3 ? park.createAttraction(p[0], p[1], p[2])
                        : usage("ATTRACTION CREATE;Id;Nombre;Capacidad");
            case "ATTRACTION DELETE":
                return p.length == 1 ? park.deleteAttraction(p[0]) : usage("ATTRACTION DELETE;Id");
            case "ATTRACTION ENTER":
                return p.length == 2 ? park.enterAttraction(p[0], p[1])
                        : usage("ATTRACTION ENTER;IdAtraccion;IdEntrada");
            case "ATTRACTION EXIT":
                return p.length == 2 ? park.exitAttraction(p[0], p[1])
                        : usage("ATTRACTION EXIT;IdAtraccion;IdEntrada");
            case "ATTRACTION STATUS":
                return p.length == 1 ? park.attractionStatus(p[0]) : usage("ATTRACTION STATUS;IdAtraccion");
            case "HELP":
                return p.length == 0 ? HELP_TEXT : usage("HELP");
            case "EXIT":
                if (p.length != 0) {
                    return usage("EXIT");
                }
                finished = true;
                return "OK EXIT";
            default:
                return "ERROR Unknown command: " + operation + ". Type HELP to see the available commands";
        }
    }

    private String userUpdate(String[] p) {
        if ("DNI".equals(p[2])) {
            return "ERROR The DNI cannot be modified";
        }
        UserField field = UserField.fromName(p[2]);
        if (field == null) {
            return "ERROR Unknown field: " + p[2] + ". Editable fields: " + Arrays.toString(UserField.values());
        }
        return park.updateUser(p[0], p[1], field, p[3]);
    }

    private static String usage(String syntax) {
        return "ERROR Wrong number of parameters. Usage: " + syntax;
    }
}
