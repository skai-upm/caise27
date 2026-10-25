package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket for one user. It owns its personal passes: any number of SpeedPasses and at most one Special Pass
 * over its whole lifetime.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private static final String SPEED_PASS_SUFFIX = "-SP";
    private static final String SPECIAL_PASS_SUFFIX = "-SPECIAL";

    private final User user;
    private final BigDecimal price;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;
    private boolean specialPassPurchased;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        this(id, date, user, price, false);
    }

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price, boolean specialPassPurchased) {
        super(id, date);
        this.user = user;
        this.price = price;
        this.specialPassPurchased = specialPassPurchased;
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
    public List<IndividualTicket> getIndividualAccesses() {
        return List.of(this);
    }

    public List<SpeedPass> getSpeedPasses() {
        return List.copyOf(speedPasses);
    }

    @Override
    public SpeedPass findUnusedSpeedPass() {
        for (SpeedPass pass : speedPasses) {
            if (!pass.isConsumed()) {
                return pass;
            }
        }
        return null;
    }

    @Override
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    /**
     * True once a Special Pass has been bought for this ticket, even if it was later refunded.
     */
    public boolean isSpecialPassPurchased() {
        return specialPassPurchased;
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

    /**
     * Identifier for the next SpeedPass: {@code <ticket>-SP<n>} with n one higher than any existing one.
     */
    public String nextSpeedPassId() {
        int highest = 0;
        for (SpeedPass pass : speedPasses) {
            String number = pass.getId().substring(pass.getId().lastIndexOf(SPEED_PASS_SUFFIX)
                    + SPEED_PASS_SUFFIX.length());
            highest = Math.max(highest, Integer.parseInt(number));
        }
        return getId() + SPEED_PASS_SUFFIX + (highest + 1);
    }

    public String specialPassId() {
        return getId() + SPECIAL_PASS_SUFFIX;
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    /**
     * Attaches the Special Pass; afterwards no other Special Pass may be bought for this ticket.
     */
    public void attachSpecialPass(SpecialPass pass) {
        this.specialPass = pass;
        this.specialPassPurchased = true;
    }

    public void removeSpecialPass() {
        this.specialPass = null;
    }
}
