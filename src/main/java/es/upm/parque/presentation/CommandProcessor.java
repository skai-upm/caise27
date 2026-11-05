package es.upm.parque.presentation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private static final String HELP = "HELP";
    private static final String EXIT = "EXIT";
    private static final String SYSTEM_DATE = "SYSTEM DATE";
    private static final String SYSTEM_PRICE = "SYSTEM PRICE";

    /** Command syntax exactly as listed by the assignment, in its order. */
    static final List<String> HELP_SYNTAX = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
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
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP",
            "EXIT");
    private static final String HELP_TEXT = "Available commands:" + System.lineSeparator() + "  "
            + String.join(System.lineSeparator() + "  ", HELP_SYNTAX) + System.lineSeparator()
            + "  (USER UPDATE <Field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)";

    private final SystemManager system;
    private final UserCommands users;
    private final GroupCommands groups;
    private final TicketCommands tickets;
    private final AttractionCommands attractions;
    private final InvoiceCommands invoices;
    private boolean exitRequested;

    /** Processes commands with the singleton controllers configured during application initialization. */
    public CommandProcessor() {
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
        return switch (command) {
            case UserCommands.USER_CREATE -> users.create(elements);
            case UserCommands.USER_DELETE -> users.delete(elements);
            case UserCommands.USER_UPDATE -> users.update(elements);
            case HELP -> help(elements);
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

    private static String help(String[] e) throws InvalidCommandException {
        expect(e, 1, "HELP takes no fields");
        return HELP_TEXT;
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
