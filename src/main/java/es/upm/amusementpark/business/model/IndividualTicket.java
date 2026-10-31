package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A ticket for a single user. It owns the personal passes bought for it. */
public class IndividualTicket extends Ticket {

    private final User user;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;
    private boolean specialPassIssued;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    /** Returns an unused SpeedPass of this ticket, or {@code null} if none is left. */
    public SpeedPass findUnusedSpeedPass() {
        for (SpeedPass pass : speedPasses) {
            if (!pass.isConsumed()) {
                return pass;
            }
        }
        return null;
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    /** Attaches the ticket's Special Pass; the ticket remembers it ever had one. */
    public void setSpecialPass(SpecialPass pass) {
        specialPass = pass;
        specialPassIssued = true;
    }

    public void removeSpecialPass() {
        specialPass = null;
    }

    /** Whether a Special Pass was ever issued for this ticket, even if later used or refunded. */
    public boolean isSpecialPassIssued() {
        return specialPassIssued;
    }

    /** Restores the lifetime flag when loading a ticket whose Special Pass was refunded. */
    public void markSpecialPassIssued() {
        specialPassIssued = true;
    }

    /** Every pass of this ticket, SpeedPasses first. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
