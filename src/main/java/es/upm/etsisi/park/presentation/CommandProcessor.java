package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.controller.ParkController;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.EntryResult;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.PassType;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.PersistenceException;
import es.upm.etsisi.park.presentation.help.CommandCatalog;
import es.upm.etsisi.park.presentation.help.CommandInfo;
import es.upm.etsisi.park.presentation.help.HelpGenerator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses ';'-separated command lines and dispatches them to the {@link ParkController}.
 * Used identically by every input source; business exceptions are reported, never propagated.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String NEWLINE = System.lineSeparator();
    private static final String SPEED_OPTION = "SPEED";

    private final ParkController park;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;

    public CommandProcessor(ParkController park, HelpGenerator help, HelpGenerator extendedHelp) {
        this.park = park;
        this.help = help;
        this.extendedHelp = extendedHelp;
    }

    public CommandResult process(String line) {
        if (line == null || line.isBlank()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = normalizeKeyword(parts[0]);
        try {
            return dispatch(command, parts);
        } catch (ParkException | PersistenceException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected error processing command: " + e.getMessage());
        }
    }

    private CommandResult dispatch(String command, String[] parts) {
        return switch (command) {
            case "USER CREATE" -> userCreate(parts);
            case "USER DELETE" -> userDelete(parts);
            case "USER UPDATE" -> userUpdate(parts);
            case "SYSTEM DATE" -> systemDate(parts);
            case "SYSTEM PRICE" -> systemPrice(parts);
            case "GROUP CREATE" -> groupCreate(parts);
            case "GROUP ADD" -> groupAdd(parts);
            case "GROUP REMOVE" -> groupRemove(parts);
            case "GROUP DELETE" -> groupDelete(parts);
            case "TICKET CREATE" -> ticketCreate(parts);
            case "TICKET DELETE" -> ticketDelete(parts);
            case "GROUPTICKET CREATE" -> groupTicketCreate(parts);
            case "GROUPTICKET DELETE" -> groupTicketDelete(parts);
            case "ATTRACTION CREATE" -> attractionCreate(parts);
            case "ATTRACTION ENTER" -> attractionEnter(parts);
            case "ATTRACTION EXIT" -> attractionExit(parts);
            case "ATTRACTION LIST" -> attractionList(parts);
            case "ATTRACTION STATUS" -> attractionStatus(parts);
            case "SPEEDPASS CREATE" -> speedPassCreate(parts);
            case "SPEEDPASS DELETE" -> speedPassDelete(parts);
            case "SPECIALPASS CREATE" -> specialPassCreate(parts);
            case "SPECIALPASS DELETE" -> specialPassDelete(parts);
            case "INVOICE CREATE" -> invoiceCreate(parts);
            case "HELP" -> CommandResult.of(help.generate());
            case "HELP EXTENDED" -> CommandResult.of(extendedHelp.generate());
            case "EXIT" -> CommandResult.exit("BYE");
            default -> CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
        };
    }

    // ----- Users -----

    private CommandResult userCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.USER_CREATE, 7);
        park.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return CommandResult.of("USER CREATED");
    }

    private CommandResult userDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.USER_DELETE, 2);
        park.deleteUser(parts[1], parts[2]);
        return CommandResult.of("USER DELETED");
    }

    private CommandResult userUpdate(String[] parts) {
        requireArguments(parts, CommandCatalog.USER_UPDATE, 4);
        park.updateUser(parts[1], parts[2], parts[3], parts[4]);
        return CommandResult.of("USER UPDATED");
    }

    // ----- System -----

    private CommandResult systemDate(String[] parts) {
        requireArguments(parts, CommandCatalog.SYSTEM_DATE, 1);
        return CommandResult.of("SYSTEM DATE SET TO " + park.setCurrentDate(parts[1]));
    }

    private CommandResult systemPrice(String[] parts) {
        requireArguments(parts, CommandCatalog.SYSTEM_PRICE, 1);
        return CommandResult.of("SYSTEM PRICE SET TO " + money(park.setCurrentPrice(parts[1])));
    }

    // ----- Groups -----

    private CommandResult groupCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUP_CREATE, 2);
        park.createGroup(parts[1], parts[2]);
        return CommandResult.of("GROUP CREATED");
    }

    private CommandResult groupAdd(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUP_ADD, 2);
        park.addToGroup(parts[1], parts[2]);
        return CommandResult.of("USER ADDED TO GROUP");
    }

    private CommandResult groupRemove(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUP_REMOVE, 2);
        park.removeFromGroup(parts[1], parts[2]);
        return CommandResult.of("USER REMOVED FROM GROUP");
    }

    private CommandResult groupDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUP_DELETE, 1);
        park.deleteGroup(parts[1]);
        return CommandResult.of("GROUP DELETED");
    }

    // ----- Tickets -----

    private CommandResult ticketCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.TICKET_CREATE, 3);
        IndividualTicket ticket = park.createTicket(parts[1], parts[2], parts[3]);
        return CommandResult.of("TICKET CREATED. PRICE: " + money(ticket.getPrice()) + " EUR");
    }

    private CommandResult ticketDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.TICKET_DELETE, 1);
        park.deleteTicket(parts[1]);
        return CommandResult.of("TICKET DELETED");
    }

    private CommandResult groupTicketCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUPTICKET_CREATE, 3);
        GroupTicket ticket = park.createGroupTicket(parts[1], parts[2], parts[3]);
        return CommandResult.of("GROUP TICKET CREATED. MEMBERS: " + ticket.getTickets().size()
                + ", DISCOUNT: " + ticket.getDiscount().getPercent() + "%, TOTAL PRICE: "
                + money(ticket.getPrice()) + " EUR");
    }

    private CommandResult groupTicketDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.GROUPTICKET_DELETE, 1);
        park.deleteGroupTicket(parts[1]);
        return CommandResult.of("GROUP TICKET DELETED");
    }

    // ----- Passes -----

    private CommandResult speedPassCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.SPEEDPASS_CREATE, 3);
        List<SpeedPass> passes = park.buySpeedPasses(parts[1], parts[2], parts[3]);
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (SpeedPass pass : passes) {
            total = total.add(pass.getPrice());
        }
        String range = passes.size() == 1 ? passes.get(0).getId()
                : passes.get(0).getId() + ".." + passes.get(passes.size() - 1).getId();
        return CommandResult.of("SPEEDPASS PURCHASED: " + passes.size() + " PASS(ES) " + range
                + ". TOTAL PRICE: " + money(total) + " EUR");
    }

    private CommandResult speedPassDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.SPEEDPASS_DELETE, 1);
        Pass pass = park.refundSpeedPass(parts[1]);
        return CommandResult.of("SPEEDPASS REFUNDED: " + money(pass.getPrice()) + " EUR");
    }

    private CommandResult specialPassCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.SPECIALPASS_CREATE, 2);
        SpecialPass pass = park.buySpecialPass(parts[1], parts[2]);
        return CommandResult.of("SPECIAL PASS PURCHASED. PRICE: " + money(pass.getPrice()) + " EUR");
    }

    private CommandResult specialPassDelete(String[] parts) {
        requireArguments(parts, CommandCatalog.SPECIALPASS_DELETE, 1);
        Pass pass = park.refundSpecialPass(parts[1]);
        return CommandResult.of("SPECIAL PASS REFUNDED: " + money(pass.getPrice()) + " EUR");
    }

    // ----- Attractions -----

    private CommandResult attractionCreate(String[] parts) {
        if (parts.length == 4) {
            park.createAttraction(parts[1], parts[2], parts[3], null);
        } else {
            requireArguments(parts, CommandCatalog.ATTRACTION_CREATE_TYPED, 4);
            park.createAttraction(parts[1], parts[2], parts[3], parts[4]);
        }
        return CommandResult.of("ATTRACTION CREATED");
    }

    private CommandResult attractionEnter(String[] parts) {
        boolean speed = false;
        if (parts.length == 4) {
            if (!SPEED_OPTION.equals(parts[3].toUpperCase(Locale.ROOT))) {
                throw new ValidationException("Unknown entry option '" + parts[3] + "'. Usage: "
                        + CommandCatalog.ATTRACTION_ENTER_SPEED.syntax());
            }
            speed = true;
        } else {
            requireArguments(parts, CommandCatalog.ATTRACTION_ENTER, 2);
        }
        EntryResult result = park.enterAttraction(parts[1], parts[2], speed);
        StringBuilder text = new StringBuilder(result.entered().size() + " USER(S) ENTERED " + parts[1]);
        List<String> speedIds = new ArrayList<>();
        for (Pass pass : result.consumed()) {
            if (pass.getType() == PassType.SPECIAL) {
                text.append(". SPECIAL PASS CONSUMED: ").append(pass.getId());
            } else {
                speedIds.add(pass.getId());
            }
        }
        if (!speedIds.isEmpty()) {
            text.append(". SPEEDPASS CONSUMED: ").append(String.join(", ", speedIds));
        }
        return CommandResult.of(text.toString());
    }

    private CommandResult attractionExit(String[] parts) {
        requireArguments(parts, CommandCatalog.ATTRACTION_EXIT, 2);
        park.exitAttraction(parts[1], parts[2]);
        return CommandResult.of("USER LEFT " + parts[1]);
    }

    private CommandResult attractionList(String[] parts) {
        requireArguments(parts, CommandCatalog.ATTRACTION_LIST, 1);
        Attraction<?> attraction = park.requireAttraction(parts[1]);
        if (attraction.getOccupants().isEmpty()) {
            return CommandResult.of("NO USERS IN " + attraction.getId());
        }
        return CommandResult.of(String.join(NEWLINE, emails(attraction)));
    }

    private CommandResult attractionStatus(String[] parts) {
        requireArguments(parts, CommandCatalog.ATTRACTION_STATUS, 1);
        Attraction<?> attraction = park.requireAttraction(parts[1]);
        List<String> occupants = emails(attraction);
        String text = String.join(NEWLINE,
                "ATTRACTION: " + attraction.getId(),
                "NAME: " + attraction.getName(),
                "TYPE: " + attraction.getType(),
                "CAPACITY: " + attraction.getCapacity(),
                "USERS INSIDE: " + attraction.getOccupancy(),
                "AVAILABLE PLACES: " + attraction.getAvailablePlaces(),
                "OCCUPANTS: " + (occupants.isEmpty() ? "-" : String.join(", ", occupants)));
        return CommandResult.of(text);
    }

    private static List<String> emails(Attraction<?> attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    // ----- Invoices -----

    private CommandResult invoiceCreate(String[] parts) {
        requireArguments(parts, CommandCatalog.INVOICE_USER, 2);
        return CommandResult.of(park.createInvoice(parts[1], parts[2]).toText());
    }

    // ----- Helpers -----

    private static void requireArguments(String[] parts, CommandInfo command, int count) {
        if (parts.length != count + 1) {
            throw new ValidationException("Wrong number of arguments. Usage: " + command.syntax());
        }
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString();
    }

    private static String normalizeKeyword(String keyword) {
        StringBuilder sb = new StringBuilder();
        for (String word : keyword.trim().split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(word.toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
