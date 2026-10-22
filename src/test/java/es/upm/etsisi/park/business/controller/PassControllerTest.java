package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.persistence.PassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private PassController passes;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        passes = park.getPassController();
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getTicketController().createIndividual("T002", "marta@correo.com", "2027-04-20");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
    }

    private PassRepository repository() {
        return park.getRepositories().passes();
    }

    @Test
    void packageOfTenAtTwentyPercent() {
        List<SpeedPass> bought = passes.buySpeedPasses("SP01", "T001", "10");

        assertEquals(10, bought.size());
        assertEquals("SP01:1", bought.get(0).getId());
        assertEquals("SP01:10", bought.get(9).getId());
        assertEquals(new BigDecimal("8.00"), bought.get(0).getPrice());
        assertEquals(10, repository().findAll().size());
    }

    @Test
    void singlePassUsesSameNamingAndDiscountedComponentPrice() {
        SpeedPass pass = passes.buySpeedPasses("SP02", "TG01:12345678Z", "1").get(0);

        assertEquals("SP02:1", pass.getId());
        assertEquals(new BigDecimal("7.20"), pass.getPrice());
    }

    @Test
    void priceDoesNotFollowLaterSystemPrice() {
        park.getSystemController().setPrice("45.50");

        assertEquals(new BigDecimal("8.00"), passes.buySpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), passes.buySpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void rejectsInvalidSpeedPassPurchases() {
        passes.buySpecialPass("SP01:7", "T002");

        assertThrows(ValidationException.class, () -> passes.buySpeedPasses("SP09", "T001", "5"));
        assertThrows(DuplicateException.class, () -> passes.buySpeedPasses("SP01", "T001", "10"));
        assertThrows(RuleViolationException.class, () -> passes.buySpeedPasses("SP05", "TG01", "1"));
        assertThrows(NotFoundException.class, () -> passes.buySpeedPasses("SP05", "T404", "1"));
        assertEquals(1, repository().findAll().size());
    }

    @Test
    void historicalTicketsCannotGetPasses() {
        park.getSystemController().setDate("2027-04-16");

        assertThrows(RuleViolationException.class, () -> passes.buySpeedPasses("SP05", "T001", "1"));
        assertThrows(RuleViolationException.class, () -> passes.buySpecialPass("PE05", "T001"));
    }

    @Test
    void atMostOneSpecialPassPerTicket() {
        SpecialPass pass = passes.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());

        assertThrows(RuleViolationException.class, () -> passes.buySpecialPass("PE02", "T001"));
        pass.consume();
        assertThrows(RuleViolationException.class, () -> passes.buySpecialPass("PE02", "T001"));
        assertThrows(DuplicateException.class, () -> passes.buySpecialPass("PE01", "T002"));
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() {
        passes.buySpeedPasses("SP07", "T002", "10");
        passes.buySpeedPasses("SP08", "T001", "1");
        passes.buySpecialPass("PE02", "T002");

        passes.refundSpeedPass("SP07:3");
        assertTrue(repository().findById("SP07:3").isEmpty());
        assertEquals(9, repository().findAll().stream().filter(p -> p.getId().startsWith("SP07")).count());

        repository().findById("SP07:4").orElseThrow().consume();
        assertThrows(RuleViolationException.class, () -> passes.refundSpeedPass("SP07:4"));
        assertThrows(RuleViolationException.class, () -> passes.refundSpeedPass("SP08:1"));
        assertThrows(NotFoundException.class, () -> passes.refundSpecialPass("SP07:5"));
        assertThrows(NotFoundException.class, () -> passes.refundSpeedPass("PE02"));
        passes.refundSpecialPass("PE02");
        assertTrue(repository().findById("PE02").isEmpty());
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        passes.buySpeedPasses("SP07", "T002", "10");
        passes.buySpeedPasses("SP09", "T002", "1");
        passes.buySpecialPass("PE02", "T002");

        park.getTicketController().deleteIndividual("T002");

        assertTrue(repository().findAll().isEmpty());
    }

    @Test
    void groupTicketDeletionCascadesToMemberPasses() {
        park.getTicketController().createGroup("TG02", "G01", "2027-04-20");
        passes.buySpeedPasses("SP10", "TG02:12345678Z", "1");
        passes.buySpecialPass("PE10", "TG02:87654321X");
        passes.buySpeedPasses("SP11", "TG01:12345678Z", "1");

        park.getTicketController().deleteGroup("TG02");

        List<Pass> remaining = repository().findAll();
        assertEquals(1, remaining.size());
        assertEquals("SP11:1", remaining.get(0).getId());
    }

    @Test
    void failedDeletionKeepsPasses() {
        passes.buySpeedPasses("SP01", "T001", "1");

        assertThrows(ParkException.class, () -> park.getTicketController().deleteIndividual("T001"));
        assertEquals(1, repository().findAll().size());
        IndividualTicket ticket = (IndividualTicket) park.getTicketController().find("T001");
        assertEquals(1, passes.findByTicket(ticket).size());
    }
}
