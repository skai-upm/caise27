package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassControllerTest {

    @TempDir
    Path dir;

    private final SystemDate date = new SystemDate(LocalDate.of(2026, 10, 1));
    private TicketController tickets;
    private PassController passes;

    @BeforeEach
    void setUp() throws IOException {
        UserController users = UserController.getInstance()
                .configure(new UserRepository(dir.resolve("users.txt")), new PasswordHasher(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        GroupController groups = GroupController.getInstance().configure(new GroupRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets = TicketController.getInstance()
                .configure(new TicketRepository(dir.resolve("t.txt"), dir.resolve("gt.txt")), users, groups, date);
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        passes = newPassController();
    }

    private PassController newPassController() throws IOException {
        return PassController.getInstance().configure(new PassRepository(dir.resolve("passes.txt")), tickets, date);
    }

    @Test
    void speedPassesOneOrTen() {
        List<SpeedPass> pack = passes.buySpeedPasses("T001", "10");
        assertEquals(10, pack.size());
        assertEquals(10, pack.stream().map(Pass::getId).distinct().count());
        assertTrue(pack.stream().allMatch(pass -> pass.getPrice().compareTo(new BigDecimal("8.00")) == 0 && !pass.isConsumed()));
        assertEquals(new BigDecimal("80.00"), PassController.total(pack));
        assertEquals("T001-SP11", passes.buySpeedPasses("T001", "1").get(0).getId());
        assertThrows(BusinessException.class, () -> passes.buySpeedPasses("T001", "5"));
        assertEquals(11, passes.getPasses().size());
    }

    @Test
    void specialPassOncePerTicket() {
        Pass special = passes.buySpecialPass("T001");
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertThrows(BusinessException.class, () -> passes.buySpecialPass("T001"));
        special.consume();
        assertThrows(BusinessException.class, () -> passes.buySpecialPass("T001"));
    }

    @Test
    void eligibility() {
        assertEquals(1, passes.buySpeedPasses("TG01-1", "1").size());
        assertThrows(BusinessException.class, () -> passes.buySpeedPasses("TG01", "1"));
        assertThrows(BusinessException.class, () -> passes.buySpecialPass("T999"));
        date.set(LocalDate.of(2026, 10, 16));
        assertThrows(BusinessException.class, () -> passes.buySpeedPasses("T001", "1"));
    }

    @Test
    void refundsFollowTheDateRule() {
        passes.buySpeedPasses("T001", "1");
        passes.buySpeedPasses("T001", "1");
        passes.buySpecialPass("T001");
        assertEquals(new BigDecimal("8.00"), passes.refundSpeedPass("T001-SP1").getPrice());
        assertNull(passes.find("T001-SP1"));
        passes.find("T001-SP2").consume();
        assertThrows(BusinessException.class, () -> passes.refundSpeedPass("T001-SP2"));
        assertThrows(BusinessException.class, () -> passes.refundSpeedPass("T001-SPECIAL"));
        date.set(LocalDate.of(2026, 10, 15));
        assertThrows(BusinessException.class, () -> passes.refundSpecialPass("T001"));
        date.set(LocalDate.of(2026, 10, 14));
        assertEquals(new BigDecimal("40.00"), passes.refundSpecialPass("T001").getPrice());
        assertThrows(BusinessException.class, () -> passes.refundSpecialPass("T001"));
    }

    @Test
    void hasPassesIncludesGroupComposition() {
        assertFalse(passes.hasPasses(tickets.find("TG01")));
        passes.buySpeedPasses("TG01-2", "1");
        assertTrue(passes.hasPasses(tickets.find("TG01")));
        assertTrue(passes.hasPasses(tickets.find("TG01-2")));
        assertFalse(passes.hasPasses(tickets.find("T001")));
    }

    @Test
    void lookupAndConsumption() throws IOException {
        IndividualTicket t001 = (IndividualTicket) tickets.find("T001");
        assertNull(passes.firstUnusedSpeedPass(t001));
        assertNull(passes.specialPassOf(t001));
        passes.buySpeedPasses("T001", "1");
        passes.buySpeedPasses("T001", "1");
        passes.buySpecialPass("T001");
        assertEquals("T001-SP1", passes.firstUnusedSpeedPass(t001).getId());
        passes.consume(List.of(passes.firstUnusedSpeedPass(t001), passes.specialPassOf(t001)));
        assertEquals("T001-SP2", passes.firstUnusedSpeedPass(t001).getId());
        assertTrue(newPassController().find("T001-SPECIAL").isConsumed());
    }

    @Test
    void passesSurviveRestart() throws IOException {
        passes.buySpeedPasses("T001", "1").get(0).consume();
        passes.buySpecialPass("T001");
        PassController restarted = newPassController();
        assertTrue(restarted.find("T001-SP1").isConsumed());
        assertEquals(new BigDecimal("40.00"), restarted.find("T001-SPECIAL").getPrice());
        assertSame(tickets.find("T001"), restarted.find("T001-SPECIAL").getTicket());
    }
}
