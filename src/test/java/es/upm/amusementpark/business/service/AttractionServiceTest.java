package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.AttractionRepository;

class AttractionServiceTest {

    private final AttractionService attractions = AttractionService.getInstance().configure(new AttractionRepository(),
            ticket -> List.of());

    @Test
    void createdAttractionIsEmpty() throws ParkException {
        Attraction<?> a = attractions.createAttraction("A01", "Dragon Rojo", 20);
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertTrue(attractions.listOccupantEmails("A01").isEmpty());
    }

    @Test
    void creationRules() throws ParkException {
        attractions.createAttraction("A01", "Dragon Rojo", 20);
        assertThrows(ParkException.class, () -> attractions.createAttraction("A01", "Otra", 5));
        assertThrows(ParkException.class, () -> attractions.createAttraction("A02", "Otra", 0));
        assertThrows(ParkException.class, () -> attractions.createAttraction("", "Otra", 5));
        assertThrows(ParkException.class, () -> attractions.createAttraction("A03", "", 5));
        assertEquals(1, attractions.findAll().size());
    }

    @Test
    void listShowsOccupantEmailsInEntryOrderWithoutChangingThem() throws ParkException {
        Attraction<?> a = attractions.createAttraction("A01", "Dragon Rojo", 2);
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        a.addOccupant(ana);
        a.addOccupant(pedro);
        assertTrue(a.isFull());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), attractions.listOccupantEmails("A01"));
        assertEquals(2, a.getOccupants().size());
        a.getOccupants().clear();
        assertEquals(2, a.getOccupants().size());
        assertThrows(ParkException.class, () -> attractions.listOccupantEmails("A99"));
    }

    private static final java.time.LocalDate TODAY = java.time.LocalDate.of(2027, 4, 15);

    private static User user(int i) {
        return new User("U" + i, "S", "0000000" + i + "X", "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static es.upm.amusementpark.business.model.IndividualTicket ticket(User u, java.time.LocalDate day) {
        return new es.upm.amusementpark.business.model.IndividualTicket("T-" + u.getDni(), day, u, new java.math.BigDecimal("40.00"));
    }

    @Test
    void enterWithIndividualTicketForToday() throws ParkException {
        attractions.createAttraction("A01", "Dragon Rojo", 20);
        User ana = user(1);
        assertEquals(List.of(ana), attractions.enter("A01", ticket(ana, TODAY), TODAY));
        assertEquals(List.of("u1@correo.com"), attractions.listOccupantEmails("A01"));
    }

    @Test
    void entryRules() throws ParkException {
        attractions.createAttraction("A01", "Uno", 1);
        User a = user(1);
        User b = user(2);
        assertThrows(ParkException.class, () -> attractions.enter("A01", ticket(a, TODAY.plusDays(1)), TODAY));
        assertThrows(ParkException.class, () -> attractions.enter("A01", ticket(a, TODAY.minusDays(1)), TODAY));
        attractions.enter("A01", ticket(a, TODAY), TODAY);
        assertThrows(ParkException.class, () -> attractions.enter("A01", ticket(a, TODAY), TODAY));
        assertThrows(ParkException.class, () -> attractions.enter("A01", ticket(b, TODAY), TODAY));
        assertThrows(ParkException.class, () -> attractions.enter("A99", ticket(b, TODAY), TODAY));
        assertEquals(List.of("u1@correo.com"), attractions.listOccupantEmails("A01"));
    }

    @Test
    void multiUserTicketEntersAllOrNothing() throws ParkException {
        attractions.createAttraction("A01", "Tres", 3);
        attractions.enter("A01", ticket(user(9), TODAY), TODAY);
        List<User> members = List.of(user(1), user(2), user(3));
        var group = new es.upm.amusementpark.business.model.GroupTicket("TG01", TODAY,
                new es.upm.amusementpark.business.model.Group("G01", "g"), members, new java.math.BigDecimal("40.00"));
        assertThrows(ParkException.class, () -> attractions.enter("A01", group, TODAY));
        assertEquals(1, attractions.listOccupantEmails("A01").size());
    }
}
