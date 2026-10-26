package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.persistence.UserRepository;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentConfigurationTest {

    private static final List<Supplier<Object>> SINGLETONS = List.of(MainController::getInstance,
            UserController::getInstance, GroupController::getInstance, TicketController::getInstance,
            AttractionController::getInstance, InvoiceController::getInstance);

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        for (Supplier<Object> access : SINGLETONS) {
            assertSame(access.get(), access.get());
        }
    }

    @Test
    void controllersHaveNoPublicConstructors() {
        for (Supplier<Object> access : SINGLETONS) {
            for (Constructor<?> c : access.get().getClass().getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), c.toString());
            }
        }
    }

    @Test
    void unconfiguredControllerFailsClearly() throws ReflectiveOperationException {
        Constructor<UserController> constructor = UserController.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        UserController fresh = constructor.newInstance();
        IllegalStateException e = assertThrows(IllegalStateException.class, fresh::getUsers);
        assertEquals("UserController is not configured", e.getMessage());
    }

    @Test
    void repositoriesAreOrdinaryObjects() {
        UserRepository a = new UserRepository(dir.resolve("a").resolve("users.txt"));
        UserRepository b = new UserRepository(dir.resolve("b").resolve("users.txt"));
        assertNotSame(a, b);
    }

    @Test
    void initialisationInjectsCollaboratorsAndSwitchesData() {
        TestPark normal = new TestPark(dir.resolve("data"));
        normal.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        normal.groups.create("G01", "F");
        normal.groups.addMember("G01", "ana@correo.com");

        TestPark fly = new TestPark(dir.resolve("tmp").resolve("20260901_100000"));
        assertSame(normal.users, fly.users);
        assertNull(fly.users.findByEmail("ana@correo.com"));
        assertNull(fly.groups.findById("G01"));

        TestPark back = new TestPark(dir.resolve("data"));
        assertEquals(1, back.groups.findById("G01").getMembers().size());
    }
}
