package es.upm.etsisi.park.business.model.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.user.User;

/**
 * Ticket for a single user, priced as the base price minus a percentage
 * discount. It keeps the passes bought for it: any number of SpeedPasses
 * and at most one Special Pass.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final User user;
    private final BigDecimal basePrice;
    private final int discountPercent;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice, int discountPercent) {
        super(id, date);
        this.user = user;
        this.basePrice = Money.of(basePrice);
        this.discountPercent = discountPercent;
    }

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal basePrice) {
        this(id, date, user, basePrice, 0);
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public BigDecimal getPrice() {
        return Money.of(basePrice.multiply(BigDecimal.valueOf(100L - discountPercent)).divide(HUNDRED));
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return List.of(this);
    }

    @Override
    public IndividualTicket getIndividualTicket() {
        return this;
    }

    // ------------------------------------------------------------------ passes

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public boolean hasSpecialPass() {
        return specialPass != null;
    }

    /** First SpeedPass not yet consumed, or null. */
    public SpeedPass findUnconsumedSpeedPass() {
        for (SpeedPass pass : speedPasses) {
            if (!pass.isConsumed()) {
                return pass;
            }
        }
        return null;
    }

    /** All passes of this ticket (SpeedPasses first, then the Special Pass). */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    /** Links a pass created for this ticket. */
    public void attach(Pass pass) {
        if (pass.getTicket() != this) {
            throw new IllegalArgumentException("Pass " + pass.getId() + " belongs to another ticket");
        }
        if (pass instanceof SpeedPass speedPass) {
            speedPasses.add(speedPass);
        } else if (pass instanceof SpecialPass special) {
            if (specialPass != null) {
                throw new IllegalStateException("Ticket " + getId() + " already has a Special Pass");
            }
            specialPass = special;
        }
    }

    /** Unlinks a refunded or cascaded pass. */
    public void detach(Pass pass) {
        if (pass == specialPass) {
            specialPass = null;
        } else {
            speedPasses.remove(pass);
        }
    }
}
