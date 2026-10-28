package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkControllerAttractionTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
    }

    @Test
    void createAndFind() {
        controller.createAttraction("A01", "Dragon Rojo", "20");
        assertEquals(20, controller.findAttraction("A01").orElseThrow().getCapacity());
        assertTrue(controller.findAttraction("A02").isEmpty());
    }

    @Test
    void duplicateRejected() {
        controller.createAttraction("A01", "Dragon Rojo", "20");
        assertThrows(ParkException.class, () -> controller.createAttraction("A01", "Otra", "5"));
        assertEquals("Dragon Rojo", controller.findAttraction("A01").orElseThrow().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "1.5", "", "99999999999"})
    void invalidCapacityRejected(String capacity) {
        assertThrows(ParkException.class, () -> controller.createAttraction("A01", "Noria", capacity));
        assertTrue(controller.findAttraction("A01").isEmpty());
    }
}
