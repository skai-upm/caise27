package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.business.controller.ParkController;
import com.parque.business.service.InvoiceDirector;
import com.parque.presentation.CommandProcessor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentWiringTest {
    @TempDir
    Path dir;

    @AfterEach
    void endExecution() {
        ApplicationContext.stop();
    }

    @Test
    void controllerIsASingletonPerExecution() throws Exception {
        ParkController c = TestPark.start(dir);
        assertSame(c, ParkController.getInstance());
        assertSame(c, new CommandProcessor().getController(), "every element gets the same instance");
        assertThrows(IllegalStateException.class, () -> ApplicationContext.start(dir.resolve("other")));
        assertSame(c, ParkController.getInstance(), "existing instance kept");
    }

    @Test
    void accessBeforeInitializationFails() {
        ParkController.release();
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        assertThrows(IllegalStateException.class, CommandProcessor::new);
    }

    @Test
    void newExecutionGetsFreshState() throws Exception {
        ParkController first = TestPark.start(dir);
        first.setSystemDate(LocalDate.of(2030, 1, 1));
        first.setTicketPrice(new BigDecimal("60.00"));
        ApplicationContext.stop();
        ParkController second = ApplicationContext.start(dir);
        assertNotSame(first, second);
        assertEquals(LocalDate.now(), second.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), second.getTicketPrice());
    }

    @Test
    void notDirectlyInstantiableAndNoSelfWiring() {
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()), "constructor must be private");
        }
        for (Method m : ParkController.class.getDeclaredMethods()) {
            assertFalse(m.getName().equals("open"), "the controller must not build its own dependencies");
        }
    }

    @Test
    void invoiceDirectorIsInjected() throws Exception {
        ParkController c = TestPark.start(dir);
        Field f = ParkController.class.getDeclaredField("invoiceDirector");
        f.setAccessible(true);
        Object director = f.get(c);
        assertTrue(director instanceof InvoiceDirector);
        c.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        c.createUserInvoice("ana@correo.com");
        c.createUserInvoice("ana@correo.com");
        assertSame(director, f.get(c), "same injected collaborator used by every invoice");
    }

    @Test
    void repositoriesConfiguredPerDirectory() throws Exception {
        Path a = dir.resolve("a");
        Path b = dir.resolve("b");
        TestPark.start(a).createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        TestPark.start(b).createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(a.resolve("users.txt")));
        assertEquals(List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(b.resolve("users.txt")));
    }

    @Test
    void appReleasesTheControllerAfterEachRun() {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(out, true);
        assertEquals(0, App.run(new String[0], new java.io.ByteArrayInputStream("EXIT\n".getBytes()), ps, ps, dir));
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        assertEquals(0, App.run(new String[] {"--fly"}, new java.io.ByteArrayInputStream("EXIT\n".getBytes()), ps, ps, dir));
        assertThrows(IllegalStateException.class, ParkController::getInstance);
    }
}
