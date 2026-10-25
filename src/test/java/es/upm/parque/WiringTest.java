package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;

class WiringTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() throws Exception {
        ParkController park = App.initialize(dir, new PasswordHasher(4));
        assertSame(ParkController.getInstance(), park);
        assertSame(UserController.getInstance(), park.getUserController());
        assertSame(GroupController.getInstance(), park.getGroupController());
        assertSame(TicketController.getInstance(), park.getTicketController());
        assertSame(AttractionController.getInstance(), park.getAttractionController());
        assertSame(InvoiceController.getInstance(), park.getInvoiceController());
    }

    @Test
    void sameControllersWorkWithDifferentDataDirectories() throws Exception {
        ParkController park = App.initialize(dir.resolve("data"), new PasswordHasher(4));
        assertEquals("OK USER CREATED", park.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!",
                "6", "1234567890123456"));
        park = App.initialize(dir.resolve("tmp/20260826_190315"), new PasswordHasher(4));
        assertTrue(park.getUserController().getUsers().isEmpty());
        park = App.initialize(dir.resolve("data"), new PasswordHasher(4));
        assertEquals(1, park.getUserController().getUsers().size());
    }
}
