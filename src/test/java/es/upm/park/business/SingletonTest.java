package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

class SingletonTest {

    private static final List<Class<?>> CONTROLLERS = List.of(ParkController.class, UserManager.class,
            GroupManager.class, TicketManager.class, AttractionManager.class, PassManager.class);

    @Test
    void controllersCannotBeInstantiatedFromOutside() {
        for (Class<?> type : CONTROLLERS) {
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            assertEquals(1, constructors.length, type.getSimpleName());
            assertTrue(Modifier.isPrivate(constructors[0].getModifiers()), type.getSimpleName());
            assertTrue(Modifier.isFinal(type.getModifiers()), type.getSimpleName());
        }
    }

    @Test
    void everyCallerGetsTheSameInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(UserManager.getInstance(), UserManager.getInstance());
        assertSame(GroupManager.getInstance(), GroupManager.getInstance());
        assertSame(TicketManager.getInstance(), TicketManager.getInstance());
        assertSame(AttractionManager.getInstance(), AttractionManager.getInstance());
    }
}
