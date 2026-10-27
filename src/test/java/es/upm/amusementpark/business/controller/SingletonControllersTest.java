package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.invoice.InvoiceController;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

class SingletonControllersTest {

    private static final List<Class<?>> CONTROLLERS = List.of(ParkController.class, UserController.class,
            GroupController.class, TicketController.class, AttractionController.class, InvoiceController.class);

    @Test
    void controllersAreSingletons() throws Exception {
        for (Class<?> type : CONTROLLERS) {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), type.getSimpleName());
            }
            Method getInstance = type.getMethod("getInstance");
            assertSame(getInstance.invoke(null), getInstance.invoke(null), type.getSimpleName());
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        for (Class<?> repo : List.of(UserRepository.class, GroupRepository.class)) {
            for (Method m : repo.getDeclaredMethods()) {
                assertFalse(m.getName().equals("getInstance"), repo.getSimpleName());
            }
        }
        ApplicationInitializer.startInMemory(new UserRepository());
        GroupRepository first = ParkController.getInstance().getGroups();
        ApplicationInitializer.startInMemory(new UserRepository());
        assertNotSame(first, ParkController.getInstance().getGroups());
    }

    @Test
    void substituteRepositoryIsUsedWithoutCodeChanges() throws Exception {
        List<String> added = new ArrayList<>();
        ApplicationInitializer.startInMemory(new UserRepository() {
            @Override
            public void add(User user) {
                added.add(user.getDni());
                super.add(user);
            }
        });
        UserController.getInstance().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "1",
                "1234567890123456");
        assertEquals(List.of("12345678Z"), added);
    }
}
