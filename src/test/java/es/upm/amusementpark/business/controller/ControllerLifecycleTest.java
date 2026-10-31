package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.invoice.InvoiceService;
import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ControllerLifecycleTest {

    /** Keeps users in memory: an alternative repository implementation. */
    static class InMemoryUserRepository implements UserRepository {
        final List<User> stored = new ArrayList<>();

        @Override
        public List<User> loadAll() {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<User> users) {
            stored.clear();
            stored.addAll(users);
        }
    }

    static class EmptyGroupRepository implements GroupRepository {
        @Override
        public List<Group> loadAll(Function<String, User> userByDni) {
            return new ArrayList<>();
        }

        @Override
        public void saveAll(Collection<Group> groups) {
            // nothing stored
        }
    }

    static class EmptyTicketRepository implements TicketRepository {
        @Override
        public Tickets loadAll(Function<String, User> userByDni, Function<String, Group> groupById) {
            return new Tickets(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public void saveAll(List<IndividualTicket> individual, List<GroupTicket> group) {
            // nothing stored
        }
    }

    static class EmptyAttractionRepository implements AttractionRepository {
        @Override
        public List<Attraction> loadAll() {
            return new ArrayList<>();
        }

        @Override
        public void saveAll(Collection<Attraction> attractions) {
            // nothing stored
        }
    }

    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();

    private ParkController start() throws IOException {
        UserManager users = UserManager.initialize(userRepository);
        GroupManager groups = GroupManager.initialize(new EmptyGroupRepository(), users);
        TicketManager tickets = TicketManager.initialize(new EmptyTicketRepository(), users, groups);
        AttractionManager attractions = AttractionManager.initialize(new EmptyAttractionRepository());
        return ParkController.initialize(users, groups, tickets, attractions,
                new InvoiceService(StandardInvoiceBuilder::new), LocalDate.of(2026, 10, 1));
    }

    @AfterEach
    void stop() {
        ParkController.reset();
        AttractionManager.reset();
        TicketManager.reset();
        GroupManager.reset();
        UserManager.reset();
    }

    @Test
    void controllersReturnTheSameInstanceWithinAnExecution() throws IOException {
        ParkController park = start();
        assertSame(park, ParkController.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(park.getUsers(), UserManager.getInstance());
        assertSame(park.getGroups(), GroupManager.getInstance());
        assertSame(park.getTickets(), TicketManager.getInstance());
        assertSame(park.getAttractions(), AttractionManager.getInstance());
    }

    @Test
    void secondInitialisationIsRejected() throws IOException {
        start();
        assertThrows(IllegalStateException.class, () -> UserManager.initialize(new InMemoryUserRepository()));
        assertThrows(IllegalStateException.class, this::start);
    }

    @Test
    void accessBeforeInitialisationIsRejected() {
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        assertThrows(IllegalStateException.class, UserManager::getInstance);
    }

    @Test
    void injectedInMemoryRepositoryIsUsed() throws IOException {
        ParkController park = start();
        park.getUsers().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals(1, userRepository.stored.size());

        stop();
        ParkController restarted = start();
        assertTrue(restarted.getUsers().findByEmail("ana@correo.com") != null);
    }
}
