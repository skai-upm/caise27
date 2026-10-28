package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;
import es.upm.etsisi.amusementpark.business.repository.GroupRepository;
import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import es.upm.etsisi.amusementpark.business.repository.TicketRepository;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DependencyInjectionTest {

    /** In-memory repositories: a different implementation injected without changing the controller. */
    private static final class MemoryUsers implements UserRepository {
        private List<User> stored = new ArrayList<>();

        public List<User> load() {
            return stored;
        }

        public void save(Collection<User> users) {
            stored = new ArrayList<>(users);
        }

        public List<String> getProblems() {
            return List.of();
        }
    }

    private static final class MemoryGroups implements GroupRepository {
        public List<Group> load(Function<String, Optional<User>> usersByEmail) {
            return List.of();
        }

        public void save(Collection<Group> groups) {
        }

        public List<String> getProblems() {
            return List.of();
        }
    }

    private static final class MemoryTickets implements TicketRepository {
        public List<Ticket> load(Function<String, Optional<Group>> groups, BiFunction<String, String, User> users) {
            return List.of();
        }

        public void save(Collection<Ticket> tickets) {
        }

        public List<String> getProblems() {
            return List.of();
        }
    }

    private static final class MemoryAttractions implements AttractionRepository {
        private List<Attraction<?>> stored = new ArrayList<>();

        public List<Attraction<?>> load() {
            return stored;
        }

        public void save(Collection<Attraction<?>> attractions) {
            stored = new ArrayList<>(attractions);
        }

        public List<String> getProblems() {
            return List.of();
        }
    }

    private static final class MemoryPasses implements PassRepository {
        public List<Pass> load(Function<String, Optional<IndividualTicket>> tickets) {
            return List.of();
        }

        public void save(Collection<Pass> passes) {
        }

        public List<String> getProblems() {
            return List.of();
        }
    }

    private final ParkController park = ParkController.getInstance();

    @AfterEach
    void detach() {
        park.configure((ParkRepositories) null);
        InvoiceController.getInstance().configure(park);
    }

    @Test
    void alternativeRepositoriesAreInjected() {
        MemoryUsers users = new MemoryUsers();
        MemoryAttractions attractions = new MemoryAttractions();
        park.configure(new ParkRepositories(users, new MemoryGroups(), new MemoryTickets(), attractions, new MemoryPasses()));
        park.reset();
        park.addUser(new User("12345678Z", "ana@correo.com"));
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.saveState();
        assertEquals(1, users.stored.size());
        park.reset();
        assertTrue(park.loadState().isEmpty());
        assertTrue(park.findUser("ana@correo.com").isPresent());
        assertTrue(park.findAttraction("A01").isPresent());
    }

    @Test
    void singletonControllers() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        for (Class<?> type : List.of(ParkController.class, InvoiceController.class)) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), type.getSimpleName());
            }
        }
    }

    @Test
    void invoiceControllerNeedsItsCollaborator() {
        InvoiceController.getInstance().configure(null);
        assertThrows(OperationNotAllowedException.class,
                () -> InvoiceController.getInstance().createGroupInvoice("G01"));
    }

    @Test
    void repositoriesAreNotSingletons() throws Exception {
        Class<?> factory = Class.forName("es.upm.etsisi.amusementpark.persistence.TextRepositories");
        Object first = factory.getMethod("inDirectory", java.nio.file.Path.class).invoke(null, java.nio.file.Path.of("a"));
        Object second = factory.getMethod("inDirectory", java.nio.file.Path.class).invoke(null, java.nio.file.Path.of("a"));
        assertTrue(first != second && ((ParkRepositories) first).users() != ((ParkRepositories) second).users());
    }
}
