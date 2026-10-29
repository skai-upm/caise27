package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.DuplicateElementException;
import es.upm.amusementpark.business.exception.ElementNotFoundException;
import es.upm.amusementpark.business.exception.InvalidValueException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExceptionTypesTest {

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.setCurrentDate("2027-04-15");
    }

    @Test
    void specificTypesShareTheBusinessSupertype() {
        assertTrue(ParkException.class.isAssignableFrom(ElementNotFoundException.class));
        assertTrue(ParkException.class.isAssignableFrom(DuplicateElementException.class));
        assertTrue(ParkException.class.isAssignableFrom(InvalidValueException.class));
        assertTrue(ParkException.class.isAssignableFrom(OperationNotAllowedException.class));
    }

    @Test
    void notFound() {
        assertThrows(ElementNotFoundException.class, () -> park.addUserToGroup("G99", "ana@correo.com"));
        assertThrows(ElementNotFoundException.class, () -> park.deleteTicket("T999"));
        assertThrows(ElementNotFoundException.class, () -> park.existingAttraction("A99"));
    }

    @Test
    void duplicate() {
        park.createGroup("G01", "F");
        assertThrows(DuplicateElementException.class, () -> park.createGroup("G01", "Otra"));
        park.addUserToGroup("G01", "ana@correo.com");
        assertThrows(DuplicateElementException.class, () -> park.addUserToGroup("G01", "ana@correo.com"));
    }

    @Test
    void invalidValue() {
        assertThrows(InvalidValueException.class, () -> park.setCurrentDate("2027-02-30"));
        assertThrows(InvalidValueException.class, () -> park.setBasePrice("-3"));
        assertThrows(InvalidValueException.class, () -> park.createAttraction("A01", "X", "0"));
        assertThrows(InvalidValueException.class,
                () -> park.createIndividualTicket("T001", "ana@correo.com", "2027-04-14"));
    }

    @Test
    void operationNotAllowed() {
        park.createGroup("G01", "F");
        assertThrows(OperationNotAllowedException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> park.deleteTicket("T001"));
    }

    @Test
    void processingContinuesAfterEachKindOfFailure() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        processor.run(new BufferedReader(new StringReader(
                "GROUP CREATE;G01;A\nGROUP CREATE;G01;A\nGROUP ADD;G99;ana@correo.com\n"
                        + "SYSTEM PRICE;abc\nGROUPTICKET CREATE;TG01;G01;2027-04-15\nGROUP CREATE;G02\nHELP\n")));
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(out.split("Error:", -1).length - 1 == 5);
        assertTrue(out.contains("Available commands:"));
    }
}
