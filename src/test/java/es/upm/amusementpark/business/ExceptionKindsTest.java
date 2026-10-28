package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;

/**
 * Each kind of abnormal situation is signalled with its own exception type.
 */
class ExceptionKindsTest {

    private final ParkController park = ParkController.getInstance();
    private final LocalDate today = LocalDate.of(2027, 4, 15);

    @BeforeEach
    void setUp() throws Exception {
        park.initialize(new InMemoryUserStorage());
        park.setCurrentDate(today);
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.groups().createGroup("G01", "Familia");
        park.groups().addMember("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", today);
    }

    @Test
    void notFound() {
        assertThrows(NotFoundException.class, () -> park.tickets().deleteIndividualTicket("T999"));
        assertThrows(NotFoundException.class, () -> park.groups().addMember("G99", "ana@correo.com"));
        assertThrows(NotFoundException.class, () -> park.enterAttraction("A99", "TG01"));
    }

    @Test
    void conflict() {
        assertThrows(ConflictException.class, () -> park.users().createUser("Otra", "X", "12345678Z", "otra@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(ConflictException.class, () -> park.groups().createGroup("G01", "Otro"));
        assertThrows(ConflictException.class, () -> park.tickets().createIndividualTicket("TG01", "ana@correo.com", today));
    }

    @Test
    void notAllowed() {
        assertThrows(OperationNotAllowedException.class, () -> park.groups().removeMember("G01", "ana@correo.com"));
        assertThrows(OperationNotAllowedException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertThrows(OperationNotAllowedException.class, () -> park.deleteUser("ana@correo.com", "Mala1!"));
        assertThrows(OperationNotAllowedException.class, () -> park.tickets().deleteGroupTicket("TG01"));
        assertThrows(OperationNotAllowedException.class,
                () -> park.tickets().createIndividualTicket("T002", "ana@correo.com", today.minusDays(1)));
    }

    @Test
    void validation() {
        assertThrows(ValidationException.class, () -> park.users().createUser("B", "X", "12345678A", "b@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(ValidationException.class, () -> park.attractions().createAttraction("A01", "X", 0));
    }
}
