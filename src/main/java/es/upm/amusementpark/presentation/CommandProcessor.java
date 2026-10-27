package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.InvoiceController;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.presentation.help.SyntaxHelpFormatter;

/**
 * Presentation entry point for commands: splits each line on ';', dispatches it to the
 * command handlers and turns exceptions into error messages. The same instance serves
 * interactive and file input.
 */
public class CommandProcessor {

    private final UserCommands users;
    private final SystemCommands system;
    private final GroupCommands groups;
    private final TicketCommands tickets;
    private final AttractionCommands attractions;
    private final InvoiceCommands invoices;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private boolean exitRequested;

    /** Uses the default help generators (syntax and extended). */
    public CommandProcessor() {
        this(new HelpGenerator(new CommandCatalog(), new SyntaxHelpFormatter()),
                new HelpGenerator(new CommandCatalog(), new ExtendedHelpFormatter()));
    }

    /** Wires the command handlers to the configured singleton controllers and the injected help generators. */
    public CommandProcessor(HelpGenerator help, HelpGenerator extendedHelp) {
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.users = new UserCommands(UserController.getInstance());
        this.system = new SystemCommands(ParkController.getInstance());
        this.groups = new GroupCommands(GroupController.getInstance());
        this.tickets = new TicketCommands(TicketController.getInstance());
        this.attractions = new AttractionCommands(AttractionController.getInstance());
        this.invoices = new InvoiceCommands(InvoiceController.getInstance());
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
            case "INVOICE CREATE" -> invoices.create(in);
            case "HELP" -> help.generate();
            case "HELP EXTENDED" -> extendedHelp.generate();
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
