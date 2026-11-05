package es.upm.parque.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;

/**
 * Stores SpeedPasses and Special Passes in one identifier space, independent of ticket identifiers.
 */
public class PassRepository {

    private final Map<String, Pass> passesById = new LinkedHashMap<>();

    public boolean exists(String id) {
        return passesById.containsKey(id);
    }

    public void add(Pass pass) {
        passesById.put(pass.getId(), pass);
    }

    public Pass findById(String id) {
        return passesById.get(id);
    }

    public void remove(String id) {
        passesById.remove(id);
    }

    public List<Pass> findAll() {
        return new ArrayList<>(passesById.values());
    }

    /** Passes associated with the ticket, found by traversing the stored passes. */
    public List<Pass> passesOf(IndividualTicket ticket) {
        return passesById.values().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    /** The Special Pass of the ticket, consumed or not, or {@code null} when it has none. */
    public SpecialPass specialPassOf(IndividualTicket ticket) {
        for (Pass pass : passesById.values()) {
            if (pass.getTicket() == ticket && pass instanceof SpecialPass special) {
                return special;
            }
        }
        return null;
    }
}
