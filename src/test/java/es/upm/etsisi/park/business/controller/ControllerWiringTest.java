package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.persistence.DataPaths;
import es.upm.etsisi.park.persistence.Repositories;
import es.upm.etsisi.park.persistence.file.FileRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerWiringTest {

    @TempDir
    Path first;

    @TempDir
    Path second;

    @Test
    void controllersAreSingletons() {
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), Park.getInstance().getGroupController());
        assertSame(TicketController.getInstance(), Park.getInstance().getTicketController());
        assertSame(PassController.getInstance(), Park.getInstance().getPassController());
        assertSame(AttractionController.getInstance(), Park.getInstance().getAttractionController());
        assertSame(InvoiceController.getInstance(), Park.getInstance().getInvoiceController());
        assertSame(SystemController.getInstance(), Park.getInstance().getSystemController());
    }

    @Test
    void repositoriesAreNotSingletons() {
        Repositories a = FileRepositories.open(new DataPaths(first));
        Repositories b = FileRepositories.open(new DataPaths(first));

        assertNotSame(a.users(), b.users());
        assertNotSame(a.passes(), b.passes());
    }

    @Test
    void injectedRepositoriesDecideWhereDataGoes() {
        Park park = TestPark.withUsers(first);
        assertEquals(4, park.getRepositories().users().findAll().size());

        Park other = TestPark.configure(second);
        assertSame(park, other);
        assertTrue(other.getRepositories().users().findAll().isEmpty());
    }

    @Test
    void unconfiguredDependencyIsReported() {
        assertThrows(IllegalStateException.class, () -> Checks.configured(null, "X"));
    }
}
