package com.amusementpark.presentation;

import com.amusementpark.business.controller.ParkController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Interprets and executes text commands. Shared by interactive and file modes.
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
            "  TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd",
            "  TICKET DELETE;IdEntrada",
            "  GROUPTICKET CREATE;IdEntradaGrupo;IdGrupo;yyyy-MM-dd",
            "  GROUPTICKET DELETE;IdEntradaGrupo",
            "  ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
            "  ATTRACTION ENTER;IdAtraccion;IdEntrada",
            "  ATTRACTION EXIT;IdAtraccion;IdEntrada|Correo",
            "  ATTRACTION STATUS;IdAtraccion",
            "  INVOICE CREATE;USER;Correo",
            "  INVOICE CREATE;GROUP;IdGrupo",
            "  HELP",
            "  EXIT");

    /** A command's parameter count and the action run with the split command parts. */
    private record Operation(int parameters, Function<String[], String> action) {
    }

    private final Map<String, Operation> operations = new LinkedHashMap<>();
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.out = out;
        operations.put("USER CREATE", new Operation(7,
                p -> park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7])));
        operations.put("USER DELETE", new Operation(2, p -> park.deleteUser(p[1], p[2])));
        operations.put("USER UPDATE", new Operation(4, p -> park.updateUser(p[1], p[2], p[3], p[4])));
        operations.put("SYSTEM DATE", new Operation(1, p -> park.setSystemDate(p[1])));
        operations.put("SYSTEM PRICE", new Operation(1, p -> park.setBasePrice(p[1])));
        operations.put("GROUP CREATE", new Operation(2, p -> park.createGroup(p[1], p[2])));
        operations.put("GROUP ADD", new Operation(2, p -> park.addToGroup(p[1], p[2])));
        operations.put("GROUP REMOVE", new Operation(2, p -> park.removeFromGroup(p[1], p[2])));
        operations.put("GROUP DELETE", new Operation(1, p -> park.deleteGroup(p[1])));
        operations.put("TICKET CREATE", new Operation(3, p -> park.createTicket(p[1], p[2], p[3])));
        operations.put("TICKET DELETE", new Operation(1, p -> park.deleteTicket(p[1])));
        operations.put("GROUPTICKET CREATE", new Operation(3, p -> park.createGroupTicket(p[1], p[2], p[3])));
        operations.put("GROUPTICKET DELETE", new Operation(1, p -> park.deleteGroupTicket(p[1])));
        operations.put("ATTRACTION CREATE", new Operation(3, p -> park.createAttraction(p[1], p[2], p[3])));
        operations.put("ATTRACTION ENTER", new Operation(2, p -> park.enterAttraction(p[1], p[2])));
        operations.put("ATTRACTION EXIT", new Operation(2, p -> park.exitAttraction(p[1], p[2])));
        operations.put("ATTRACTION STATUS", new Operation(1, p -> park.attractionStatus(p[1])));
        operations.put("INVOICE CREATE", new Operation(2, p -> park.createInvoice(p[1], p[2])));
        operations.put("HELP", new Operation(0, p -> HELP_TEXT));
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (!process(line)) {
                return;
            }
        }
    }

    /**
     * Processes a single command line.
     *
     * @return {@code false} if the command was EXIT, {@code true} otherwise
     */
    public boolean process(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        String[] parts = command.split(";", -1);
        String name = parts[0].trim();
        if (name.equals("EXIT")) {
            if (hasParameters(parts, 0)) {
                out.println("OK: Bye.");
                return false;
            }
            return true;
        }
        Operation operation = operations.get(name);
        if (operation == null) {
            out.println("ERROR: Unknown command: " + name + " (type HELP for the list)");
        } else if (hasParameters(parts, operation.parameters())) {
            out.println(operation.action().apply(parts));
        }
        return true;
    }

    private boolean hasParameters(String[] parts, int expected) {
        if (parts.length - 1 == expected) {
            return true;
        }
        out.println("ERROR: " + parts[0].trim() + " expects " + expected
                + " parameter(s) but got " + (parts.length - 1) + ".");
        return false;
    }
}
