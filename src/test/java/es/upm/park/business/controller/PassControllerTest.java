package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static es.upm.park.business.controller.ParkFixture.price;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private PassController passes;
    private TicketController tickets;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        passes = PassController.getInstance();
        tickets = TicketController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
    }

    @Test
    void packageOfTenWithDerivedIdsAndPrices() throws ParkException {
        park.setCurrentPrice(price("45.50"));
        List<SpeedPass> created = passes.buySpeedPasses("SP02", "T001", 10);

        assertEquals(10, created.size());
        assertEquals("SP02:1", created.get(0).getId());
        assertEquals("SP02:10", created.get(9).getId());
        for (SpeedPass pass : created) {
            assertEquals(price("8.00"), pass.getPrice());
            assertFalse(pass.isConsumed());
        }
    }

    @Test
    void singlePassUsesSameRule() throws ParkException {
        assertEquals("SP01:1", passes.buySpeedPasses("SP01", "T001", 1).get(0).getId());
    }

    @Test
    void discountedComponentPrices() throws ParkException {
        assertEquals(price("7.20"), passes.buySpeedPasses("SP02", "TG01:12345678Z", 1).get(0).getPrice());
        assertEquals(price("36.00"), passes.buySpecialPass("PE02", "TG01:12345678Z").getPrice());
    }

    @Test
    void invalidPurchases() throws ParkException {
        assertThrows(ValidationException.class, () -> passes.buySpeedPasses("SP09", "T001", 5));
        assertThrows(NotFoundException.class, () -> passes.buySpeedPasses("SP09", "T404", 1));
        assertThrows(ValidationException.class, () -> passes.buySpeedPasses("SP09", "TG01", 1));
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(BusinessRuleException.class, () -> passes.buySpeedPasses("SP09", "T001", 1));
        assertThrows(BusinessRuleException.class, () -> passes.buySpecialPass("PE09", "T001"));
        assertNull(park.findPass("SP09:1"));
    }

    @Test
    void conflictCreatesNothing() throws ParkException {
        passes.buySpecialPass("SP03:4", "T002");
        assertThrows(ConflictException.class, () -> passes.buySpeedPasses("SP03", "T001", 10));
        for (int i = 1; i <= 10; i++) {
            if (i != 4) {
                assertNull(park.findPass("SP03:" + i));
            }
        }
    }

    @Test
    void specialPassAtMostOncePerTicket() throws ParkException {
        SpecialPass pass = passes.buySpecialPass("PE01", "T001");
        assertEquals(price("40.00"), pass.getPrice());
        assertThrows(ConflictException.class, () -> passes.buySpecialPass("PE02", "T001"));
        pass.consume();
        assertThrows(ConflictException.class, () -> passes.buySpecialPass("PE02", "T001"));
        assertThrows(ConflictException.class, () -> passes.buySpecialPass("PE01", "T002"));
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() throws ParkException {
        passes.buySpeedPasses("SP05", "T002", 10);
        passes.buySpecialPass("PE05", "T002");
        passes.buySpeedPasses("SP06", "T001", 1);

        passes.refundSpeedPass("SP05:3");
        assertNull(park.findPass("SP05:3"));
        assertNotNull(park.findPass("SP05:1"));

        assertThrows(BusinessRuleException.class, () -> passes.refundSpeedPass("SP06:1"));
        assertThrows(NotFoundException.class, () -> passes.refundSpecialPass("SP05:1"));
        assertThrows(NotFoundException.class, () -> passes.refundSpeedPass("PE05"));
        assertThrows(NotFoundException.class, () -> passes.refundSpeedPass("SP99:1"));

        park.findPass("SP05:2").consume();
        assertThrows(BusinessRuleException.class, () -> passes.refundSpeedPass("SP05:2"));

        passes.refundSpecialPass("PE05");
        assertNull(park.findPass("PE05"));
        assertTrue(passes.specialPassOf("T002").isEmpty());
    }
}
