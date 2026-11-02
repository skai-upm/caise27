package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** When saving fails, every mutating operation must leave memory exactly as before. */
class ParkRollbackTest extends ParkTestSupport {

    private File data;

    @BeforeEach
    void setUp() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.create("G02", "Amigos");
        groups.add("G02", "pedro@correo.com");
        tickets.create("T1", "ana@correo.com", TOMORROW_S);
        tickets.createGroupTicket("TG1", "G02", TOMORROW_S);
        attractions.create("A01", "Dragon", "5", null);
        data = dir.toFile();
        assumeTrue(data.setWritable(false), "cannot make directory read-only");
        assumeTrue(!data.canWrite(), "running with privileges that ignore permissions");
    }

    @AfterEach
    void restoreWritable() {
        data.setWritable(true);
    }

    private static void fails(Executable operation) {
        assertThrows(ParkException.class, operation);
    }

    @Test
    void userOperationsRollBack() {
        fails(() -> users.create("Luis", "Ruiz", dni(11111111), "luis@correo.com", "Clave1!", "600", "1111222233334444"));
        assertNull(park.findUser("luis@correo.com"));

        fails(() -> users.update("ana@correo.com", "Clave1!", "EMAIL", "ana2@correo.com"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertNull(park.findUser("ana2@correo.com"));
        assertEquals("ana@correo.com", park.findUser("ana@correo.com").getEmail());

        fails(() -> users.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", park.findUser("ana@correo.com").getPhone());
    }

    @Test
    void userDeletionRollsBackGroupMembership() {
        data.setWritable(true);
        tickets.delete("T1");
        data.setWritable(false);
        User ana = park.findUser("ana@correo.com");
        fails(() -> users.delete("ana@correo.com", "Clave1!"));
        assertSame(ana, park.findUser("ana@correo.com"));
        assertTrue(park.findGroup("G01").contains(ana));
    }

    @Test
    void groupOperationsRollBack() {
        fails(() -> groups.create("G03", "Nuevo"));
        assertNull(park.findGroup("G03"));

        fails(() -> groups.add("G01", "pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());

        fails(() -> groups.remove("G01", "ana@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());

        fails(() -> groups.delete("G01"));
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void ticketOperationsRollBack() {
        fails(() -> tickets.create("T2", "ana@correo.com", TODAY_S));
        assertNull(park.findTicket("T2"));

        fails(() -> tickets.delete("T1"));
        assertNotNull(park.findTicket("T1"));

        fails(() -> tickets.createGroupTicket("TG2", "G01", TODAY_S));
        assertNull(park.findTicket("TG2"));

        fails(() -> tickets.deleteGroupTicket("TG1"));
        assertNotNull(park.findTicket("TG1"));
        assertTrue(park.isGroupLocked(park.findGroup("G02")));
    }

    @Test
    void attractionCreationRollsBack() {
        fails(() -> attractions.create("A02", "Torre", "3", null));
        assertNull(park.findAttraction("A02"));
    }
}
