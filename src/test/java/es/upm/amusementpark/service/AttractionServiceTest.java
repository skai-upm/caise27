package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.AttractionRepository;

class AttractionServiceTest {

    private final AttractionService attractions = new AttractionService(new AttractionRepository());

    @Test
    void createdAttractionIsEmpty() throws OperationException {
        Attraction a = attractions.createAttraction("A01", "Dragon Rojo", 20);
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertTrue(attractions.listOccupantEmails("A01").isEmpty());
    }

    @Test
    void creationRules() throws OperationException {
        attractions.createAttraction("A01", "Dragon Rojo", 20);
        assertThrows(OperationException.class, () -> attractions.createAttraction("A01", "Otra", 5));
        assertThrows(OperationException.class, () -> attractions.createAttraction("A02", "Otra", 0));
        assertThrows(OperationException.class, () -> attractions.createAttraction("", "Otra", 5));
        assertThrows(OperationException.class, () -> attractions.createAttraction("A03", "", 5));
        assertEquals(1, attractions.findAll().size());
    }

    @Test
    void listShowsOccupantEmailsInEntryOrderWithoutChangingThem() throws OperationException {
        Attraction a = attractions.createAttraction("A01", "Dragon Rojo", 2);
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        a.addOccupant(ana);
        a.addOccupant(pedro);
        assertTrue(a.isFull());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), attractions.listOccupantEmails("A01"));
        assertEquals(2, a.getOccupants().size());
        a.getOccupants().clear();
        assertEquals(2, a.getOccupants().size());
        assertThrows(OperationException.class, () -> attractions.listOccupantEmails("A99"));
    }
}
