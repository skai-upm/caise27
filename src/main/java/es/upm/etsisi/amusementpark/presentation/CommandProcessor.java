package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.exception.PersistenceException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.model.attraction.EntryResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets text commands and presents their results. It is the place where business exceptions
 * become error messages, so a failing command never stops processing. The same instance and
 * {@link #run} loop serve interactive and file input.
 */
public class CommandProcessor {

    private static final String SPEED = "SPEED";

    private final ParkController park;
    private final InvoiceController invoices;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private boolean exitRequested;

    public CommandProcessor(ParkController park, InvoiceController invoices, HelpGenerator help,
                            HelpGenerator extendedHelp) {
        this.park = park;
        this.invoices = invoices;
        this.help = help;
        this.extendedHelp = extendedHelp;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input, PrintStream output) throws IOException {
        String line;
        while (!exitRequested && (line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            output.println(process(line));
        }
    }

    /**
     * Executes one command line and returns the message to show. Never throws for bad input.
     */
    public String process(String line) {
        String[] p = line.strip().split(";", -1);
        String command = p[0].strip();
        try {
            return execute(command, p);
        } catch (ParkException e) {
            return "ERROR: " + e.getMessage();
        } catch (PersistenceException e) {
            return "ERROR: data could not be saved or read: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be processed: " + e.getMessage();
        }
    }

    private String execute(String command, String[] p) throws ParkException {
        return switch (command) {
            case "USER CREATE" -> {
                requireArgs(p, 8, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                yield "USER CREATED";
            }
            case "USER DELETE" -> {
                requireArgs(p, 3, "USER DELETE;<Email>;<Password>");
                park.deleteUser(p[1], p[2]);
                yield "USER DELETED";
            }
            case "USER UPDATE" -> {
                requireArgs(p, 5, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                park.updateUser(p[1], p[2], p[3], p[4]);
                yield "USER UPDATED";
            }
            case "SYSTEM DATE" -> {
                requireArgs(p, 2, "SYSTEM DATE;<yyyy-MM-dd>");
                park.setCurrentDate(p[1]);
                yield "SYSTEM DATE SET: " + park.getCurrentDate();
            }
            case "SYSTEM PRICE" -> {
                requireArgs(p, 2, "SYSTEM PRICE;<Price>");
                park.setCurrentPrice(p[1]);
                yield "SYSTEM PRICE SET: " + money(park.getCurrentPrice());
            }
            case "GROUP CREATE" -> {
                requireArgs(p, 3, "GROUP CREATE;<Identifier>;<Name>");
                park.createGroup(p[1], p[2]);
                yield "GROUP CREATED";
            }
            case "GROUP ADD" -> {
                requireArgs(p, 3, "GROUP ADD;<Identifier>;<Email>");
                park.addToGroup(p[1], p[2]);
                yield "USER ADDED TO GROUP";
            }
            case "GROUP REMOVE" -> {
                requireArgs(p, 3, "GROUP REMOVE;<Identifier>;<Email>");
                park.removeFromGroup(p[1], p[2]);
                yield "USER REMOVED FROM GROUP";
            }
            case "GROUP DELETE" -> {
                requireArgs(p, 2, "GROUP DELETE;<Identifier>");
                park.deleteGroup(p[1]);
                yield "GROUP DELETED";
            }
            case "TICKET CREATE" -> {
                requireArgs(p, 4, "TICKET CREATE;<identifier>;<email>;<date>");
                Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                yield "TICKET CREATED. PRICE: " + money(ticket.getPrice());
            }
            case "TICKET DELETE" -> {
                requireArgs(p, 2, "TICKET DELETE;<identifier>");
                park.deleteTicket(p[1]);
                yield "TICKET DELETED";
            }
            case "GROUPTICKET CREATE" -> {
                requireArgs(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                yield "GROUPTICKET CREATED. TOTAL PRICE: " + money(ticket.getPrice());
            }
            case "GROUPTICKET DELETE" -> {
                requireArgs(p, 2, "GROUPTICKET DELETE;<identifier>");
                park.deleteGroupTicket(p[1]);
                yield "GROUPTICKET DELETED";
            }
            case "ATTRACTION CREATE" -> createAttraction(p);
            case "ATTRACTION ENTER" -> enterAttraction(p);
            case "ATTRACTION EXIT" -> {
                requireArgs(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
                park.exitAttraction(p[1], p[2]);
                yield "ATTRACTION EXITED";
            }
            case "ATTRACTION LIST" -> {
                requireArgs(p, 2, "ATTRACTION LIST;<attraction>");
                yield listOccupants(park.requireAttraction(p[1]));
            }
            case "ATTRACTION STATUS" -> {
                requireArgs(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
                yield status(park.requireAttraction(p[1]));
            }
            case "SPEEDPASS CREATE" -> {
                requireArgs(p, 4, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
                List<SpeedPass> bought = park.buySpeedPasses(p[1], p[2], p[3]);
                BigDecimal total = BigDecimal.ZERO.setScale(2);
                for (SpeedPass pass : bought) {
                    total = total.add(pass.getPrice());
                }
                yield "SPEEDPASS CREATED: " + ids(bought) + ". TOTAL PRICE: " + money(total);
            }
            case "SPEEDPASS DELETE" -> {
                requireArgs(p, 2, "SPEEDPASS DELETE;<speedPassIdentifier>");
                park.refundSpeedPass(p[1]);
                yield "SPEEDPASS DELETED";
            }
            case "SPECIALPASS CREATE" -> {
                requireArgs(p, 3, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
                SpecialPass pass = park.buySpecialPass(p[1], p[2]);
                yield "SPECIALPASS CREATED. PRICE: " + money(pass.getPrice());
            }
            case "SPECIALPASS DELETE" -> {
                requireArgs(p, 2, "SPECIALPASS DELETE;<specialPassIdentifier>");
                park.refundSpecialPass(p[1]);
                yield "SPECIALPASS DELETED";
            }
            case "INVOICE CREATE" -> {
                requireArgs(p, 3, "INVOICE CREATE;USER;<email> or INVOICE CREATE;GROUP;<groupIdentifier>");
                yield switch (p[1]) {
                    case "USER" -> InvoicePrinter.print(invoices.userInvoice(p[2]));
                    case "GROUP" -> InvoicePrinter.print(invoices.groupInvoice(p[2]));
                    default -> throw new ValidationException("invoice type must be USER or GROUP: " + p[1]);
                };
            }
            case "HELP" -> {
                requireArgs(p, 1, "HELP");
                yield help.generate();
            }
            case "HELP EXTENDED" -> {
                requireArgs(p, 1, "HELP EXTENDED");
                yield extendedHelp.generate();
            }
            case "EXIT" -> {
                requireArgs(p, 1, "EXIT");
                exitRequested = true;
                yield "BYE";
            }
            default -> throw new ValidationException("unknown command: " + command);
        };
    }

    private String createAttraction(String[] p) throws ParkException {
        if (p.length == 4) {
            park.createAttraction(p[1], p[2], p[3]);
        } else {
            requireArgs(p, 5, "ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]");
            park.createAttraction(p[1], p[2], p[3], p[4]);
        }
        return "ATTRACTION CREATED";
    }

    private String enterAttraction(String[] p) throws ParkException {
        boolean speed = p.length == 4;
        if (!speed) {
            requireArgs(p, 3, "ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]");
        } else if (!SPEED.equals(p[3].strip())) {
            throw new ValidationException("the optional entry mode must be SPEED: " + p[3]);
        }
        EntryResult result = park.enterAttraction(p[1], p[2], speed);
        StringBuilder message = new StringBuilder("ATTRACTION ENTERED: " + result.users() + " user(s)");
        List<String> speedIds = new ArrayList<>();
        for (Pass pass : result.consumedPasses()) {
            if (pass instanceof SpecialPass) {
                message.append(". SPECIAL PASS CONSUMED: ").append(pass.getId());
            } else {
                speedIds.add(pass.getId());
            }
        }
        if (!speedIds.isEmpty()) {
            message.append(". SPEEDPASS CONSUMED: ").append(String.join(", ", speedIds));
        }
        return message.toString();
    }

    private static String ids(List<? extends Pass> passes) {
        List<String> ids = new ArrayList<>();
        for (Pass pass : passes) {
            ids.add(pass.getId());
        }
        return String.join(", ", ids);
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + " EUR";
    }

    private static String listOccupants(Attraction<?> attraction) {
        List<String> emails = emails(attraction);
        if (emails.isEmpty()) {
            return "NO USERS IN ATTRACTION " + attraction.getId();
        }
        return String.join(System.lineSeparator(), emails);
    }

    private static String status(Attraction<?> a) {
        List<String> emails = emails(a);
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "TYPE: " + a.getType(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + emails.size(),
                "AVAILABLE: " + a.getAvailablePlaces(),
                "USERS: " + (emails.isEmpty() ? "-" : String.join(", ", emails)));
    }

    private static List<String> emails(Attraction<?> attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    private static void requireArgs(String[] parts, int expected, String syntax) throws ValidationException {
        if (parts.length != expected) {
            throw new ValidationException("wrong number of arguments, expected " + syntax);
        }
    }
}
