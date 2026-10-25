package com.amusementpark.business.controller;

import com.amusementpark.business.model.Attraction;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;
import com.amusementpark.business.model.Ticket;
import com.amusementpark.business.model.User;
import com.amusementpark.business.validation.Validator;
import com.amusementpark.persistence.AttractionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages attractions and ticket-based entry and exit, including SpeedPass and Special Pass use.
 */
public final class AttractionController {
    private static final AttractionController INSTANCE = new AttractionController();

    private AttractionRepository repository;
    private ParkController parkController;
    private TicketController ticketController;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository repository, ParkController parkController,
                          TicketController ticketController) {
        this.repository = repository;
        this.parkController = parkController;
        this.ticketController = ticketController;
        attractions.clear();
    }

    public void load() throws IOException {
        attractions.clear();
        attractions.addAll(repository.load());
    }

    public Attraction<?> findAttraction(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    /**
     * Creates an attraction; {@code typeName} null means GENERAL.
     */
    public String create(String id, String name, String capacityText, String typeName) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid attraction id";
        }
        if (name.isBlank()) {
            return "ERROR: attraction name is required";
        }
        int capacity = Validator.parseCapacity(capacityText);
        if (capacity <= 0) {
            return "ERROR: capacity must be a positive integer";
        }
        AttractionType type = typeName == null ? AttractionType.GENERAL : AttractionType.fromName(typeName);
        if (type == null) {
            return "ERROR: unknown attraction type " + typeName + " (GENERAL, INDIVIDUAL, GROUP, SPECIAL)";
        }
        if (findAttraction(id) != null) {
            return "ERROR: attraction " + id + " already exists";
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.add(attraction);
        if (!persist()) {
            attractions.remove(attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: " + type + " attraction created";
    }

    public String delete(String id) {
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        if (!attraction.getPresent().isEmpty()) {
            return "ERROR: attraction " + id + " has users present";
        }
        int index = attractions.indexOf(attraction);
        attractions.remove(index);
        if (!persist()) {
            attractions.add(index, attraction);
            return "ERROR: could not save attractions";
        }
        return "OK: attraction deleted";
    }

    /**
     * Admits the ticket's users; {@code option} is null for normal entry or "SPEED".
     */
    public String enter(String id, String ticketId, String option) {
        if (option != null && !"SPEED".equals(option)) {
            return "ERROR: unknown entry option " + option + " (only SPEED)";
        }
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        Ticket ticket = ticketController.findTicket(ticketId);
        if (ticket == null) {
            return "ERROR: unknown ticket " + ticketId;
        }
        return enter(attraction, ticket, option != null);
    }

    /**
     * Typed entry: every check happens before anyone enters or any pass is consumed.
     */
    private <T extends Ticket> String enter(Attraction<T> attraction, Ticket ticket, boolean speed) {
        T admitted = attraction.admit(ticket);
        if (admitted == null) {
            return "ERROR: " + attraction.getType() + " attraction " + attraction.getId() + " does not admit "
                    + (ticket instanceof IndividualTicket ? "individual" : "group") + " tickets";
        }
        LocalDate today = parkController.getCurrentDate();
        if (!admitted.isValidOn(today)) {
            return "ERROR: ticket " + admitted.getId() + " is for " + admitted.getDate() + ", not " + today;
        }
        if (speed && !attraction.allowsSpeed()) {
            return "ERROR: SPEED is not accepted at " + attraction.getType() + " attractions";
        }
        List<Pass> toConsume = new ArrayList<>();
        for (IndividualTicket member : admitted.admittedTickets()) {
            if (speed) {
                SpeedPass speedPass = member.findUnusedSpeedPass();
                if (speedPass == null) {
                    return "ERROR: ticket " + member.getId() + " has no unused SpeedPass";
                }
                toConsume.add(speedPass);
            }
            if (attraction.requiresSpecialPass()) {
                SpecialPass specialPass = member.getSpecialPass();
                if (specialPass == null || specialPass.isConsumed()) {
                    return "ERROR: ticket " + member.getId() + " has no unused Special Pass";
                }
                toConsume.add(specialPass);
            }
        }
        String error = attraction.enter(admitted);
        if (error != null) {
            return "ERROR: " + error;
        }
        StringBuilder result = new StringBuilder("OK: ").append(admitted.getUsers().size())
                .append(" user(s) entered ").append(attraction.getId());
        if (!toConsume.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (Pass pass : toConsume) {
                pass.consume();
                ids.add(pass.getId());
            }
            result.append("; ").append(speed ? "SpeedPass" : "Special Pass").append(" consumed: ")
                    .append(String.join(", ", ids));
            if (!ticketController.savePasses()) {
                result.append(System.lineSeparator()).append("ERROR: could not save pass consumption");
            }
        }
        return result.toString();
    }

    public String exit(String id, String ticketId) {
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        Ticket ticket = ticketController.findTicket(ticketId);
        if (ticket == null) {
            return "ERROR: unknown ticket " + ticketId;
        }
        int left = attraction.exit(ticket);
        if (left == 0) {
            return "ERROR: no user of ticket " + ticketId + " is in " + id;
        }
        return "OK: " + left + " user(s) left " + id;
    }

    public String status(String id) {
        Attraction<?> attraction = findAttraction(id);
        if (attraction == null) {
            return "ERROR: unknown attraction " + id;
        }
        StringBuilder status = new StringBuilder();
        status.append("Attraction ").append(attraction.getId()).append(" - ").append(attraction.getName())
                .append(System.lineSeparator())
                .append("  Capacity: ").append(attraction.getCapacity()).append(System.lineSeparator())
                .append("  Present: ").append(attraction.getPresent().size()).append(System.lineSeparator())
                .append("  Available: ").append(attraction.getAvailable()).append(System.lineSeparator())
                .append("  Users:");
        if (attraction.getPresent().isEmpty()) {
            status.append(" none");
        }
        for (User user : attraction.getPresent()) {
            status.append(System.lineSeparator()).append("    ").append(user.getName()).append(' ')
                    .append(user.getSurname()).append(" (").append(user.getDni()).append(')');
        }
        return status.toString();
    }

    private boolean persist() {
        try {
            repository.save(attractions);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
