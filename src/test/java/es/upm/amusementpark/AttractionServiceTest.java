package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionServiceTest extends ParkTestSupport {

    private List<String> occupants(String id) {
        return park.getAttractions().findById(id).getOccupants().stream().map(User::getEmail).toList();
    }

    @Test
    void attractionCreatedEmpty() {
        Attraction attraction = attractions.create("A01", "Dragon Rojo", "20");
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", "5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "veinte", "2.5", ""})
    void invalidCapacityRejected(String capacity) {
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Torre del Cielo", capacity));
    }

    @Test
    void individualEntryAndExit() {
        attractions.create("A01", "Dragon Rojo", "20");
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        attractions.enter("A01", "T001");
        assertEquals(List.of(email(1)), occupants("A01"));
        attractions.exit("A01", email(1));
        assertTrue(occupants("A01").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> attractions.exit("A01", email(1)));
    }

    @Test
    void groupEntryTogetherAndIndividualExit() {
        attractions.create("A01", "Dragon Rojo", "3");
        groupWith("G01", 3);
        tickets.createGroup("TG01", "G01", "2027-04-10");
        attractions.enter("A01", "TG01");
        assertEquals(List.of(email(1), email(2), email(3)), occupants("A01"));
        attractions.exit("A01", email(1));
        assertEquals(List.of(email(2), email(3)), occupants("A01"));
    }

    @Test
    void insufficientCapacityIsIndivisible() {
        attractions.create("A01", "Dragon Rojo", "2");
        groupWith("G01", 3);
        tickets.createGroup("TG01", "G01", "2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "TG01"));
        assertTrue(occupants("A01").isEmpty());
    }

    @Test
    void ticketMustBeForCurrentDate() {
        attractions.create("A01", "Dragon Rojo", "20");
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-11");
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T001"));
        system.setDate("2027-04-11");
        attractions.enter("A01", "T001");
        system.setDate("2027-04-12");
        assertEquals(List.of(email(1)), occupants("A01"));
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T999"));
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A99", "T001"));
    }

    @Test
    void alreadyInsideRejectsWholeEntry() {
        attractions.create("A01", "Dragon Rojo", "20");
        groupWith("G01", 2);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        tickets.createGroup("TG01", "G01", "2027-04-10");
        attractions.enter("A01", "T001");
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "TG01"));
        assertEquals(List.of(email(1)), occupants("A01"));
    }

    @Test
    void composedTicketAdmitsItsUser() {
        attractions.create("A01", "Dragon Rojo", "20");
        groupWith("G01", 2);
        tickets.createGroup("TG01", "G01", "2027-04-10");
        attractions.enter("A01", "TG01:" + dni(2));
        assertEquals(List.of(email(2)), occupants("A01"));
    }

    @Test
    void statusReflectsAttractionState() {
        attractions.create("A01", "Dragon Rojo", "20");
        user(9);
        groupWith("G01", 2);
        tickets.createIndividual("T001", email(9), "2027-04-10");
        tickets.createGroup("TG01", "G01", "2027-04-10");
        attractions.enter("A01", "T001");
        attractions.enter("A01", "TG01");
        Attraction attraction = attractions.existing("A01");
        assertEquals(3, attraction.getOccupants().size());
        assertEquals(17, attraction.getAvailablePlaces());
    }
}
