package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerPassTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.withDnis(Map.of(ANA, "12345678Z", "pedro@correo.com", "87654321X")));
        park.createIndividualTicket("T001", ANA, TODAY);
        park.createIndividualTicket("T002", ANA, TODAY.plusDays(1));
    }

    @AfterEach
    void tearDown() {
        park.reset();
    }

    @Test
    void speedPassPackages() {
        List<SpeedPass> one = park.buySpeedPasses("SP01", "T001", 1);
        assertEquals("SP01:1", one.get(0).getId());
        List<SpeedPass> ten = park.buySpeedPasses("SP02", "T001", 10);
        assertEquals(10, ten.size());
        assertEquals("SP02:10", ten.get(9).getId());
        assertEquals(new BigDecimal("8.00"), ten.get(0).getPrice());
    }

    @Test
    void speedPassRejectionsCreateNothing() {
        park.buySpeedPasses("SP02", "T001", 1);
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP02", "T001", 10), "SP02:1 conflicts");
        assertTrue(park.findPass("SP02:2").isEmpty());
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T001", 5));
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T999", 1));
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T001", 1), "ticket in the past");
        assertTrue(park.findPass("SP03:1").isEmpty());
    }

    @Test
    void oneSpecialPassPerTicketEvenAfterConsumption() {
        SpecialPass pass = park.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        pass.consume();
        assertThrows(ParkException.class, () -> park.buySpecialPass("PE02", "T001"));
        assertThrows(ParkException.class, () -> park.buySpecialPass("PE01", "T002"), "duplicate id");
        assertTrue(park.findPass("PE02").isEmpty());
    }

    @Test
    void passPricesIgnoreLaterSystemPriceAndUseGroupDiscount() {
        park.setBasePrice(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("8.00"), park.buySpeedPasses("SP01", "T001", 1).get(0).getPrice());
        park.setBasePrice(new BigDecimal("40.00"));
        park.createGroup("G01", "Grupo");
        for (int i = 0; i < 6; i++) {
            String email = "u" + i + "@correo.com";
            park.setUserDirectory(TestUsers.of("u0@correo.com", "u1@correo.com", "u2@correo.com", "u3@correo.com",
                    "u4@correo.com", "u5@correo.com"));
            park.addUserToGroup("G01", email);
        }
        park.createGroupTicket("TG02", "G01", TODAY);
        assertEquals(new BigDecimal("6.40"), park.buySpeedPasses("SP09", "TG02:DNI1", 1).get(0).getPrice());
    }

    @Test
    void refundRules() {
        park.buySpeedPasses("SP02", "T002", 10);
        park.refundPass("SP02:3", SpeedPass.class);
        assertTrue(park.findPass("SP02:3").isEmpty());
        assertTrue(park.findPass("SP02:4").isPresent());
        park.findPass("SP02:4").orElseThrow().consume();
        assertThrows(ParkException.class, () -> park.refundPass("SP02:4", SpeedPass.class), "consumed");
        assertThrows(ParkException.class, () -> park.refundPass("SP02:5", SpecialPass.class), "wrong kind");
        assertThrows(ParkException.class, () -> park.refundPass("SP99:1", SpeedPass.class), "unknown");
        park.buySpeedPasses("SP01", "T001", 1);
        assertThrows(ParkException.class, () -> park.refundPass("SP01:1", SpeedPass.class), "ticket for today");
        assertTrue(park.findPass("SP01:1").isPresent());
    }

    @Test
    void ticketDeletionRemovesItsPasses() {
        park.buySpeedPasses("SP01", "T002", 1);
        park.buySpeedPasses("SP02", "T002", 10);
        park.buySpecialPass("PE01", "T002");
        park.buySpeedPasses("SP03", "T001", 1);
        park.deleteTicket("T002");
        assertTrue(park.findPass("SP01:1").isEmpty());
        assertTrue(park.findPass("SP02:7").isEmpty());
        assertTrue(park.findPass("PE01").isEmpty());
        assertTrue(park.findPass("SP03:1").isPresent());
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertTrue(park.findPass("SP03:1").isPresent(), "rejected deletion keeps passes");
    }

    @Test
    void groupTicketDeletionRemovesPassesOfComposition() {
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(3));
        park.buySpeedPasses("SP05", "TG01:12345678Z", 1);
        park.buySpecialPass("PE05", "TG01:87654321X");
        park.deleteGroupTicket("TG01");
        assertTrue(park.findPass("SP05:1").isEmpty());
        assertTrue(park.findPass("PE05").isEmpty());
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
    }
}
