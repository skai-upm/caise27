package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceDirector;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.Repositories;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.repository.UserRepository;
import es.upm.amusementpark.persistence.TextFileRepositories;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DependencyInjectionTest {

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        park.reset();
    }

    @Test
    void controllersAreSingletons() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        for (Class<?> type : List.of(ParkController.class, InvoiceController.class)) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), type + " has a non-private constructor");
            }
        }
    }

    @Test
    void repositoriesAreIndependentPerDirectory() throws Exception {
        Path a = Files.createDirectory(base.resolve("a"));
        Path b = Files.createDirectory(base.resolve("b"));
        Repositories first = TextFileRepositories.create(a);
        Repositories second = TextFileRepositories.create(b);
        assertNotSame(first.users(), second.users());

        park.configure(first);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.persist();
        park.reset();
        park.configure(second);
        park.load();
        assertNull(park.findAttraction("A01"));
        park.reset();
        park.configure(first);
        park.load();
        assertNotNull(park.findAttraction("A01"));
    }

    /** In-memory replacements proving the controller only depends on the abstractions. */
    private static final class MemoryUsers implements UserRepository {
        private final List<User> stored = new ArrayList<>();

        @Override
        public void saveAll(Collection<User> users) {
            stored.clear();
            stored.addAll(users);
        }

        @Override
        public List<User> loadAll(List<String> skipped) {
            return List.copyOf(stored);
        }
    }

    private static final class MemoryAttractions implements AttractionRepository {
        private final List<Attraction> stored = new ArrayList<>();

        @Override
        public void saveAll(Collection<Attraction> attractions) {
            stored.clear();
            stored.addAll(attractions);
        }

        @Override
        public void loadInto(ParkController controller, List<String> skipped) {
            for (Attraction attraction : stored) {
                controller.createAttraction(attraction.getIdentifier(), attraction.getName(),
                        String.valueOf(attraction.getCapacity()));
            }
        }
    }

    private static final class NoGroups implements GroupRepository {
        @Override
        public void saveAll(Collection<Group> groups) {
            // nothing stored in this test double
        }

        @Override
        public void loadInto(ParkController controller, List<String> skipped) {
            // nothing to rebuild
        }
    }

    private static final class NoTickets implements TicketRepository {
        @Override
        public void saveAll(Collection<Ticket> tickets) {
            // nothing stored in this test double
        }

        @Override
        public void loadInto(ParkController controller, List<String> skipped) {
            // nothing to rebuild
        }
    }

    @Test
    void inMemoryRepositoriesCanReplaceTextFiles() {
        Repositories memory = new Repositories(new MemoryUsers(), new NoGroups(), new NoTickets(),
                new MemoryAttractions());
        park.configure(memory);
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.persist();
        park.reset();
        park.configure(memory);
        park.load();
        assertNotNull(park.findUser("ana@correo.com"));
        assertEquals(20, park.findAttraction("A01").getCapacity());
    }

    @Test
    void invoiceControllerUsesInjectedCollaborators() {
        List<String> calls = new ArrayList<>();
        InvoiceDirector recordingDirector = new InvoiceDirector() {
            @Override
            public Invoice groupInvoice(Group group, List<es.upm.amusementpark.business.model.GroupTicket> tickets) {
                calls.add(group.getIdentifier());
                return super.groupInvoice(group, tickets);
            }
        };
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.configure(park, recordingDirector);
        park.createGroup("G01", "F");
        invoices.createGroupInvoice("G01");
        assertEquals(List.of("G01"), calls);
        invoices.configure(park, new InvoiceDirector());
    }
}
