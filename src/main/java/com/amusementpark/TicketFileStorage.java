package com.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists individual tickets ({@code id;date;price;dni;email;name;surname;groupTicketId}) and
 * group tickets ({@code id;date;price;groupId;groupName}). Holders and groups that no longer exist
 * are rebuilt from the stored snapshot so historical tickets survive.
 */
public class TicketFileStorage {

    /** Tickets read back from disk. */
    public record Loaded(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) {
    }

    private final LineFile ticketFile;
    private final LineFile groupTicketFile;

    public TicketFileStorage(Path ticketFile, Path groupTicketFile) {
        this.ticketFile = new LineFile(ticketFile);
        this.groupTicketFile = new LineFile(groupTicketFile);
    }

    public Loaded load(List<User> users, List<Group> groups) throws IOException {
        Map<String, User> detachedUsers = new HashMap<>();
        Map<String, List<IndividualTicket>> constituents = new LinkedHashMap<>();
        List<IndividualTicket> tickets = new ArrayList<>();
        for (String[] f : ticketFile.read(8)) {
            User user = GroupFileStorage.findByDni(users, f[3]);
            if (user == null) {
                user = detachedUsers.computeIfAbsent(f[3], dni -> new User(f[5], f[6], dni, f[4], "", "", ""));
            }
            IndividualTicket ticket;
            try {
                ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), user);
            } catch (DateTimeParseException | NumberFormatException e) {
                System.err.println("WARNING: skipping ticket with invalid date or price: " + f[0]);
                continue;
            }
            tickets.add(ticket);
            if (!f[7].isEmpty()) {
                constituents.computeIfAbsent(f[7], id -> new ArrayList<>()).add(ticket);
            }
        }
        List<GroupTicket> groupTickets = new ArrayList<>();
        for (String[] f : groupTicketFile.read(5)) {
            Group group = findGroup(groups, f[3]);
            if (group == null) {
                group = new Group(f[3], f[4]);
            }
            try {
                groupTickets.add(new GroupTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), group,
                        constituents.getOrDefault(f[0], List.of())));
            } catch (DateTimeParseException | NumberFormatException e) {
                System.err.println("WARNING: skipping group ticket with invalid date or price: " + f[0]);
            }
        }
        return new Loaded(tickets, groupTickets);
    }

    public void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException {
        Map<IndividualTicket, String> owner = new HashMap<>();
        List<String> groupLines = new ArrayList<>();
        for (GroupTicket gt : groupTickets) {
            for (IndividualTicket t : gt.getTickets()) {
                owner.put(t, gt.getId());
            }
            groupLines.add(LineFile.join(gt.getId(), gt.getDate().toString(), gt.getPrice().toPlainString(),
                    gt.getGroup().getId(), gt.getGroup().getName()));
        }
        List<String> ticketLines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            User u = t.getUser();
            ticketLines.add(LineFile.join(t.getId(), t.getDate().toString(), t.getPrice().toPlainString(),
                    u.getDni(), u.getEmail(), u.getName(), u.getSurname(), owner.getOrDefault(t, "")));
        }
        ticketFile.write(ticketLines);
        groupTicketFile.write(groupLines);
    }

    private static Group findGroup(List<Group> groups, String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }
}
