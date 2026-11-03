package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.UserRepository;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentLifecycleTest {

    static final List<Class<?>> CONTROLLERS = List.of(UserController.class, GroupController.class,
            TicketController.class, AttractionController.class, PassController.class, ParkController.class);

    @TempDir
    Path dir;

    /** Repository kept in memory: a different implementation of the same interface. */
    static class InMemoryUserRepository implements UserRepository {
        final List<User> stored = new ArrayList<>();
        int saves;

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void save(List<User> users) {
            stored.clear();
            stored.addAll(users);
            saves++;
        }
    }

    @Test
    void everyElementObtainsTheSameControllerInstances() {
        CommandProcessor processor = App.createProcessor(dir);
        ParkController park = ParkController.getInstance();
        assertSame(park, processor.getPark());
        assertSame(UserController.getInstance(), park.users());
        assertSame(GroupController.getInstance(), park.groups());
        assertSame(TicketController.getInstance(), park.tickets());
        assertSame(AttractionController.getInstance(), park.attractions());
        assertSame(PassController.getInstance(), park.passes());
        processor.process(TestData.CREATE_ANA);
        assertTrue(UserController.getInstance().findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void controllerConstructorsAreNotAccessible() {
        for (Class<?> c : CONTROLLERS) {
            for (Constructor<?> k : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(k.getModifiers()), c.getSimpleName());
            }
        }
    }

    @Test
    void newExecutionReplacesTheInstances() {
        App.createProcessor(dir);
        UserController first = UserController.getInstance();
        App.createProcessor(dir.resolve("other"));
        assertNotSame(first, UserController.getInstance());
    }

    @Test
    void requestingAnUninitializedControllerFails() throws ReflectiveOperationException {
        App.createProcessor(dir);
        for (Class<?> c : CONTROLLERS) {
            Field field = c.getDeclaredField("instance");
            field.setAccessible(true);
            Object saved = field.get(null);
            field.set(null, null);
            try {
                Exception e = assertThrows(java.lang.reflect.InvocationTargetException.class,
                        () -> c.getMethod("getInstance").invoke(null));
                assertTrue(e.getCause() instanceof IllegalStateException, c.getSimpleName());
            } finally {
                field.set(null, saved);
            }
        }
    }

    @Test
    void alternativeRepositoryWorksWithoutChangingTheController() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.stored.add(TestData.user("87654321X", "pedro@correo.com"));
        UserController users = UserController.initialize(repository, TestData.HASHER);
        assertEquals(1, users.getUsers().size());
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertEquals(2, repository.stored.size());
        assertEquals(1, repository.saves);
        assertTrue(Files.notExists(dir.resolve("users.txt")));
    }

    @Test
    void normalAndFlyRunsUseDifferentPathsWithTheSameLogic() throws IOException {
        String session = String.join("\n", TestData.CREATE_ANA, "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com",
                "SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15", "INVOICE CREATE;USER;ana@correo.com",
                "EXIT") + "\n";
        List<String> normal = run(session, new String[0]);
        List<String> fly = run(session, new String[] {"--fly"});
        assertEquals(normal, fly);
        assertTrue(Files.exists(dir.resolve("users.txt")));
        assertTrue(Files.notExists(dir.resolve("fly").resolve("users.txt")));
        assertTrue(Files.notExists(dir.resolve("tmp")));
    }

    private List<String> run(String stdin, String[] args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Path work = args.length == 0 ? dir : dir.resolve("fly");
        App.execute(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), work);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void onlyTheCompositionRootConstructsRepositoriesAndControllers() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (f.getFileName().toString().equals("App.java")) {
                    continue;
                }
                String src = Files.readString(f);
                String own = f.getFileName().toString().replace(".java", "");
                for (String c : List.of("UserFileStorage", "GroupFileStorage", "TicketFileStorage",
                        "GroupTicketFileStorage", "AttractionFileStorage", "PassFileStorage", "UserController",
                        "GroupController", "PassController",
                        "TicketController", "AttractionController", "ParkController")) {
                    if (!c.equals(own)) {
                        assertTrue(!src.contains("new " + c + "("), f + " constructs " + c);
                    }
                }
            }
        }
    }
}
