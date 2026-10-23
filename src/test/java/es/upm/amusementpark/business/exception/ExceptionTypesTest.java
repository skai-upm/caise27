package es.upm.amusementpark.business.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Business operations signal each kind of abnormal situation with its own
 * exception type.
 */
class ExceptionTypesTest {

    @TempDir
    Path dir;

    private Park park;

    @BeforeEach
    void setUp() throws ParkException {
        park = TestParks.open(dir);
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void hierarchyIsRootedInAnAbstractCheckedException() {
        assertTrue(Modifier.isAbstract(ParkException.class.getModifiers()));
        assertTrue(Exception.class.isAssignableFrom(ParkException.class));
        assertTrue(!RuntimeException.class.isAssignableFrom(ParkException.class));
        for (Class<?> type : new Class<?>[] {InvalidDataException.class, NotFoundException.class,
            OperationNotAllowedException.class, PersistenceException.class}) {
            assertTrue(ParkException.class.isAssignableFrom(type), type.getName());
        }
    }

    @Test
    void invalidData() {
        assertThrows(InvalidDataException.class, () -> park.setDate("2027-02-30"));
        assertThrows(InvalidDataException.class, () -> park.setPrice("-1"));
        assertThrows(InvalidDataException.class, () -> park.createUser("Ana", "G", "12345678A", "x@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(InvalidDataException.class, () -> park.createAttraction("A01", "Noria", "cero"));
    }

    @Test
    void notFound() {
        assertThrows(NotFoundException.class, () -> park.deleteTicket("NOPE"));
        assertThrows(NotFoundException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(NotFoundException.class, () -> park.deleteUser("nobody@correo.com", "Clave1!"));
        assertThrows(NotFoundException.class, () -> park.enterAttraction("A99", "T001"));
    }

    @Test
    void operationNotAllowed() throws ParkException {
        assertThrows(OperationNotAllowedException.class, () -> park.deleteUser("ana@correo.com", "Wrong1!"));
        assertThrows(OperationNotAllowedException.class, () -> park.createUser("Ana", "G", "87654321X",
                "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> park.deleteTicket("T001"));
        assertThrows(OperationNotAllowedException.class,
                () -> park.createTicket("T002", "ana@correo.com", "2027-04-14"));
    }

    @Test
    void persistenceFailure() {
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertThrows(PersistenceException.class, () -> park.createGroup("G01", "Familia"));
            assertThrows(PersistenceException.class, () -> park.createUser("Pedro", "Lopez", "87654321X",
                    "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
    }
}
