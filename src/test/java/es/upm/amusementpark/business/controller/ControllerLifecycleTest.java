package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ControllerLifecycleTest {

    /** In-memory repositories show that controllers work with any injected implementation. */
    static final class MemoryUsers implements UserRepository {
        final List<User> saved = new ArrayList<>();

        @Override
        public List<User> load() {
            return List.copyOf(saved);
        }

        @Override
        public void save(List<User> users) {
            saved.clear();
            saved.addAll(users);
        }
    }

    static final class MemoryGroups implements GroupRepository {
        @Override
        public List<Group> load(Map<String, User> usersByDni) {
            return List.of();
        }

        @Override
        public void save(List<Group> groups) {
            // nothing to keep for these tests
        }
    }

    static final class MemoryTickets implements TicketRepository {
        final List<Ticket> saved = new ArrayList<>();

        @Override
        public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) {
            return List.copyOf(saved);
        }

        @Override
        public void save(List<Ticket> tickets) {
            saved.clear();
            saved.addAll(tickets);
        }
    }

    static final class MemoryAttractions implements AttractionRepository {
        @Override
        public List<Attraction> load() {
            return List.of();
        }

        @Override
        public void save(List<Attraction> attractions) {
            // nothing to keep for these tests
        }
    }

    @AfterEach
    void release() {
        InvoiceController.release();
        ParkController.release();
        UserController.release();
        SystemController.release();
    }

    @Test
    void controllersAreSingletons() throws IOException {
        SystemController system = SystemController.initialize(LocalDate.of(2026, 10, 1));
        UserController users = UserController.initialize(new MemoryUsers());
        ParkController park = ParkController.initialize(system, users, new MemoryGroups(), new MemoryTickets(),
                new MemoryAttractions());
        InvoiceController invoices = InvoiceController.initialize(users, park, StandardInvoiceBuilder::new);

        assertSame(system, SystemController.getInstance());
        assertSame(users, UserController.getInstance());
        assertSame(park, ParkController.getInstance());
        assertSame(invoices, InvoiceController.getInstance());
    }

    @Test
    void secondInitializationIsRejected() throws IOException {
        SystemController.initialize(LocalDate.of(2026, 10, 1));
        UserController.initialize(new MemoryUsers());
        LocalDate date = LocalDate.of(2026, 10, 2);
        MemoryUsers repository = new MemoryUsers();
        assertThrows(IllegalStateException.class, () -> SystemController.initialize(date));
        assertThrows(IllegalStateException.class, () -> UserController.initialize(repository));
    }

    @Test
    void accessBeforeInitializationIsRejected() {
        assertThrows(IllegalStateException.class, SystemController::getInstance);
        assertThrows(IllegalStateException.class, UserController::getInstance);
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        assertThrows(IllegalStateException.class, InvoiceController::getInstance);
    }

    @Test
    void injectedInMemoryRepositoriesAreUsed() throws IOException {
        MemoryUsers userRepository = new MemoryUsers();
        MemoryTickets ticketRepository = new MemoryTickets();
        SystemController system = SystemController.initialize(LocalDate.of(2026, 10, 1));
        UserController users = UserController.initialize(userRepository);
        ParkController park = ParkController.initialize(system, users, new MemoryGroups(), ticketRepository,
                new MemoryAttractions());

        users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "600000000", "1234567890123456");
        park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));

        assertEquals(1, userRepository.saved.size());
        assertEquals("T001", ticketRepository.saved.get(0).getId());
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertTrue(park.findTicket("T001") != null);
    }
}
