package es.upm.parque;

import java.util.List;

/**
 * Interprets one text command, split on ';', and returns the response text.
 * Interactive and file modes both use this class, so every command behaves the same in both.
 */
public class CommandProcessor {

    static final String USER_CREATE_SYNTAX = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    static final String USER_DELETE_SYNTAX = "USER DELETE;Correo;Contraseña";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";
    static final String HELP_SYNTAX = "HELP";
    static final String EXIT_SYNTAX = "EXIT";

    /** Syntax of every supported command, in the order HELP shows them. */
    static final List<String> COMMAND_SYNTAX = List.of(
            USER_CREATE_SYNTAX, USER_DELETE_SYNTAX, USER_UPDATE_SYNTAX, SYSTEM_DATE_SYNTAX,
            HELP_SYNTAX, EXIT_SYNTAX);

    private final UserManager userManager;
    private final MainController mainController;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, MainController mainController) {
        this.userManager = userManager;
        this.mainController = mainController;
    }

    /**
     * True once an EXIT command has been processed.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String operation = parts[0].toUpperCase();
        try {
            return switch (operation) {
                case "USER CREATE" -> {
                    requireParams(parts, USER_CREATE_SYNTAX);
                    userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    yield "OK USER CREATED";
                }
                case "USER DELETE" -> {
                    requireParams(parts, USER_DELETE_SYNTAX);
                    userManager.delete(parts[1], parts[2]);
                    yield "OK USER DELETED";
                }
                case "USER UPDATE" -> {
                    requireParams(parts, USER_UPDATE_SYNTAX);
                    userManager.update(parts[1], parts[2], parts[3], parts[4]);
                    yield "OK USER UPDATED";
                }
                case "SYSTEM DATE" -> {
                    requireParams(parts, SYSTEM_DATE_SYNTAX);
                    yield setSystemDate(parts[1]);
                }
                case "HELP" -> {
                    requireParams(parts, HELP_SYNTAX);
                    yield "OK Available commands:" + System.lineSeparator()
                            + String.join(System.lineSeparator(), COMMAND_SYNTAX);
                }
                case "EXIT" -> {
                    requireParams(parts, EXIT_SYNTAX);
                    exitRequested = true;
                    yield "OK EXIT";
                }
                default -> "ERROR Unknown command: " + parts[0];
            };
        } catch (ParkException e) {
            return "ERROR " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR Unexpected error: " + e.getMessage();
        }
    }

    private String setSystemDate(String text) {
        mainController.setCurrentDate(MainController.parseDate(text));
        return "OK SYSTEM DATE SET -> " + MainController.formatDate(mainController.getCurrentDate());
    }

    /**
     * Checks that the command has as many parameters as its syntax declares.
     */
    private static void requireParams(String[] parts, String syntax) {
        int expected = syntax.split(";", -1).length - 1;
        if (parts.length - 1 != expected) {
            throw new ParkException("Wrong number of parameters (expected " + expected + ", got "
                    + (parts.length - 1) + "). Usage: " + syntax);
        }
    }
}
