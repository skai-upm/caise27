package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket for one user, with the price paid for it. It may carry several SpeedPasses and at most one Special Pass.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {
    private final User user;
    private final BigDecimal price;
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

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void setSpecialPass(SpecialPass pass) {
        if (specialPass != null) {
            throw new IllegalArgumentException("ticket " + getId() + " already has a Special Pass");
        }
        specialPass = pass;
    }

    /** All passes of this ticket: its SpeedPasses followed by its Special Pass, if any. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    public boolean hasPasses() {
        return !speedPasses.isEmpty() || specialPass != null;
    }

    /** Removes every pass of this ticket (used when the ticket itself is deleted). */
    public void clearPasses() {
        speedPasses.clear();
        specialPass = null;
    }

    /** Re-attaches passes previously returned by {@link #getPasses()}, e.g. to undo a failed deletion. */
    public void restorePasses(List<Pass> passes) {
        clearPasses();
        for (Pass pass : passes) {
            if (pass instanceof SpecialPass special) {
                specialPass = special;
            } else {
                speedPasses.add((SpeedPass) pass);
            }
        }
    }

    public void removePass(Pass pass) {
        if (pass == specialPass) {
            specialPass = null;
        } else {
            speedPasses.remove(pass);
        }
    }
}
