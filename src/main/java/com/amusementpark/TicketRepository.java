package com.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores individual tickets bought on their own as {@code id;date;price;dni}.
 */
public class TicketRepository {
    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    public List<IndividualTicket> load(List<User> users) throws IOException {
        List<IndividualTicket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(";", -1);
            if (fields.length != 4) {
                continue;
            }
            LocalDate date = Validator.parseDate(fields[1]);
            BigDecimal price = Validator.parsePrice(fields[2]);
            if (date == null || price == null) {
                continue;
            }
            tickets.add(new IndividualTicket(fields[0], date, price, Repositories.userOrDetached(users, fields[3])));
        }
        return tickets;
    }

    public void save(List<IndividualTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(ticket.getId() + ";" + ticket.getDate() + ";" + ticket.getPrice().toPlainString()
                    + ";" + ticket.getUser().getDni());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
