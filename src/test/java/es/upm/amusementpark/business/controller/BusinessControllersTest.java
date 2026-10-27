package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.persistence.repository.UserRepository;

class BusinessControllersTest {

    private final LocalDate today = LocalDate.of(2027, 4, 15);
    private ParkController park;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private AttractionController attractions;

    @BeforeEach
    void setUp() throws Exception {
        park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        park.setCurrentDate(today);
        users = UserController.getInstance();
        groups = GroupController.getInstance();
        tickets = TicketController.getInstance();
        attractions = AttractionController.getInstance();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void invalidDataRaisesInvalidDataException() {
        InvalidDataException e = assertThrows(InvalidDataException.class, () -> users.create("P", "L", "12345678A",
                "p@correo.com", "Clave1!", "1", "1234567890123456"));
        assertEquals("Invalid DNI", e.getMessage());
        assertThrows(InvalidDataException.class, () -> park.setCurrentPrice(BigDecimal.ZERO));
        assertThrows(InvalidDataException.class, () -> attractions.create("A01", "X", 0, AttractionType.GENERAL));
    }

    @Test
    void duplicatesRaiseDuplicateException() throws Exception {
        assertThrows(DuplicateException.class, () -> users.create("A", "B", "12345678Z", "x@correo.com", "Clave1!", "1",
                "1234567890123456"));
        groups.create("G01", "F");
        assertThrows(DuplicateException.class, () -> groups.create("G01", "Otro"));
    }

    @Test
    void missingElementsRaiseNotFoundException() {
        assertThrows(NotFoundException.class, () -> users.delete("nadie@correo.com", "Clave1!"));
        assertThrows(NotFoundException.class, () -> groups.delete("G99"));
        assertThrows(NotFoundException.class, () -> tickets.deleteIndividual("T999"));
        assertThrows(NotFoundException.class, () -> attractions.find("A99"));
    }

    @Test
    void stateRulesRaiseOperationNotAllowedException() throws Exception {
        assertThrows(OperationNotAllowedException.class, () -> users.delete("ana@correo.com", "Mala1!"));
        assertThrows(OperationNotAllowedException.class,
                () -> tickets.createIndividual("T0", "ana@correo.com", today.minusDays(1)));
        tickets.createIndividual("T001", "ana@correo.com", today);
        OperationNotAllowedException e = assertThrows(OperationNotAllowedException.class,
                () -> users.delete("ana@correo.com", "Clave1!"));
        assertEquals("User has active tickets", e.getMessage());
        assertThrows(OperationNotAllowedException.class, () -> tickets.deleteIndividual("T001"));
        attractions.create("A01", "Mini", 1, AttractionType.GENERAL);
        attractions.enter("A01", "T001", false);
        assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A01", "T001", false));
    }
}
