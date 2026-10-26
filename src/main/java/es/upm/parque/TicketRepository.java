package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in two text files.
 * Individual tickets: {@code id;date;price;dni;email;name;surname;groupTicketId} (last field empty for
 * standalone tickets; the holder's identity is kept so tickets of deleted users remain as history).
 * Group tickets: {@code id;date;groupId;groupName}.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public TicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    /**
     * Loads all individual and group tickets. Holders are resolved by DNI and groups by identifier; when they
     * no longer exist they are rebuilt from the stored data. Malformed lines are skipped.
     */
    public List<Ticket> loadAll(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException {
        Map<String, String[]> groupTicketLines = new LinkedHashMap<>();
        for (String line : readLines(groupTicketsFile)) {
            String[] fields = line.split(SEPARATOR, -1);
            if (fields.length == 4) {
                groupTicketLines.put(fields[0], fields);
            }
        }
        List<Ticket> tickets = new ArrayList<>();
        Map<String, List<IndividualTicket>> internalTickets = new HashMap<>();
        for (String line : readLines(ticketsFile)) {
            String[] fields = line.split(SEPARATOR, -1);
            IndividualTicket ticket = parseIndividual(fields, userByDni);
            if (ticket != null) {
                tickets.add(ticket);
                if (fields.length == 8 && !fields[7].isEmpty()) {
                    internalTickets.computeIfAbsent(fields[7], k -> new ArrayList<>()).add(ticket);
                }
            }
        }
        for (String[] fields : groupTicketLines.values()) {
            List<IndividualTicket> composition = internalTickets.get(fields[0]);
            if (composition != null) {
                Group group = groupById.apply(fields[2]);
                if (group == null) {
                    group = new Group(fields[2], fields[3]);
                }
                tickets.add(new GroupTicket(fields[0], LocalDate.parse(fields[1]), group, composition));
            }
        }
        return tickets;
    }

    private static IndividualTicket parseIndividual(String[] fields, Function<String, User> userByDni) {
        if (fields.length != 7 && fields.length != 8) {
            return null;
        }
        try {
            User user = userByDni.apply(fields[3]);
            if (user == null) {
                user = new User(fields[5], fields[6], fields[3], fields[4], "", "", "");
            }
            return new IndividualTicket(fields[0], LocalDate.parse(fields[1]), user, new BigDecimal(fields[2]));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static List<String> readLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (Files.exists(file)) {
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public void saveAll(Collection<Ticket> tickets) throws IOException {
        Map<IndividualTicket, String> owners = new HashMap<>();
        List<String> groupLines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                groupTicket.getTickets().forEach(internal -> owners.put(internal, groupTicket.getId()));
                groupLines.add(String.join(SEPARATOR, groupTicket.getId(), groupTicket.getDate().toString(),
                        groupTicket.getGroup().getId(), groupTicket.getGroup().getName()));
            }
        }
        List<String> ticketLines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket individual) {
                User user = individual.getUser();
                ticketLines.add(String.join(SEPARATOR, individual.getId(), individual.getDate().toString(),
                        individual.getPrice().toPlainString(), user.getDni(), user.getEmail(),
                        user.getName(), user.getSurname(), owners.getOrDefault(individual, "")));
            }
        }
        writeLines(ticketsFile, ticketLines);
        writeLines(groupTicketsFile, groupLines);
    }

    private static void writeLines(Path file, List<String> lines) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
