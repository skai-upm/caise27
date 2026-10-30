package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores passes as {@code SPEED|SPECIAL;passId;ticketId;price;consumed}, one pass per line.
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
        Map<String, IndividualTicket> byId = new HashMap<>();
        for (IndividualTicket t : tickets) {
            byId.put(t.getId(), t);
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.trim().isEmpty() && !attach(line, byId)) {
                System.err.println("WARNING: skipping invalid pass record: " + line);
            }
        }
    }

    private static boolean attach(String line, Map<String, IndividualTicket> byId) {
        String[] f = line.split(";", -1);
        if (f.length != 5 || !byId.containsKey(f[2])) {
            return false;
        }
        IndividualTicket ticket = byId.get(f[2]);
        BigDecimal price;
        try {
            price = new BigDecimal(f[3]);
        } catch (NumberFormatException e) {
            return false;
        }
        boolean consumed = Boolean.parseBoolean(f[4]);
        if (SPEED.equals(f[0])) {
            ticket.attachSpeedPass(new SpeedPass(f[1], price, consumed));
            return true;
        }
        if (SPECIAL.equals(f[0])) {
            ticket.setSpecialPass(new SpecialPass(f[1], price, consumed));
            return true;
        }
        return false;
    }

    @Override
    public void save(List<IndividualTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            for (Pass pass : t.getPasses()) {
                String kind = pass instanceof SpecialPass ? SPECIAL : SPEED;
                lines.add(String.join(";", kind, pass.getId(), t.getId(), Formats.formatPrice(pass.getPrice()),
                        String.valueOf(pass.isConsumed())));
            }
        }
        TextFiles.write(file, lines);
    }
}
