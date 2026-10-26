package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An individual ticket for one date, held by a registered user.
 */
public class IndividualTicket extends Ticket implements IndividualAdmission {

    private final User holder;
    private final BigDecimal price;
    private final List<SpeedPass> speedPasses = new ArrayList<>();
    private SpecialPass specialPass;

    public IndividualTicket(String id, LocalDate date, User holder, BigDecimal price) {
        super(id, date);
        this.holder = holder;
        this.price = price;
    }

    @Override
    public User getHolder() {
        return holder;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<User> getVisitors() {
        return List.of(holder);
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return List.of(this);
    }

    public List<SpeedPass> getSpeedPasses() {
        return Collections.unmodifiableList(speedPasses);
    }

    public void addSpeedPass(SpeedPass pass) {
        speedPasses.add(pass);
    }

    public boolean removeSpeedPass(SpeedPass pass) {
        return speedPasses.remove(pass);
    }

    /**
     * The ticket's Special Pass, or null when it has none.
     */
    @Override
    public SpecialPass getSpecialPass() {
        return specialPass;
    }

    public void setSpecialPass(SpecialPass pass) {
        this.specialPass = pass;
    }

    /**
     * Removes every personal pass of this ticket and returns them, SpeedPasses first.
     */
    public List<PersonalPass> clearPasses() {
        List<PersonalPass> removed = getPasses();
        speedPasses.clear();
        specialPass = null;
        return removed;
    }

    /**
     * Attaches again passes previously removed with clearPasses.
     */
    public void restorePasses(List<PersonalPass> passes) {
        for (PersonalPass pass : passes) {
            if (pass instanceof SpeedPass speedPass) {
                speedPasses.add(speedPass);
            } else if (pass instanceof SpecialPass special) {
                specialPass = special;
            }
        }
    }

    /**
     * All personal passes of this ticket: SpeedPasses first, then the Special Pass if any.
     */
    public List<PersonalPass> getPasses() {
        List<PersonalPass> passes = new ArrayList<>(speedPasses);
        if (specialPass != null) {
            passes.add(specialPass);
        }
        return passes;
    }
}
