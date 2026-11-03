package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchitectureTest {

    @TempDir
    Path dir;

    private static List<Path> sources(String layer) throws IOException {
        try (var files = Files.walk(Path.of("src/main/java/es/upm/etsisi/park", layer))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void presentationAndBusinessDoNotTouchFiles() throws IOException {
        for (String layer : List.of("presentation", "business")) {
            for (Path p : sources(layer)) {
                String code = Files.readString(p);
                for (String forbidden : List.of("java.nio.file", "java.io.File", "FileReader", "FileWriter",
                        "es.upm.etsisi.park.persistence")) {
                    assertFalse(code.contains(forbidden), p + " uses " + forbidden);
                }
            }
        }
    }

    @Test
    void everyClassIsInANamedLayerPackage() throws IOException {
        try (var files = Files.walk(Path.of("src/main/java"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String code = Files.readString(p);
                assertTrue(code.startsWith("package es.upm.etsisi.park"), p.toString());
            }
        }
        assertFalse(sources("presentation").isEmpty());
        assertFalse(sources("business").isEmpty());
        assertFalse(sources("persistence").isEmpty());
    }

    @Test
    void controllersAreSingletons() {
        List<Supplier<Object>> accessors = List.of(UserController::getInstance, SystemController::getInstance,
                GroupController::getInstance, TicketController::getInstance, PassController::getInstance,
                AttractionController::getInstance, InvoiceController::getInstance);
        for (Supplier<Object> accessor : accessors) {
            Object instance = accessor.get();
            assertSame(instance, accessor.get());
            for (Constructor<?> c : instance.getClass().getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), instance.getClass().getName());
            }
        }
    }

    @Test
    void injectedRepositoryIsUsedWithoutCodeChanges() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor p = TestParks.processor(new FailingRepository(dir), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        p.process(ParkFixture.ANA);
        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith("ERROR: could not save park data: disk full"));
        assertTrue(Park.getInstance().getUsers().isEmpty());
    }

    @Test
    void failedCommandDoesNotStopProcessing() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        assertTrue(f.run("ATTRACTION ENTER;A99;T1").startsWith("ERROR"));
        assertEquals("USER CREATED", f.run(ParkFixture.ANA));
    }

    @Test
    void exceptionsFormAHierarchy() {
        for (Class<?> type : List.of(ValidationException.class, NotFoundException.class,
                OperationNotAllowedException.class, PersistenceException.class)) {
            assertTrue(ParkException.class.isAssignableFrom(type));
        }
    }

    @Test
    void attractionsUseBoundedGenerics() {
        assertEquals(GeneralAccess.class, Attraction.class.getTypeParameters()[0].getBounds()[0]);
        assertEquals(GeneralAccess.class, typeArgument(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAccess.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertFalse(GroupAccess.class.isAssignableFrom(IndividualTicket.class));
    }

    private static Object typeArgument(Class<?> type) {
        return ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Test
    void helpFormattersAreInterchangeable() throws IOException {
        CommandProcessor p = TestParks.processor(new es.upm.etsisi.park.persistence.TextParkRepository(dir),
                new PrintStream(new ByteArrayOutputStream()));
        List<CommandInfo> info = p.commandInfo();
        String syntax = new HelpGenerator(new SyntaxHelpFormatter()).generate(info);
        String extended = new HelpGenerator(new ExtendedHelpFormatter()).generate(info);
        assertEquals(info.size(), syntax.lines().count());
        assertEquals(2L * info.size(), extended.lines().count());
        HelpGenerator custom = new HelpGenerator(commands -> "COUNT " + commands.size());
        assertEquals("COUNT " + info.size(), custom.generate(info));
    }
}
