package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.ParkInitializer;
import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.persistence.FileAttractionRepository;
import es.upm.amusementpark.persistence.FileGroupRepository;
import es.upm.amusementpark.persistence.FilePassRepository;
import es.upm.amusementpark.persistence.FileTicketRepository;
import es.upm.amusementpark.persistence.FileUserRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Singleton controllers and injected, replaceable repositories (Delivery 3, F3-C03).
 */
class DependencyInjectionTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        List<Supplier<Object>> accessors = List.of(Park::getInstance, SystemController::getInstance,
                UserController::getInstance, GroupController::getInstance, TicketController::getInstance,
                PassController::getInstance, AttractionController::getInstance, InvoiceController::getInstance);
        for (Supplier<Object> accessor : accessors) {
            Object instance = accessor.get();
            assertSame(instance, accessor.get());
            for (Constructor<?> constructor : instance.getClass().getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), instance.getClass().getSimpleName());
            }
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        for (Class<?> repository : List.of(FileUserRepository.class, FileGroupRepository.class,
                FileTicketRepository.class, FileAttractionRepository.class, FilePassRepository.class)) {
            boolean publicConstructor = Stream.of(repository.getConstructors())
                    .anyMatch(c -> Modifier.isPublic(c.getModifiers()));
            assertTrue(publicConstructor, repository.getSimpleName());
            for (Method method : repository.getDeclaredMethods()) {
                assertFalse(Modifier.isStatic(method.getModifiers()) && method.getReturnType() == repository,
                        repository.getSimpleName() + " exposes a static instance accessor");
            }
        }
    }

    @Test
    void inMemoryRepositoriesWorkWithoutFiles() throws Exception {
        Path workingDir = Path.of("").toAbsolutePath();
        long filesBefore;
        try (Stream<Path> listing = Files.list(workingDir)) {
            filesBefore = listing.count();
        }
        Park park = ParkInitializer.initialize(InMemoryRepositories.create(), TestParks.FAST_HASHER,
                LocalDate.of(2027, 4, 15));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.createAttraction("A01", "Dragon Rojo", "20", "SPECIAL");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.buySpecialPass("PE01", "T001");
        assertEquals(1, park.enterAttraction("A01", "T001", false).admitted());

        assertNotNull(park.findUser("ana@correo.com"));
        assertTrue(park.findPass("PE01").isConsumed());
        try (Stream<Path> listing = Files.list(workingDir)) {
            assertEquals(filesBefore, listing.count());
        }
        assertFalse(Files.exists(workingDir.resolve("users.txt")));
    }
}
