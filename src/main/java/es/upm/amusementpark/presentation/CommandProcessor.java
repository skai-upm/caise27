package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interprets one ';'-separated command line and executes it through the injected controllers. Every exception
 * raised by an operation is reported as an "ERROR: ..." message, so processing never stops unexpectedly.
 */
public class CommandProcessor {

    /**
     * Outcome of a command: the text to show and whether the application must stop.
     */
    public record Result(String output, boolean exit) {
    }

    private static final String SPEED = "SPEED";

    private final UserController users;
    private final SystemController system;
    private final GroupController groups;
    private final TicketController tickets;
    private final PassController passes;
    private final AttractionController attractions;
    private final InvoiceController invoices;
    private final Map<String, HelpGenerator> helpGenerators;
    private final InvoiceRenderer invoiceRenderer = new InvoiceRenderer();

    public CommandProcessor(UserController users, SystemController system, GroupController groups,
                            TicketController tickets, PassController passes, AttractionController attractions,
                            InvoiceController invoices, Map<String, HelpGenerator> helpGenerators) {
        this.users = users;
        this.system = system;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
        this.attractions = attractions;
        this.invoices = invoices;
        this.helpGenerators = Map.copyOf(helpGenerators);
    }

    public Result process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = parts[0];
        try {
            if (helpGenerators.containsKey(command)) {
                requireArguments(parts, command);
                return ok(helpGenerators.get(command).generate());
            }
            return execute(command, parts);
        } catch (ParkException e) {
            return error(e.getMessage());
        }
    }

    private Result execute(String command, String[] parts) {
        switch (command) {
            case "USER CREATE":
                requireArguments(parts, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("USER CREATED");
            case "USER DELETE":
                requireArguments(parts, "USER DELETE;<Email>;<Password>");
                users.delete(parts[1], parts[2]);
                return ok("USER DELETED");
            case "USER UPDATE":
                requireArguments(parts, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new InvalidDataException("field " + parts[3]
                            + " cannot be modified (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                users.update(parts[1], parts[2], field, parts[4]);
                return ok("USER UPDATED");
            case "SYSTEM DATE":
                requireArguments(parts, "SYSTEM DATE;<yyyy-MM-dd>");
                return ok("SYSTEM DATE SET TO " + ParkFormat.formatDate(system.setDate(parts[1])));
            case "SYSTEM PRICE":
                requireArguments(parts, "SYSTEM PRICE;<Price>");
                return ok("SYSTEM PRICE SET TO " + ParkFormat.formatPrice(system.setPrice(parts[1])));
            case "GROUP CREATE":
                requireArguments(parts, "GROUP CREATE;<Identifier>;<Name>");
                groups.create(parts[1], parts[2]);
                return ok("GROUP CREATED");
            case "GROUP ADD":
                requireArguments(parts, "GROUP ADD;<Identifier>;<Email>");
                groups.add(parts[1], parts[2]);
                return ok("USER ADDED TO GROUP");
            case "GROUP REMOVE":
                requireArguments(parts, "GROUP REMOVE;<Identifier>;<Email>");
                groups.remove(parts[1], parts[2]);
                return ok("USER REMOVED FROM GROUP");
            case "GROUP DELETE":
                requireArguments(parts, "GROUP DELETE;<Identifier>");
                groups.delete(parts[1]);
                return ok("GROUP DELETED");
            case "TICKET CREATE":
                requireArguments(parts, "TICKET CREATE;<identifier>;<email>;<date>");
                return ok("TICKET CREATED. PRICE: "
                        + euros(tickets.createIndividual(parts[1], parts[2], parts[3]).getPrice()));
            case "TICKET DELETE":
                requireArguments(parts, "TICKET DELETE;<identifier>");
                tickets.deleteIndividual(parts[1]);
                return ok("TICKET DELETED");
            case "GROUPTICKET CREATE":
                requireArguments(parts, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                GroupTicket groupTicket = tickets.createGroup(parts[1], parts[2], parts[3]);
                return ok("GROUP TICKET CREATED FOR " + groupTicket.getComposition().size()
                        + " USER(S). TOTAL PRICE: " + euros(groupTicket.getPrice()));
            case "GROUPTICKET DELETE":
                requireArguments(parts, "GROUPTICKET DELETE;<identifier>");
                tickets.deleteGroup(parts[1]);
                return ok("GROUP TICKET DELETED");
            case "ATTRACTION CREATE":
                if (parts.length == 5) {
                    requireArguments(parts, "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>");
                    attractions.create(parts[1], parts[2], parts[3], parts[4]);
                } else {
                    requireArguments(parts, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                    attractions.create(parts[1], parts[2], parts[3]);
                }
                return ok("ATTRACTION CREATED");
            case "ATTRACTION ENTER":
                return enter(parts);
            case "ATTRACTION EXIT":
                requireArguments(parts, "ATTRACTION EXIT;<attraction>;<email>");
                attractions.exit(parts[1], parts[2]);
                return ok("EXITED ATTRACTION " + parts[1] + ": " + parts[2]);
            case "ATTRACTION LIST":
                requireArguments(parts, "ATTRACTION LIST;<attraction>");
                return ok(list(attractions.existing(parts[1])));
            case "ATTRACTION STATUS":
                requireArguments(parts, "ATTRACTION STATUS;<attractionIdentifier>");
                return ok(status(attractions.existing(parts[1])));
            case "SPEEDPASS CREATE":
                requireArguments(parts, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
                return ok(speedPassPurchase(passes.buySpeedPasses(parts[1], parts[2], parts[3])));
            case "SPEEDPASS DELETE":
                requireArguments(parts, "SPEEDPASS DELETE;<speedPassIdentifier>");
                SpeedPass refundedSpeed = passes.refundSpeedPass(parts[1]);
                return ok("SPEEDPASS DELETED. REFUND: " + euros(refundedSpeed.getPrice()));
            case "SPECIALPASS CREATE":
                requireArguments(parts, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
                return ok("SPECIAL PASS CREATED. PRICE: " + euros(passes.buySpecialPass(parts[1], parts[2]).getPrice()));
            case "SPECIALPASS DELETE":
                requireArguments(parts, "SPECIALPASS DELETE;<specialPassIdentifier>");
                SpecialPass refundedSpecial = passes.refundSpecialPass(parts[1]);
                return ok("SPECIAL PASS DELETED. REFUND: " + euros(refundedSpecial.getPrice()));
            case "INVOICE CREATE":
                requireArguments(parts, "INVOICE CREATE;<USER|GROUP>;<email|groupIdentifier>");
                return ok(invoiceRenderer.render(invoices.create(parts[1], parts[2])));
            case "EXIT":
                requireArguments(parts, "EXIT");
                return new Result("BYE", true);
            default:
                throw new InvalidDataException("unknown command '" + command
                        + "'. Type HELP to list the available commands");
        }
    }

    private Result enter(String[] parts) {
        boolean speed = parts.length == 4;
        if (speed) {
            requireArguments(parts, "ATTRACTION ENTER;<attraction>;<ticket>;SPEED");
            if (!parts[3].equals(SPEED)) {
                throw new InvalidDataException("unknown entry option " + parts[3] + " (only SPEED is allowed)");
            }
        } else {
            requireArguments(parts, "ATTRACTION ENTER;<attraction>;<ticket>");
        }
        AttractionController.Entry entry = attractions.enter(parts[1], parts[2], speed);
        StringBuilder output = new StringBuilder("ENTERED ATTRACTION " + parts[1] + ": " + emails(entry.users()));
        List<String> speedIds = new ArrayList<>();
        for (Pass pass : entry.consumedPasses()) {
            if (pass instanceof SpecialPass) {
                output.append(". SPECIAL PASS CONSUMED: ").append(pass.getId());
            } else {
                speedIds.add(pass.getId());
            }
        }
        if (!speedIds.isEmpty()) {
            output.append(". SPEEDPASS CONSUMED: ").append(String.join(", ", speedIds));
        }
        return ok(output.toString());
    }

    /**
     * Checks that the line has as many values as the syntax declares and that none is blank.
     */
    private static void requireArguments(String[] parts, String syntax) {
        int expected = syntax.split(";", -1).length - 1;
        if (parts.length - 1 != expected) {
            throw new InvalidDataException("wrong number of arguments. Usage: " + syntax);
        }
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new InvalidDataException("empty value in argument " + i + ". Usage: " + syntax);
            }
        }
    }

    private static String speedPassPurchase(List<SpeedPass> bought) {
        BigDecimal total = BigDecimal.ZERO;
        for (SpeedPass pass : bought) {
            total = total.add(pass.getPrice());
        }
        String ids = bought.size() == 1 ? bought.get(0).getId()
                : bought.get(0).getId() + " - " + bought.get(bought.size() - 1).getId();
        return bought.size() + " SPEEDPASS(ES) CREATED (" + ids + "). TOTAL PRICE: " + euros(total);
    }

    private static String list(Attraction<?> attraction) {
        if (attraction.getOccupants().isEmpty()) {
            return "NO USERS IN ATTRACTION " + attraction.getId();
        }
        List<String> lines = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            lines.add(user.getEmail());
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static String status(Attraction<?> attraction) {
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + attraction.getId(),
                "NAME: " + attraction.getName(),
                "TYPE: " + attraction.getType(),
                "CAPACITY: " + attraction.getCapacity(),
                "USERS INSIDE: " + attraction.getOccupants().size(),
                "AVAILABLE PLACES: " + attraction.getAvailablePlaces(),
                "OCCUPANTS: " + (attraction.getOccupants().isEmpty() ? "-" : emails(attraction.getOccupants())));
    }

    private static String emails(List<User> users) {
        List<String> emails = new ArrayList<>();
        for (User user : users) {
            emails.add(user.getEmail());
        }
        return String.join(", ", emails);
    }

    private static String euros(BigDecimal amount) {
        return ParkFormat.formatPrice(amount) + " EUR";
    }

    private static Result ok(String output) {
        return new Result(output, false);
    }

    private static Result error(String message) {
        return new Result("ERROR: " + message, false);
    }
}
