package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket for a single user. It may belong to a group ticket that generated it and owns its personal passes.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;
    private final BigDecimal price;
    private GroupTicket groupTicket;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        super(id, date);
        this.user = user;
        this.price = price;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return List.of(this);
    }

    public GroupTicket getGroupTicket() {
        return groupTicket;
    }

    void setGroupTicket(GroupTicket groupTicket) {
        this.groupTicket = groupTicket;
    }

    public List<SpeedPass> getSpeedPasses() {
        return List.copyOf(speedPasses);
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public boolean removeSpeedPass(SpeedPass pass) {
        return speedPasses.remove(pass);
    }

    /**
     * First SpeedPass not yet consumed, or null.
     */
    public SpeedPass firstUnusedSpeedPass() {
        for (SpeedPass p : speedPasses) {
            if (!p.isConsumed()) {
                return p;
            }
        }
        return null;
    }

    @Override
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void setSpecialPass(SpecialPass specialPass) {
        this.specialPass = specialPass;
    }

    /**
     * All passes of this ticket: SpeedPasses first, then the Special Pass if any.
     */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
