package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.exception.DuplicateException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ticket for a single user. Also used as the components of a {@link GroupTicket}.
 * Holds the SpeedPasses (0..*) and the Special Pass (0..1) bought for it.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private final User user;
    private final int discountPercent;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user) {
        this(id, date, basePrice, user, 0);
    }

    IndividualTicket(String id, LocalDate date, BigDecimal basePrice, User user, int discountPercent) {
        super(id, date, basePrice);
        this.user = user;
        this.discountPercent = discountPercent;
    }

    public User getUser() {
        return user;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public BigDecimal getPrice() {
        return discounted(getBasePrice(), discountPercent);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    @Override
    public IndividualTicket getIndividualTicket() {
        return this;
    }

    @Override
    public List<IndividualTicket> getAccessTickets() {
        return List.of(this);
    }

    // ----- Passes -----

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public Optional<SpecialPass> getSpecialPass() {
        return Optional.ofNullable(specialPass);
    }

    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    public Optional<SpeedPass> findAvailableSpeedPass() {
        for (SpeedPass pass : speedPasses) {
            if (!pass.isConsumed()) {
                return Optional.of(pass);
            }
        }
        return Optional.empty();
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void setSpecialPass(SpecialPass pass) {
        if (specialPass != null) {
            throw new DuplicateException("Ticket " + getId() + " already has Special Pass " + specialPass.getId());
        }
        specialPass = pass;
    }

    public void removePass(Pass pass) {
        if (pass == specialPass) {
            specialPass = null;
        } else {
            speedPasses.remove(pass);
        }
    }
}
