package es.upm.parque;

/**
 * Interprets text commands (components separated by ';') and executes them through the park controller.
 * Shared by interactive and file execution modes.
 */
public class CommandProcessor {

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
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
            "  TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd",
            "  TICKET DELETE;IdEntrada",
            "  GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd",
            "  GROUPTICKET DELETE;IdEntrada",
            "  ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
            "  ATTRACTION DELETE;IdAtraccion",
            "  ATTRACTION ENTER;IdAtraccion;IdEntrada",
            "  ATTRACTION EXIT;IdAtraccion;IdEntrada",
            "  ATTRACTION STATUS;IdAtraccion",
            "  HELP",
            "  EXIT");

    private final ParkController controller;
    private boolean exitRequested;

    public CommandProcessor(ParkController controller) {
        this.controller = controller;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Executes one command line and returns the response to show to the user.
     */
    public String process(String line) {
        String[] p = line.trim().split(";", -1);
        String operation = p[0].trim().toUpperCase();
        try {
            return switch (operation) {
                case "USER CREATE" -> p.length != 8
                        ? usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta")
                        : controller.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                case "USER DELETE" -> p.length != 3
                        ? usage("USER DELETE;Correo;Contraseña")
                        : controller.deleteUser(p[1], p[2]);
                case "USER UPDATE" -> p.length != 5
                        ? usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor")
                        : controller.updateUser(p[1], p[2], p[3], p[4]);
                case "SYSTEM DATE" -> p.length != 2
                        ? usage("SYSTEM DATE;yyyy-MM-dd")
                        : controller.setDate(p[1]);
                case "SYSTEM PRICE" -> p.length != 2
                        ? usage("SYSTEM PRICE;Precio")
                        : controller.setPrice(p[1]);
                case "GROUP CREATE" -> p.length != 3
                        ? usage("GROUP CREATE;IdGrupo;Nombre")
                        : controller.createGroup(p[1], p[2]);
                case "GROUP ADD" -> p.length != 3
                        ? usage("GROUP ADD;IdGrupo;Correo")
                        : controller.addToGroup(p[1], p[2]);
                case "GROUP REMOVE" -> p.length != 3
                        ? usage("GROUP REMOVE;IdGrupo;Correo")
                        : controller.removeFromGroup(p[1], p[2]);
                case "GROUP DELETE" -> p.length != 2
                        ? usage("GROUP DELETE;IdGrupo")
                        : controller.deleteGroup(p[1]);
                case "TICKET CREATE" -> p.length != 4
                        ? usage("TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd")
                        : controller.createTicket(p[1], p[2], p[3]);
                case "TICKET DELETE" -> p.length != 2
                        ? usage("TICKET DELETE;IdEntrada")
                        : controller.deleteTicket(p[1]);
                case "GROUPTICKET CREATE" -> p.length != 4
                        ? usage("GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd")
                        : controller.createGroupTicket(p[1], p[2], p[3]);
                case "GROUPTICKET DELETE" -> p.length != 2
                        ? usage("GROUPTICKET DELETE;IdEntrada")
                        : controller.deleteGroupTicket(p[1]);
                case "ATTRACTION CREATE" -> p.length != 4
                        ? usage("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad")
                        : controller.createAttraction(p[1], p[2], p[3]);
                case "ATTRACTION DELETE" -> p.length != 2
                        ? usage("ATTRACTION DELETE;IdAtraccion")
                        : controller.deleteAttraction(p[1]);
                case "ATTRACTION ENTER" -> p.length != 3
                        ? usage("ATTRACTION ENTER;IdAtraccion;IdEntrada")
                        : controller.enterAttraction(p[1], p[2]);
                case "ATTRACTION EXIT" -> p.length != 3
                        ? usage("ATTRACTION EXIT;IdAtraccion;IdEntrada")
                        : controller.exitAttraction(p[1], p[2]);
                case "ATTRACTION STATUS" -> p.length != 2
                        ? usage("ATTRACTION STATUS;IdAtraccion")
                        : controller.attractionStatus(p[1]);
                case "HELP" -> p.length != 1 ? usage("HELP") : HELP_TEXT;
                case "EXIT" -> {
                    if (p.length != 1) {
                        yield usage("EXIT");
                    }
                    exitRequested = true;
                    yield "OK BYE";
                }
                default -> "ERROR unknown command: " + p[0].trim() + " (type HELP)";
            };
        } catch (Exception e) {
            return "ERROR could not execute command: " + e.getMessage();
        }
    }

    private static String usage(String syntax) {
        return "ERROR wrong number of parameters. Usage: " + syntax;
    }
}
