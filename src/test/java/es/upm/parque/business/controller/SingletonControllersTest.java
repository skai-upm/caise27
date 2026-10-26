package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingletonControllersTest {

    @TempDir
    Path dir;

    @Test
    void controllersHaveASingleInstanceAndNoPublicConstructor() {
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), GroupController.getInstance());
        assertSame(TicketController.getInstance(), TicketController.getInstance());
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        for (Class<?> c : List.of(UserController.class, GroupController.class, TicketController.class,
                AttractionController.class, InvoiceController.class)) {
            for (Constructor<?> constructor : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), c.getSimpleName());
            }
        }
    }

    @Test
    void reconfigurationReplacesDataAndChecks() {
        SystemDate date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        Path normal = dir.resolve("normal");
        UserController users = UserController.getInstance().configure(new FileUserRepository(normal.resolve("u.txt")), new BCryptPasswordEncoder(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        GroupController groups = GroupController.getInstance()
                .configure(new FileGroupRepository(normal.resolve("g.txt")), users);
        TicketController.getInstance().configure(
                new FileTicketRepository(normal.resolve("t.txt"), normal.resolve("gt.txt")), users, groups, date)
                .create("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));

        Path fly = dir.resolve("fly");
        users.configure(new FileUserRepository(fly.resolve("u.txt")), new BCryptPasswordEncoder(4));
        assertEquals(0, users.getUsers().size());
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.delete("ana@correo.com", "Clave1!");
        assertNull(users.findByEmail("ana@correo.com"));

        users.configure(new FileUserRepository(normal.resolve("u.txt")), new BCryptPasswordEncoder(4));
        assertEquals(1, users.getUsers().size());
    }
}
