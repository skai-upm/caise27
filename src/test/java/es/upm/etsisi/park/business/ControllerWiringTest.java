package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.ParkApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ControllerWiringTest extends ParkTestSupport {

    private static final List<Class<?>> SINGLETONS = List.of(Park.class, UserController.class, GroupController.class,
            TicketController.class, PassController.class, AttractionController.class, InvoiceController.class);

    @Test
    void controllersAreSingletons() throws ReflectiveOperationException {
        for (Class<?> c : SINGLETONS) {
            Object a = c.getMethod("getInstance").invoke(null);
            Object b = c.getMethod("getInstance").invoke(null);
            assertSame(a, b, c.getSimpleName());
            for (Constructor<?> k : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(k.getModifiers()), c.getSimpleName() + " constructor must be private");
            }
        }
        assertSame(UserController.getInstance(), services.users());
        assertSame(Park.getInstance(), services.park());
    }

    @Test
    void repositoryIsNotASingletonAndIsReplaceable() {
        InMemoryRepository memory = new InMemoryRepository();
        ParkServices s = ParkApplication.start(memory, TODAY, HASHER);
        s.users().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        s.attractions().create("A01", "Dragon", "5", "GROUP");
        assertEquals(2, memory.saves);
        assertEquals(1, memory.last.users().size());
        assertEquals(AttractionType.GROUP, memory.last.attractions().get(0).getType());
        // a second repository instance is independent: nothing was written to the temporary directory
        assertTrue(dir.toFile().list().length == 0);
        // restarting on the in-memory data reconstructs the state with the same controllers
        ParkApplication.start(memory, TODAY, HASHER);
        assertNotNull(Park.getInstance().findUser("ana@correo.com"));
    }

    /** Alternative persistence implementation used only to prove the abstraction is replaceable. */
    private static final class InMemoryRepository implements ParkRepository {
        private ParkData last = ParkData.empty();
        private int saves;

        @Override
        public ParkData load() {
            List<Attraction<?>> attractions = new ArrayList<>();
            for (Attraction<?> a : last.attractions()) {
                attractions.add(a.getType().create(a.getId(), a.getName(), a.getCapacity()));
            }
            return new ParkData(last.users(), last.groups(), last.tickets(), last.passes(), attractions);
        }

        @Override
        public void save(ParkData data) {
            last = data;
            saves++;
        }
    }
}
