package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;

class PassFlowTest {

    private final ParkController park = ParkController.getInstance();
    private final LocalDate today = LocalDate.of(2027, 4, 15);

    @BeforeEach
    void setUp() throws Exception {
        park.initialize(new InMemoryUserStorage());
        park.setCurrentDate(today);
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users().createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.users().createUser("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "6", "1111222233334444");
        park.tickets().createIndividualTicket("T001", "ana@correo.com", today);
        park.tickets().createIndividualTicket("T002", "ana@correo.com", today.plusDays(1));
        park.setBasePrice(new BigDecimal("50.00"));
    }

    @Test
    void pricesComeFromTheTicket() throws Exception {
        assertEquals(new BigDecimal("8.00"), park.passes().buySpeedPasses("SP01", "T001", 1).get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), park.passes().buySpecialPass("PE01", "T001").getPrice());
        park.groups().createGroup("G01", "F");
        for (String e : List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com")) {
            park.groups().addMember("G01", e);
        }
        park.setBasePrice(new BigDecimal("40.00"));
        park.createGroupTicket("TG01", "G01", today);
        assertEquals(new BigDecimal("7.20"), park.passes().buySpeedPasses("SP03", "TG01:12345678Z", 1).get(0).getPrice());
    }

    @Test
    void packOfTenWithDerivedIds() throws Exception {
        List<SpeedPass> pack = park.passes().buySpeedPasses("SP02", "T001", 10);
        assertEquals(10, pack.size());
        assertEquals("SP02:1", pack.get(0).getId());
        assertEquals("SP02:10", pack.get(9).getId());
        assertEquals(new BigDecimal("80.00"), PassService.total(pack));
        assertTrue(pack.stream().noneMatch(Pass::isConsumed));
    }

    @Test
    void invalidPurchasesCreateNothing() throws Exception {
        park.passes().buySpeedPasses("SP02", "T001", 1);
        assertThrows(ValidationException.class, () -> park.passes().buySpeedPasses("SP09", "T001", 5));
        assertThrows(NotFoundException.class, () -> park.passes().buySpeedPasses("SP09", "T999", 1));
        assertThrows(ConflictException.class, () -> park.passes().buySpeedPasses("SP02", "T002", 10));
        park.setCurrentDate(today.plusDays(1));
        assertThrows(OperationNotAllowedException.class, () -> park.passes().buySpeedPasses("SP09", "T001", 1));
        assertEquals(1, park.passes().findAll().size());
    }

    @Test
    void oneSpecialPassPerTicket() throws Exception {
        park.passes().buySpecialPass("PE01", "T002");
        assertThrows(ConflictException.class, () -> park.passes().buySpecialPass("PE02", "T002"));
        assertThrows(ConflictException.class, () -> park.passes().buySpecialPass("PE01", "T001"));
        park.passes().refundSpecialPass("PE01");
        park.passes().buySpecialPass("PE02", "T002");
    }

    @Test
    void refundRules() throws Exception {
        park.passes().buySpeedPasses("SP02", "T002", 10);
        park.passes().buySpeedPasses("SP01", "T001", 1);
        park.passes().refundSpeedPass("SP02:4");
        assertEquals(10, park.passes().findAll().size());
        assertThrows(OperationNotAllowedException.class, () -> park.passes().refundSpeedPass("SP01:1"));
        park.passes().findAll().get(0).consume();
        assertThrows(OperationNotAllowedException.class, () -> park.passes().refundSpeedPass("SP02:1"));
        assertThrows(NotFoundException.class, () -> park.passes().refundSpecialPass("SP02:2"));
        assertThrows(NotFoundException.class, () -> park.passes().refundSpeedPass("SP02:4"));
    }

    @Test
    void passesAreDeletedWithTheirTicket() throws Exception {
        park.passes().buySpeedPasses("SP01", "T002", 1);
        park.passes().buySpeedPasses("SP02", "T002", 10);
        park.passes().buySpecialPass("PE01", "T002");
        park.passes().buySpeedPasses("SP03", "T001", 1);
        assertThrows(OperationNotAllowedException.class, () -> park.deleteIndividualTicket("T001"));
        assertEquals(13, park.passes().findAll().size());
        park.deleteIndividualTicket("T002");
        assertEquals(List.of("SP03:1"), park.passes().findAll().stream().map(Pass::getId).toList());
    }

    @Test
    void groupTicketDeletionRemovesCompositionPasses() throws Exception {
        park.groups().createGroup("G01", "F");
        park.groups().addMember("G01", "ana@correo.com");
        park.groups().addMember("G01", "pedro@correo.com");
        park.createGroupTicket("TG02", "G01", today.plusDays(3));
        park.passes().buySpeedPasses("SP05", "TG02:87654321X", 10);
        park.passes().buySpecialPass("PE05", "TG02:12345678Z");
        park.deleteGroupTicket("TG02");
        assertTrue(park.passes().findAll().isEmpty());
        assertFalse(park.tickets().findIndividualById("TG02:12345678Z").isPresent());
    }

    @Test
    void userInvoiceIncludesPassesAndGroupInvoiceDoesNot() throws Exception {
        park.setBasePrice(new BigDecimal("40.00"));
        park.groups().createGroup("G01", "F");
        for (String e : List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com")) {
            park.groups().addMember("G01", e);
        }
        park.createGroupTicket("TG01", "G01", today);
        park.passes().buySpeedPasses("SP01", "TG01:12345678Z", 1);
        park.passes().buySpecialPass("PE01", "T001");
        Invoice ana = park.invoices().userInvoice("ana@correo.com");
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"),
                ana.getSections().stream().map(s -> s.title()).toList());
        assertEquals(new BigDecimal("127.20"), ana.getTotal());
        assertEquals(new BigDecimal("108.00"), park.invoices().groupInvoice("G01").getTotal());
        Invoice pedro = park.invoices().userInvoice("pedro@correo.com");
        assertTrue(pedro.getSections().isEmpty());
    }
}
