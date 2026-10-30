package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ticket for one user, bought on its own or as part of a group ticket. Owns its personal passes.
 */
public class IndividualTicket extends Ticket implements IndividualAccess {

    private static final String SPEED_PASS_PREFIX = "-SP";

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
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public List<IndividualAccess> getAccessUnits() {
        return List.of(this);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    @Override
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    /** All passes of this ticket: SpeedPasses first, then the Special Pass if any. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    /** Creates a new unused SpeedPass with the next identifier {@code <ticketId>-SP<n>}. */
    public SpeedPass addSpeedPass() {
        SpeedPass pass = new SpeedPass(getId() + SPEED_PASS_PREFIX + nextSpeedPassNumber(), SpeedPass.priceFor(this),
                false);
        speedPasses.add(pass);
        return pass;
    }

    /** Attaches an existing SpeedPass (used when restoring stored passes). */
    public void attachSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    /** Re-inserts a SpeedPass at a position (used to undo a refund that could not be saved). */
    public void insertSpeedPass(int index, SpeedPass pass) {
        speedPasses.add(index, pass);
    }

    public boolean removeSpeedPass(SpeedPass pass) {
        return speedPasses.remove(pass);
    }

    /** Sets or removes ({@code null}) the Special Pass. */
    public void setSpecialPass(SpecialPass specialPass) {
        this.specialPass = specialPass;
    }

    public SpecialPass createSpecialPass() {
        return new SpecialPass(getId() + "-SPECIAL", SpecialPass.priceFor(this), false);
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

    private int nextSpeedPassNumber() {
        int max = 0;
        String prefix = getId() + SPEED_PASS_PREFIX;
        for (SpeedPass pass : speedPasses) {
            if (pass.getId().startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(pass.getId().substring(prefix.length())));
                } catch (NumberFormatException e) {
                    // identifiers not following the pattern do not affect numbering
                }
            }
        }
        return max + 1;
    }
}
