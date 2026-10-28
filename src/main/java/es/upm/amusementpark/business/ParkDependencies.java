package es.upm.amusementpark.business;

import es.upm.amusementpark.persistence.file.ParkStorage;
import es.upm.amusementpark.persistence.file.UserStorage;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Repositories and storages of one execution, created at initialization and injected into the
 * controllers. {@code userStorage} and {@code storage} are absent for purely in-memory executions.
 */
public record ParkDependencies(UserRepository users, GroupRepository groups, TicketRepository tickets,
        AttractionRepository attractions, PassRepository passes, UserStorage userStorage, ParkStorage storage) {
}
