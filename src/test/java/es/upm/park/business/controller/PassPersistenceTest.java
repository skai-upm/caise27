package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.service.ParkException;
import es.upm.park.persistence.repository.ParkRepositories;

class PassPersistenceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path dir;

    private ParkRepositories restart() {
        ParkRepositories repos = ParkRepositories.load(dir);
        park.start(repos, DAY);
        return repos;
    }

    private void populate() {
        restart();
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getAttractionService().create("A01", "Dragon Rojo", "20", "GENERAL");
        park.getAttractionService().create("A04", "Terror", "15", "SPECIAL");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.getTicketService().createIndividual("T002", "ana@correo.com", DAY.plusDays(3));
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", DAY);
        park.getPassService().buySpeedPasses("SP01", "T001", "10");
        park.getPassService().buySpecialPass("PE01", "TG01:12345678Z");
        park.getPassService().buySpeedPasses("SP02", "T002", "1");
        park.getPassService().buySpecialPass("PE02", "T002");
        park.enterAttraction("A01", "T001", true);
    }

    private Pass pass(ParkRepositories repos, String id) {
        return repos.passes().findById(id).orElseThrow();
    }

    @Test
    void passesRecoveredWithTicketPriceAndState() {
        populate();
        ParkRepositories repos = restart();
        assertEquals(13, repos.passes().findAll().size());
        Pass sp1 = pass(repos, "SP01:1");
        assertTrue(sp1.isConsumed());
        assertFalse(pass(repos, "SP01:2").isConsumed());
        assertSame(repos.tickets().findIndividual("T001").orElseThrow(), sp1.getTicket());
        assertEquals(new BigDecimal("8.00"), sp1.getPrice());
        Pass pe1 = pass(repos, "PE01");
        assertSame(repos.tickets().findIndividual("TG01:12345678Z").orElseThrow(), pe1.getTicket());
        assertEquals(new BigDecimal("40.00"), pe1.getPrice());
        ParkException e = assertThrows(ParkException.class,
                () -> park.getPassService().buySpecialPass("PE09", "TG01:12345678Z"));
        assertEquals("ticket TG01:12345678Z already has a special pass", e.getMessage());
    }

    @Test
    void specialPassConsumptionPersisted() {
        populate();
        park.enterAttraction("A04", "TG01:12345678Z", false);
        restart();
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction("A04", "TG01:12345678Z"));
        assertEquals("ticket TG01:12345678Z needs an unconsumed special pass for attraction A04", e.getMessage());
    }

    @Test
    void refundsAndTicketDeletionsRemovedFromStorage() {
        populate();
        park.getPassService().refundSpeedPass("SP02:1");
        restart();
        assertFalse(park.getTicketService().find("T002").getId().isEmpty());
        park.deleteTicket("T002");
        ParkRepositories repos = restart();
        assertEquals(List.of(), repos.passes().findAll().stream()
                .filter(p -> p.getTicket().getId().equals("T002")).toList());
        assertEquals(11, repos.passes().findAll().size());
    }

    @Test
    void orphanAndMalformedRecordsDroppedOnLoad() throws Exception {
        populate();
        Path file = dir.resolve(ParkRepositories.PASSES_FILE);
        List<String> lines = new java.util.ArrayList<>(Files.readAllLines(file));
        lines.add("SPEED;X:1;T999;8.00;false");
        lines.add("SPEED;X:2;T001;abc;false");
        lines.add("SPECIAL;X:3;T001;40.00;maybe");
        lines.add("OTHER;X:4;T001;40.00;false");
        Files.write(file, lines);
        ParkRepositories repos = restart();
        assertEquals(13, repos.passes().findAll().size());
        assertTrue(repos.passes().findAll().stream().anyMatch(SpecialPass.class::isInstance));
    }
}
