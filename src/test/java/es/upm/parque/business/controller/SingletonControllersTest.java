package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

class SingletonControllersTest {

    private static final List<Class<?>> CONTROLLERS = List.of(UserController.class, GroupController.class,
            TicketController.class, AttractionController.class, InvoiceController.class);

    @Test
    void getInstanceAlwaysReturnsTheSameObject() {
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), GroupController.getInstance());
        assertSame(TicketController.getInstance(), TicketController.getInstance());
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void constructorsArePrivate() {
        for (Class<?> controller : CONTROLLERS) {
            Constructor<?>[] constructors = controller.getDeclaredConstructors();
            assertEquals(1, constructors.length, controller.getSimpleName());
            assertTrue(Modifier.isPrivate(constructors[0].getModifiers()), controller.getSimpleName());
        }
    }

    @Test
    void unconfiguredControllerRejectsOperations() throws ReflectiveOperationException {
        Constructor<UserController> constructor = UserController.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        UserController fresh = constructor.newInstance();
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> fresh.findByEmail("a@b.com"));
        assertEquals("UserController is not configured", error.getMessage());
    }
}
