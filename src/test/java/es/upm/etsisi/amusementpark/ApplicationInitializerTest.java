package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationInitializerTest {

    @TempDir
    Path dir;

    private final PrintStream sink = new PrintStream(OutputStream.nullOutputStream());

    @Test
    void controllersAreSingletons() {
        for (Class<?> type : List.of(ParkController.class, UserController.class, GroupController.class,
                TicketController.class, PassController.class, AttractionController.class, InvoiceController.class)) {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), type.getSimpleName());
            }
        }
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
    }

    @Test
    void reconfigurationKeepsControllersAndSwitchesRepositories() throws Exception {
        Path first = Files.createDirectory(dir.resolve("first"));
        Path second = Files.createDirectory(dir.resolve("second"));
        UserController users = UserController.getInstance();

        ApplicationInitializer.initialize(first, ParkFixture.FAST_HASHER, sink);
        users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        ApplicationInitializer.initialize(second, ParkFixture.FAST_HASHER, sink);

        assertSame(users, UserController.getInstance());
        assertTrue(ParkController.getInstance().users().findAll().isEmpty());
        assertTrue(Files.exists(first.resolve("users.txt")));
        assertFalse(Files.exists(second.resolve("users.txt")));
    }
}
