package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;
import es.upm.etsisi.amusementpark.business.repository.GroupRepository;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import es.upm.etsisi.amusementpark.business.repository.TicketRepository;
import es.upm.etsisi.amusementpark.business.repository.UserRepository;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final Path MAIN = Path.of("src/main/java");
    private static final Path ROOT = MAIN.resolve("es/upm/etsisi/amusementpark");

    private static List<Path> javaFiles(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layeredPackagesExistAndDefaultPackageUnused() throws Exception {
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(ROOT.resolve(layer)), layer);
        }
        for (Path file : javaFiles(MAIN)) {
            assertTrue(Files.readString(file).startsWith("package es.upm.etsisi.amusementpark"), file.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws Exception {
        for (Path file : javaFiles(ROOT.resolve("presentation"))) {
            String code = Files.readString(file);
            assertFalse(code.contains("amusementpark.persistence"), file.toString());
            assertFalse(code.contains("java.nio.file"), file.toString());
        }
    }

    @Test
    void businessDoesNotDependOnPersistenceOrPresentation() throws Exception {
        for (Path file : javaFiles(ROOT.resolve("business"))) {
            String code = Files.readString(file);
            assertFalse(code.contains("amusementpark.persistence"), file.toString());
            assertFalse(code.contains("amusementpark.presentation"), file.toString());
            assertFalse(code.contains("java.nio.file"), file.toString());
        }
    }

    @Test
    void controllersAreSingletons() {
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        for (Class<?> type : List.of(UserController.class, ParkController.class, PassController.class,
                InvoiceController.class)) {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), type.getSimpleName());
            }
        }
    }

    @Test
    void repositoriesAreNotSingletons() throws Exception {
        for (Path file : javaFiles(ROOT.resolve("persistence"))) {
            assertFalse(Files.readString(file).contains("getInstance"), file.toString());
        }
    }

    // ----- in-memory repositories prove that persistence is replaceable -----

    static final class MemoryUsers implements UserRepository {
        List<User> stored = new ArrayList<>();

        public List<User> load() {
            return new ArrayList<>(stored);
        }

        public void save(List<User> users) {
            stored = new ArrayList<>(users);
        }
    }

    static final class MemoryGroups implements GroupRepository {
        List<Group> stored = new ArrayList<>();

        public List<Group> load(Function<String, User> userByDni) {
            return new ArrayList<>(stored);
        }

        public void save(List<Group> groups) {
            stored = new ArrayList<>(groups);
        }
    }

    static final class MemoryTickets implements TicketRepository {
        List<Ticket> stored = new ArrayList<>();

        public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
            return new ArrayList<>(stored);
        }

        public void save(List<Ticket> tickets) {
            stored = new ArrayList<>(tickets);
        }
    }

    static final class MemoryAttractions implements AttractionRepository {
        List<Attraction<?>> stored = new ArrayList<>();

        public List<Attraction<?>> load() {
            return new ArrayList<>(stored);
        }

        public void save(List<Attraction<?>> attractions) {
            stored = new ArrayList<>(attractions);
        }
    }

    static final class MemoryPasses implements PassRepository {
        List<Pass> stored = new ArrayList<>();

        public List<Pass> load(Function<String, IndividualTicket> ticketById) {
            return new ArrayList<>(stored);
        }

        public void save(List<Pass> passes) {
            stored = new ArrayList<>(passes);
        }
    }

    @Test
    void controllersWorkWithInjectedInMemoryRepositories() throws ParkException {
        MemoryUsers users = new MemoryUsers();
        MemoryTickets tickets = new MemoryTickets();
        MemoryPasses passes = new MemoryPasses();
        UserController userController = UserController.getInstance();
        userController.configure(users, new BCryptPasswordHasher(4));
        PassController passController = PassController.getInstance();
        passController.configure(passes);
        ParkController park = ParkController.getInstance();
        park.configure(LocalDate.of(2027, 4, 15), userController, passController, new MemoryGroups(), tickets,
                new MemoryAttractions());

        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.buySpeedPasses("SP01", "T001", "1");

        assertEquals(1, users.stored.size());
        assertEquals(1, tickets.stored.size());
        assertEquals(1, passes.stored.size());
        assertNotNull(park.findTicket("T001"));
    }
}
