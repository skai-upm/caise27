package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.AttractionRepository;

class AttractionServiceTest {

    private AttractionRepository repo;
    private AttractionService service;

    @BeforeEach
    void setUp() {
        repo = new AttractionRepository();
        service = AttractionService.getInstance().configure(repo);
    }

    private void assertRejected(String message, Runnable action) {
        int before = repo.findAll().size();
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
        assertEquals(before, repo.findAll().size());
    }

    @Test
    void createsAndListsAttractions() {
        Attraction<?> a = service.create("A01", "Dragon Rojo", "20");
        assertEquals(20, a.getCapacity());
        assertTrue(service.occupantEmails("A01").isEmpty());
        a.enter(new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(List.of("ana@correo.com"), service.occupantEmails("A01"));
        assertEquals(1, a.getOccupants().size());
    }

    @Test
    void rejectsInvalidAttractions() {
        service.create("A01", "Dragon Rojo", "20");
        assertRejected("attraction A01 already exists", () -> service.create("A01", "Otra", "5"));
        for (String bad : new String[] {"0", "-3", "doce", "", "1.5", "9999999999", "000"}) {
            assertRejected("invalid capacity " + bad, () -> service.create("A02", "Torre", bad));
        }
        assertRejected("invalid capacity null", () -> service.create("A02", "Torre", null));
        assertRejected("attraction name is required", () -> service.create("A02", " ", "5"));
        assertRejected("attraction identifier is required", () -> service.create("", "T", "5"));
        assertRejected("no attraction A99", () -> service.occupantEmails("A99"));
    }
}
