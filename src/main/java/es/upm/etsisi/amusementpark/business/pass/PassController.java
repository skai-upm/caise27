package es.upm.etsisi.amusementpark.business.pass;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.attraction.PassBook;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Singleton controller for SpeedPass and Special Pass purchases and refunds. Passes of a
 * ticket are found by traversing the passes managed by the park.
 */
public final class PassController implements PassBook {

    public static final int SINGLE = 1;
    public static final int PACK = 10;

    private Park park;

    private PassController() {
    }

    private static final class Holder {
        private static final PassController INSTANCE = new PassController();
    }

    public static PassController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park parkState) {
        park = parkState;
    }

    public List<Pass> passesOf(IndividualTicket ticket) {
        return park.passes().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    @Override
    public Optional<SpeedPass> unusedSpeedPass(IndividualTicket ticket) {
        return park.passes().stream()
                .filter(p -> p instanceof SpeedPass && p.getTicket() == ticket && !p.isConsumed())
                .map(SpeedPass.class::cast).findFirst();
    }

    @Override
    public Optional<SpecialPass> specialPass(IndividualTicket ticket) {
        return park.passes().stream().filter(p -> p instanceof SpecialPass && p.getTicket() == ticket)
                .map(SpecialPass.class::cast).findFirst();
    }

    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity) throws BusinessException {
        if (quantity != SINGLE && quantity != PACK) {
            throw new InvalidDataException("SpeedPass quantity must be 1 or 10");
        }
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = SpeedPass.passId(purchaseId, n);
            requireUnusedId(id);
            created.add(new SpeedPass(id, ticket));
        }
        park.passes().addAll(created);
        park.commit(() -> park.passes().removeAll(created));
        return created;
    }

    public SpecialPass buySpecialPass(String id, String ticketId) throws BusinessException {
        IndividualTicket ticket = requirePurchasableTicket(ticketId);
        if (specialPass(ticket).isPresent()) {
            throw new OperationNotAllowedException("ticket " + ticketId + " already has a Special Pass");
        }
        requireUnusedId(id);
        SpecialPass pass = new SpecialPass(id, ticket);
        park.passes().add(pass);
        park.commit(() -> park.passes().remove(pass));
        return pass;
    }

    public void refundSpeedPass(String id) throws BusinessException {
        refund(id, SpeedPass.class);
    }

    public void refundSpecialPass(String id) throws BusinessException {
        refund(id, SpecialPass.class);
    }

    private void refund(String id, Class<? extends Pass> type) throws BusinessException {
        Pass pass = park.findPass(id).filter(type::isInstance)
                .orElseThrow(() -> new NotFoundException(type.getSimpleName() + " " + id + " not found"));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("pass " + id + " has been consumed");
        }
        if (!pass.getTicket().isDeletableOn(park.getCurrentDate())) {
            throw new OperationNotAllowedException("pass " + id + " belongs to a ticket that is not in the future");
        }
        int index = park.passes().indexOf(pass);
        park.passes().remove(index);
        park.commit(() -> park.passes().add(index, pass));
    }

    private IndividualTicket requirePurchasableTicket(String ticketId) throws BusinessException {
        Ticket ticket = park.requireTicket(ticketId);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new InvalidDataException("ticket " + ticketId + " is not an individual ticket");
        }
        if (ticket.getDate().isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("ticket " + ticketId + " is dated before the current date");
        }
        return individual;
    }

    private void requireUnusedId(String id) throws OperationNotAllowedException {
        if (park.findPass(id).isPresent()) {
            throw new OperationNotAllowedException("pass identifier " + id + " already in use");
        }
    }
}
