package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket for one user. It owns the personal passes bought for it: any number
 * of SpeedPasses and, during its whole lifetime, at most one Special Pass.
 */
public class IndividualTicket extends Ticket implements IndividualAdmission {

    private static final String SPEED_PASS_PREFIX = "-SP";

    private final User user;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;
    private boolean specialPassIssued;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    /** All passes of this ticket: its SpeedPasses followed by its Special Pass, if any. */
    public List<Pass> getPasses() {
        List<Pass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }

    /** Whether a Special Pass was ever issued for this ticket, even if later consumed or refunded. */
    public boolean isSpecialPassIssued() {
        return specialPassIssued;
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public void removeSpeedPass(SpeedPass pass) {
        speedPasses.remove(pass);
    }

    /** Identifier for the next SpeedPass of this ticket: {@code <ticketId>-SP<n>}. */
    public String nextSpeedPassId() {
        int highest = 0;
        String prefix = getId() + SPEED_PASS_PREFIX;
        for (SpeedPass pass : speedPasses) {
            highest = Math.max(highest, Integer.parseInt(pass.getId().substring(prefix.length())));
        }
        return prefix + (highest + 1);
    }

    public String specialPassId() {
        return getId() + "-SPECIAL";
    }

    /** Attaches the ticket's only Special Pass. */
    public void issueSpecialPass(SpecialPass pass) {
        if (specialPassIssued) {
            throw new IllegalStateException("ticket " + getId() + " already had a Special Pass");
        }
        specialPass = pass;
        specialPassIssued = true;
    }

    /** Detaches the Special Pass; the ticket still counts as having had one. */
    public void removeSpecialPass() {
        specialPass = null;
    }

    /** Restores the state of a stored ticket whose Special Pass was issued and later refunded. */
    public void markSpecialPassIssued() {
        specialPassIssued = true;
    }

    /** Reverts {@link #issueSpecialPass} when the purchase cannot be completed. */
    public void revokeSpecialPass() {
        specialPass = null;
        specialPassIssued = false;
    }

    @Override
    public List<User> getUsers() {
        return List.of(user);
    }

    @Override
    public List<IndividualTicket> getIndividualAdmissions() {
        return List.of(this);
    }
}
