package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.controller.ParkTestSupport;
import es.upm.amusementpark.persistence.StorageLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationBootstrapTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), GroupController.getInstance());
        assertSame(TicketController.getInstance(), TicketController.getInstance());
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void bootstrapInjectsTheUniqueControllers() throws IOException {
        ApplicationBootstrap.start(new StorageLocation(dir), new PrintStream(new ByteArrayOutputStream(), true,
                StandardCharsets.UTF_8));
        ParkController park = ParkController.getInstance();
        assertSame(UserController.getInstance(), park.getUserController());
        assertSame(GroupController.getInstance(), park.getGroupController());
        assertSame(TicketController.getInstance(), park.getTicketController());
        assertSame(AttractionController.getInstance(), park.getAttractionController());
    }

    @Test
    void repositoriesFollowTheStorageLocation() throws IOException {
        Path normal = dir.resolve("normal");
        Files.createDirectories(normal);
        PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);

        ApplicationBootstrap.start(new StorageLocation(normal), out).process(ParkTestSupport.ANA);
        assertTrue(Files.exists(normal.resolve("users.txt")));

        StorageLocation fly = StorageLocation.createTemporary(dir.resolve("tmp"));
        ApplicationBootstrap.start(fly, out).process(ParkTestSupport.PEDRO);
        assertTrue(Files.readString(fly.usersFile()).contains("pedro@correo.com"));
        assertFalse(Files.readString(fly.usersFile()).contains("ana@correo.com"));
        assertFalse(Files.readString(normal.resolve("users.txt")).contains("pedro@correo.com"));
        fly.deleteIfTemporary();
    }
}
