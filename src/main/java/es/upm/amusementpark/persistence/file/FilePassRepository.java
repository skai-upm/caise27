package es.upm.amusementpark.persistence.file;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.persistence.TicketRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Stores passes, one per line: {@code SPEED|SPECIAL;id;ticketId;price;consumed}. Tickets are linked by identifier,
 * so this repository is loaded after the tickets.
 */
public class FilePassRepository implements PassRepository {

    public static final String FILE_NAME = "passes.txt";

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;
    private final TicketRepository tickets;
    private final List<Pass> passes = new ArrayList<>();

    public FilePassRepository(Path file, TicketRepository tickets) {
        this.file = file;
        this.tickets = tickets;
    }

    public void load() {
        passes.clear();
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(";", -1);
                Ticket ticket = fields.length == 5 ? tickets.findById(fields[2]) : null;
                if (!(ticket instanceof IndividualTicket individual)) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                    continue;
                }
                BigDecimal price = new BigDecimal(fields[3]);
                boolean consumed = Boolean.parseBoolean(fields[4]);
                if (fields[0].equals(SPEED)) {
                    passes.add(new SpeedPass(fields[1], individual, price, consumed));
                } else if (fields[0].equals(SPECIAL)) {
                    passes.add(new SpecialPass(fields[1], individual, price, consumed));
                } else {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot read passes file " + file, e);
        }
    }

    @Override
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Pass pass : passes) {
                writer.write(String.join(";", pass instanceof SpecialPass ? SPECIAL : SPEED, pass.getId(),
                        pass.getTicket().getId(), pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot write passes file " + file, e);
        }
    }

    @Override
    public List<Pass> findAll() {
        return Collections.unmodifiableList(passes);
    }

    @Override
    public Pass findById(String id) {
        for (Pass pass : passes) {
            if (pass.getId().equals(id)) {
                return pass;
            }
        }
        return null;
    }

    @Override
    public boolean exists(String id) {
        return findById(id) != null;
    }

    @Override
    public List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    @Override
    public void addAll(Collection<? extends Pass> newPasses) {
        passes.addAll(newPasses);
        save();
    }

    @Override
    public void removeAll(Collection<? extends Pass> removed) {
        passes.removeAll(removed);
        save();
    }
}
