package es.upm.park.business.controller;

import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupRepository;
import es.upm.park.persistence.Repositories;
import es.upm.park.persistence.UserRepository;
import es.upm.park.persistence.file.FileRepositories;
import es.upm.park.persistence.DataPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingletonWiringTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        List<Class<?>> singletons = List.of(Park.class, UserController.class, GroupController.class,
                TicketController.class, PassController.class, AttractionController.class, InvoiceController.class);
        for (Class<?> type : singletons) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), type + " must not be instantiable");
            }
        }
        assertSame(Park.getInstance(), Park.getInstance());
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
    }

    @Test
    void loadResetsDateAndPrice() {
        Park park = ParkFixture.openEmpty(dir);
        park.setCurrentDate(LocalDate.of(2030, 1, 1));
        park.setCurrentPrice(new BigDecimal("45.50"));

        park.load();

        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void repositoriesAreInjectedAndReplaceable() throws Exception {
        List<User> stored = new ArrayList<>();
        UserRepository inMemoryUsers = new UserRepository() {
            @Override
            public List<User> loadAll() {
                return new ArrayList<>(stored);
            }

            @Override
            public void saveAll(List<User> users) {
                stored.clear();
                stored.addAll(users);
            }
        };
        Repositories files = FileRepositories.create(new DataPaths(dir));
        GroupRepository groups = files.groups();
        Repositories mixed = new Repositories(inMemoryUsers, groups, files.tickets(), files.attractions(),
                files.passes());

        es.upm.park.App.initialize(mixed, ParkFixture.FAST_HASHER);
        UserController.getInstance().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");

        assertEquals(1, stored.size());
        assertTrue(java.nio.file.Files.notExists(dir.resolve("users.txt")));
    }

    @Test
    void unconfiguredControllerFailsFast() {
        UserController.getInstance().configure(null, null);
        assertThrows(IllegalStateException.class,
                () -> UserController.getInstance().authenticate("a@b.com", "x"));
        ParkFixture.openEmpty(dir);
    }

    @Test
    void flyAndNormalModeOnlyDifferInRepositoryPaths() throws Exception {
        Path normal = java.nio.file.Files.createDirectory(dir.resolve("normal"));
        Path fly = java.nio.file.Files.createDirectory(dir.resolve("fly"));
        ParkFixture.openEmpty(normal);
        UserController.getInstance().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");

        ParkFixture.openEmpty(fly);
        assertTrue(Park.getInstance().getUsers().isEmpty());
        ParkFixture.openEmpty(normal);
        assertEquals(1, Park.getInstance().getUsers().size());
    }
}
