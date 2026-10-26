package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FilePassRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassControllerTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private SystemDate date;
    private TicketController tickets;
    private PassController passes;

    @BeforeEach
    void setUp() {
        date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        configure();
        UserController.getInstance().create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
        tickets.create("T001", "ana@correo.com", OCT_15);
    }

    private void configure() {
        UserController users = UserController.getInstance()
                .configure(new FileUserRepository(dir.resolve("users.txt")), new BCryptPasswordEncoder(4));
        GroupController groups = GroupController.getInstance()
                .configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(
                new FileTicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")), users, groups,
                date);
        passes = PassController.getInstance()
                .configure(new FilePassRepository(dir.resolve("passes.txt")), tickets, date);
    }

    private IndividualTicket t001() {
        return (IndividualTicket) tickets.findById("T001");
    }

    @Test
    void packOfTenCreatesIndependentPasses() {
        List<SpeedPass> bought = passes.buySpeedPasses("T001", 10);
        assertEquals(10, bought.size());
        assertEquals(10, t001().getSpeedPasses().stream().map(SpeedPass::getId).distinct().count());
        assertTrue(bought.stream().allMatch(p -> p.getPrice().toPlainString().equals("8.00") && !p.isConsumed()));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("T001", 2));
    }

    @Test
    void specialPassIsUniqueEvenWhenUsed() {
        passes.buySpecialPass("T001").consume();
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("T001"));
    }

    @Test
    void refundsFollowTheDateRuleAndConsumption() {
        passes.buySpeedPasses("T001", 1);
        passes.buySpeedPasses("T001", 1);
        passes.buySpecialPass("T001");
        t001().getSpeedPasses().get(0).consume();
        assertThrows(IllegalArgumentException.class, () -> passes.refundSpeedPass("T001-SP1"));
        assertEquals("8.00", passes.refundSpeedPass("T001-SP2").getPrice().toPlainString());
        assertThrows(IllegalArgumentException.class, () -> passes.refundSpeedPass("T001-SPECIAL"));
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> passes.refundSpecialPass("T001-SPECIAL"));
        date.set(OCT_15.minusDays(1));
        passes.refundSpecialPass("T001-SPECIAL");
        assertNull(t001().getSpecialPass());
    }

    @Test
    void pastTicketsCannotReceivePasses() {
        date.set(OCT_15.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("T001", 1));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("T001"));
    }

    @Test
    void ticketsWithPassesCannotBeDeleted() {
        passes.buySpeedPasses("T001", 1);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001"));
    }

    @Test
    void passesAreRestoredWithPriceAndStatus() throws Exception {
        passes.buySpeedPasses("T001", 1);
        passes.buySpeedPasses("T001", 1);
        passes.buySpecialPass("T001");
        t001().getSpeedPasses().get(0).consume();
        passes.save();
        assertEquals("SPEED;T001-SP1;T001;8.00;true", Files.readAllLines(dir.resolve("passes.txt")).get(0));
        configure();
        IndividualTicket restored = t001();
        assertEquals(3, restored.getPasses().size());
        assertTrue(restored.getSpeedPasses().get(0).isConsumed());
        assertFalse(restored.getSpeedPasses().get(1).isConsumed());
        assertEquals("40.00", restored.getSpecialPass().getPrice().toPlainString());
    }
}
