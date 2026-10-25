package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassKind;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.repository.PassRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the personal passes attached to individual tickets (Singleton): purchase, refund,
 * consumption and cascading deletion.
 */
public final class PassController {

    public static final int PACK_SIZE = 10;

    private static final PassController INSTANCE = new PassController();

    private final List<Pass> passes = new ArrayList<>();
    private PassRepository repository;
    private TicketController tickets;
    private ParkController park;

    private PassController() {
    }

    public static PassController getInstance() {
        return INSTANCE;
    }

    public void configure(PassRepository repository, TicketController tickets,
                          ParkController park) {
        this.repository = repository;
        this.tickets = tickets;
        this.park = park;
    }

    public void load() {
        passes.clear();
        passes.addAll(repository.load(tickets::findIndividual));
    }

    public List<Pass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public Pass find(String id) {
        return passes.stream().filter(pass -> pass.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        return passes.stream().filter(pass -> pass.getTicket() == ticket).toList();
    }

    public SpeedPass findUnusedSpeedPass(IndividualTicket ticket) {
        return passesOf(ticket).stream()
                .filter(pass -> pass instanceof SpeedPass && !pass.isConsumed())
                .map(SpeedPass.class::cast)
                .findFirst().orElse(null);
    }

    public SpecialPass findSpecialPass(IndividualTicket ticket) {
        return passesOf(ticket).stream()
                .filter(SpecialPass.class::isInstance)
                .map(SpecialPass.class::cast)
                .findFirst().orElse(null);
    }

    /**
     * Buys 1 or {@value #PACK_SIZE} SpeedPasses for an active individual ticket.
     */
    public List<SpeedPass> buySpeedPasses(String ticketId, String quantityText) {
        IndividualTicket ticket = activeIndividualTicket(ticketId);
        int quantity = parseQuantity(quantityText);
        int next = passesOf(ticket).stream()
                .filter(SpeedPass.class::isInstance)
                .mapToInt(pass -> sequenceOf(pass.getId(), ticket))
                .max().orElse(0) + 1;
        List<SpeedPass> bought = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            bought.add(new SpeedPass(ticket.getId() + "-SP" + (next + i), ticket, SpeedPass.priceFor(ticket), false));
        }
        passes.addAll(bought);
        save();
        return bought;
    }

    public SpecialPass buySpecialPass(String ticketId) {
        IndividualTicket ticket = activeIndividualTicket(ticketId);
        if (findSpecialPass(ticket) != null) {
            throw new ParkException("ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(ticket.getId() + "-SPECIAL", ticket, SpecialPass.priceFor(ticket), false);
        passes.add(pass);
        save();
        return pass;
    }

    /**
     * Refunds (deletes) an unused pass of the given kind whose ticket is still in the future.
     */
    public Pass refund(String passId, PassKind kind) {
        Pass pass = find(passId);
        if (pass == null || pass.getKind() != kind) {
            throw new ParkException("no " + kind.getDisplayName() + " with identifier " + passId);
        }
        if (pass.isConsumed()) {
            throw new ParkException(kind.getDisplayName() + " " + passId + " has already been used");
        }
        if (!pass.getTicket().isFutureOn(park.getCurrentDate())) {
            throw new ParkException(kind.getDisplayName() + " " + passId + " can only be refunded before "
                    + pass.getTicket().getDate() + "; current date is " + park.getCurrentDate());
        }
        passes.remove(pass);
        save();
        return pass;
    }

    public void consume(Collection<? extends Pass> used) {
        used.forEach(Pass::consume);
        save();
    }

    /**
     * Removes every pass attached to the given tickets.
     */
    public void deleteForTickets(Collection<IndividualTicket> deletedTickets) {
        if (passes.removeIf(pass -> deletedTickets.contains(pass.getTicket()))) {
            save();
        }
    }

    private IndividualTicket activeIndividualTicket(String ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ParkException("passes can only be bought for individual tickets; " + ticketId + " is a group ticket");
        }
        if (!individual.isActiveOn(park.getCurrentDate())) {
            throw new ParkException("ticket " + ticketId + " is for a past date (" + individual.getDate() + ")");
        }
        return individual;
    }

    private static int parseQuantity(String text) {
        String trimmed = text.trim();
        if (trimmed.equals("1")) {
            return 1;
        }
        if (trimmed.equals(String.valueOf(PACK_SIZE))) {
            return PACK_SIZE;
        }
        throw new ParkException("SpeedPasses are sold individually (1) or in packs of " + PACK_SIZE);
    }

    private static int sequenceOf(String passId, IndividualTicket ticket) {
        try {
            return Integer.parseInt(passId.substring((ticket.getId() + "-SP").length()));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private void save() {
        repository.saveAll(passes);
    }
}
