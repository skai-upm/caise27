package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A ticket for a single user, either bought on its own or as part of a group ticket's composition.
 * It owns the passes bought for it: any number of SpeedPasses and at most one Special Pass.
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
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
    }

    @Override
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
        return basePrice.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<User> getHolders() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getEntryTickets() {
        return List.of(this);
    }

    public List<SpeedPass> getSpeedPasses() {
        return List.copyOf(speedPasses);
    }

    /** The first unconsumed SpeedPass in purchase order, if any. */
    public Optional<SpeedPass> firstAvailableSpeedPass() {
        return speedPasses.stream().filter(p -> !p.isConsumed()).findFirst();
    }

    @Override
    public Optional<SpecialPass> getSpecialPass() {
        return Optional.ofNullable(specialPass);
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    public void setSpecialPass(SpecialPass pass) {
        this.specialPass = pass;
    }

    /** All passes owned by this ticket. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
