package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.business.model.AttractionType;
import es.upm.park.business.service.ParkException;
import es.upm.park.persistence.repository.ParkRepositories;

class ParkControllerAttractionTypesTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park.start(ParkRepositories.load(dir), DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getAttractionService().create("A01", "Dragon Rojo", "20", "GENERAL");
        park.getAttractionService().create("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        park.getAttractionService().create("A03", "Rafting Familiar", "24", "GROUP");
        park.getAttractionService().create("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.getTicketService().createIndividual("T002", "pedro@correo.com", DAY);
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", DAY);
    }

    private void assertRejected(String message, String attraction, String ticket) {
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction(attraction, ticket));
        assertEquals(message, e.getMessage());
        assertTrue(park.getAttractionService().occupantEmails(attraction).isEmpty());
    }

    @Test
    void ticketTypesPerAttraction() {
        assertRejected("attraction A02 (INDIVIDUAL) does not accept ticket TG01", "A02", "TG01");
        assertRejected("attraction A03 (GROUP) does not accept ticket T001", "A03", "T001");
        assertRejected("attraction A04 (SPECIAL) does not accept ticket TG01", "A04", "TG01");
        assertEquals(List.of("ana@correo.com"), park.enterAttraction("A02", "T001").stream().map(u -> u.getEmail()).toList());
        assertEquals(2, park.enterAttraction("A03", "TG01").size());
        park.enterAttraction("A01", "T002");
        assertEquals(List.of("pedro@correo.com"), park.getAttractionService().occupantEmails("A01"));
    }

    @Test
    void generalAcceptsGroupTickets() {
        assertEquals(2, park.enterAttraction("A01", "TG01").size());
    }

    @Test
    void specialRequiresUnconsumedSpecialPass() {
        assertRejected("ticket T001 needs an unconsumed special pass for attraction A04", "A04", "T001");
        park.getPassService().buySpecialPass("PE01", "T002");
        assertEquals(1, park.enterAttraction("A04", "T002").size());
    }

    @Test
    void typeIsPersistedAndLegacyLinesLoadAsGeneral() throws Exception {
        Files.writeString(dir.resolve("attractions.txt"),
                Files.readString(dir.resolve("attractions.txt")) + "A05;Antigua;10" + System.lineSeparator());
        park.start(ParkRepositories.load(dir), DAY);
        assertEquals(AttractionType.SPECIAL, park.getAttractionService().find("A04").getType());
        assertEquals(AttractionType.GROUP, park.getAttractionService().find("A03").getType());
        assertEquals(AttractionType.GENERAL, park.getAttractionService().find("A05").getType());
        assertTrue(Files.readAllLines(dir.resolve("attractions.txt")).contains("A04;Pasaje del Terror;15;SPECIAL"));
    }

    @Test
    void invalidTypeRejected() {
        ParkException e = assertThrows(ParkException.class,
                () -> park.getAttractionService().create("A09", "X", "10", "VIP"));
        assertEquals("invalid attraction type VIP", e.getMessage());
        assertThrows(ParkException.class, () -> park.getAttractionService().find("A09"));
    }
}
