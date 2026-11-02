package es.upm.etsisi.amusementpark.presentation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import es.upm.etsisi.amusementpark.business.user.UserField;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;

/**
 * Interprets and executes one command line. Elements are separated by ';' and parsed
 * with String.split; the same processor serves interactive and file input. Values are
 * parsed here, business rules live in the controllers, and every {@link ParkException}
 * is turned into an error message so processing can continue.
 */
public class CommandProcessor {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_DECIMALS = 2;
    private static final String SPEED = "SPEED";

    private final ParkController park;
    private final InvoiceController invoices;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;

    public CommandProcessor(ParkController park, InvoiceController invoices, HelpGenerator help,
                            HelpGenerator extendedHelp) {
        this.park = park;
        this.invoices = invoices;
        this.help = help;
        this.extendedHelp = extendedHelp;
    }

    public CommandResult process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return execute(parts[0], parts);
        } catch (ParkException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private static void checkForm(String keyword, String[] parts) throws InvalidCommandException {
        List<CommandSpec> forms = CommandCatalog.ALL.stream().filter(c -> c.keyword().equals(keyword)).toList();
        if (forms.isEmpty()) {
            throw new InvalidCommandException("unknown command '" + keyword + "'; type HELP for the list of commands");
        }
        if (forms.stream().noneMatch(c -> c.arity() == parts.length)) {
            throw new InvalidCommandException("wrong number of elements; usage: "
                    + forms.stream().map(CommandSpec::syntax).collect(Collectors.joining(" | ")));
        }
    }

    private CommandResult execute(String keyword, String[] p) throws ParkException {
        checkForm(keyword, p);
        return switch (keyword) {
            case "USER CREATE" -> {
                park.createUser(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
                yield CommandResult.message("USER CREATED");
            }
            case "USER DELETE" -> {
                park.deleteUser(p[1], p[2]);
                yield CommandResult.message("USER DELETED");
            }
            case "USER UPDATE" -> updateUser(p);
            case "SYSTEM DATE" -> {
                park.setCurrentDate(parseDate(p[1]));
                yield CommandResult.message("SYSTEM DATE SET: " + park.getCurrentDate());
            }
            case "SYSTEM PRICE" -> {
                park.setBasePrice(parsePrice(p[1]));
                yield CommandResult.message("SYSTEM PRICE SET: " + euros(park.getBasePrice()));
            }
            case "HELP" -> CommandResult.message(help.generate());
            case "HELP EXTENDED" -> CommandResult.message(extendedHelp.generate());
            case "EXIT" -> new CommandResult("BYE", true);
            case "INVOICE CREATE" -> invoice(p);
            default -> keyword.startsWith("GROUP ") ? group(keyword, p) : purchaseOrAttraction(keyword, p);
        };
    }

    private CommandResult updateUser(String[] p) throws ParkException {
        UserField field = UserField.fromName(p[3]);
        if (field == null) {
            throw new InvalidCommandException("field '" + p[3] + "' cannot be modified; allowed: NAME, SURNAME, "
                    + "EMAIL, PASSWORD, PHONE, CARD");
        }
        park.updateUser(p[1], p[2], field, p[4]);
        return CommandResult.message("USER UPDATED");
    }

    private CommandResult group(String keyword, String[] p) throws ParkException {
        return switch (keyword) {
            case "GROUP CREATE" -> {
                park.createGroup(p[1], p[2]);
                yield CommandResult.message("GROUP CREATED");
            }
            case "GROUP ADD" -> {
                park.addToGroup(p[1], p[2]);
                yield CommandResult.message("GROUP USER ADDED");
            }
            case "GROUP REMOVE" -> {
                park.removeFromGroup(p[1], p[2]);
                yield CommandResult.message("GROUP USER REMOVED");
            }
            default -> {
                park.deleteGroup(p[1]);
                yield CommandResult.message("GROUP DELETED");
            }
        };
    }

    private CommandResult purchaseOrAttraction(String keyword, String[] p) throws ParkException {
        return switch (keyword) {
            case "TICKET CREATE" -> {
                IndividualTicket t = park.createTicket(p[1], p[2], parseDate(p[3]));
                yield CommandResult.message("TICKET CREATED. PRICE: " + euros(t.getPrice()));
            }
            case "TICKET DELETE" -> {
                park.deleteTicket(p[1]);
                yield CommandResult.message("TICKET DELETED");
            }
            case "GROUPTICKET CREATE" -> {
                GroupTicket t = park.createGroupTicket(p[1], p[2], parseDate(p[3]));
                yield CommandResult.message("GROUPTICKET CREATED. TOTAL PRICE: " + euros(t.getPrice()));
            }
            case "GROUPTICKET DELETE" -> {
                park.deleteGroupTicket(p[1]);
                yield CommandResult.message("GROUPTICKET DELETED");
            }
            case "SPEEDPASS CREATE", "SPEEDPASS DELETE", "SPECIALPASS CREATE", "SPECIALPASS DELETE" ->
                    pass(keyword, p);
            default -> attraction(keyword, p);
        };
    }

    private CommandResult pass(String keyword, String[] p) throws ParkException {
        return switch (keyword) {
            case "SPEEDPASS CREATE" -> {
                List<SpeedPass> created = park.createSpeedPasses(p[1], p[2], parseInteger(p[3], "quantity"));
                BigDecimal total = created.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                yield CommandResult.message("SPEEDPASS CREATED: " + created.get(0).getIdentifier()
                        + (created.size() > 1 ? " to " + created.get(created.size() - 1).getIdentifier() : "")
                        + ". TOTAL PRICE: " + euros(total));
            }
            case "SPEEDPASS DELETE" -> {
                park.deleteSpeedPass(p[1]);
                yield CommandResult.message("SPEEDPASS DELETED");
            }
            case "SPECIALPASS CREATE" -> {
                SpecialPass pass = park.createSpecialPass(p[1], p[2]);
                yield CommandResult.message("SPECIALPASS CREATED. PRICE: " + euros(pass.getPrice()));
            }
            default -> {
                park.deleteSpecialPass(p[1]);
                yield CommandResult.message("SPECIALPASS DELETED");
            }
        };
    }

    private CommandResult attraction(String keyword, String[] p) throws ParkException {
        return switch (keyword) {
            case "ATTRACTION CREATE" -> {
                AttractionType type = p.length == 5 ? AttractionType.fromName(p[4]) : AttractionType.GENERAL;
                if (type == null) {
                    throw new InvalidCommandException("unknown attraction type '" + p[4]
                            + "'; allowed: GENERAL, INDIVIDUAL, GROUP, SPECIAL");
                }
                park.createAttraction(p[1], p[2], parseInteger(p[3], "capacity"), type);
                yield CommandResult.message("ATTRACTION CREATED");
            }
            case "ATTRACTION ENTER" -> {
                if (p.length == 4 && !p[3].equals(SPEED)) {
                    throw new InvalidCommandException("unknown entry option '" + p[3] + "'; only SPEED is allowed");
                }
                yield CommandResult.message(entered(park.enter(p[1], p[2], p.length == 4)));
            }
            case "ATTRACTION EXIT" -> {
                park.exit(p[1], p[2]);
                yield CommandResult.message("ATTRACTION EXITED");
            }
            case "ATTRACTION LIST" -> CommandResult.message(list(park.requireAttraction(p[1])));
            default -> CommandResult.message(status(park.requireAttraction(p[1])));
        };
    }

    private CommandResult invoice(String[] p) throws ParkException {
        return switch (p[1]) {
            case "USER" -> CommandResult.message(invoices.userInvoice(p[2]).render());
            case "GROUP" -> CommandResult.message(invoices.groupInvoice(p[2]).render());
            default -> throw new InvalidCommandException("unknown invoice recipient '" + p[1]
                    + "'; use USER or GROUP");
        };
    }

    private static String entered(EntryResult entry) {
        StringBuilder text = new StringBuilder("ATTRACTION ENTERED: " + entry.users().size() + " user(s)");
        List<String> speed = entry.consumedPasses().stream().filter(SpeedPass.class::isInstance)
                .map(Pass::getIdentifier).toList();
        if (!speed.isEmpty()) {
            text.append(". SPEEDPASS CONSUMED: ").append(String.join(", ", speed));
        }
        entry.consumedPasses().stream().filter(SpecialPass.class::isInstance)
                .forEach(s -> text.append(". SPECIAL PASS CONSUMED: ").append(s.getIdentifier()));
        return text.toString();
    }

    private static String emails(Attraction<?> a) {
        if (a.getOccupants().isEmpty()) {
            return "(none)";
        }
        return a.getOccupants().stream().map(User::getEmail).collect(Collectors.joining(", "));
    }

    private static String list(Attraction<?> a) {
        return "USERS IN " + a.getIdentifier() + ": " + emails(a);
    }

    private static String status(Attraction<?> a) {
        String nl = System.lineSeparator();
        return "ATTRACTION " + a.getIdentifier() + " - " + a.getName() + " (" + a.getType() + ")" + nl
                + "  Capacity: " + a.getCapacity() + nl
                + "  Inside: " + a.getOccupants().size() + nl
                + "  Available: " + a.getAvailablePlaces() + nl
                + "  Users: " + emails(a);
    }

    private static String euros(BigDecimal amount) {
        return amount.toPlainString() + " EUR";
    }

    static LocalDate parseDate(String text) throws InvalidCommandException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidCommandException("invalid date '" + text + "'; expected yyyy-MM-dd");
        }
    }

    static BigDecimal parsePrice(String text) throws InvalidCommandException {
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        boolean valid = !whole.isEmpty() && isDigits(whole) && isDigits(decimals)
                && decimals.length() <= MAX_DECIMALS && (dot < 0 || !decimals.isEmpty());
        BigDecimal price = valid ? new BigDecimal(text) : BigDecimal.ZERO;
        if (price.signum() <= 0) {
            throw new InvalidCommandException("invalid price '" + text + "'; expected a positive amount such as 45.50");
        }
        return price;
    }

    static int parseInteger(String text, String what) throws InvalidCommandException {
        int value = isDigits(text) && !text.isEmpty() && text.length() < 10 ? Integer.parseInt(text) : 0;
        if (value <= 0) {
            throw new InvalidCommandException("invalid " + what + " '" + text + "'; expected a positive whole number");
        }
        return value;
    }

    private static boolean isDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
