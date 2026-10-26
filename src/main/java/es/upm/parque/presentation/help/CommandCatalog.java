package es.upm.parque.presentation.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The commands registered in the application, in the order they are presented by the help.
 */
public class CommandCatalog {
    private final List<CommandDescriptor> commands = new ArrayList<>();

    public CommandCatalog register(String syntax, String description) {
        commands.add(new CommandDescriptor(syntax, description));
        return this;
    }

    public List<CommandDescriptor> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /** Catalog with every command of the amusement-park application. */
    public static CommandCatalog parkCommands() {
        return new CommandCatalog()
                .register("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique.")
                .register("USER DELETE;Correo;Contraseña",
                        "Deletes the user after checking the password, unless it has current or future tickets.")
                .register("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the password.")
                .register("GROUP CREATE;Id;Nombre", "Creates an empty group of users.")
                .register("GROUP ADD;Id;Correo",
                        "Adds a registered user to the group; not allowed while the group has valid group tickets.")
                .register("GROUP REMOVE;Id;Correo",
                        "Removes a member from the group; not allowed while the group has valid group tickets.")
                .register("GROUP DELETE;Id", "Deletes the group (not its users) unless it has valid group tickets.")
                .register("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for the user and date at the current price.")
                .register("TICKET DELETE;Id", "Deletes an individual ticket whose date is after the system date.")
                .register("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per group member with a size discount (3-5: 10%, 6-10: 20%, >10: 50%).")
                .register("GROUPTICKET DELETE;Id",
                        "Deletes a future group ticket together with the individual tickets it contains.")
                .register("SPEEDPASS CREATE;IdEntrada;Cantidad",
                        "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price.")
                .register("SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass if its ticket date is after the system date.")
                .register("SPECIALPASS CREATE;IdEntrada",
                        "Buys the ticket's only Special Pass, costing the same as the ticket.")
                .register("SPECIALPASS DELETE;IdPase",
                        "Refunds an unused Special Pass if its ticket date is after the system date.")
                .register("ATTRACTION CREATE;Id;Nombre;Capacidad", "Creates an attraction with a maximum capacity.")
                .register("ATTRACTION DELETE;Id", "Deletes an attraction that has nobody inside.")
                .register("ATTRACTION ENTER;IdAtraccion;IdEntrada",
                        "Lets the ticket's users in if the ticket is for today and there is room for all of them.")
                .register("ATTRACTION EXIT;IdAtraccion;IdEntrada", "The ticket's users leave the attraction.")
                .register("ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, free places and the users inside the attraction.")
                .register("INVOICE CREATE;USER;Correo", "Shows an invoice with the user's direct purchases.")
                .register("INVOICE CREATE;GROUP;IdGrupo", "Shows an invoice with the group's group-ticket purchases.")
                .register("SYSTEM DATE;yyyy-MM-dd", "Sets the simulated current date used by every date rule.")
                .register("SYSTEM PRICE;Importe", "Sets the price of new individual tickets (40.00 at start-up).")
                .register("HELP", "Lists the syntax of every command.")
                .register("HELP EXTENDED", "Lists every command with its syntax and a brief description.")
                .register("EXIT", "Ends the session; no further commands are executed.");
    }
}
