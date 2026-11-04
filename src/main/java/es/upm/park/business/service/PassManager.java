package es.upm.park.business.service;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.persistence.PassFileStorage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Personal pass operations: purchase of SpeedPasses (1 or 10) and Special Passes
 * (one per ticket), and refunds. Every check happens before any change is saved.
 */
public class PassManager {

    /** Allowed SpeedPass purchase quantities. */
    public static final List<Integer> SPEEDPASS_QUANTITIES = List.of(1, 10);

    private static PassManager instance;

    /** Creates the single instance for this execution with its injected repository; fails if it exists. */
    public static synchronized PassManager initialize(PassFileStorage storage, TicketManager tickets) {
        if (instance != null) {
            throw new IllegalStateException("PassManager is already initialized");
        }
        instance = new PassManager(storage, tickets);
        return instance;
    }

    /** The single instance of this execution; fails if not initialized. */
    public static synchronized PassManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PassManager is not initialized");
        }
        return instance;
    }

    /** Ends the current execution's instance so a new one can be initialized. */
    public static synchronized void reset() {
        instance = null;
    }

    private final PassFileStorage storage;
    private Map<String, Pass> passes = new LinkedHashMap<>();

    private PassManager(PassFileStorage storage, TicketManager tickets) {
        this.storage = storage;
        for (Pass p : storage.load(tickets::findIndividualTicket)) {
            passes.put(p.getId(), p);
        }
    }

    public Collection<Pass> getPasses() {
        return Collections.unmodifiableCollection(passes.values());
    }

    public Pass find(String id) {
        return passes.get(id);
    }

    public List<Pass> passesFor(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass p : passes.values()) {
            if (p.isFor(ticket)) {
                result.add(p);
            }
        }
        return result;
    }

    /** Buys {@code quantity} (1 or 10) SpeedPasses; returns the created passes. */
    public List<Pass> buySpeedPasses(String id, IndividualTicket ticket, int quantity) {
        requireValid(UserValidator.isNonBlank(id), "invalid pass identifier");
        requireValid(SPEEDPASS_QUANTITIES.contains(quantity),
                "invalid quantity " + quantity + ", SpeedPasses are sold in quantities of 1 or 10");
        List<String> ids = new ArrayList<>();
        if (quantity == 1) {
            ids.add(id);
        } else {
            for (int k = 1; k <= quantity; k++) {
                ids.add(id + "-" + k);
            }
        }
        for (String passId : ids) {
            requireValid(!passes.containsKey(passId), "a pass with identifier " + passId + " already exists");
        }
        BigDecimal price = SpeedPass.priceFor(ticket);
        List<Pass> created = new ArrayList<>();
        for (String passId : ids) {
            created.add(new SpeedPass(passId, ticket, ticket.getId(), price, false));
        }
        commitAdding(created);
        return created;
    }

    /** Buys the Special Pass of {@code ticket}; only one may exist per ticket. */
    public Pass buySpecialPass(String id, IndividualTicket ticket) {
        requireValid(UserValidator.isNonBlank(id), "invalid pass identifier");
        requireValid(!passes.containsKey(id), "a pass with identifier " + id + " already exists");
        for (Pass p : passesFor(ticket)) {
            requireValid(!(p instanceof SpecialPass),
                    "ticket " + ticket.getId() + " already has Special Pass " + p.getId());
        }
        Pass pass = new SpecialPass(id, ticket, ticket.getId(), SpecialPass.priceFor(ticket), false);
        commitAdding(List.of(pass));
        return pass;
    }

    /** Refunds an unconsumed pass of the given kind whose ticket is dated after {@code today}. */
    public Pass refund(String id, String kind, LocalDate today) {
        Pass pass = passes.get(id);
        requireValid(pass != null && pass.kind().equals(kind), "no " + kind + " pass with identifier " + id);
        requireValid(!pass.isConsumed(), "pass " + id + " has been consumed and cannot be refunded");
        requireValid(pass.getTicket() != null && pass.getTicket().getDate().isAfter(today),
                "pass " + id + " can only be refunded if its ticket is dated after the system date " + today);
        Map<String, Pass> next = new LinkedHashMap<>(passes);
        next.remove(id);
        storage.save(next.values());
        passes = next;
        return pass;
    }

    /** Persists the current state, e.g. after passes were marked consumed. */
    public void save() {
        storage.save(passes.values());
    }

    private void commitAdding(List<Pass> created) {
        Map<String, Pass> next = new LinkedHashMap<>(passes);
        for (Pass p : created) {
            next.put(p.getId(), p);
        }
        storage.save(next.values());
        passes = next;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
