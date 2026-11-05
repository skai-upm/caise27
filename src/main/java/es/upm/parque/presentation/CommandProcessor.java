package es.upm.parque.presentation;

import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.parque.business.AttractionManager;
import es.upm.parque.business.GroupManager;
import es.upm.parque.business.InvoiceManager;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.SystemManager;
import es.upm.parque.business.TicketManager;
import es.upm.parque.business.UserManager;

/**
 * Interprets text commands whose elements are separated by ';' and executes them through the business layer.
 *
 * <p>Every command produces a response text. Malformed commands ({@link InvalidCommandException}) and failed
 * operations ({@link ParkException}) are caught here and reported as {@code ERROR} responses, so command
 * processing never terminates unexpectedly.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String EXIT = "EXIT";
    private static final String SYSTEM_DATE = "SYSTEM DATE";
    private static final String SYSTEM_PRICE = "SYSTEM PRICE";

    private final HelpCommands help;
    private final SystemManager system;
    private final UserCommands users;
    private final GroupCommands groups;
    private final TicketCommands tickets;
    private final AttractionCommands attractions;
    private final InvoiceCommands invoices;
    private boolean exitRequested;

    /** Processes commands with the standard help variants and the configured singleton controllers. */
    public CommandProcessor() {
        this(HelpConfiguration.standard());
    }

    /**
     * Processes commands with the injected help variants and the singleton controllers configured during
     * application initialization.
     */
    public CommandProcessor(HelpCommands help) {
        this.help = help;
        this.system = SystemManager.getInstance();
        this.users = new UserCommands(UserManager.getInstance());
        this.groups = new GroupCommands(GroupManager.getInstance());
        this.tickets = new TicketCommands(TicketManager.getInstance());
        this.attractions = new AttractionCommands(AttractionManager.getInstance());
        this.invoices = new InvoiceCommands(InvoiceManager.getInstance());
    }

    /**
     * Processes one command line.
     *
     * @return the response to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] elements = line.split(SEPARATOR, -1);
        for (int i = 0; i < elements.length; i++) {
            elements[i] = elements[i].trim();
        }
        try {
            return dispatch(elements);
        } catch (InvalidCommandException | ParkException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("the command could not be processed");
        }
    }

    private String dispatch(String[] elements) throws InvalidCommandException, ParkException {
        String command = elements[0];
        if (help.handles(command)) {
            return help.respond(elements);
        }
        return switch (command) {
            case UserCommands.USER_CREATE -> users.create(elements);
            case UserCommands.USER_DELETE -> users.delete(elements);
            case UserCommands.USER_UPDATE -> users.update(elements);
            case EXIT -> exit(elements);
            case SYSTEM_DATE -> systemDate(elements);
            case SYSTEM_PRICE -> systemPrice(elements);
            case GroupCommands.GROUP_CREATE -> groups.create(elements);
            case GroupCommands.GROUP_ADD -> groups.add(elements);
            case GroupCommands.GROUP_REMOVE -> groups.remove(elements);
            case GroupCommands.GROUP_DELETE -> groups.delete(elements);
            case TicketCommands.TICKET_CREATE -> tickets.create(elements);
            case TicketCommands.TICKET_DELETE -> tickets.delete(elements);
            case TicketCommands.GROUPTICKET_CREATE -> tickets.createGroupTicket(elements);
            case TicketCommands.GROUPTICKET_DELETE -> tickets.deleteGroupTicket(elements);
            case AttractionCommands.ATTRACTION_CREATE -> attractions.create(elements);
            case AttractionCommands.ATTRACTION_LIST -> attractions.list(elements);
            case AttractionCommands.ATTRACTION_ENTER -> attractions.enter(elements);
            case AttractionCommands.ATTRACTION_EXIT -> attractions.exit(elements);
            case AttractionCommands.ATTRACTION_STATUS -> attractions.status(elements);
            case InvoiceCommands.INVOICE_CREATE -> invoices.create(elements);
            default -> throw new InvalidCommandException("unknown command '" + command + "'");
        };
    }

    /** Whether an EXIT command has been processed; no further commands should then be read. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /** Throws when the command does not have exactly {@code count} elements. */
    static void expect(String[] elements, int count, String usage) throws InvalidCommandException {
        if (elements.length != count) {
            throw new InvalidCommandException(usage);
        }
    }

    private String systemDate(String[] e) throws InvalidCommandException, ParkException {
        expect(e, 2, "SYSTEM DATE expects a valid date: SYSTEM DATE;<yyyy-MM-dd>");
        LocalDate date = system.setDate(e[1]);
        return "OK: system date set to " + date;
    }

    private String systemPrice(String[] e) throws InvalidCommandException, ParkException {
        expect(e, 2, "SYSTEM PRICE expects a positive amount with at most 2 decimals: SYSTEM PRICE;<price>");
        BigDecimal price = system.setPrice(e[1]);
        return "OK: system price set to " + price.toPlainString();
    }

    private String exit(String[] e) throws InvalidCommandException {
        expect(e, 1, "EXIT takes no fields");
        exitRequested = true;
        return "OK: exiting application";
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
