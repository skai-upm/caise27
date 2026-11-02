package es.upm.etsisi.park;

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
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroup("G02", "Amigos");
        park.addToGroup("G02", "pedro@correo.com");
        park.createTicket("T1", "ana@correo.com", TOMORROW_S);
        park.createGroupTicket("TG1", "G02", TOMORROW_S);
        park.createAttraction("A01", "Dragon", "5");
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
        fails(() -> park.createUser("Luis", "Ruiz", dni(11111111), "luis@correo.com", "Clave1!", "600", "1111222233334444"));
        assertNull(park.findUser("luis@correo.com"));

        fails(() -> park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana2@correo.com"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertNull(park.findUser("ana2@correo.com"));
        assertEquals("ana@correo.com", park.findUser("ana@correo.com").getEmail());

        fails(() -> park.updateUser("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("612345678", park.findUser("ana@correo.com").getPhone());
    }

    @Test
    void userDeletionRollsBackGroupMembership() {
        data.setWritable(true);
        park.deleteTicket("T1");
        data.setWritable(false);
        User ana = park.findUser("ana@correo.com");
        fails(() -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertSame(ana, park.findUser("ana@correo.com"));
        assertTrue(park.findGroup("G01").contains(ana));
    }

    @Test
    void groupOperationsRollBack() {
        fails(() -> park.createGroup("G03", "Nuevo"));
        assertNull(park.findGroup("G03"));

        fails(() -> park.addToGroup("G01", "pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());

        fails(() -> park.removeFromGroup("G01", "ana@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());

        fails(() -> park.deleteGroup("G01"));
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void ticketOperationsRollBack() {
        fails(() -> park.createTicket("T2", "ana@correo.com", TODAY_S));
        assertNull(park.findTicket("T2"));

        fails(() -> park.deleteTicket("T1"));
        assertNotNull(park.findTicket("T1"));

        fails(() -> park.createGroupTicket("TG2", "G01", TODAY_S));
        assertNull(park.findTicket("TG2"));

        fails(() -> park.deleteGroupTicket("TG1"));
        assertNotNull(park.findTicket("TG1"));
        assertTrue(park.isGroupLocked(park.findGroup("G02")));
    }

    @Test
    void attractionCreationRollsBack() {
        fails(() -> park.createAttraction("A02", "Torre", "3"));
        assertNull(park.findAttraction("A02"));
    }
}
