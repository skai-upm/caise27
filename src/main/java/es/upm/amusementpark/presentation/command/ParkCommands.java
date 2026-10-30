package es.upm.amusementpark.presentation.command;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.presentation.help.HelpGenerator;

import java.util.List;

/**
 * Registers every application command with its syntax, description and handler.
 */
public final class ParkCommands {

    private ParkCommands() {
    }

    public static void registerBusinessCommands(CommandRegistry registry, ParkController park) {
        UserController users = park.getUserController();
        registry.register(CommandDefinition.of("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7])))
                .register(CommandDefinition.of("USER DELETE;Correo;Contraseña",
                        "Deletes the user if the password matches and no ticket is dated today or later.",
                        p -> park.deleteUser(p[1], p[2])))
                .register(CommandDefinition.of("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the password.",
                        p -> users.update(p[1], p[2], p[3], p[4])))
                .register(CommandDefinition.of("SYSTEM DATE;yyyy-MM-dd",
                        "Sets the simulated system date used by every date rule.",
                        p -> park.setSystemDate(p[1])))
                .register(CommandDefinition.of("SYSTEM PRICE;Precio",
                        "Sets the base price of individual tickets for later purchases.",
                        p -> park.setBasePrice(p[1])))
                .register(CommandDefinition.of("GROUP CREATE;IdGrupo;Nombre",
                        "Creates an empty group.",
                        p -> park.createGroup(p[1], p[2])))
                .register(CommandDefinition.of("GROUP ADD;IdGrupo;Correo",
                        "Adds a registered user to a group that has no current or future group ticket.",
                        p -> park.addToGroup(p[1], p[2])))
                .register(CommandDefinition.of("GROUP REMOVE;IdGrupo;Correo",
                        "Removes a member from a group that has no current or future group ticket.",
                        p -> park.removeFromGroup(p[1], p[2])))
                .register(CommandDefinition.of("GROUP DELETE;IdGrupo",
                        "Deletes a group that has no current or future group ticket.",
                        p -> park.deleteGroup(p[1])))
                .register(CommandDefinition.of("TICKET CREATE;IdTicket;Correo;Fecha",
                        "Buys an individual ticket for a user and a date at the current base price.",
                        p -> park.createTicket(p[1], p[2], p[3])))
                .register(CommandDefinition.of("TICKET DELETE;IdTicket",
                        "Refunds an individual ticket dated after the system date.",
                        p -> park.deleteTicket(p[1])))
                .register(CommandDefinition.of("GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha",
                        "Buys a ticket for every current member of a group, with a discount by group size.",
                        p -> park.createGroupTicket(p[1], p[2], p[3])))
                .register(CommandDefinition.of("GROUPTICKET DELETE;IdTicket",
                        "Refunds a group ticket dated after the system date together with its member tickets.",
                        p -> park.deleteGroupTicket(p[1])))
                .register(new CommandDefinition("ATTRACTION CREATE", List.of("ATTRACTION CREATE;IdAtraccion;Nombre;Aforo[;Tipo]"),
                        "Creates an attraction with a maximum capacity; Tipo is GENERAL (default), INDIVIDUAL, GROUP or SPECIAL.",
                        4, 5, p -> park.createAttraction(p[1], p[2], p[3], p.length == 5 ? p[4] : null)))
                .register(CommandDefinition.of("ATTRACTION ENTER;IdAtraccion;IdTicket",
                        "Admits the users of a ticket valid today if there is room for all of them.",
                        p -> park.enterAttraction(p[1], p[2])))
                .register(CommandDefinition.of("ATTRACTION EXIT;IdAtraccion;IdTicket",
                        "Lets the users of a ticket leave the attraction.",
                        p -> park.exitAttraction(p[1], p[2])))
                .register(CommandDefinition.of("ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, available places and users present.",
                        p -> park.attractionStatus(p[1])))
                .register(new CommandDefinition("INVOICE CREATE",
                        List.of("INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo"),
                        "Prints the invoice of a user (own tickets and passes) or of a group (group tickets).",
                        3, 3, p -> park.createInvoice(p[1], p[2])))
                .register(CommandDefinition.of("SPEEDPASS CREATE;IdTicket;Cantidad",
                        "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price.",
                        p -> park.buySpeedPasses(p[1], p[2])))
                .register(CommandDefinition.of("SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass of a ticket dated after the system date.",
                        p -> park.refundSpeedPass(p[1])))
                .register(CommandDefinition.of("SPECIALPASS CREATE;IdTicket",
                        "Buys the single Special Pass of an individual ticket at the ticket price.",
                        p -> park.buySpecialPass(p[1])))
                .register(CommandDefinition.of("SPECIALPASS DELETE;IdPase",
                        "Refunds an unused Special Pass of a ticket dated after the system date.",
                        p -> park.refundSpecialPass(p[1])));
    }

    public static void registerGeneralCommands(CommandRegistry registry, HelpGenerator help) {
        registry.register(CommandDefinition.of("HELP", "Lists the available commands.", p -> help.concise()))
                .register(CommandDefinition.of("HELP EXTENDED", "Lists the available commands with a description.",
                        p -> help.extended()))
                .register(CommandDefinition.of("EXIT", "Ends the execution.", null));
    }
}
