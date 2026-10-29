package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionTest extends ParkFixture {

    @BeforeEach
    void setUpAttractions() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createAttraction("A02", "Torre del Cielo", "2");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
    }

    @Test
    void createAttraction() {
        Attraction<?> a = park.findAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }

    @Test
    void invalidAttractionsRejected() {
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", "5"));
        assertThrows(ParkException.class, () -> park.createAttraction("A03", "Noria", "0"));
        assertThrows(ParkException.class, () -> park.createAttraction("A03", "Noria", "-1"));
        assertThrows(ParkException.class, () -> park.createAttraction("A03", "Noria", "diez"));
        assertThrows(ParkException.class, () -> park.createAttraction("", "Noria", "5"));
    }

    @Test
    void individualEntryAndExit() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        assertEquals(1, park.enterAttraction("A01", "T001", false).users());
        assertEquals(List.of(user(ANA)), park.findAttraction("A01").getOccupants());
        park.exitAttraction("A01", ANA);
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertThrows(ParkException.class, () -> park.exitAttraction("A01", ANA));
    }

    @Test
    void groupEntryThenIndividualExits() throws Exception {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(2, park.enterAttraction("A01", "TG01", false).users());
        park.exitAttraction("A01", ANA);
        assertEquals(List.of(user(PEDRO)), park.findAttraction("A01").getOccupants());
    }

    @Test
    void entryRequiresTicketValidToday() throws Exception {
        park.createTicket("T001", ANA, "2027-04-16");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001", false).users());
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "NOPE", false).users());
        assertThrows(ParkException.class, () -> park.enterAttraction("A99", "T001", false).users());
        park.setCurrentDate("2027-04-16");
        park.enterAttraction("A01", "T001", false).users();
        park.setCurrentDate("2027-04-17");
        assertEquals(List.of(user(ANA)), park.findAttraction("A01").getOccupants());
    }

    @Test
    void groupEntryIsAtomic() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        park.createGroup("G02", "Otros");
        park.addToGroup("G02", PEDRO);
        addMembers("G02", 1);
        park.createGroupTicket("TG02", "G02", "2027-04-15");
        park.enterAttraction("A02", "T001", false).users();
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG02", false).users());
        assertEquals(List.of(user(ANA)), park.findAttraction("A02").getOccupants());
        assertEquals(1, park.findAttraction("A02").getAvailablePlaces());
    }

    @Test
    void userAlreadyInsideCannotReenter() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        park.enterAttraction("A01", "T001", false).users();
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001", false).users());
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void compositionTicketAdmitsOnlyItsUser() throws Exception {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(1, park.enterAttraction("A01", "TG01:87654321X", false).users());
        assertEquals(List.of(user(PEDRO)), park.findAttraction("A01").getOccupants());
    }

    @Test
    void listAndStatusThroughCommands() throws Exception {
        CommandProcessor processor = processor();
        assertEquals("NO USERS IN ATTRACTION A01", processor.process("ATTRACTION LIST;A01"));
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        processor.process("ATTRACTION ENTER;A01;TG01");
        String list = processor.process("ATTRACTION LIST;A01");
        assertEquals(String.join(System.lineSeparator(), ANA, PEDRO), list);
        assertEquals(list, processor.process("ATTRACTION LIST;A01"));
        String status = processor.process("ATTRACTION STATUS;A01");
        assertTrue(status.contains("ATTRACTION: A01"));
        assertTrue(status.contains("NAME: Dragon Rojo"));
        assertTrue(status.contains("CAPACITY: 20"));
        assertTrue(status.contains("INSIDE: 2"));
        assertTrue(status.contains("AVAILABLE: 18"));
        assertTrue(status.contains(ANA + ", " + PEDRO));
        assertTrue(processor.process("ATTRACTION STATUS;A99").startsWith("ERROR:"));
    }
}
