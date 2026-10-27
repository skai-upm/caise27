package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.persistence.PersistenceException;

/**
 * Presentation entry point for commands: splits each line on ';', dispatches it to the
 * command handlers and turns exceptions into error messages. The same instance serves
 * interactive and file input.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join("\n",
            "Available commands:",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "  <Field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "HELP",
            "EXIT");

    private final UserCommands users;
    private final SystemCommands system;
    private final GroupCommands groups;
    private final TicketCommands tickets;
    private final AttractionCommands attractions;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.users = new UserCommands(new UserController(park));
        this.system = new SystemCommands(park);
        this.groups = new GroupCommands(new GroupController(park));
        this.tickets = new TicketCommands(new TicketController(park));
        this.attractions = new AttractionCommands(new AttractionController(park));
    }

    /**
     * Executes one command line and returns the response to show, or null when there
     * is nothing to show (blank line, EXIT).
     */
    public String process(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return dispatch(parts[0], new CommandInput(parts));
        } catch (InvalidCommandException | ParkException e) {
            return "ERROR: " + e.getMessage();
        } catch (PersistenceException e) {
            return "ERROR: Could not save data";
        } catch (RuntimeException e) {
            return "ERROR: Command could not be processed";
        }
    }

    private String dispatch(String command, CommandInput in) throws InvalidCommandException, ParkException {
        return switch (command) {
            case "USER CREATE" -> users.create(in);
            case "USER DELETE" -> users.delete(in);
            case "USER UPDATE" -> users.update(in);
            case "SYSTEM DATE" -> system.date(in);
            case "SYSTEM PRICE" -> system.price(in);
            case "GROUP CREATE" -> groups.create(in);
            case "GROUP ADD" -> groups.add(in);
            case "GROUP REMOVE" -> groups.remove(in);
            case "GROUP DELETE" -> groups.delete(in);
            case "TICKET CREATE" -> tickets.create(in);
            case "TICKET DELETE" -> tickets.delete(in);
            case "GROUPTICKET CREATE" -> tickets.createGroup(in);
            case "GROUPTICKET DELETE" -> tickets.deleteGroup(in);
            case "ATTRACTION CREATE" -> attractions.create(in);
            case "ATTRACTION ENTER" -> attractions.enter(in);
            case "ATTRACTION EXIT" -> attractions.exit(in);
            case "ATTRACTION LIST" -> attractions.list(in);
            case "ATTRACTION STATUS" -> attractions.status(in);
            case "HELP" -> HELP_TEXT;
            case "EXIT" -> {
                exitRequested = true;
                yield null;
            }
            default -> throw new InvalidCommandException("Unknown command");
        };
    }

    /** True once EXIT has been processed; no further command must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }
}
