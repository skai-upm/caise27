package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket for one user on one date. It owns its personal passes: any number of
 * SpeedPasses and at most one Special Pass over its whole lifetime.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;
    private boolean specialPassAcquired;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    public List<SpeedPass> getSpeedPasses() {
        return List.copyOf(speedPasses);
    }

    /**
     * First SpeedPass not yet consumed, or null if there is none.
     */
    public SpeedPass getUnusedSpeedPass() {
        for (SpeedPass p : speedPasses) {
            if (!p.isConsumed()) {
                return p;
            }
        }
        return null;
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    /**
     * The ticket's Special Pass, or null if it has none (never bought or refunded).
     */
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void setSpecialPass(SpecialPass pass) {
        specialPass = pass;
        specialPassAcquired = true;
    }

    public void removeSpecialPass() {
        specialPass = null;
    }

    /**
     * Reverts a Special Pass purchase that could not be persisted.
     */
    public void undoSpecialPassPurchase() {
        specialPass = null;
        specialPassAcquired = false;
    }

    /**
     * True if a Special Pass was ever bought for this ticket, even if later consumed or refunded.
     */
    public boolean hasAcquiredSpecialPass() {
        return specialPassAcquired;
    }

    public void markSpecialPassAcquired() {
        specialPassAcquired = true;
    }

    /**
     * All passes attached to this ticket.
     */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
