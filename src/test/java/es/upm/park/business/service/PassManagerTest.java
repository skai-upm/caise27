package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.PassFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassManagerTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;
    TicketManager tickets;
    PassManager passes;
    IndividualTicket t001;

    @BeforeEach
    void setUp() {
        ParkSession.resetControllers();
        UserManager users = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")),
                new BCryptPasswordHasher(4));
        users.create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        tickets = TicketManager.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), users, groups);
        User ana = users.findByEmail("ana@correo.com");
        t001 = tickets.create("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), TODAY);
        passes = PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
    }

    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    private PassManager restart() {
        PassManager.reset();
        return PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
    }

    @Test
    void singleAndPackOfTenSpeedPasses() {
        List<Pass> one = passes.buySpeedPasses("SP1", t001, 1);
        assertEquals("SP1", one.get(0).getId());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());
        List<Pass> ten = passes.buySpeedPasses("PK", t001, 10);
        assertEquals(10, ten.size());
        assertEquals("PK-1", ten.get(0).getId());
        assertEquals("PK-10", ten.get(9).getId());
        assertTrue(ten.stream().allMatch(p -> !p.isConsumed() && p.getPrice().equals(new BigDecimal("8.00"))));
        assertEquals(11, passes.passesFor(t001).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 5, 9, 11, -1})
    void onlyOneOrTenAccepted(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("SP1", t001, quantity));
        assertTrue(passes.getPasses().isEmpty());
    }

    @Test
    void conflictingIdentifierCreatesNothing() throws IOException {
        passes.buySpeedPasses("PK-3", t001, 1);
        List<String> before = Files.readAllLines(dir.resolve("passes.txt"));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("PK", t001, 10));
        assertEquals(1, passes.getPasses().size());
        assertEquals(before, Files.readAllLines(dir.resolve("passes.txt")));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("PK-3", t001));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses(" ", t001, 1));
    }

    @Test
    void specialPassOncePerTicketEvenWhenConsumed() {
        Pass sx = passes.buySpecialPass("SX1", t001);
        assertEquals(new BigDecimal("40.00"), sx.getPrice());
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("SX2", t001));
        sx.markConsumed();
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("SX2", t001));
    }

    @Test
    void refundRules() {
        passes.buySpeedPasses("SP1", t001, 1);
        passes.buySpecialPass("SX1", t001);
        assertThrows(IllegalArgumentException.class, () -> passes.refund("SX1", "SPEED", TODAY), "wrong kind");
        assertThrows(IllegalArgumentException.class, () -> passes.refund("SP1", "SPEED", LocalDate.of(2026, 10, 15)));
        assertThrows(IllegalArgumentException.class, () -> passes.refund("SP9", "SPEED", TODAY));
        passes.find("SX1").markConsumed();
        assertThrows(IllegalArgumentException.class, () -> passes.refund("SX1", "SPECIAL", TODAY), "consumed");
        passes.refund("SP1", "SPEED", TODAY);
        assertNull(passes.find("SP1"));
        assertNotNull(passes.find("SX1"), "consumed pass kept");
    }

    @Test
    void consumedStateSurvivesRestart() {
        passes.buySpeedPasses("SP1", t001, 1);
        passes.find("SP1").markConsumed();
        passes.save();
        PassManager again = restart();
        Pass sp = again.find("SP1");
        assertTrue(sp.isConsumed());
        assertTrue(sp.isFor(t001));
        assertThrows(IllegalArgumentException.class, () -> again.refund("SP1", "SPEED", TODAY));
        assertFalse(again.getPasses().isEmpty());
    }
}
