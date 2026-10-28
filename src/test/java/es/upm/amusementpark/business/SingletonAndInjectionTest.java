package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.file.DataLocation;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;

class SingletonAndInjectionTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() throws Exception {
        for (Class<?> c : List.of(ParkController.class, UserService.class, GroupService.class, TicketService.class,
                AttractionService.class, InvoiceService.class)) {
            for (Constructor<?> constructor : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), c.getSimpleName());
            }
            Object first = c.getMethod("getInstance").invoke(null);
            assertSame(first, c.getMethod("getInstance").invoke(null), c.getSimpleName());
        }
        ParkController park = ParkController.getInstance();
        assertSame(UserService.getInstance(), park.users());
        assertSame(TicketService.getInstance(), park.tickets());
    }

    @Test
    void eachInitializationInjectsFreshRepositories() throws Exception {
        ParkController park = ParkController.getInstance();
        park.initialize(new DataLocation(dir.resolve("normal")));
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        Object first = injectedRepositories(park);
        park.initialize(new DataLocation(dir.resolve("other")));
        assertNotSame(first, injectedRepositories(park));
        assertTrue(park.users().findAll().isEmpty());
        park.initialize(new DataLocation(dir.resolve("normal")));
        assertEquals(1, park.users().findAll().size());
        park.initialize(new InMemoryUserStorage());
    }

    private static Object injectedRepositories(ParkController park) throws Exception {
        var field = ParkController.class.getDeclaredField("dependencies");
        field.setAccessible(true);
        return ((ParkDependencies) field.get(park)).users();
    }
}
