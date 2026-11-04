package com.parque.persistence;

import com.parque.business.model.IndividualTicket;
import com.parque.business.model.Pass;
import com.parque.business.model.SpecialPass;
import com.parque.business.model.SpeedPass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes, one per line: {@code Tipo;Identificador;Ticket;Precio;Consumido} ({@code SPEED}/{@code SPECIAL},
 * {@code true}/{@code false}).
 */
public class PassRepository {
    private static final String SEPARATOR = ";";

    private final Path file;

    public PassRepository(Path file) {
        this.file = file;
    }

    public List<Pass> load(Function<String, IndividualTicket> ticketById) throws IOException {
        List<Pass> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != 5 || !(f[4].equals("true") || f[4].equals("false"))) {
                    throw new IllegalArgumentException("malformed");
                }
                IndividualTicket ticket = ticketById.apply(f[2]);
                if (ticket == null) {
                    System.err.println("Ignoring pass " + f[1] + " of unknown ticket " + f[2]);
                    continue;
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                switch (f[0]) {
                    case "SPEED" -> result.add(new SpeedPass(f[1], ticket, price, consumed));
                    case "SPECIAL" -> result.add(new SpecialPass(f[1], ticket, price, consumed));
                    default -> throw new IllegalArgumentException("unknown kind");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed pass line " + lineNumber + " in " + file);
            }
        }
        return result;
    }

    public void save(List<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(SEPARATOR, p.getKind(), p.getId(), p.getTicket().getId(), p.getPrice().toPlainString(),
                    String.valueOf(p.isConsumed())));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
