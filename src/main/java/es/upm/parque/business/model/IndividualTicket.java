package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket for one user on one date. It owns the personal passes bought for it: any number of
 * SpeedPasses and at most one Special Pass.
 */
public class IndividualTicket extends Ticket implements IndividualAdmission {

    private final User user;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        super(id, date, price);
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public boolean removeSpeedPass(SpeedPass pass) {
        return speedPasses.remove(pass);
    }

    /**
     * The Special Pass of this ticket, or {@code null} if it has none.
     */
    @Override
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void setSpecialPass(SpecialPass specialPass) {
        this.specialPass = specialPass;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return List.of(this);
    }

    /**
     * The first SpeedPass of this ticket not yet consumed, or {@code null} if there is none.
     */
    public SpeedPass nextUnusedSpeedPass() {
        return speedPasses.stream().filter(p -> !p.isConsumed()).findFirst().orElse(null);
    }

    /**
     * Every pass of this ticket: its SpeedPasses followed by its Special Pass, if any.
     */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
