package es.upm.etsisi.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.PassFileRepository;
import es.upm.etsisi.amusementpark.persistence.TicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private PassManager passes;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserManager(new UserFileRepository(dir.resolve("users.txt")), TestSupport.HASHER);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        users.create("Luis", "Martin Ruiz", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        tickets = restartTickets();
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        passes = new PassManager(new PassFileRepository(dir.resolve("passes.txt")), tickets);
    }

    private TicketManager restartTickets() throws Exception {
        return new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), users, groups);
    }

    @Test
    void packOfTenCreatesTenIndependentPasses() throws Exception {
        assertEquals(new BigDecimal("80.00"), passes.buySpeedPasses("SP1", "T001", "10", OCT_1));
        assertEquals(10, passes.getPasses().size());
        for (int i = 1; i <= 10; i++) {
            Pass p = passes.find("SP1-" + i).orElseThrow();
            assertEquals(new BigDecimal("8.00"), p.getPrice());
            assertFalse(p.isConsumed());
        }
        assertEquals(new BigDecimal("8.00"), passes.buySpeedPasses("SP2", "T001", "1", OCT_1));
        assertTrue(passes.find("SP2-1").isPresent());
    }

    @Test
    void invalidPurchasesCreateNothing() throws Exception {
        passes.buySpeedPasses("SP1", "T001", "1", OCT_1);
        for (String q : new String[] {"5", "0", "11", "diez", "", "01"}) {
            assertThrows(UserException.class, () -> passes.buySpeedPasses("SPX", "T001", q, OCT_1), q);
        }
        assertThrows(UserException.class, () -> passes.buySpeedPasses("SP1", "T001", "10", OCT_1));
        assertThrows(UserException.class, () -> passes.buySpeedPasses("SPY", "T999", "1", OCT_1));
        assertThrows(UserException.class, () -> passes.buySpeedPasses("SPY", "T001", "1", OCT_15.plusDays(1)));
        assertThrows(UserException.class, () -> passes.buySpeedPasses(" ", "T001", "1", OCT_1));
        assertEquals(1, passes.getPasses().size());
    }

    @Test
    void oneSpecialPassPerTicketEvenWhenConsumed() throws Exception {
        assertEquals(new BigDecimal("40.00"), passes.buySpecialPass("VIP1", "T001", OCT_1));
        assertThrows(UserException.class, () -> passes.buySpecialPass("VIP2", "T001", OCT_1));
        passes.find("VIP1").orElseThrow().markConsumed();
        assertThrows(UserException.class, () -> passes.buySpecialPass("VIP2", "T001", OCT_1));
        assertThrows(UserException.class, () -> passes.buySpecialPass("VIP1", "T001", OCT_1));
    }

    @Test
    void passesOnGroupTicketMembersUseTheDiscountedPrice() throws Exception {
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "luis@correo.com");
        Group g = groups.find("G01").orElseThrow();
        tickets.createGroupTicket("TG01", g, OCT_15, OCT_1, PRICE);
        assertEquals(new BigDecimal("7.20"), passes.buySpeedPasses("SP4", "TG01-12345678Z", "1", OCT_1));
        assertEquals(new BigDecimal("36.00"), passes.buySpecialPass("VIP3", "TG01-12345678Z", OCT_1));
        assertThrows(UserException.class, () -> passes.buySpecialPass("VIP4", "TG01", OCT_1));
        assertEquals(2, passes.passesOfUser(users.findByEmail("ana@correo.com").orElseThrow()).size());
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() throws Exception {
        passes.buySpeedPasses("SP1", "T001", "10", OCT_1);
        passes.buySpecialPass("VIP1", "T001", OCT_1);
        passes.refund("SP1-1", SpeedPass.class, OCT_1);
        assertFalse(passes.find("SP1-1").isPresent());
        passes.find("SP1-2").orElseThrow().markConsumed();
        assertThrows(UserException.class, () -> passes.refund("SP1-2", SpeedPass.class, OCT_1));
        assertThrows(UserException.class, () -> passes.refund("SP1-3", SpeedPass.class, OCT_15));
        assertThrows(UserException.class, () -> passes.refund("SP1-3", SpecialPass.class, OCT_1));
        assertThrows(UserException.class, () -> passes.refund("VIP1", SpeedPass.class, OCT_1));
        assertThrows(UserException.class, () -> passes.refund("NOPE", SpeedPass.class, OCT_1));
        passes.refund("VIP1", SpecialPass.class, OCT_1);
        assertEquals(new BigDecimal("40.00"), passes.buySpecialPass("VIP2", "T001", OCT_1));
    }

    @Test
    void passesSurviveRestartLinkedToTheirTickets() throws Exception {
        passes.buySpeedPasses("SP1", "T001", "1", OCT_1);
        passes.buySpecialPass("VIP1", "T001", OCT_1);
        TicketManager restartedTickets = restartTickets();
        PassManager restarted = new PassManager(new PassFileRepository(dir.resolve("passes.txt")), restartedTickets);
        assertSame(restartedTickets.find("T001").orElseThrow(), restarted.find("VIP1").orElseThrow().getTicket());
        assertTrue(restarted.hasPasses(restartedTickets.find("T001").orElseThrow()));
        assertThrows(UserException.class, () -> restarted.buySpecialPass("VIP2", "T001", OCT_1));
    }

    @Test
    void failedSaveRollsBack() throws Exception {
        Path plain = dir.resolve("plain");
        Files.writeString(plain, "x");
        PassManager broken = new PassManager(new PassFileRepository(plain.resolve("passes.txt")), tickets);
        assertThrows(UserException.class, () -> broken.buySpeedPasses("SP1", "T001", "10", OCT_1));
        assertThrows(UserException.class, () -> broken.buySpecialPass("VIP1", "T001", OCT_1));
        assertTrue(broken.getPasses().isEmpty());
    }
}
