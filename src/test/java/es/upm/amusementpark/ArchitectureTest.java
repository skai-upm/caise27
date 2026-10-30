package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.invoice.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchitectureTest extends ParkTestSupport {

    @TempDir
    java.nio.file.Path otherDir;

    @Test
    void controllersAreSingletons() {
        assertSame(Park.getInstance(), Park.getInstance());
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), GroupController.getInstance());
        assertSame(TicketController.getInstance(), TicketController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        assertSame(SystemController.getInstance(), SystemController.getInstance());
    }

    @Test
    void repositoriesAreNewPerInitialisationAndBoundToTheirDirectory() {
        var first = park.getUsers();
        user(1);
        ApplicationInitializer.initialize(otherDir, HASHER);
        assertNotSame(first, park.getUsers());
        assertNull(park.getUsers().findByEmail(email(1)));
        assertTrue(Files.exists(dir.resolve("users.txt")));
        assertFalse(Files.exists(otherDir.resolve("users.txt")));
    }

    @Test
    void layersArePackages() throws Exception {
        java.nio.file.Path root = java.nio.file.Path.of("src/main/java/es/upm/amusementpark");
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(root.resolve(layer)), layer);
        }
        try (Stream<java.nio.file.Path> files = Files.list(root.resolve("presentation"))) {
            for (java.nio.file.Path file : files.toList()) {
                String source = Files.readString(file);
                assertFalse(source.contains("es.upm.amusementpark.persistence"), file.toString());
                assertFalse(source.contains(".txt\""), file.toString());
            }
        }
    }

    @Test
    void rejectedOperationsAreTypedExceptions() {
        user(1);
        assertThrows(InvalidDataException.class, () -> users.create("A", "B", "12345678A", "a@b.com", "Clave1!",
                "1", "1234567890123456"));
        assertThrows(DuplicateException.class, () -> user(1));
        assertThrows(AuthenticationException.class, () -> users.delete(email(1), "Mala1!"));
        assertThrows(NotFoundException.class, () -> groups.add("G99", email(1)));
        assertThrows(OperationNotAllowedException.class, () -> tickets.createIndividual("T1", email(1), "2000-01-01"));
    }

    @Test
    void errorsDoNotStopProcessing() {
        assertTrue(processor.process("USER CREATE;A;B;12345678A;a@correo.com;Clave1!;1;1234567890123456").output()
                .startsWith("ERROR:"));
        assertEquals("USER CREATED",
                processor.process("USER CREATE;A;B;12345678Z;a@correo.com;Clave1!;1;1234567890123456").output());
    }
}
