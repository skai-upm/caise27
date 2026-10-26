package es.upm.parque.presentation;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.EntryResult;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.presentation.help.HelpGenerator;

import java.util.List;

/**
 * Interprets one text command, executes it and returns the response line.
 * Commands have the form {@code OPERATION;param1;param2;...}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String SPEED_OPTION = "SPEED";



    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final AttractionController attractionController;
    private final InvoiceController invoiceController;
    private final PassController passController;
    private final SystemDate systemDate;
    private final HelpGenerator helpGenerator;
    private boolean exitRequested;

    public CommandProcessor(BusinessControllers controllers, SystemDate systemDate, HelpGenerator helpGenerator) {
        this.userController = controllers.users();
        this.groupController = controllers.groups();
        this.ticketController = controllers.tickets();
        this.attractionController = controllers.attractions();
        this.invoiceController = controllers.invoices();
        this.passController = controllers.passes();
        this.systemDate = systemDate;
        this.helpGenerator = helpGenerator;
    }

    public String process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                case "GROUP CREATE" -> createGroup(parts);
                case "GROUP ADD" -> addToGroup(parts);
                case "GROUP REMOVE" -> removeFromGroup(parts);
                case "GROUP DELETE" -> deleteGroup(parts);
                case "TICKET CREATE" -> createTicket(parts);
                case "TICKET DELETE" -> deleteTicket(parts);
                case "GROUPTICKET CREATE" -> createGroupTicket(parts);
                case "GROUPTICKET DELETE" -> deleteGroupTicket(parts);
                case "ATTRACTION CREATE" -> createAttraction(parts);
                case "ATTRACTION DELETE" -> deleteAttraction(parts);
                case "ATTRACTION ENTER" -> enterAttraction(parts);
                case "ATTRACTION EXIT" -> exitAttraction(parts);
                case "ATTRACTION STATUS" -> attractionStatus(parts);
                case "INVOICE CREATE" -> createInvoice(parts);
                case "SPEEDPASS CREATE" -> buySpeedPasses(parts);
                case "SPEEDPASS DELETE" -> refundSpeedPass(parts);
                case "SPECIALPASS CREATE" -> buySpecialPass(parts);
                case "SPECIALPASS DELETE" -> refundSpecialPass(parts);
                case "SYSTEM DATE" -> setSystemDate(parts);
                case "SYSTEM PRICE" -> setSystemPrice(parts);
                case "EXIT" -> exit(parts);
                default -> isHelp(operation) ? help(operation, parts) : "ERROR Unknown command: " + operation;
            };
        } catch (BusinessException | IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8) {
            return usage("USER CREATE");
        }
        userController.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3) {
            return usage("USER DELETE");
        }
        User user = userController.authenticate(parts[1], parts[2]);
        if (ticketController.hasCurrentOrFutureTicket(user)) {
            return "ERROR User " + parts[1] + " has current or future tickets and cannot be deleted";
        }
        userController.delete(user);
        groupController.removeFromAllGroups(user);
        return "OK USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5) {
            return usage("USER UPDATE");
        }
        userController.update(parts[1], parts[2], parts[3], parts[4]);
        return "OK USER UPDATED";
    }

    private String createGroup(String[] parts) {
        if (parts.length != 3) {
            return usage("GROUP CREATE");
        }
        groupController.create(parts[1], parts[2]);
        return "OK GROUP CREATED";
    }

    private String addToGroup(String[] parts) {
        if (parts.length != 3) {
            return usage("GROUP ADD");
        }
        requireUnlocked(parts[1]);
        groupController.addMember(parts[1], parts[2]);
        return "OK USER ADDED TO GROUP";
    }

    private String removeFromGroup(String[] parts) {
        if (parts.length != 3) {
            return usage("GROUP REMOVE");
        }
        requireUnlocked(parts[1]);
        groupController.removeMember(parts[1], parts[2]);
        return "OK USER REMOVED FROM GROUP";
    }

    private String deleteGroup(String[] parts) {
        if (parts.length != 2) {
            return usage("GROUP DELETE");
        }
        requireUnlocked(parts[1]);
        groupController.delete(parts[1]);
        return "OK GROUP DELETED";
    }

    private void requireUnlocked(String groupId) {
        Group group = groupController.find(groupId);
        if (group != null && ticketController.isGroupLocked(group)) {
            throw new BusinessException("Group " + groupId + " has current or future group tickets and cannot be modified");
        }
    }

    private String createGroupTicket(String[] parts) {
        if (parts.length != 4) {
            return usage("GROUPTICKET CREATE");
        }
        GroupTicket ticket = ticketController.createGroupTicket(parts[1], parts[2], parts[3]);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteGroupTicket(String[] parts) {
        if (parts.length != 2) {
            return usage("GROUPTICKET DELETE");
        }
        requireNoPasses(parts[1]);
        ticketController.deleteGroupTicket(parts[1]);
        return "OK GROUP TICKET DELETED";
    }

    private String createAttraction(String[] parts) {
        if (parts.length == 4) {
            attractionController.create(parts[1], parts[2], parts[3]);
        } else if (parts.length == 5) {
            attractionController.create(parts[1], parts[2], parts[3], parts[4]);
        } else {
            return usage("ATTRACTION CREATE");
        }
        return "OK ATTRACTION CREATED";
    }

    private String deleteAttraction(String[] parts) {
        if (parts.length != 2) {
            return usage("ATTRACTION DELETE");
        }
        attractionController.delete(parts[1]);
        return "OK ATTRACTION DELETED";
    }

    private String enterAttraction(String[] parts) {
        boolean speed = parts.length == 4 && SPEED_OPTION.equals(parts[3].trim());
        if (parts.length != 3 && !speed) {
            return usage("ATTRACTION ENTER");
        }
        EntryResult result = attractionController.enter(parts[1], parts[2], speed);
        StringBuilder response = new StringBuilder("OK ATTRACTION ENTERED -> ").append(result.users()).append(" users");
        List<Pass> consumed = result.consumedPasses();
        if (!consumed.isEmpty()) {
            boolean special = consumed.get(0) instanceof SpecialPass;
            response.append(special ? ", Special Pass consumed: " : ", SpeedPasses consumed: ")
                    .append(String.join(", ", consumed.stream().map(Pass::getId).toList()));
        }
        return response.toString();
    }

    private String exitAttraction(String[] parts) {
        if (parts.length != 3) {
            return usage("ATTRACTION EXIT");
        }
        int left = attractionController.exit(parts[1], parts[2]);
        return "OK ATTRACTION EXITED -> " + left + " users";
    }

    private String attractionStatus(String[] parts) {
        if (parts.length != 2) {
            return usage("ATTRACTION STATUS");
        }
        Attraction<?> attraction = attractionController.require(parts[1]);
        List<User> visitors = attraction.getVisitors();
        StringBuilder status = new StringBuilder("OK ATTRACTION STATUS ").append(attraction.getId())
                .append(System.lineSeparator()).append("Name: ").append(attraction.getName())
                .append(System.lineSeparator()).append("Type: ").append(attraction.getType())
                .append(System.lineSeparator()).append("Capacity: ").append(attraction.getCapacity())
                .append(System.lineSeparator()).append("Present: ").append(visitors.size())
                .append(System.lineSeparator()).append("Available: ").append(attraction.available())
                .append(System.lineSeparator()).append("Users:");
        if (visitors.isEmpty()) {
            status.append(System.lineSeparator()).append("- none");
        }
        for (User user : visitors) {
            status.append(System.lineSeparator()).append("- ").append(user.getName()).append(' ')
                    .append(user.getSurname()).append(" (").append(user.getDni()).append(')');
        }
        return status.toString();
    }

    private void requireNoPasses(String ticketId) {
        Ticket ticket = ticketController.find(ticketId);
        if (ticket != null && passController.hasPasses(ticket)) {
            throw new BusinessException("Ticket " + ticketId + " has passes; refund them before deleting the ticket");
        }
    }

    private String buySpeedPasses(String[] parts) {
        if (parts.length != 3) {
            return usage("SPEEDPASS CREATE");
        }
        List<SpeedPass> bought = passController.buySpeedPasses(parts[1], parts[2]);
        return "OK SPEEDPASS CREATED -> " + bought.size() + " passes, "
                + PassController.total(bought).toPlainString();
    }

    private String buySpecialPass(String[] parts) {
        if (parts.length != 2) {
            return usage("SPECIALPASS CREATE");
        }
        Pass pass = passController.buySpecialPass(parts[1]);
        return "OK SPECIALPASS CREATED -> " + pass.getPrice().toPlainString();
    }

    private String refundSpeedPass(String[] parts) {
        if (parts.length != 2) {
            return usage("SPEEDPASS DELETE");
        }
        Pass pass = passController.refundSpeedPass(parts[1]);
        return "OK SPEEDPASS DELETED -> refunded " + pass.getPrice().toPlainString();
    }

    private String refundSpecialPass(String[] parts) {
        if (parts.length != 2) {
            return usage("SPECIALPASS DELETE");
        }
        Pass pass = passController.refundSpecialPass(parts[1]);
        return "OK SPECIALPASS DELETED -> refunded " + pass.getPrice().toPlainString();
    }

    private String createInvoice(String[] parts) {
        if (parts.length != 3) {
            return usage("INVOICE CREATE");
        }
        Invoice invoice = switch (parts[1].trim()) {
            case "USER" -> invoiceController.userInvoice(parts[2]);
            case "GROUP" -> invoiceController.groupInvoice(parts[2]);
            default -> throw new BusinessException("Invalid invoice recipient type: " + parts[1] + " (USER or GROUP)");
        };
        return "OK INVOICE" + System.lineSeparator() + String.join(System.lineSeparator(), invoice.toLines());
    }

    private String createTicket(String[] parts) {
        if (parts.length != 4) {
            return usage("TICKET CREATE");
        }
        IndividualTicket ticket = ticketController.create(parts[1], parts[2], parts[3]);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteTicket(String[] parts) {
        if (parts.length != 2) {
            return usage("TICKET DELETE");
        }
        requireNoPasses(parts[1]);
        ticketController.delete(parts[1]);
        return "OK TICKET DELETED";
    }

    private String setSystemPrice(String[] parts) {
        if (parts.length != 2) {
            return usage("SYSTEM PRICE");
        }
        ticketController.setCurrentPrice(parts[1]);
        return "OK SYSTEM PRICE " + ticketController.getCurrentPrice().toPlainString();
    }

    private String setSystemDate(String[] parts) {
        if (parts.length != 2) {
            return usage("SYSTEM DATE");
        }
        systemDate.set(SystemDate.parse(parts[1].trim()));
        return "OK SYSTEM DATE " + SystemDate.format(systemDate.get());
    }

    private static boolean isHelp(String operation) {
        return operation.equals("HELP") || operation.startsWith("HELP ");
    }

    private String help(String operation, String[] parts) {
        if (parts.length != 1) {
            return usage(operation);
        }
        String mode = operation.substring("HELP".length()).trim();
        String text = helpGenerator.generate(mode);
        if (text == null) {
            return "ERROR Unknown help mode: " + mode + " (available: HELP, "
                    + String.join(", ", helpGenerator.modes().stream().filter(m -> !m.isEmpty())
                            .map(m -> "HELP " + m).toList()) + ")";
        }
        return text;
    }

    private String exit(String[] parts) {
        if (parts.length != 1) {
            return usage("EXIT");
        }
        exitRequested = true;
        return "OK EXIT";
    }

    /**
     * Tells whether EXIT has been processed, so no further commands must be executed.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String usage(String operation) {
        return "ERROR Wrong number of parameters. Usage: " + helpGenerator.getCatalog().syntaxOf(operation);
    }
}
