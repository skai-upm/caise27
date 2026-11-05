package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkRepository;
import es.upm.amusementpark.persistence.ParkStorage;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DependencyInjectionTest {

    /** Alternative repository implementation keeping groups in memory (no files). */
    static final class InMemoryRepository implements ParkRepository {
        final Map<String, String> groups = new LinkedHashMap<>();
        int saves;

        @Override
        public List<String> load(ParkController park) {
            groups.forEach((id, name) -> park.restoreGroup(id, name, List.of()));
            return new ArrayList<>();
        }

        @Override
        public void save(ParkController park) {
            saves++;
            groups.clear();
            park.getGroups().forEach(g -> groups.put(g.getId(), g.getName()));
        }
    }

    @TempDir
    Path dir;
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
    }

    @Test
    void injectedRepositoryIsUsedWithoutFiles() {
        InMemoryRepository repository = new InMemoryRepository();
        park.setRepository(repository);
        park.createGroup("G01", "Familia");
        park.saveState();
        park.reset();
        assertNull(park.findGroup("G01"));
        park.setRepository(repository);
        park.loadState();
        assertEquals("Familia", park.findGroup("G01").getName());
        assertEquals(1, repository.saves);
    }

    @Test
    void noRepositoryPersistsNothing() {
        park.createGroup("G01", "Familia");
        park.saveState();
        assertTrue(park.loadState().isEmpty());
    }

    @Test
    void storagesAreIndependentInstances() {
        ParkStorage normal = ParkStorage.inDirectory(dir.resolve("data"));
        ParkStorage fly = ParkStorage.inDirectory(dir.resolve("tmp").resolve("x"));
        assertNotSame(normal, fly);
        park.setRepository(fly);
        park.createGroup("G09", "Fly");
        park.saveState();
        assertTrue(Files.exists(dir.resolve("tmp").resolve("x").resolve("groups.txt")));
        assertTrue(Files.notExists(dir.resolve("data")));
        assertTrue(Modifier.isPublic(ParkStorage.class.getConstructors()[0].getModifiers()));
    }

    @Test
    void centralManagementIsSingleton() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertEquals(0, ParkController.class.getConstructors().length);
    }
}
