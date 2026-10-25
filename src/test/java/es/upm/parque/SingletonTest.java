package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.controller.AttractionManager;
import es.upm.parque.business.controller.GroupManager;
import es.upm.parque.business.controller.InvoiceManager;
import es.upm.parque.business.controller.TicketManager;
import es.upm.parque.business.controller.UserManager;
import es.upm.parque.presentation.CommandProcessor;

class SingletonTest {

    @TempDir
    Path dir;

    @Test
    void controllersCannotBeCreatedFromOutside() {
        for (Class<?> controller : List.of(UserManager.class, GroupManager.class, TicketManager.class,
                AttractionManager.class, InvoiceManager.class)) {
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), controller.getSimpleName());
            }
        }
    }

    @Test
    void startupWiresTheUniqueInstances() throws IOException {
        CommandProcessor processor = Bootstrap.configure(dir, LocalDate.of(2026, 10, 1));
        assertSame(UserManager.getInstance(), UserManager.getInstance());
        assertEquals("OK USER CREATED", processor.process(Fixtures.CREATE_ANA));
        assertEquals("ana@correo.com", UserManager.getInstance().findByEmail("ana@correo.com").getEmail());
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia"));
        assertEquals("Familia", GroupManager.getInstance().find("G01").getName());
        assertEquals("OK ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Tornado;5"));
        assertEquals(5, AttractionManager.getInstance().find("A01").getCapacity());
        assertEquals("OK SYSTEM PRICE -> 50.00", processor.process("SYSTEM PRICE;50"));
        assertEquals("50.00", TicketManager.getInstance().getPrice().toPlainString());
        assertTrue(processor.process("INVOICE CREATE;GROUP;G01").startsWith("INVOICE GROUP G01"));
        assertSame(InvoiceManager.getInstance(), InvoiceManager.getInstance());
    }

    @Test
    void reinitialisationReplacesTheInstance() throws IOException {
        Bootstrap.configure(dir, LocalDate.of(2026, 10, 1));
        UserManager first = UserManager.getInstance();
        Bootstrap.configure(dir.resolve("other"), LocalDate.of(2026, 10, 1));
        assertNotSame(first, UserManager.getInstance());
    }
}
