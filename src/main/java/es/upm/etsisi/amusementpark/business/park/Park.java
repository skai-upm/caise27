package es.upm.etsisi.amusementpark.business.park;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Central park management: the single holder of users, groups, tickets, passes and
 * attractions, the current system date and the base price. Controllers operate on this
 * state and call {@link #commit(Runnable)} to persist it through the injected repository.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private ParkRepository repository;
    private final List<User> users = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Pass> passes = new ArrayList<>();
    private final List<Attraction<?>> attractions = new ArrayList<>();
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private Park() {
    }

    private static final class Holder {
        private static final Park INSTANCE = new Park();
    }

    public static Park getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the repository, loads the stored state and resets date and price for a new run. */
    public void configure(ParkRepository parkRepository, LocalDate startDate) throws StorageException {
        ParkData data = parkRepository.load();
        repository = parkRepository;
        replace(users, data.users());
        replace(groups, data.groups());
        replace(tickets, data.tickets());
        replace(passes, data.passes());
        replace(attractions, data.attractions());
        currentDate = startDate;
        basePrice = INITIAL_PRICE;
    }

    // ---- live state, mutated only by controllers ------------------------------------

    public List<User> users() {
        return users;
    }

    public List<Group> groups() {
        return groups;
    }

    /** Top-level tickets: standalone individual tickets and group tickets. */
    public List<Ticket> tickets() {
        return tickets;
    }

    public List<Pass> passes() {
        return passes;
    }

    public List<Attraction<?>> attractions() {
        return attractions;
    }

    // ---- system settings -------------------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) throws InvalidDataException {
        if (price == null || price.signum() <= 0) {
            throw new InvalidDataException("price must be a positive amount");
        }
        basePrice = price;
    }

    // ---- lookups ---------------------------------------------------------------------

    public Optional<User> findUser(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    public User requireUser(String email) throws NotFoundException {
        return findUser(email).orElseThrow(() -> new NotFoundException("user " + email + " not found"));
    }

    public Optional<Group> findGroup(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public Group requireGroup(String id) throws NotFoundException {
        return findGroup(id).orElseThrow(() -> new NotFoundException("group " + id + " not found"));
    }

    /** All tickets in the shared identifier space: top-level tickets and group-ticket constituents. */
    public Stream<Ticket> allTickets() {
        return Stream.concat(tickets.stream(),
                tickets.stream().flatMap(t -> t.isComposite() ? t.getIndividualTickets().stream() : Stream.empty()));
    }

    public Optional<Ticket> findTicket(String id) {
        return allTickets().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Ticket requireTicket(String id) throws NotFoundException {
        return findTicket(id).orElseThrow(() -> new NotFoundException("ticket " + id + " not found"));
    }

    public Optional<Pass> findPass(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Optional<Attraction<?>> findAttraction(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public Attraction<?> requireAttraction(String id) throws NotFoundException {
        return findAttraction(id).orElseThrow(() -> new NotFoundException("attraction " + id + " not found"));
    }

    // ---- persistence -----------------------------------------------------------------

    /** Persists the current state; if that fails, runs {@code undo} and reports the failure. */
    public void commit(Runnable undo) throws StorageException {
        try {
            repository.save(new ParkData(List.copyOf(users), List.copyOf(groups), List.copyOf(tickets),
                    List.copyOf(passes), List.copyOf(attractions)));
        } catch (StorageException e) {
            undo.run();
            throw e;
        }
    }

    private static <T> void replace(List<T> target, List<? extends T> source) {
        target.clear();
        target.addAll(source);
    }
}
