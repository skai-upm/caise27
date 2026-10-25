package es.upm.parque.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The commands available in the application, in the order they are presented to users.
 */
public class CommandRegistry {

    private final List<CommandInfo> commands = new ArrayList<>();

    /**
     * The park's commands with their syntax and behavior.
     */
    public static CommandRegistry standard() {
        return new CommandRegistry()
                .register("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a new user after validating all its data; DNI and email must be unique.")
                .register("USER DELETE;Correo;Contraseña",
                        "Deletes the user after checking email and password, unless it holds current tickets.")
                .register("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user.")
                .register("GROUP CREATE;Id;Nombre", "Creates an empty group of users.")
                .register("GROUP ADD;Id;Correo", "Adds a registered user to a group that is not locked.")
                .register("GROUP REMOVE;Id;Correo", "Removes a member from a group that is not locked.")
                .register("GROUP DELETE;Id", "Deletes a group that is not locked; its users are kept.")
                .register("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user and date at the current price.")
                .register("TICKET DELETE;Id", "Refunds an individual ticket dated after the system date.")
                .register("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per group member with a discount that depends on the group size.")
                .register("GROUPTICKET DELETE;Id",
                        "Refunds a group ticket dated after the system date together with its member tickets.")
                .register("ATTRACTION CREATE;Id;Nombre;Aforo", "Creates an attraction with a maximum capacity.")
                .register("ATTRACTION DELETE;Id", "Deletes an attraction with nobody inside.")
                .register("ATTRACTION ENTER;IdAtraccion;IdEntrada",
                        "Lets in the users of a ticket valid today if they all fit in the attraction.")
                .register("ATTRACTION EXIT;IdAtraccion;IdEntrada", "Lets out the users of a ticket, freeing places.")
                .register("ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, available places and users inside an attraction.")
                .register("INVOICE CREATE;USER;Correo", "Shows the invoice of a user's direct purchases.")
                .register("INVOICE CREATE;GROUP;IdGrupo", "Shows the invoice of a group's group tickets.")
                .register("SYSTEM DATE;yyyy-MM-dd", "Sets the simulated current date of the application.")
                .register("SYSTEM PRICE;Precio", "Sets the current individual ticket price.")
                .register("HELP", "Lists the syntax of every command.")
                .register("HELP EXTENDED", "Lists every command with its syntax and a brief description.")
                .register("EXIT", "Ends the application; no further commands are executed.");
    }

    public CommandRegistry register(String syntax, String description) {
        commands.add(new CommandInfo(syntax, description));
        return this;
    }

    public List<CommandInfo> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
