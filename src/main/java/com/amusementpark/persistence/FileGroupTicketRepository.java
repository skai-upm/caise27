package com.amusementpark.persistence;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.User;
import com.amusementpark.business.validation.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores group tickets as
 * {@code id;date;totalPrice;groupId;groupName;ticketId:dni:price,...}, one per line.
 */
public class FileGroupTicketRepository implements GroupTicketRepository {
    private final Path file;

    public FileGroupTicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads group tickets linked to the given users and groups. A group that no longer exists is
     * restored as a detached group so the ticket history is kept.
     */
    @Override
    public List<GroupTicket> load(List<User> users, List<Group> groups) throws IOException {
        List<GroupTicket> groupTickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return groupTickets;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            GroupTicket groupTicket = parse(line, users, groups);
            if (groupTicket != null) {
                groupTickets.add(groupTicket);
            }
        }
        return groupTickets;
    }

    private static GroupTicket parse(String line, List<User> users, List<Group> groups) {
        String[] fields = line.split(";", -1);
        if (fields.length != 6) {
            return null;
        }
        LocalDate date = Validator.parseDate(fields[1]);
        BigDecimal total = Validator.parsePrice(fields[2]);
        if (date == null || total == null || fields[5].isEmpty()) {
            return null;
        }
        List<IndividualTicket> tickets = new ArrayList<>();
        for (String item : fields[5].split(",")) {
            String[] parts = item.split(":", -1);
            if (parts.length != 3) {
                return null;
            }
            BigDecimal price = Validator.parsePrice(parts[2]);
            if (price == null) {
                return null;
            }
            tickets.add(new IndividualTicket(parts[0], date, price, References.userOrDetached(users, parts[1])));
        }
        Group group = findGroup(groups, fields[3]);
        if (group == null) {
            group = new Group(fields[3], fields[4]);
        }
        return new GroupTicket(fields[0], date, total, group, tickets);
    }

    private static Group findGroup(List<Group> groups, String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public void save(List<GroupTicket> groupTickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (GroupTicket groupTicket : groupTickets) {
            List<String> items = new ArrayList<>();
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                items.add(ticket.getId() + ":" + ticket.getUser().getDni() + ":" + ticket.getPrice().toPlainString());
            }
            lines.add(groupTicket.getId() + ";" + groupTicket.getDate() + ";"
                    + groupTicket.getPrice().toPlainString() + ";" + groupTicket.getGroup().getId() + ";"
                    + groupTicket.getGroup().getName() + ";" + String.join(",", items));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
