package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Persistent store of tickets, including historical ones.
 */
public interface TicketRepository {

    /**
     * @param groups       resolves a group identifier among the existing groups
     * @param userResolver resolves {@code (dni, email)} to the active user or a historical user record
     */
    List<Ticket> load(Function<String, Optional<Group>> groups, BiFunction<String, String, User> userResolver);

    void save(Collection<Ticket> tickets);

    List<String> getProblems();
}
