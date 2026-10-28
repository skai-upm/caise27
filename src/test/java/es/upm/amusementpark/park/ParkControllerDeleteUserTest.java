package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.persistence.InMemoryUserStorage;
import es.upm.amusementpark.service.OperationException;

class ParkControllerDeleteUserTest {

    private final ParkController park = ParkController.getInstance();
    private final LocalDate today = LocalDate.of(2027, 4, 15);

    @BeforeEach
    void setUp() throws Exception {
        park.initialize(new InMemoryUserStorage());
        park.setCurrentDate(today);
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    }

    @Test
    void activeTicketBlocksDeletion() throws OperationException {
        park.tickets().createIndividualTicket("T001", "ana@correo.com", today);
        OperationException e = assertThrows(OperationException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(e.getMessage().contains("active tickets"));
        assertTrue(park.users().findByDni("12345678Z").isPresent());
    }

    @Test
    void historicalTicketsDoNotBlockDeletionAndAreKept() throws OperationException {
        park.tickets().createIndividualTicket("T001", "ana@correo.com", today);
        park.setCurrentDate(today.plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.users().findByDni("12345678Z").isEmpty());
        assertEquals(1, park.tickets().findAll().size());
    }

    @Test
    void futureTicketCanBeDeletedFirst() throws OperationException {
        park.tickets().createIndividualTicket("T002", "ana@correo.com", today.plusDays(1));
        park.tickets().deleteIndividualTicket("T002");
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.users().findAll().isEmpty());
    }
}
