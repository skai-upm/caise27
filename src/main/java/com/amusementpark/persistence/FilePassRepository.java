package com.amusementpark.persistence;

import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores passes as {@code passId;SPEED|SPECIAL;ticketId;price;consumed}, one per line.
 */
public class FilePassRepository implements PassRepository {
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public FilePassRepository(Path file) {
        this.file = file;
    }

    @Override
    public void load(List<IndividualTicket> tickets) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(";", -1);
            if (fields.length != 5) {
                continue;
            }
            BigDecimal price = parseAmount(fields[3]);
            IndividualTicket ticket = findTicket(tickets, fields[2]);
            boolean consumed = Boolean.parseBoolean(fields[4]);
            if (price == null || ticket == null) {
                continue;
            }
            if (SPEED.equals(fields[1])) {
                ticket.attach(new SpeedPass(fields[0], price, consumed));
            } else if (SPECIAL.equals(fields[1])) {
                ticket.attach(new SpecialPass(fields[0], price, consumed));
            }
        }
    }

    @Override
    public void save(List<IndividualTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            for (SpeedPass speedPass : ticket.getSpeedPasses()) {
                lines.add(line(speedPass, SPEED, ticket));
            }
            if (ticket.getSpecialPass() != null) {
                lines.add(line(ticket.getSpecialPass(), SPECIAL, ticket));
            }
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static String line(Pass pass, String kind, IndividualTicket ticket) {
        return pass.getId() + ";" + kind + ";" + ticket.getId() + ";" + pass.getPrice().toPlainString() + ";"
                + pass.isConsumed();
    }

    private static BigDecimal parseAmount(String text) {
        try {
            BigDecimal amount = new BigDecimal(text);
            return amount.signum() < 0 ? null : amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static IndividualTicket findTicket(List<IndividualTicket> tickets, String id) {
        for (IndividualTicket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }
}
