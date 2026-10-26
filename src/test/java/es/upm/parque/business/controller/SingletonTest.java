package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestSupport;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class SingletonTest {

    /** Repository kept in memory, showing that controllers accept any implementation. */
    private static final class InMemoryUsers implements UserRepository {
        private List<User> stored = new ArrayList<>();

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void save(Collection<User> users) {
            stored = new ArrayList<>(users);
        }
    }

    private static final GroupRepository NO_GROUPS = new GroupRepository() {
        @Override
        public List<Group> load(Function<String, User> userByDni) {
            return List.of();
        }

        @Override
        public void save(Collection<Group> groups) {
            // nothing to store
        }
    };

    private static final TicketRepository NO_TICKETS = new TicketRepository() {
        @Override
        public List<Ticket> load(Function<String, User> userByDni,
                Function<String, Group> groupById) {
            return List.of();
        }

        @Override
        public void save(Collection<Ticket> tickets) {
            // nothing to store
        }
    };

    private static final AttractionRepository NO_ATTRACTIONS = new AttractionRepository() {
        @Override
        public List<Attraction> load() {
            return List.of();
        }

        @Override
        public void save(Collection<Attraction> attractions) {
            // nothing to store
        }
    };

    @Test
    void controllersHaveOneInstanceAndPrivateConstructors() {
        assertSame(UserManager.getInstance(), UserManager.getInstance());
        assertSame(GroupManager.getInstance(), GroupManager.getInstance());
        assertSame(TicketManager.getInstance(), TicketManager.getInstance());
        assertSame(AttractionManager.getInstance(), AttractionManager.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Class<?> c : List.of(UserManager.class, GroupManager.class, TicketManager.class, AttractionManager.class,
                ParkController.class)) {
            for (Constructor<?> constructor : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), c.getSimpleName());
            }
        }
    }

    @Test
    void anyRepositoryImplementationCanBeInjectedAndReconfigurationReloads() {
        InMemoryUsers repository = new InMemoryUsers();
        UserManager users = UserManager.getInstance().configure(repository, TestSupport.FAST_HASHER);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertEquals(1, repository.stored.size());

        assertTrue(UserManager.getInstance().configure(new InMemoryUsers(), TestSupport.FAST_HASHER).getUsers().isEmpty());
        assertEquals(1, UserManager.getInstance().configure(repository, TestSupport.FAST_HASHER).getUsers().size());
    }

    @Test
    void parkControllerUsesTheInjectedCollaborators() {
        InMemoryUsers repository = new InMemoryUsers();
        UserManager users = UserManager.getInstance().configure(repository, TestSupport.FAST_HASHER);
        GroupManager groups = GroupManager.getInstance().configure(users, NO_GROUPS);
        TicketManager tickets = TicketManager.getInstance().configure(users, groups, NO_TICKETS);
        AttractionManager attractions = AttractionManager.getInstance().configure(NO_ATTRACTIONS);
        List<InvoiceBuilder> used = new ArrayList<>();
        InvoiceDirector director = new InvoiceDirector() {
            @Override
            public Invoice construct(InvoiceBuilder builder, LocalDate issueDate) {
                used.add(builder);
                return super.construct(builder, issueDate);
            }
        };
        ParkController park = ParkController.getInstance()
            .configure(users, groups, tickets, attractions, director, LocalDate.of(2026, 10, 15));

        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertNotNull(park.createUserInvoice("ana@correo.com"));
        assertEquals(1, used.size());
        assertEquals(1, repository.stored.size());
        assertEquals(ParkController.INITIAL_PRICE, park.getCurrentPrice());
    }
}
