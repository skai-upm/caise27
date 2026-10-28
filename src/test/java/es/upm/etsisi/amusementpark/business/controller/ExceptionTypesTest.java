package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.etsisi.amusementpark.business.exception.AlreadyExistsException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExceptionTypesTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-15");
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.createGroup("G01", "Familia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.createAttraction("A01", "Dragon Rojo", "1");
    }

    @Test
    void notFound() {
        assertThrows(NotFoundException.class, () -> controller.deleteGroup("G99"));
        assertThrows(NotFoundException.class, () -> controller.deleteUser("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> controller.removeUserFromGroup("G01", "x@correo.com"));
        assertThrows(NotFoundException.class, () -> controller.exitAttraction("A01", "ana@correo.com"));
        assertThrows(NotFoundException.class, () -> controller.enterAttraction("A01", "T999"));
    }

    @Test
    void alreadyExists() {
        assertThrows(AlreadyExistsException.class, () -> controller.createGroup("G01", "Otro"));
        assertThrows(AlreadyExistsException.class, () -> controller.addUserToGroup("G01", "ana@correo.com"));
        assertThrows(AlreadyExistsException.class, () -> controller.createAttraction("A01", "Otra", "2"));
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(AlreadyExistsException.class,
                () -> controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-16"));
    }

    @Test
    void invalidValues() {
        assertThrows(InvalidValueException.class, () -> controller.setCurrentDate("2027-02-30"));
        assertThrows(InvalidValueException.class, () -> controller.setCurrentPrice("-1"));
        assertThrows(InvalidValueException.class, () -> controller.createAttraction("A02", "Noria", "0"));
    }

    @Test
    void operationsNotAllowed() {
        assertThrows(OperationNotAllowedException.class,
                () -> controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-14"));
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteGroup("G01"));
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteGroupTicket("TG01"));
        assertThrows(OperationNotAllowedException.class, () -> controller.deleteUser("ana@correo.com"));
        controller.createGroup("G02", "Vacio");
        assertThrows(OperationNotAllowedException.class, () -> controller.createGroupTicket("TG02", "G02", "2027-04-15"));
    }
}
