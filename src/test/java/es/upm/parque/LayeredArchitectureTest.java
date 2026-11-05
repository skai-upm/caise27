package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.RuleViolationException;
import es.upm.parque.business.UserManager;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.UserRepository;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.TestSupport;

/**
 * Guards the layered organization (presentation, business, persistence) and the exception-based error flow.
 */
class LayeredArchitectureTest {

    private static final Path ROOT = Path.of("src", "main", "java");
    private static final String[] LAYERS = {
        "package es.upm.parque.presentation;", "package es.upm.parque.business;",
        "package es.upm.parque.business.model;", "package es.upm.parque.persistence;"};

    private static List<Path> sources(String subdir) throws IOException {
        try (Stream<Path> files = Files.walk(ROOT.resolve("es/upm/parque").resolve(subdir))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static String read(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    @Test
    void everyClassBelongsToALayerAndNoneIsInTheDefaultPackage() throws IOException {
        try (Stream<Path> files = Files.walk(ROOT)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = read(file);
                boolean inLayer = false;
                for (String layer : LAYERS) {
                    inLayer |= code.contains(layer);
                }
                assertTrue(inLayer, "not in a layer package: " + file);
            }
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path file : sources("presentation")) {
            assertFalse(read(file).contains("es.upm.parque.persistence"), file.toString());
        }
    }

    @Test
    void modelPerformsNoPersistence() throws IOException {
        for (Path file : sources("business/model")) {
            String code = read(file);
            assertFalse(code.contains("es.upm.parque.persistence"), file.toString());
            assertFalse(code.contains("java.nio.file"), file.toString());
        }
    }

    @Test
    void businessOperationsThrowTypedExceptions() throws ParkException {
        ParkConfiguration.initialize(ParkRepositories.inMemory());
        UserManager users = UserManager.getInstance();
        users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "600", "1234567890123456");
        RuleViolationException e = assertThrows(RuleViolationException.class, () -> users.create("Otra", "X",
                "12345678Z", "otra@correo.com", "Clave1!", "600", "1234567890123456"));
        assertEquals("a user with DNI 12345678Z already exists", e.getMessage());
    }

    @Test
    void exceptionsBecomeErrorResponsesAndProcessingContinues() {
        CommandProcessor processor = TestSupport.processor(new UserRepository());
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600;1234567890123456");
        assertEquals("ERROR: a user with DNI 12345678Z already exists",
                processor.process("USER CREATE;Otra;X;12345678Z;otra@correo.com;Clave1!;600;1234567890123456"));
        assertTrue(processor.process("USER CREATE;Bad").startsWith("ERROR: USER CREATE expects"));
        assertTrue(processor.process("GROUP ADD;G99;ana@correo.com").startsWith("ERROR: no group"));
        assertTrue(processor.process("USER CREATE;Luis;R;87654321X;luis@correo.com;Clave1!;600;1234567890123457")
                .startsWith("OK"));
    }
}
