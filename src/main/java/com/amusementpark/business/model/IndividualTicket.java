package com.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket for one user, owning its personal passes.
 */
public class IndividualTicket extends Ticket {
    private static final String SPEED_PREFIX = "-SP";

    private final User user;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<IndividualTicket> admittedTickets() {
        return List.of(this);
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    /**
     * Creates and attaches new unused SpeedPasses priced at 20% of this ticket's price.
     */
    public List<SpeedPass> buySpeedPasses(int quantity) {
        int next = nextSpeedPassNumber();
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            bought.add(new SpeedPass(getId() + SPEED_PREFIX + (next + i), SpeedPass.priceFor(getPrice()), false));
        }
        speedPasses.addAll(bought);
        return bought;
    }

    /**
     * Creates and attaches the Special Pass, or returns null if the ticket already has one.
     */
    public SpecialPass buySpecialPass() {
        if (specialPass != null) {
            return null;
        }
        specialPass = new SpecialPass(getId() + "-SPECIAL", getPrice(), false);
        return specialPass;
    }

    /**
     * Attaches a pass restored from storage.
     */
    public void attach(Pass pass) {
        if (pass instanceof SpeedPass speedPass) {
            speedPasses.add(speedPass);
        } else if (pass instanceof SpecialPass special) {
            specialPass = special;
        }
    }

    /**
     * Detaches the given pass; returns whether it belonged to this ticket.
     */
    public boolean remove(Pass pass) {
        if (pass == specialPass) {
            specialPass = null;
            return true;
        }
        return speedPasses.remove(pass);
    }

    /**
     * Finds one of this ticket's passes by identifier.
     */
    public Pass findPass(String id) {
        if (specialPass != null && specialPass.getId().equals(id)) {
            return specialPass;
        }
        for (SpeedPass speedPass : speedPasses) {
            if (speedPass.getId().equals(id)) {
                return speedPass;
            }
        }
        return null;
    }

    public SpeedPass findUnusedSpeedPass() {
        for (SpeedPass speedPass : speedPasses) {
            if (!speedPass.isConsumed()) {
                return speedPass;
            }
        }
        return null;
    }

    private int nextSpeedPassNumber() {
        int highest = 0;
        for (SpeedPass speedPass : speedPasses) {
            String id = speedPass.getId();
            int at = id.lastIndexOf(SPEED_PREFIX);
            if (at >= 0) {
                try {
                    highest = Math.max(highest, Integer.parseInt(id.substring(at + SPEED_PREFIX.length())));
                } catch (NumberFormatException e) {
                    // not a generated identifier; ignore
                }
            }
        }
        return highest + 1;
    }
}
