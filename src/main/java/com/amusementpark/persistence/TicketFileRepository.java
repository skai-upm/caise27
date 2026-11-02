package com.amusementpark.persistence;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;
import com.amusementpark.business.model.User;

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
 * Persists individual tickets ({@code id;date;price;dni;email;name;surname;groupTicketId;hadSpecialPass}),
 * their passes, and
 * group tickets ({@code id;date;price;groupId;groupName}). Holders and groups that no longer exist
 * are rebuilt from the stored snapshot so historical tickets survive.
 */
public class TicketFileRepository implements TicketRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final LineFile ticketFile;
    private final LineFile groupTicketFile;
    private final LineFile passFile;

    public TicketFileRepository(Path ticketFile, Path groupTicketFile, Path passFile) {
        this.ticketFile = new LineFile(ticketFile);
        this.groupTicketFile = new LineFile(groupTicketFile);
        this.passFile = new LineFile(passFile);
    }

    @Override
    public Loaded load(List<User> users, List<Group> groups) throws IOException {
        Map<String, User> detachedUsers = new HashMap<>();
        Map<String, List<IndividualTicket>> constituents = new LinkedHashMap<>();
        List<IndividualTicket> tickets = new ArrayList<>();
        for (String[] f : ticketFile.read(8, 9)) {
            User user = GroupFileRepository.findByDni(users, f[3]);
            if (user == null) {
                user = detachedUsers.computeIfAbsent(f[3], dni -> new User(f[5], f[6], dni, f[4], "", "", ""));
            }
            IndividualTicket ticket;
            try {
                boolean hadSpecialPass = f.length > 8 && Boolean.parseBoolean(f[8]);
                ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), user, hadSpecialPass);
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
        loadPasses(tickets);
        return new Loaded(tickets, groupTickets);
    }

    /** Attaches stored passes ({@code id;kind;ticketId;price;consumed}) to their tickets. */
    private void loadPasses(List<IndividualTicket> tickets) throws IOException {
        Map<String, IndividualTicket> byId = new HashMap<>();
        for (IndividualTicket t : tickets) {
            byId.put(t.getId(), t);
        }
        for (String[] f : passFile.read(5)) {
            IndividualTicket ticket = byId.get(f[2]);
            if (ticket == null) {
                System.err.println("WARNING: skipping pass of unknown ticket: " + f[0]);
                continue;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(f[3]);
            } catch (NumberFormatException e) {
                System.err.println("WARNING: skipping pass with invalid price: " + f[0]);
                continue;
            }
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[1])) {
                ticket.addSpeedPass(new SpeedPass(f[0], price, consumed));
            } else if (SPECIAL.equals(f[1])) {
                ticket.setSpecialPass(new SpecialPass(f[0], price, consumed));
            } else {
                System.err.println("WARNING: skipping pass of unknown kind: " + f[0]);
            }
        }
    }

    @Override
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
        List<String> passLines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            User u = t.getUser();
            ticketLines.add(LineFile.join(t.getId(), t.getDate().toString(), t.getPrice().toPlainString(),
                    u.getDni(), u.getEmail(), u.getName(), u.getSurname(), owner.getOrDefault(t, ""),
                    Boolean.toString(t.hasHadSpecialPass())));
            for (Pass p : t.getPasses()) {
                passLines.add(LineFile.join(p.getId(), p instanceof SpecialPass ? SPECIAL : SPEED, t.getId(),
                        p.getPrice().toPlainString(), Boolean.toString(p.isConsumed())));
            }
        }
        // Dependents first: passes, then individual tickets, then group tickets.
        passFile.write(passLines);
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
