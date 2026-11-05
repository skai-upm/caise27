package es.upm.amusementpark.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DependencyInjectionTest {

    private static final String ANA = "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456";

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        List<Supplier<Object>> controllers = List.of(UserService::getInstance, GroupService::getInstance,
                TicketService::getInstance, AttractionService::getInstance, InvoiceService::getInstance, Park::getInstance);
        for (Supplier<Object> c : controllers) {
            Object a = c.get();
            assertSame(a, c.get());
            for (Constructor<?> k : a.getClass().getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(k.getModifiers()), a.getClass().getSimpleName());
            }
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        assertFalse(Repositories.inMemory().users() == Repositories.inMemory().users());
        for (Constructor<?> k : UserRepository.class.getConstructors()) {
            assertTrue(Modifier.isPublic(k.getModifiers()));
        }
    }

    @Test
    void injectedRepositoriesAreUsed() {
        UserRepository injected = new UserRepository();
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory(injected));
        p.process(ANA);
        assertEquals(1, injected.count());
        assertSame(injected, Park.getInstance().users());
    }

    @Test
    void differentConfigurationsSameControllers() throws Exception {
        Path a = Files.createDirectory(dir.resolve("a"));
        Path b = Files.createDirectory(dir.resolve("b"));
        ApplicationInitializer.initialize(Repositories.files(a)).process(ANA);
        ApplicationInitializer.initialize(Repositories.files(b)).process("ATTRACTION CREATE;A01;X;5");
        assertTrue(Files.exists(a.resolve("users.txt")));
        assertFalse(Files.exists(a.resolve("attractions.txt")));
        assertTrue(Files.exists(b.resolve("attractions.txt")));
        assertFalse(Files.exists(b.resolve("users.txt")) && !Files.readAllLines(b.resolve("users.txt")).isEmpty());
    }
}
