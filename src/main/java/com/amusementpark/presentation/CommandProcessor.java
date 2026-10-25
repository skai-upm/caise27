package com.amusementpark.presentation;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.controller.GroupController;
import com.amusementpark.business.controller.InvoiceController;
import com.amusementpark.business.controller.ParkController;
import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.controller.UserController;
import com.amusementpark.presentation.help.HelpGenerator;

import java.io.PrintStream;

/**
 * Interprets one text command at a time. Shared by interactive and file modes.
 */
public class CommandProcessor {
    private final ParkController park;
    private final UserController users;
    private final GroupController groups;
    private final TicketController tickets;
    private final AttractionController attractions;
    private final InvoiceController invoices;
    private final HelpGenerator help;
    private final PrintStream out;

    public CommandProcessor(ParkController park, UserController users, GroupController groups,
                            TicketController tickets, AttractionController attractions, InvoiceController invoices,
                            HelpGenerator help, PrintStream out) {
        this.park = park;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoices = invoices;
        this.help = help;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return false when processing must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        if (line == null || line.isBlank()) {
            return true;
        }
        String[] parts = line.trim().split(";", -1);
        String operation = parts[0].trim();
        switch (operation) {
            case "USER CREATE":
                if (hasParameters(parts, 7)) {
                    out.println(users.create(parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7]));
                }
                return true;
            case "USER DELETE":
                if (hasParameters(parts, 2)) {
                    out.println(users.delete(parts[1], parts[2]));
                }
                return true;
            case "USER UPDATE":
                if (hasParameters(parts, 4)) {
                    out.println(users.update(parts[1], parts[2], parts[3], parts[4]));
                }
                return true;
            case "SYSTEM DATE":
                if (hasParameters(parts, 1)) {
                    out.println(park.setDate(parts[1]));
                }
                return true;
            case "SYSTEM PRICE":
                if (hasParameters(parts, 1)) {
                    out.println(park.setPrice(parts[1]));
                }
                return true;
            case "GROUP CREATE":
                if (hasParameters(parts, 2)) {
                    out.println(groups.create(parts[1], parts[2]));
                }
                return true;
            case "GROUP ADD":
                if (hasParameters(parts, 2)) {
                    out.println(groups.addMember(parts[1], parts[2]));
                }
                return true;
            case "GROUP REMOVE":
                if (hasParameters(parts, 2)) {
                    out.println(groups.removeMember(parts[1], parts[2]));
                }
                return true;
            case "GROUP DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(groups.delete(parts[1]));
                }
                return true;
            case "TICKET CREATE":
                if (hasParameters(parts, 3)) {
                    out.println(tickets.createTicket(parts[1], parts[2], parts[3]));
                }
                return true;
            case "TICKET DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(tickets.deleteTicket(parts[1]));
                }
                return true;
            case "GROUPTICKET CREATE":
                if (hasParameters(parts, 3)) {
                    out.println(tickets.createGroupTicket(parts[1], parts[2], parts[3]));
                }
                return true;
            case "GROUPTICKET DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(tickets.deleteGroupTicket(parts[1]));
                }
                return true;
            case "SPEEDPASS CREATE":
                if (hasParameters(parts, 2)) {
                    out.println(tickets.buySpeedPasses(parts[1], parts[2]));
                }
                return true;
            case "SPEEDPASS DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(tickets.refundSpeedPass(parts[1]));
                }
                return true;
            case "SPECIALPASS CREATE":
                if (hasParameters(parts, 1)) {
                    out.println(tickets.buySpecialPass(parts[1]));
                }
                return true;
            case "SPECIALPASS DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(tickets.refundSpecialPass(parts[1]));
                }
                return true;
            case "ATTRACTION CREATE":
                if (hasParameters(parts, 3, 4)) {
                    out.println(attractions.create(parts[1], parts[2], parts[3], parts.length > 4 ? parts[4] : null));
                }
                return true;
            case "ATTRACTION DELETE":
                if (hasParameters(parts, 1)) {
                    out.println(attractions.delete(parts[1]));
                }
                return true;
            case "ATTRACTION ENTER":
                if (hasParameters(parts, 2, 3)) {
                    out.println(attractions.enter(parts[1], parts[2], parts.length > 3 ? parts[3] : null));
                }
                return true;
            case "ATTRACTION EXIT":
                if (hasParameters(parts, 2)) {
                    out.println(attractions.exit(parts[1], parts[2]));
                }
                return true;
            case "ATTRACTION STATUS":
                if (hasParameters(parts, 1)) {
                    out.println(attractions.status(parts[1]));
                }
                return true;
            case "INVOICE CREATE":
                if (hasParameters(parts, 2)) {
                    out.println(invoices.create(parts[1], parts[2]));
                }
                return true;
            case "HELP":
                out.println(help.generate(""));
                return true;
            case "HELP EXTENDED":
                out.println(help.generate("EXTENDED"));
                return true;
            case "EXIT":
                out.println("Bye");
                return false;
            default:
                out.println("ERROR: unknown command: " + operation + " (type HELP for the list)");
                return true;
        }
    }

    private boolean hasParameters(String[] parts, int expected) {
        return hasParameters(parts, expected, expected);
    }

    private boolean hasParameters(String[] parts, int min, int max) {
        int received = parts.length - 1;
        if (received < min || received > max) {
            String expected = min == max ? String.valueOf(min) : min + " or " + max;
            out.println("ERROR: " + parts[0].trim() + " expects " + expected
                    + " parameters but received " + received);
            return false;
        }
        return true;
    }
}
