package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A ticket for one user. It may be bought directly or be part of a {@link GroupTicket},
 * and it owns the user's personal passes.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final String ownerDni;
    private final GroupTicket groupTicket;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    /**
     * @param user        owner, or null for a historical ticket whose user was deleted
     * @param ownerDni    DNI of the owner, kept for history
     * @param groupTicket group ticket this ticket belongs to, or null if bought individually
     */
    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user, String ownerDni,
                            GroupTicket groupTicket) {
        super(id, date, price);
        this.user = user;
        this.ownerDni = ownerDni;
        this.groupTicket = groupTicket;
    }

    public User getUser() {
        return user;
    }

    public String getOwnerDni() {
        return ownerDni;
    }

    public GroupTicket getGroupTicket() {
        return groupTicket;
    }

    public boolean isPartOfGroupTicket() {
        return groupTicket != null;
    }

    @Override
    public List<User> getUsers() {
        return user == null ? List.of() : List.of(user);
    }

    @Override
    public List<IndividualTicket> getAccessTickets() {
        return List.of(this);
    }

    public List<SpeedPass> getSpeedPasses() {
        return List.copyOf(speedPasses);
    }

    public Optional<SpeedPass> firstUnusedSpeedPass() {
        return speedPasses.stream().filter(p -> !p.isConsumed()).findFirst();
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void setSpecialPass(SpecialPass specialPass) {
        this.specialPass = specialPass;
    }

    /** All personal passes of this ticket. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    public void removeAllPasses() {
        speedPasses.clear();
        specialPass = null;
    }
}
